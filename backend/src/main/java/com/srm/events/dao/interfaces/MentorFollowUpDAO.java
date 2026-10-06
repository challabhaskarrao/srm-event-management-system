package com.srm.events.dao.interfaces;

import com.srm.events.dto.MentorFollowUpItemDTO;
import java.util.List;

public interface MentorFollowUpDAO {
    List<MentorFollowUpItemDTO> findForMentor(Long facultyId);
}
