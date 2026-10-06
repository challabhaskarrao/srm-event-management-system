package com.srm.events.controller;

import com.srm.events.dto.CreateParticipationRequestDTO;
import com.srm.events.dto.ResubmitParticipationRequestDTO;
import com.srm.events.dto.PostEventReportRequestDTO;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.dao.interfaces.NotificationRecipientDAO;
import com.srm.events.service.interfaces.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/** Student-originated requests reuse events + applications so legacy history and reviews remain readable. */
@RestController
@RequestMapping("/api/participations")
public class ParticipationController {
    private final DataSource ds;
    private final JwtTokenProvider jwt;
    private final NotificationService notificationService;
    private final NotificationRecipientDAO notificationRecipients;
    public ParticipationController(DataSource ds, JwtTokenProvider jwt, NotificationService notificationService, NotificationRecipientDAO notificationRecipients) { this.ds = ds; this.jwt = jwt; this.notificationService = notificationService; this.notificationRecipients = notificationRecipients; }
    private String token(String h) {
        if (h == null || !h.startsWith("Bearer ") || !jwt.validateToken(h.substring(7).trim())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return h.substring(7).trim();
    }

    @PostMapping
    public ResponseEntity<Map<String,Object>> create(@RequestHeader(value="Authorization",required=false) String h, @Valid @RequestBody CreateParticipationRequestDTO r) throws Exception {
        String t = token(h);
        if (!"STUDENT".equals(jwt.getRoleFromToken(t))) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Student access required");
        validateRequest(r);
        long uid = jwt.getUserIdFromToken(t);
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                long sid, academicYearId; String registrationNumber;
                int studentSemester;
                int studentYear;
                try (PreparedStatement q = c.prepareStatement("SELECT student_id, academic_year_id, semester, student_year, register_number FROM students WHERE user_id=?")) {
                    q.setLong(1, uid); try (ResultSet x = q.executeQuery()) { if (!x.next()) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Student profile required"); sid=x.getLong("student_id"); academicYearId=x.getLong("academic_year_id"); studentSemester=x.getInt("semester"); studentYear=x.getInt("student_year"); registrationNumber=x.getString("register_number"); }
                }
                long eventId, eventTypeId;
                String eventTypeName;
                try (PreparedStatement q = c.prepareStatement("SELECT event_type_id, name FROM event_types WHERE code=? AND is_active=TRUE")) {
                    q.setString(1, r.eventTypeCode().trim().toUpperCase(Locale.ROOT));
                    try (ResultSet x = q.executeQuery()) {
                        if (!x.next()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event type is not active or does not exist");
                        eventTypeId = x.getLong("event_type_id");
                        eventTypeName = x.getString("name");
                    }
                }
                long legacyCategoryId;
                try (PreparedStatement q = c.prepareStatement("SELECT category_id FROM event_categories WHERE is_active=TRUE AND category_name=? LIMIT 1")) {
                    q.setString(1, "OTHER");
                    try (ResultSet x = q.executeQuery()) {
                        if (!x.next()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Legacy event category configuration is missing");
                        legacyCategoryId = x.getLong(1);
                    }
                }
                String eventSql = "INSERT INTO events(event_name,event_type_id,category_id,other_category,organizer,start_date,end_date,venue,city,state,country,mode,event_url,description,type_specific_details,registration_fee_required,registration_fee_amount,is_student_initiated,created_by_user_id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement e = c.prepareStatement(eventSql, Statement.RETURN_GENERATED_KEYS)) {
                    e.setString(1,r.eventName().trim()); e.setLong(2,eventTypeId); e.setLong(3,legacyCategoryId); setNullableString(e,4,r.otherCategory()); e.setString(5,r.organizer().trim()); e.setDate(6,java.sql.Date.valueOf(r.startDate())); e.setDate(7,java.sql.Date.valueOf(r.endDate()));
                    setNullableString(e,8,r.venue()); setNullableString(e,9,r.city()); setNullableString(e,10,r.state()); setNullableString(e,11,r.country()); e.setString(12,r.mode()); setNullableString(e,13,r.eventUrl()); setNullableString(e,14,r.description()); setNullableString(e,15,r.typeSpecificDetails() == null ? null : new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(r.typeSpecificDetails())); e.setBoolean(16,r.registrationFeeRequired()); setNullableDecimal(e,17,r.registrationFeeAmount()); e.setBoolean(18,true); e.setLong(19,uid); e.executeUpdate();
                    try (ResultSet keys=e.getGeneratedKeys()) { if(!keys.next()) throw new SQLException("Event id was not generated"); eventId=keys.getLong(1); }
                }
                String code="PAR-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.ROOT); long applicationId;
                String appSql="INSERT INTO applications(application_code,event_id,created_by_student_id,academic_year_id,application_semester,application_student_year,participation_type,travel_required,outside_state,destination_city,destination_state,destination_country,venue_address,mode_of_travel,departure_at,return_at,accommodation_required,accommodation_name,accommodation_address,check_in_date,check_out_date,emergency_contact_name,emergency_contact_number,guardian_contact_number,travel_remarks,registration_fee_paid,registration_fee_amount,current_status,submitted_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement a=c.prepareStatement(appSql,Statement.RETURN_GENERATED_KEYS)) {
                    int i=1; a.setString(i++,code); a.setLong(i++,eventId); a.setLong(i++,sid); a.setLong(i++,academicYearId); a.setInt(i++,studentSemester); a.setInt(i++,studentYear); a.setString(i++,r.participationType().name()); a.setBoolean(i++,r.travelRequired()); a.setBoolean(i++,r.outsideState()); setNullableString(a,i++,r.city()); setNullableString(a,i++,r.state()); setNullableString(a,i++,r.country()); setNullableString(a,i++,r.venueAddress()); setNullableString(a,i++,r.modeOfTravel()); setNullableTimestamp(a,i++,r.departureAt()); setNullableTimestamp(a,i++,r.returnAt()); a.setBoolean(i++,r.accommodationRequired()); setNullableString(a,i++,r.accommodationName()); setNullableString(a,i++,r.accommodationAddress()); setNullableDate(a,i++,r.checkInDate()); setNullableDate(a,i++,r.checkOutDate()); setNullableString(a,i++,r.emergencyContactName()); setNullableString(a,i++,r.emergencyContactNumber()); setNullableString(a,i++,r.guardianContactNumber()); setNullableString(a,i++,r.travelRemarks()); a.setBoolean(i++,false); setNullableDecimal(a,i++,r.registrationFeeAmount()); a.setString(i++,"SUBMITTED"); a.setTimestamp(i,Timestamp.valueOf(LocalDateTime.now())); a.executeUpdate();
                    try(ResultSet keys=a.getGeneratedKeys()){ if(!keys.next()) throw new SQLException("Application id was not generated"); applicationId=keys.getLong(1); }
                }
                if (r.participationType().name().equals("TEAM")) {
                    long teamId;
                    try(PreparedStatement tm=c.prepareStatement("INSERT INTO teams(application_id,team_name,leader_student_id) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)){tm.setLong(1,applicationId);tm.setString(2,r.teamName().trim());tm.setLong(3,sid);tm.executeUpdate();try(ResultSet keys=tm.getGeneratedKeys()){if(!keys.next())throw new SQLException("Team id was not generated");teamId=keys.getLong(1);}}
                    try(PreparedStatement memberQuery=c.prepareStatement("SELECT student_id FROM students WHERE UPPER(register_number)=UPPER(?)"); PreparedStatement memberInsert=c.prepareStatement("INSERT INTO team_members(team_id,student_id) VALUES(?,?)")){
                        for(String raw:r.teamMemberRegistrationNumbers()){String value=raw==null?"":raw.trim();if(value.isBlank()||value.equalsIgnoreCase(registrationNumber))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Team members must be existing Students other than the leader"); memberQuery.setString(1,value); try(ResultSet member=memberQuery.executeQuery()){if(!member.next())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Team member is not an existing Student: "+value);memberInsert.setLong(1,teamId);memberInsert.setLong(2,member.getLong(1));memberInsert.addBatch();}}
                        memberInsert.executeBatch();
                    }
                }
                c.commit();
                notifyNewApplication(applicationId, code);
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("applicationId",applicationId,"applicationCode",code,"eventId",eventId,"eventTypeCode",r.eventTypeCode().trim().toUpperCase(Locale.ROOT),"eventTypeName",eventTypeName,"approvalStatus","SUBMITTED","participationStatus","NOT_YET_OCCURRED"));
            } catch(Exception e){c.rollback();if(e instanceof ResponseStatusException re)throw re;throw e;} finally{c.setAutoCommit(true);}
        }
    }

    private void validateRequest(CreateParticipationRequestDTO r){
        if(!r.declarationAccepted())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Declaration must be accepted");
        if(r.endDate().isBefore(r.startDate()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"End date cannot precede start date");
        if(r.eventTypeCode()==null||r.eventTypeCode().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Event type is required");
        if(r.eventUrl()!=null&&!r.eventUrl().isBlank()){try{URI u=new URI(r.eventUrl());if(!Set.of("http","https").contains(u.getScheme())||u.getHost()==null)throw new Exception();}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Event URL must be absolute HTTP(S)");}}
        if(r.participationType().name().equals("TEAM")){if(r.teamName()==null||r.teamName().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Team name is required");if(r.teamMemberRegistrationNumbers()==null||r.teamMemberRegistrationNumbers().isEmpty()||r.teamMemberRegistrationNumbers().size()>9)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A team must contain 2 to 10 students");Set<String> seen=new HashSet<>();for(String value:r.teamMemberRegistrationNumbers())if(value==null||!seen.add(value.trim().toUpperCase(Locale.ROOT)))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Team member registration numbers must be unique");}else if(r.teamMemberRegistrationNumbers()!=null&&!r.teamMemberRegistrationNumbers().isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Individual applications cannot contain team members");
        if(r.registrationFeeRequired()&&(r.registrationFeeAmount()==null||r.registrationFeeAmount().signum()<0))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Registration fee amount is required");
        if(r.outsideState()){if(blank(r.city())||blank(r.state())||blank(r.country())||blank(r.venueAddress())||blank(r.modeOfTravel())||blank(r.emergencyContactName())||blank(r.emergencyContactNumber())||blank(r.guardianContactNumber()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Complete outstation and emergency contact details are required");if(r.departureAt()==null||r.returnAt()==null||r.returnAt().isBefore(r.departureAt()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Valid departure and return times are required");}
        if(!r.outsideState()&&(r.departureAt()!=null||r.returnAt()!=null))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Travel times require an outstation activity");
        if(r.accommodationRequired()&&!r.outsideState())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Accommodation requires an outstation activity");
        if(r.accommodationRequired()&&(blank(r.accommodationName())||blank(r.accommodationAddress())||r.checkInDate()==null||r.checkOutDate()==null||r.checkOutDate().isBefore(r.checkInDate())))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Complete accommodation details are required");
    }

    @PutMapping("/{id}/resubmit")
    public Map<String, Object> resubmit(@PathVariable long id,
                                        @RequestHeader(value = "Authorization", required = false) String h,
                                        @Valid @RequestBody ResubmitParticipationRequestDTO r) throws Exception {
        String t = token(h);
        if (!"STUDENT".equals(jwt.getRoleFromToken(t))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
        }
        validateResubmission(r);
        long userId = jwt.getUserIdFromToken(t);

        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                long eventId;
                String previousStatus;
                long mentorId;
                try (PreparedStatement q = c.prepareStatement(
                        "SELECT a.event_id, a.current_status, a.assigned_mentor_id FROM applications a " +
                                "JOIN students s ON s.student_id = a.created_by_student_id " +
                                "WHERE a.application_id = ? AND s.user_id = ? FOR UPDATE")) {
                    q.setLong(1, id);
                    q.setLong(2, userId);
                    try (ResultSet result = q.executeQuery()) {
                        if (!result.next()) {
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
                        }
                        eventId = result.getLong("event_id");
                        previousStatus = result.getString("current_status");
                        mentorId = result.getLong("assigned_mentor_id");
                    }
                }
                if (!Set.of("RETURNED_BY_MENTOR", "RETURNED_BY_HOD").contains(previousStatus)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Only an application returned for correction can be edited and resubmitted");
                }

                try (PreparedStatement event = c.prepareStatement(
                        "UPDATE events SET event_name=?, organizer=?, start_date=?, end_date=?, mode=?, " +
                                "venue=?, event_url=?, description=?, city=?, state=?, country=?, " +
                                "updated_at=CURRENT_TIMESTAMP WHERE event_id=?")) {
                    event.setString(1, r.eventName().trim());
                    event.setString(2, r.organizer().trim());
                    event.setDate(3, java.sql.Date.valueOf(r.startDate()));
                    event.setDate(4, java.sql.Date.valueOf(r.endDate()));
                    event.setString(5, r.mode());
                    setNullableString(event, 6, r.venue());
                    setNullableString(event, 7, r.eventUrl());
                    setNullableString(event, 8, r.description());
                    setNullableString(event, 9, r.city());
                    setNullableString(event, 10, r.state());
                    setNullableString(event, 11, r.country());
                    event.setLong(12, eventId);
                    if (event.executeUpdate() != 1) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "The application event could not be updated");
                    }
                }

                try (PreparedStatement application = c.prepareStatement(
                        "UPDATE applications SET current_status='SUBMITTED', correction_return_stage=NULL, " +
                                "submitted_at=CURRENT_TIMESTAMP, updated_at=CURRENT_TIMESTAMP " +
                                "WHERE application_id=? AND current_status=?")) {
                    application.setLong(1, id);
                    application.setString(2, previousStatus);
                    if (application.executeUpdate() != 1) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Application state changed before resubmission");
                    }
                }

                Timestamp resubmissionVersion;
                try (PreparedStatement version = c.prepareStatement("SELECT updated_at FROM applications WHERE application_id=?")) {
                    version.setLong(1, id);
                    try (ResultSet result = version.executeQuery()) {
                        if (!result.next()) throw new SQLException("Application version was not available after resubmission");
                        resubmissionVersion = result.getTimestamp(1);
                    }
                }

                try (PreparedStatement history = c.prepareStatement(
                        "INSERT INTO approval_actions " +
                                "(application_id, approver_user_id, approval_level, action, previous_status, new_status, remarks) " +
                                "VALUES (?, ?, 'MENTOR', 'RESUBMITTED', ?, 'SUBMITTED', ?)")) {
                    history.setLong(1, id);
                    history.setLong(2, userId);
                    history.setString(3, previousStatus);
                    history.setString(4, "Student resubmitted the application after correction");
                    history.executeUpdate();
                }
                c.commit();
                String resubmissionKey = "APPLICATION_RESUBMITTED:" + id + ":" + resubmissionVersion.toInstant();
                notificationRecipients.findUserIdByFacultyId(mentorId).ifPresent(mentorUserId -> safeNotify(mentorUserId, "APPLICATION_RESUBMITTED", "Corrected activity application resubmitted", "A corrected activity application has been resubmitted for your review.", "APPLICATION", id, "/staff/mentor", resubmissionKey));
                return Map.of("applicationId", id, "status", "SUBMITTED", "message", "Application resubmitted to Mentor review");
            } catch (Exception e) {
                c.rollback();
                if (e instanceof ResponseStatusException re) throw re;
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private void safeNotify(Long userId, String type, String title, String message, String recordType, Long recordId, String path, String key) {
        try { notificationService.notifyOnce(userId, type, title, message, recordType, recordId, path, key); } catch (RuntimeException ignored) { }
    }

    private void notifyNewApplication(long applicationId, String code) {
        try (Connection connection = ds.getConnection(); PreparedStatement query = connection.prepareStatement(
                "SELECT a.assigned_mentor_id, s.name FROM applications a JOIN students s ON s.student_id=a.created_by_student_id " +
                "LEFT JOIN mentor_assignments ma ON ma.department_id=s.department_id AND ma.section_id=s.section_id " +
                "AND ma.academic_year_id=s.academic_year_id AND ma.student_year=s.student_year AND ma.is_active=TRUE " +
                "AND ma.active_from<=CURRENT_DATE AND (ma.active_until IS NULL OR ma.active_until>=CURRENT_DATE) " +
                "WHERE a.application_id=? ORDER BY (a.assigned_mentor_id IS NULL), ma.mentor_assignment_id LIMIT 1")) {
            query.setLong(1, applicationId);
            try (ResultSet result = query.executeQuery()) {
                if (!result.next()) return;
                long facultyId = result.getLong("assigned_mentor_id");
                if (result.wasNull()) facultyId = 0;
                if (facultyId == 0) {
                    try (PreparedStatement fallback = connection.prepareStatement(
                            "SELECT ma.faculty_id FROM applications a JOIN students s ON s.student_id=a.created_by_student_id " +
                            "JOIN mentor_assignments ma ON ma.department_id=s.department_id AND ma.section_id=s.section_id " +
                            "AND ma.academic_year_id=s.academic_year_id AND ma.student_year=s.student_year AND ma.is_active=TRUE " +
                            "AND ma.active_from<=CURRENT_DATE AND (ma.active_until IS NULL OR ma.active_until>=CURRENT_DATE) " +
                            "WHERE a.application_id=? ORDER BY ma.mentor_assignment_id LIMIT 1")) {
                        fallback.setLong(1, applicationId);
                        try (ResultSet row = fallback.executeQuery()) { if (row.next()) facultyId = row.getLong(1); }
                    }
                }
                if (facultyId > 0) {
                    final long targetFaculty = facultyId;
                    final String studentName = result.getString("name");
                    notificationRecipients.findUserIdByFacultyId(targetFaculty).ifPresent(recipientUserId -> safeNotify(recipientUserId, "APPLICATION_SUBMITTED", "New activity application", "New activity application from " + studentName + " requires your review.", "APPLICATION", applicationId, "/staff/mentor", "APPLICATION_SUBMITTED:" + applicationId + ":SUBMITTED"));
                }
            }
        } catch (Exception ignored) { }
    }

    private void validateResubmission(ResubmitParticipationRequestDTO r) {
        if (r.endDate().isBefore(r.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot precede start date");
        }
        if (r.eventUrl() != null && !r.eventUrl().isBlank()) {
            try {
                URI uri = new URI(r.eventUrl());
                if (!Set.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null) throw new Exception();
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event URL must be absolute HTTP(S)");
            }
        }
    }
    private static boolean blank(String s){return s==null||s.isBlank();}
    private static void setNullableString(PreparedStatement p,int i,String v)throws SQLException{if(blank(v))p.setNull(i,Types.VARCHAR);else p.setString(i,v.trim());}
    private static void setNullableDecimal(PreparedStatement p,int i,BigDecimal v)throws SQLException{if(v==null)p.setNull(i,Types.DECIMAL);else p.setBigDecimal(i,v);}
    private static void setNullableDate(PreparedStatement p,int i,LocalDate v)throws SQLException{if(v==null)p.setNull(i,Types.DATE);else p.setDate(i,java.sql.Date.valueOf(v));}
    private static void setNullableTimestamp(PreparedStatement p,int i,LocalDateTime v)throws SQLException{if(v==null)p.setNull(i,Types.TIMESTAMP);else p.setTimestamp(i,Timestamp.valueOf(v));}

    @PostMapping("/{id}/post-event")
    public Map<String,Object> report(@PathVariable long id,
                                      @RequestHeader(value="Authorization",required=false) String h,
                                      @Valid @RequestBody PostEventReportRequestDTO request) throws Exception {
        String t = token(h);
        if (!"STUDENT".equals(jwt.getRoleFromToken(t))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
        }
        String participation = normalize(request.getParticipationStatus());
        if (!Set.of("YES", "NO").contains(participation)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Participation status must be YES or NO");
        }
        String resultType = normalizeNullable(request.getResultType());
        if ("NO".equals(participation)) {
            if (blank(request.getNoParticipationReason())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A reason is required when the student did not participate");
            }
        } else {
            if (resultType == null || !Set.of("PARTICIPANT", "FINALIST", "WINNER", "RUNNER_UP", "POSITION", "OTHER").contains(resultType)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid result type is required when the student participated");
            }
            if ("POSITION".equals(resultType) && blank(request.getPosition())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position is required for POSITION results");
            }
        }
        validateOptionalUrl(request.getResultUrl());
        long userId = jwt.getUserIdFromToken(t);
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                String existingStatus;
                try (PreparedStatement q = c.prepareStatement(
                        "SELECT a.participation_status FROM applications a " +
                                "JOIN students s ON s.student_id=a.created_by_student_id " +
                                "JOIN events e ON e.event_id=a.event_id " +
                                "WHERE a.application_id=? AND s.user_id=? AND a.current_status='APPROVED' " +
                                "AND e.end_date < CURRENT_DATE FOR UPDATE")) {
                    q.setLong(1, id); q.setLong(2, userId);
                    try (ResultSet rs = q.executeQuery()) {
                        if (!rs.next()) throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Only your approved activity whose end date has passed can be reported");
                        existingStatus = rs.getString(1);
                    }
                }
                if (Set.of("YES", "NO", "REPORTED").contains(normalizeNullable(existingStatus))) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Post-activity details have already been submitted");
                }
                String notes = buildNotes(participation, request);
                try (PreparedStatement update = c.prepareStatement(
                        "UPDATE applications SET participation_status=?, post_event_reported_at=CURRENT_TIMESTAMP, " +
                                "outcome_notes=?, result_url=?, updated_at=CURRENT_TIMESTAMP WHERE application_id=?")) {
                    update.setString(1, participation); setNullableString(update, 2, notes);
                    setNullableString(update, 3, request.getResultUrl()); update.setLong(4, id);
                    if (update.executeUpdate() != 1) throw new ResponseStatusException(HttpStatus.CONFLICT, "Application could not be updated");
                }
                if ("YES".equals(participation)) {
                    boolean alreadyExists;
                    try (PreparedStatement q = c.prepareStatement("SELECT 1 FROM achievements WHERE application_id=? LIMIT 1")) {
                        q.setLong(1, id); try (ResultSet rs = q.executeQuery()) { alreadyExists = rs.next(); }
                    }
                    if (!alreadyExists) {
                        try (PreparedStatement insert = c.prepareStatement(
                                "INSERT INTO achievements (application_id,result_type,position,prize_details) VALUES (?,?,?,?)")) {
                            insert.setLong(1, id); insert.setString(2, resultType);
                            setNullableString(insert, 3, request.getPosition());
                            setNullableString(insert, 4, request.getResultTitle()); insert.executeUpdate();
                        }
                    }
                }
                c.commit();
                Map<String,Object> response = new LinkedHashMap<>();
                response.put("participationStatus", participation);
                response.put("postEventStatus", "COMPLETE");
                if (resultType != null) response.put("resultType", resultType);
                return response;
            } catch (Exception e) {
                c.rollback();
                if (e instanceof ResponseStatusException re) throw re;
                throw e;
            } finally { c.setAutoCommit(true); }
        }
    }

    private static String normalize(String value) { return value == null ? "" : value.trim().toUpperCase(Locale.ROOT); }
    private static String normalizeNullable(String value) { return blank(value) ? null : normalize(value); }
    private static String buildNotes(String participation, PostEventReportRequestDTO request) {
        if ("NO".equals(participation)) return request.getNoParticipationReason().trim();
        List<String> parts = new ArrayList<>();
        if (!blank(request.getRemarks())) parts.add("Remarks: " + request.getRemarks().trim());
        if (!blank(request.getReflection())) parts.add("Reflection: " + request.getReflection().trim());
        return parts.isEmpty() ? null : String.join("\n", parts);
    }
    private static void validateOptionalUrl(String value) {
        if (blank(value)) return;
        try { URI uri = new URI(value.trim());
            if (!Set.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null) throw new Exception();
        } catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Result URL must be absolute HTTP(S)"); }
    }
}
