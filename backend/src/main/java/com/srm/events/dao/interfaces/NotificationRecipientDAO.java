package com.srm.events.dao.interfaces;

import java.util.Optional;

public interface NotificationRecipientDAO {
    Optional<Long> findUserIdByFacultyId(Long facultyId);
    Optional<Long> findHodUserIdByDepartmentId(Long departmentId);
}
