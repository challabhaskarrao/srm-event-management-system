package com.srm.events.service;

import com.srm.events.dao.interfaces.NotificationDAO;
import com.srm.events.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    @Mock NotificationDAO notificationDAO;
    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationDAO);
    }

    @Test
    void notifyOnceDelegatesDeterministicDeduplicationToDao() {
        when(notificationDAO.createIfAbsent(7L, "TYPE", "Title", "Message", "APPLICATION", 11L,
                "/student/applications/PAR-001", "TYPE:11:1")).thenReturn(true);

        boolean created = service.notifyOnce(7L, "TYPE", "Title", "Message", "APPLICATION", 11L,
                "/student/applications/PAR-001", "TYPE:11:1");

        assertEquals(true, created);
        verify(notificationDAO).createIfAbsent(7L, "TYPE", "Title", "Message", "APPLICATION", 11L,
                "/student/applications/PAR-001", "TYPE:11:1");
    }

    @Test
    void invalidExternalOrTraversalActionPathsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.notifyOnce(7L, "TYPE", "Title", "Message",
                "APPLICATION", 11L, "https://example.invalid", "TYPE:11:1"));
        assertThrows(IllegalArgumentException.class, () -> service.notifyOnce(7L, "TYPE", "Title", "Message",
                "APPLICATION", 11L, "/student/../admin", "TYPE:11:2"));
        verifyNoInteractions(notificationDAO);
    }

    @Test
    void markReadIsScopedByRecipientAndMissingOtherUserNotificationIsNotFound() {
        when(notificationDAO.markRead(7L, 99L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.markRead(7L, 99L));

        verify(notificationDAO).markRead(7L, 99L);
    }

    @Test
    void listLimitIsBoundedAndUnreadCountIsDelegatedToRecipientScopedDao() {
        service.getForUser(7L, 1000);
        when(notificationDAO.countUnread(7L)).thenReturn(3L);

        assertEquals(3L, service.unreadCount(7L));
        verify(notificationDAO).findForUser(7L, 100);
        verify(notificationDAO).countUnread(7L);
    }
}
