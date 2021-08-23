package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.CaseTransferred;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityAdded;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityRemoved;
import uk.gov.hmcts.reform.fpl.exceptions.RecipientNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.notify.CaseTransferredNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.SharedLocalAuthorityChangedNotifyData;
import uk.gov.hmcts.reform.fpl.service.ApplicantLocalAuthorityService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityChangedContentProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CASE_TRANSFERRED_NEW_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CASE_TRANSFERRED_PREV_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_ADDED_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_ADDED_SHARED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_REMOVED_SHARED_LA_TEMPLATE;

@ExtendWith(MockitoExtension.class)
class LocalAuthorityChangedHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicantLocalAuthorityService localAuthorityService;

    @Mock
    private LocalAuthorityChangedContentProvider notifyDataProvider;

    @InjectMocks
    private LocalAuthorityChangedHandler underTest;

    private final CaseData caseDataBefore = CaseData.builder()
        .id(RandomUtils.nextLong())
        .build();

    private final CaseData caseData = caseDataBefore.toBuilder()
        .build();

    private final LocalAuthority localAuthority = LocalAuthority.builder()
        .email("la@test.com")
        .build();

    @Nested
    class SharedLocalAuthorityAdded {

        final SecondaryLocalAuthorityAdded event = SecondaryLocalAuthorityAdded.builder()
            .caseData(caseData)
            .build();

        final SharedLocalAuthorityChangedNotifyData notifyData = SharedLocalAuthorityChangedNotifyData.builder()
            .ccdNumber(caseData.getId().toString())
            .build();

        @Test
        void shouldNotifyAddedLocalAuthority() {

            when(localAuthorityService.getSecondaryLocalAuthority(caseData))
                .thenReturn(Optional.of(localAuthority));

            when(notifyDataProvider.getNotifyDataForAddedLocalAuthority(caseData))
                .thenReturn(notifyData);

            underTest.notifySecondaryLocalAuthority(event);

            verify(notificationService).sendEmail(
                LOCAL_AUTHORITY_ADDED_SHARED_LA_TEMPLATE,
                localAuthority.getEmail(),
                notifyData,
                caseData.getId());

            verifyNoMoreInteractions(notificationService);
        }

        @Test
        void shouldNotifyDesignatedLocalAuthority() {

            when(localAuthorityService.getDesignatedLocalAuthority(caseData))
                .thenReturn(localAuthority);

            when(notifyDataProvider.getNotifyDataForDesignatedLocalAuthority(caseData))
                .thenReturn(notifyData);

            underTest.notifyDesignatedLocalAuthority(event);

            verify(notificationService).sendEmail(
                LOCAL_AUTHORITY_ADDED_DESIGNATED_LA_TEMPLATE,
                localAuthority.getEmail(),
                notifyData,
                caseData.getId());

            verifyNoMoreInteractions(notificationService);
        }

        @Test
        void shouldThrowsExceptionWhenSecondaryLocalAuthorityNotPresent() {

            when(localAuthorityService.getSecondaryLocalAuthority(caseData))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> underTest.notifySecondaryLocalAuthority(event))
                .isInstanceOf(RecipientNotFoundException.class);

            verifyNoInteractions(notificationService);
        }
    }

    @Nested
    class SharedLocalAuthorityRemoved {

        final SecondaryLocalAuthorityRemoved event = SecondaryLocalAuthorityRemoved.builder()
            .caseData(caseData)
            .caseDataBefore(caseDataBefore)
            .build();

        final SharedLocalAuthorityChangedNotifyData notifyData = SharedLocalAuthorityChangedNotifyData.builder()
            .ccdNumber(caseData.getId().toString())
            .build();

        @Test
        void shouldNotifyRemovedLocalAuthority() {

            when(localAuthorityService.getSecondaryLocalAuthority(caseDataBefore))
                .thenReturn(Optional.of(localAuthority));

            when(notifyDataProvider.getNotifyDataForRemovedLocalAuthority(caseData, caseDataBefore))
                .thenReturn(notifyData);

            underTest.notifySecondaryLocalAuthority(event);

            verify(notificationService).sendEmail(
                LOCAL_AUTHORITY_REMOVED_SHARED_LA_TEMPLATE,
                localAuthority.getEmail(),
                notifyData,
                caseData.getId());

            verifyNoMoreInteractions(notificationService);
        }

        @Test
        void shouldThrowsExceptionWhenSecondaryLocalAuthorityNotPresent() {

            when(localAuthorityService.getSecondaryLocalAuthority(caseDataBefore))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> underTest.notifySecondaryLocalAuthority(event))
                .isInstanceOf(RecipientNotFoundException.class);

            verifyNoInteractions(notificationService);
        }
    }

    @Nested
    class CaseTransfer {

        final CaseTransferred event = CaseTransferred.builder()
            .caseData(caseData)
            .caseDataBefore(caseDataBefore)
            .build();

        final CaseTransferredNotifyData notifyData = CaseTransferredNotifyData.builder()
            .ccdNumber(caseData.getId().toString())
            .build();

        @Test
        void shouldNotifyNewDesignatedLocalAuthority() {

            when(localAuthorityService.getDesignatedLocalAuthority(caseData))
                .thenReturn(localAuthority);

            when(notifyDataProvider.getCaseTransferredNotifyData(caseData, caseDataBefore))
                .thenReturn(notifyData);

            underTest.notifyNewDesignatedLocalAuthority(event);

            verify(notificationService).sendEmail(
                CASE_TRANSFERRED_NEW_DESIGNATED_LA_TEMPLATE,
                localAuthority.getEmail(),
                notifyData,
                caseData.getId());

            verifyNoMoreInteractions(notificationService);
        }

        @Test
        void shouldNotifyPrevDesignatedLocalAuthority() {

            when(localAuthorityService.getDesignatedLocalAuthority(caseDataBefore))
                .thenReturn(localAuthority);

            when(notifyDataProvider.getCaseTransferredNotifyData(caseData, caseDataBefore))
                .thenReturn(notifyData);

            underTest.notifyPreviousDesignatedLocalAuthority(event);

            verify(notificationService).sendEmail(
                CASE_TRANSFERRED_PREV_DESIGNATED_LA_TEMPLATE,
                localAuthority.getEmail(),
                notifyData,
                caseData.getId());

            verifyNoMoreInteractions(notificationService);
        }

    }
}
