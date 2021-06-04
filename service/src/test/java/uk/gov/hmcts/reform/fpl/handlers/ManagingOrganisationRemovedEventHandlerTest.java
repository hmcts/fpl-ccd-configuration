package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.ManagingOrganisationRemoved;
import uk.gov.hmcts.reform.fpl.exceptions.RecipientNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.notify.ManagingOrganisationRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ManagingOrganisationRemovedContentProvider;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.stream.Stream;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.MANAGING_ORGANISATION_REMOVED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testEmail;

@ExtendWith(MockitoExtension.class)
class ManagingOrganisationRemovedEventHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ManagingOrganisationRemovedContentProvider contentProvider;

    @InjectMocks
    private ManagingOrganisationRemovedEventHandler underTest;

    @Test
    void shouldSendEmailToManagingOrganisationSolicitor() {

        final String solicitorEmail = testEmail().getEmail();

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .solicitor(Solicitor.builder()
                .email(solicitorEmail)
                .build())
            .build();

        final Organisation organisation = Organisation.builder().build();

        final ManagingOrganisationRemoved event = new ManagingOrganisationRemoved(caseData, organisation);

        final NotifyData notifyData = ManagingOrganisationRemovedNotifyData.builder().build();

        when(contentProvider.getEmailData(organisation, caseData)).thenReturn(notifyData);

        underTest.notifyManagingOrganisation(event);

        verify(contentProvider).getEmailData(organisation, caseData);

        verify(notificationService)
            .sendEmail(MANAGING_ORGANISATION_REMOVED_TEMPLATE, solicitorEmail, notifyData, caseData.getId());
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("missingSolicitorEmail")
    void shouldThrowsExceptionWhenSolicitorEmailNotPresent(Solicitor managingOrganisationSolicitor) {
        final CaseData caseData = CaseData.builder()
            .solicitor(managingOrganisationSolicitor)
            .build();

        final Organisation organisation = Organisation.builder().build();

        final ManagingOrganisationRemoved event = new ManagingOrganisationRemoved(caseData, organisation);

        assertThatThrownBy(() -> underTest.notifyManagingOrganisation(event))
            .isInstanceOf(RecipientNotFoundException.class);

        verifyNoInteractions(contentProvider, notificationService);
    }

    private static Stream<Solicitor> missingSolicitorEmail() {
        return Stream.of(
            Solicitor.builder().build(),
            Solicitor.builder()
                .email("")
                .build()
        );
    }
}
