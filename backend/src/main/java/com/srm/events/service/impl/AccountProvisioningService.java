package com.srm.events.service.impl;

import com.srm.events.dto.FacultyProvisionRequestDTO;
import com.srm.events.dto.StudentSignupRequestDTO;
import com.srm.events.dto.VerifyAccountOtpRequestDTO;
import com.srm.events.dto.ResendAccountOtpRequestDTO;
import com.srm.events.dto.PasswordRecoveryOtpStartRequestDTO;
import com.srm.events.dto.PasswordRecoveryOtpResetRequestDTO;
import com.srm.events.security.PasswordPolicy;
import com.srm.events.service.interfaces.EmailVerificationNotifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class AccountProvisioningService {
    private static final Duration OTP_LIFETIME = Duration.ofMinutes(10);
    private static final int MAX_ATTEMPTS = 5;
    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final EmailVerificationNotifier notifier;
    private final SecureRandom random = new SecureRandom();

    public AccountProvisioningService(DataSource dataSource, PasswordEncoder passwordEncoder,
                                      PasswordPolicy passwordPolicy, EmailVerificationNotifier notifier) {
        this.dataSource = dataSource; this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy; this.notifier = notifier;
    }

    public Map<String, Object> signupStudent(StudentSignupRequestDTO request) {
        if (!request.password().equals(request.confirmPassword())) throw bad("Passwords do not match");
        passwordPolicy.validate(request.password());
        String email = normalize(request.email());
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("SELECT sr.registry_id,sr.registration_number,sr.student_name,sr.department_id,sr.section_id,sr.academic_year_id,sr.student_year,sr.semester,sr.batch FROM student_registry sr JOIN sections sec ON sec.section_id=sr.section_id WHERE UPPER(sr.registration_number)=UPPER(?) AND UPPER(sec.section_name)=UPPER(?) AND sr.is_eligible=TRUE AND sr.claimed_user_id IS NULL FOR UPDATE")) {
                p.setString(1, request.registrationNumber().trim()); p.setString(2, request.section().trim());
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw conflict("The institutional details could not be verified.");
                    long registryId = r.getLong("registry_id");
                    assertEmailAvailable(c, email);
                    assertNoPending(c, "STUDENT", registryId);
                    long userId = insertPendingUser(c, email, passwordEncoder.encode(request.password()), roleId(c, "STUDENT"));
                    insertOtp(c, "SIGNUP_STUDENT", "STUDENT", registryId, userId, email);
                    c.commit();
                    return Map.of("message", "If the details are eligible, a verification code has been issued.", "deliveryConfigured", notifier.isDeliveryConfigured(), "registryType", "STUDENT", "identifier", request.registrationNumber().trim());
                }
            } catch (RuntimeException | SQLException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to create the account request", e); }
    }

    public List<Map<String, Object>> findProvisionableFaculty() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT registry_id,faculty_code,COALESCE(faculty_name,faculty_code) AS faculty_name,role_name,department_id FROM faculty_registry WHERE is_eligible=TRUE AND claimed_user_id IS NULL AND role_name IN ('MENTOR','HOD') ORDER BY faculty_code")) {
            List<Map<String, Object>> result = new ArrayList<>();
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    result.add(Map.of("registryId", r.getLong("registry_id"), "facultyCode", r.getString("faculty_code"),
                            "facultyName", r.getString("faculty_name"), "role", r.getString("role_name"),
                            "departmentId", r.getLong("department_id")));
                }
            }
            return result;
        } catch (SQLException e) { throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to load faculty provisioning options", e); }
    }

    public Map<String, Object> provisionFaculty(FacultyProvisionRequestDTO request) {
        if (!request.temporaryPassword().equals(request.confirmTemporaryPassword())) throw bad("Passwords do not match");
        passwordPolicy.validate(request.temporaryPassword());
        String email = normalize(request.email());
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("SELECT registry_id,faculty_code,faculty_name,department_id,role_name FROM faculty_registry WHERE registry_id=? AND is_eligible=TRUE AND claimed_user_id IS NULL AND role_name IN ('MENTOR','HOD') FOR UPDATE")) {
                p.setLong(1, request.registryId());
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw conflict("The selected faculty registry entry is unavailable.");
                    String role = r.getString("role_name").toUpperCase(Locale.ROOT);
                    assertEmailAvailable(c, email);
                    long userId = insertProvisionedUser(c, email, passwordEncoder.encode(request.temporaryPassword()), roleId(c, role));
                    try (PreparedStatement i = c.prepareStatement("INSERT INTO faculty(user_id,faculty_code,name,department_id,designation) VALUES(?,?,?,?,?)")) {
                        i.setLong(1, userId); i.setString(2, r.getString("faculty_code"));
                        i.setString(3, r.getString("faculty_name") == null ? r.getString("faculty_code") : r.getString("faculty_name"));
                        i.setLong(4, r.getLong("department_id")); i.setString(5, role); i.executeUpdate();
                    }
                    claim(c, "faculty_registry", request.registryId(), userId);
                    c.commit();
                    return Map.of("message", "Staff account provisioned.", "facultyCode", r.getString("faculty_code"), "role", role);
                }
            } catch (RuntimeException | SQLException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to provision the staff account", e); }
    }

    public Map<String, Object> verify(VerifyAccountOtpRequestDTO request) {
        requireStudentSignup(request.registryType());
        String purpose = "SIGNUP_STUDENT";
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("SELECT challenge_id,registry_id,pending_user_id,otp_hash,expires_at,attempts FROM account_otp_challenges WHERE purpose=? AND registry_type='STUDENT' AND consumed_at IS NULL AND expires_at>CURRENT_TIMESTAMP AND registry_id IN (SELECT registry_id FROM student_registry WHERE UPPER(registration_number)=UPPER(?)) ORDER BY challenge_id DESC LIMIT 1 FOR UPDATE")) {
                p.setString(1, purpose); p.setString(2, request.identifier().trim());
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw bad("The verification code is invalid or expired.");
                    long challengeId=r.getLong("challenge_id"), registryId=r.getLong("registry_id"), userId=r.getLong("pending_user_id");
                    if (r.getInt("attempts") >= MAX_ATTEMPTS || !MessageDigest.isEqual(r.getString("otp_hash").getBytes(StandardCharsets.UTF_8), digest(request.otp()).getBytes(StandardCharsets.UTF_8))) {
                        try (PreparedStatement u=c.prepareStatement("UPDATE account_otp_challenges SET attempts=attempts+1 WHERE challenge_id=?")){u.setLong(1,challengeId);u.executeUpdate();}
                        throw bad("The verification code is invalid or expired.");
                    }
                    activateStudent(c, registryId, userId);
                    try (PreparedStatement u=c.prepareStatement("UPDATE account_otp_challenges SET consumed_at=CURRENT_TIMESTAMP WHERE challenge_id=?")){u.setLong(1,challengeId);u.executeUpdate();}
                    c.commit(); return Map.of("message", "Account verified and activated.");
                }
            } catch (RuntimeException | SQLException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to verify the account", e); }
    }

    public Map<String, Object> resend(ResendAccountOtpRequestDTO request) {
        requireStudentSignup(request.registryType());
        String purpose = "SIGNUP_STUDENT";
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            String sql = "SELECT challenge_id,email FROM account_otp_challenges WHERE purpose=? AND registry_type='STUDENT' AND consumed_at IS NULL AND registry_id IN (SELECT registry_id FROM student_registry WHERE UPPER(registration_number)=UPPER(?)) ORDER BY challenge_id DESC LIMIT 1 FOR UPDATE";
            try (PreparedStatement p=c.prepareStatement(sql)) {
                p.setString(1,purpose); p.setString(2,request.identifier().trim());
                try(ResultSet r=p.executeQuery()) {
                    if(!r.next()) throw bad("The verification request could not be found.");
                    long id=r.getLong(1); String email=r.getString(2); String raw=String.format("%06d",random.nextInt(1_000_000));
                    try(PreparedStatement u=c.prepareStatement("UPDATE account_otp_challenges SET otp_hash=?,expires_at=?,attempts=0,created_at=CURRENT_TIMESTAMP WHERE challenge_id=?")){u.setString(1,digest(raw));u.setTimestamp(2,Timestamp.valueOf(LocalDateTime.now().plus(OTP_LIFETIME)));u.setLong(3,id);u.executeUpdate();}
                    notifier.sendSignupOtp(email,raw,purpose,OTP_LIFETIME); c.commit();
                    return Map.of("message","A new verification code has been issued.","deliveryConfigured",notifier.isDeliveryConfigured());
                }
            } catch(RuntimeException|SQLException e){c.rollback();throw e;}
        } catch(SQLException e){throw new ResponseStatusException(HttpStatus.CONFLICT,"Unable to resend the verification code",e);}
    }

    public Map<String,Object> startPasswordRecovery(PasswordRecoveryOtpStartRequestDTO request) {
        String type=request.registryType().toUpperCase(Locale.ROOT), identifier=request.identifier().trim();
        String lookup="STUDENT".equals(type)?"SELECT u.user_id,u.email FROM users u JOIN students s ON s.user_id=u.user_id WHERE UPPER(s.register_number)=UPPER(?) AND u.is_active=TRUE":"SELECT u.user_id,u.email FROM users u JOIN faculty f ON f.user_id=u.user_id WHERE UPPER(f.faculty_code)=UPPER(?) AND u.is_active=TRUE";
        try(Connection c=dataSource.getConnection()) { c.setAutoCommit(false); try(PreparedStatement p=c.prepareStatement(lookup)){p.setString(1,identifier);try(ResultSet r=p.executeQuery()){ if(r.next()){long user=r.getLong(1);String email=r.getString(2),raw=String.format("%06d",random.nextInt(1_000_000)); try(PreparedStatement u=c.prepareStatement("DELETE FROM account_otp_challenges WHERE purpose='PASSWORD_RESET' AND pending_user_id=? AND consumed_at IS NULL")){u.setLong(1,user);u.executeUpdate();} try(PreparedStatement i=c.prepareStatement("INSERT INTO account_otp_challenges(purpose,registry_type,registry_id,pending_user_id,email,otp_hash,expires_at) VALUES('PASSWORD_RESET',?,?,?, ?,?,?)")){i.setString(1,type);i.setLong(2,0);i.setLong(3,user);i.setString(4,email);i.setString(5,digest(raw));i.setTimestamp(6,Timestamp.valueOf(LocalDateTime.now().plus(OTP_LIFETIME)));i.executeUpdate();} notifier.sendSignupOtp(email,raw,"PASSWORD_RESET",OTP_LIFETIME);} c.commit(); return Map.of("message","If the account exists, a verification code has been issued.","deliveryConfigured",notifier.isDeliveryConfigured());}}} catch(SQLException e){throw new ResponseStatusException(HttpStatus.CONFLICT,"Unable to start password recovery",e);}
    }

    public Map<String,Object> resetPasswordWithOtp(PasswordRecoveryOtpResetRequestDTO request) {
        if(!request.password().equals(request.confirmPassword())) throw bad("Passwords do not match"); passwordPolicy.validate(request.password());
        String type=request.registryType().toUpperCase(Locale.ROOT), identifier=request.identifier().trim();
        String lookup="STUDENT".equals(type)?"SELECT u.user_id FROM users u JOIN students s ON s.user_id=u.user_id WHERE UPPER(s.register_number)=UPPER(?) AND u.is_active=TRUE":"SELECT u.user_id FROM users u JOIN faculty f ON f.user_id=u.user_id WHERE UPPER(f.faculty_code)=UPPER(?) AND u.is_active=TRUE";
        try(Connection c=dataSource.getConnection()){c.setAutoCommit(false);long userId;try(PreparedStatement p=c.prepareStatement(lookup)){p.setString(1,identifier);try(ResultSet r=p.executeQuery()){if(!r.next())throw bad("The verification code is invalid or expired.");userId=r.getLong(1);}}try(PreparedStatement p=c.prepareStatement("SELECT challenge_id,otp_hash,expires_at,attempts FROM account_otp_challenges WHERE purpose='PASSWORD_RESET' AND pending_user_id=? AND consumed_at IS NULL AND expires_at>CURRENT_TIMESTAMP ORDER BY challenge_id DESC LIMIT 1 FOR UPDATE")){p.setLong(1,userId);try(ResultSet r=p.executeQuery()){if(!r.next()||r.getInt(4)>=MAX_ATTEMPTS||!MessageDigest.isEqual(r.getString(2).getBytes(StandardCharsets.UTF_8),digest(request.otp()).getBytes(StandardCharsets.UTF_8)))throw bad("The verification code is invalid or expired.");try(PreparedStatement u=c.prepareStatement("UPDATE users SET password_hash=?,session_version=session_version+1 WHERE user_id=?")){u.setString(1,passwordEncoder.encode(request.password()));u.setLong(2,userId);u.executeUpdate();}try(PreparedStatement u=c.prepareStatement("UPDATE account_otp_challenges SET consumed_at=CURRENT_TIMESTAMP WHERE challenge_id=?")){u.setLong(1,r.getLong(1));u.executeUpdate();}}}c.commit();return Map.of("message","Password reset successfully.");}catch(SQLException e){throw new ResponseStatusException(HttpStatus.CONFLICT,"Unable to reset the password",e);}
    }

    private void activateStudent(Connection c, long registryId, long userId) throws SQLException {
        try (PreparedStatement p=c.prepareStatement("SELECT registration_number,COALESCE(student_name,registration_number),department_id,section_id,academic_year_id,student_year,semester,batch FROM student_registry WHERE registry_id=? AND claimed_user_id IS NULL AND is_eligible=TRUE FOR UPDATE")){p.setLong(1,registryId);try(ResultSet r=p.executeQuery()){if(!r.next())throw conflict("The registry claim is no longer available.");try(PreparedStatement i=c.prepareStatement("INSERT INTO students(user_id,register_number,name,department_id,section_id,academic_year_id,student_year,semester,batch) VALUES(?,?,?,?,?,?,?,?,?)")){i.setLong(1,userId);i.setString(2,r.getString(1));i.setString(3,r.getString(2));i.setLong(4,r.getLong(3));i.setLong(5,r.getLong(4));i.setLong(6,r.getLong(5));i.setInt(7,r.getInt(6));i.setInt(8,r.getInt(7));i.setString(9,r.getString(8));i.executeUpdate();}}}
        claim(c,"student_registry",registryId,userId); activateUser(c,userId);
    }
    private void claim(Connection c,String table,long registryId,long userId)throws SQLException{try(PreparedStatement p=c.prepareStatement("UPDATE "+table+" SET claimed_user_id=? WHERE registry_id=? AND claimed_user_id IS NULL")){p.setLong(1,userId);p.setLong(2,registryId);if(p.executeUpdate()!=1)throw conflict("The registry claim is no longer available.");}}
    private void activateUser(Connection c,long userId)throws SQLException{try(PreparedStatement p=c.prepareStatement("UPDATE users SET is_active=TRUE,email_verified=TRUE WHERE user_id=?")){p.setLong(1,userId);p.executeUpdate();}}
    private long insertPendingUser(Connection c,String email,String hash,long roleId)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO users(email,password_hash,role_id,is_active,email_verified) VALUES(?,?,?,FALSE,FALSE)",Statement.RETURN_GENERATED_KEYS)){p.setString(1,email);p.setString(2,hash);p.setLong(3,roleId);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();return r.getLong(1);}}}
    private long insertProvisionedUser(Connection c,String email,String hash,long roleId)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO users(email,password_hash,role_id,is_active,email_verified) VALUES(?,?,?,TRUE,TRUE)",Statement.RETURN_GENERATED_KEYS)){p.setString(1,email);p.setString(2,hash);p.setLong(3,roleId);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();return r.getLong(1);}}}
    private long roleId(Connection c,String role)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT role_id FROM roles WHERE role_name=?")){p.setString(1,role);try(ResultSet r=p.executeQuery()){if(!r.next())throw conflict("Required role is not configured.");return r.getLong(1);}}}
    private void assertEmailAvailable(Connection c,String email)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM users WHERE LOWER(email)=?")){p.setString(1,email);try(ResultSet r=p.executeQuery()){if(r.next())throw conflict("That email is already registered.");}}}
    private void assertNoPending(Connection c,String type,long registryId)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM account_otp_challenges WHERE registry_type=? AND registry_id=? AND consumed_at IS NULL AND expires_at>CURRENT_TIMESTAMP")){p.setString(1,type);p.setLong(2,registryId);try(ResultSet r=p.executeQuery()){if(r.next())throw conflict("A verification request is already pending.");}}}
    private void insertOtp(Connection c,String purpose,String type,long registryId,long userId,String email)throws SQLException{String raw=String.format("%06d",random.nextInt(1_000_000));try(PreparedStatement p=c.prepareStatement("INSERT INTO account_otp_challenges(purpose,registry_type,registry_id,pending_user_id,email,otp_hash,expires_at) VALUES(?,?,?,?,?,?,?)")){p.setString(1,purpose);p.setString(2,type);p.setLong(3,registryId);p.setLong(4,userId);p.setString(5,email);p.setString(6,digest(raw));p.setTimestamp(7,Timestamp.valueOf(LocalDateTime.now().plus(OTP_LIFETIME)));p.executeUpdate();}notifier.sendSignupOtp(email,raw,purpose,OTP_LIFETIME);}
    private String normalize(String email){return email.trim().toLowerCase(Locale.ROOT);}
    private String digest(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    private ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    private ResponseStatusException conflict(String message){return new ResponseStatusException(HttpStatus.CONFLICT,message);}
    private void requireStudentSignup(String registryType) { if (!"STUDENT".equalsIgnoreCase(registryType)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The requested signup flow is unavailable."); }
}
