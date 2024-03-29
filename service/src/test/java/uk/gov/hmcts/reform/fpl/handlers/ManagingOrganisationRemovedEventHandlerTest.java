package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.ManagingOrganisationRemoved;
import uk.gov.hmcts.reform.fpl.exceptions.RecipientNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.ManagingOrganisationRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ManagingOrganisationRemovedContentProvider;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Set;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.MANAGING_ORGANISATION_REMOVED_TEMPLATE;

@ExtendWith(MockitoExtension.class)
class ManagingOrganisationRemovedEventHandlerTest {

    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ManagingOrganisationRemovedContentProvider contentProvider;

    @InjectMocks
    private ManagingOrganisationRemovedEventHandler underTest;

    @Test
    void shouldSendEmailToManagingOrganisationSolicitor() {
        final Set<String> recipients = Set.of("email1@test.com", "email2@test.com");

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .build();

        final Organisation organisation = Organisation.builder().build();

        final ManagingOrganisationRemoved event = new ManagingOrganisationRemoved(caseData, organisation);

        final NotifyData notifyData = ManagingOrganisationRemovedNotifyData.builder().build();

        final RecipientsRequest expectedRecipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .legalRepresentativesExcluded(true)
            .secondaryLocalAuthorityExcluded(true)
            .build();

        when(contentProvider.getEmailData(organisation, caseData)).thenReturn(notifyData);
        when(localAuthorityRecipients.getRecipients(any())).thenReturn(recipients);

        underTest.notifyManagingOrganisation(event);

        verify(contentProvider).getEmailData(organisation, caseData);

        verify(notificationService)
            .sendEmail(MANAGING_ORGANISATION_REMOVED_TEMPLATE, recipients, notifyData, caseData.getId());

        verify(localAuthorityRecipients).getRecipients(expectedRecipientsRequest);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowsExceptionWhenSolicitorEmailNotPresent(Set<String> recipients) {
        final CaseData caseData = CaseData.builder().build();

        final Organisation organisation = Organisation.builder().build();

        final ManagingOrganisationRemoved event = new ManagingOrganisationRemoved(caseData, organisation);

        final RecipientsRequest expectedRecipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .legalRepresentativesExcluded(true)
            .secondaryLocalAuthorityExcluded(true)
            .build();

        when(localAuthorityRecipients.getRecipients(any())).thenReturn(recipients);

        assertThatThrownBy(() -> underTest.notifyManagingOrganisation(event))
            .isInstanceOf(RecipientNotFoundException.class);

        verifyNoInteractions(contentProvider, notificationService);

        verify(localAuthorityRecipients).getRecipients(expectedRecipientsRequest);
    }

}
