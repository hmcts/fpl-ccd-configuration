package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.fpl.events.CaseTransferred;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityAdded;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.notify.CaseTransferredNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.SharedLocalAuthorityChangedNotifyData;
import uk.gov.hmcts.reform.fpl.service.ApplicantLocalAuthorityService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityChangedContentProvider;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CASE_TRANSFERRED_NEW_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CASE_TRANSFERRED_PREV_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_ADDED_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_ADDED_SHARED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_REMOVED_SHARED_LA_TEMPLATE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class LocalAuthorityChangedHandlerTest {

    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;

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

    private final Set<String> recipients = Set.of("test1@test.com", "test2@test,com");

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

            when(localAuthorityRecipients.getRecipients(any())).thenReturn(recipients);

            when(localAuthorityService.getSecondaryLocalAuthority(caseData))
                .thenReturn(Optional.of(localAuthority));

            when(notifyDataProvider.getNotifyDataForAddedLocalAuthority(caseData))
                .thenReturn(notifyData);

            underTest.notifySecondaryLocalAuthority(event);

            final RecipientsRequest expectedRecipientsRequest = RecipientsRequest.builder()
                .caseData(caseData)
                .designatedLocalAuthorityExcluded(true)
                .legalRepresentativesExcluded(true)
                .build();

            verify(notificationService).sendEmail(
                LOCAL_AUTHORITY_ADDED_SHARED_LA_TEMPLATE,
                recipients,
                notifyData,
                caseData.getId());

            verifyNoMoreInteractions(notificationService);
            verify(localAuthorityRecipients).getRecipients(expectedRecipientsRequest);
        }

        @Test
        void shouldNotifyDesignatedLocalAuthority() {

            when(localAuthorityRecipients.getRecipients(any())).thenReturn(recipients);

            when(localAuthorityService.getDesignatedLocalAuthority(caseData))
                .thenReturn(localAuthority);

            when(notifyDataProvider.getNotifyDataForDesignatedLocalAuthority(caseData))
                .thenReturn(notifyData);

            underTest.notifyDesignatedLocalAuthority(event);

            final RecipientsRequest expectedRecipientsRequest = RecipientsRequest.builder()
                .caseData(caseData)
                .secondaryLocalAuthorityExcluded(true)
                .legalRepresentativesExcluded(true)
                .build();

            verify(notificationService).sendEmail(
                LOCAL_AUTHORITY_ADDED_DESIGNATED_LA_TEMPLATE,
                recipients,
                notifyData,
                caseData.getId());

            verifyNoMoreInteractions(notificationService);
            verify(localAuthorityRecipients).getRecipients(expectedRecipientsRequest);
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

            when(localAuthorityRecipients.getRecipients(any())).thenReturn(recipients);

            when(localAuthorityService.getSecondaryLocalAuthority(caseDataBefore))
                .thenReturn(Optional.of(localAuthority));

            when(notifyDataProvider.getNotifyDataForRemovedLocalAuthority(caseData, caseDataBefore))
                .thenReturn(notifyData);

            underTest.notifySecondaryLocalAuthority(event);

            final RecipientsRequest expectedRecipientsRequest = RecipientsRequest.builder()
                .caseData(caseData)
                .designatedLocalAuthorityExcluded(true)
                .legalRepresentativesExcluded(true)
                .build();

            verify(notificationService).sendEmail(
                LOCAL_AUTHORITY_REMOVED_SHARED_LA_TEMPLATE,
                recipients,
                notifyData,
                caseData.getId());

            verifyNoMoreInteractions(notificationService);
            verify(localAuthorityRecipients).getRecipients(expectedRecipientsRequest);
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

            when(localAuthorityRecipients.getRecipients(any())).thenReturn(recipients);

            when(localAuthorityService.getDesignatedLocalAuthority(caseData))
                .thenReturn(localAuthority);

            when(notifyDataProvider.getCaseTransferredNotifyData(caseData, caseDataBefore))
                .thenReturn(notifyData);

            underTest.notifyNewDesignatedLocalAuthority(event);

            final RecipientsRequest expectedRecipientsRequest = RecipientsRequest.builder()
                .caseData(caseData)
                .secondaryLocalAuthorityExcluded(true)
                .legalRepresentativesExcluded(true)
                .build();

            verify(notificationService).sendEmail(
                CASE_TRANSFERRED_NEW_DESIGNATED_LA_TEMPLATE,
                recipients,
                notifyData,
                caseData.getId());

            verifyNoMoreInteractions(notificationService);
            verify(localAuthorityRecipients).getRecipients(expectedRecipientsRequest);
        }

        @Test
        void shouldNotifyPrevDesignatedLocalAuthority() {

            when(localAuthorityRecipients.getRecipients(any())).thenReturn(recipients);

            when(localAuthorityService.getDesignatedLocalAuthority(caseDataBefore))
                .thenReturn(localAuthority);

            when(notifyDataProvider.getCaseTransferredNotifyData(caseData, caseDataBefore))
                .thenReturn(notifyData);

            underTest.notifyPreviousDesignatedLocalAuthority(event);

            final RecipientsRequest expectedRecipientsRequest = RecipientsRequest.builder()
                .caseData(caseData)
                .secondaryLocalAuthorityExcluded(true)
                .legalRepresentativesExcluded(true)
                .build();

            verify(notificationService).sendEmail(
                CASE_TRANSFERRED_PREV_DESIGNATED_LA_TEMPLATE,
                recipients,
                notifyData,
                caseData.getId());

            verifyNoMoreInteractions(notificationService);

            verify(localAuthorityRecipients).getRecipients(expectedRecipientsRequest);
        }

    }
}
