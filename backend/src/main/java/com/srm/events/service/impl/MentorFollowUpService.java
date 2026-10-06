package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.FacultyDAO;
import com.srm.events.dao.interfaces.MentorFollowUpDAO;
import com.srm.events.dto.MentorFollowUpItemDTO;
import com.srm.events.dto.MentorFollowUpResponseDTO;
import com.srm.events.dto.MentorFollowUpSummaryDTO;
import com.srm.events.model.Faculty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class MentorFollowUpService {
    private final FacultyDAO facultyDAO;
    private final MentorFollowUpDAO followUpDAO;

    public MentorFollowUpService(FacultyDAO facultyDAO, MentorFollowUpDAO followUpDAO) {
        this.facultyDAO = facultyDAO; this.followUpDAO = followUpDAO;
    }

    public MentorFollowUpResponseDTO get(Long userId, String followUpType, String section, Integer studentYear,
                                         String recordCategory, Long minimumDaysPending) {
        Faculty faculty = facultyDAO.findActiveByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active Mentor profile required"));
        List<MentorFollowUpItemDTO> items = followUpDAO.findForMentor(faculty.getFacultyId()).stream()
                .filter(item -> matches(item.getFollowUpType(), followUpType))
                .filter(item -> matches(item.getSection(), section))
                .filter(item -> studentYear == null || studentYear.equals(item.getStudentYear()))
                .filter(item -> matches(item.getRecordCategory(), recordCategory))
                .filter(item -> minimumDaysPending == null || item.getDaysPending() >= Math.max(0, minimumDaysPending))
                .toList();
        MentorFollowUpSummaryDTO summary = new MentorFollowUpSummaryDTO();
        items.forEach(item -> {
            switch (item.getFollowUpType()) {
                case "NEEDS_MENTOR_REVIEW" -> summary.setNeedsMentorReview(summary.getNeedsMentorReview() + 1);
                case "STUDENT_CORRECTION_PENDING" -> summary.setStudentCorrectionPending(summary.getStudentCorrectionPending() + 1);
                case "POST_EVENT_PENDING" -> summary.setPostEventPending(summary.getPostEventPending() + 1);
                case "POST_EVENT_EVIDENCE_INCOMPLETE" -> summary.setEvidenceIncomplete(summary.getEvidenceIncomplete() + 1);
                case "ACHIEVEMENT_VERIFICATION_PENDING" -> summary.setAchievementVerificationPending(summary.getAchievementVerificationPending() + 1);
                default -> { }
            }
        });
        summary.setTotalAttentionRequired(items.size());
        MentorFollowUpResponseDTO response = new MentorFollowUpResponseDTO();
        response.setSummary(summary); response.setItems(items); return response;
    }

    private boolean matches(String actual, String requested) {
        return requested == null || requested.isBlank() || (actual != null && actual.equalsIgnoreCase(requested.trim()));
    }
}
