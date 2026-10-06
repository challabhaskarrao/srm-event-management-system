package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.AchievementDAO;
import com.srm.events.model.Achievement;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class JdbcAchievementDAO implements AchievementDAO {
    private final DataSource dataSource;
    public JdbcAchievementDAO(DataSource dataSource) { this.dataSource = dataSource; }
    private Achievement map(ResultSet rs) throws SQLException {
        Achievement a=new Achievement(); a.setAchievementId(rs.getLong("achievement_id")); a.setApplicationId(rs.getLong("application_id"));
        a.setResultType(rs.getString("result_type")); a.setPosition(rs.getString("position")); a.setPrizeDetails(rs.getString("prize_details"));
        a.setVerificationStatus(rs.getString("verification_status")); a.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime());
        long reviewer=rs.getLong("verified_by_user_id"); if(!rs.wasNull()) a.setVerifiedByUserId(reviewer);
        Timestamp verified=rs.getTimestamp("verified_at"); if(verified!=null) a.setVerifiedAt(verified.toLocalDateTime());
        a.setRemarks(rs.getString("remarks")); return a;
    }
    @Override public Achievement create(Achievement a) {
        String sql="INSERT INTO achievements (application_id,result_type,position,prize_details) VALUES (?,?,?,?)";
        try(Connection c=dataSource.getConnection();PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            p.setLong(1,a.getApplicationId());p.setString(2,a.getResultType());p.setString(3,a.getPosition());p.setString(4,a.getPrizeDetails());p.executeUpdate();
            try(ResultSet k=p.getGeneratedKeys()){if(k.next())a.setAchievementId(k.getLong(1));}
            return findByApplicationId(a.getApplicationId()).orElseThrow();
        }catch(SQLException e){throw new RuntimeException("Error saving achievement",e);}
    }
    @Override public Optional<Achievement> findByApplicationId(Long id) {
        try(Connection c=dataSource.getConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM achievements WHERE application_id=? ORDER BY achievement_id DESC LIMIT 1")){
            p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(map(r)):Optional.empty();}
        }catch(SQLException e){throw new RuntimeException("Error loading achievement",e);}
    }
    @Override public Achievement review(Long id,String status,String remarks,Long userId){
        String sql="UPDATE achievements SET verification_status=?, remarks=?, verified_by_user_id=?, verified_at=CURRENT_TIMESTAMP WHERE achievement_id=?";
        try(Connection c=dataSource.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,status);p.setString(2,remarks);p.setLong(3,userId);p.setLong(4,id);
            if(p.executeUpdate()!=1)throw new SQLException("Achievement not found");
            try(PreparedStatement q=c.prepareStatement("SELECT * FROM achievements WHERE achievement_id=?")){q.setLong(1,id);try(ResultSet r=q.executeQuery()){if(r.next())return map(r);}}
            throw new SQLException("Achievement not found");
        }catch(SQLException e){throw new RuntimeException("Error reviewing achievement",e);}
    }
}
