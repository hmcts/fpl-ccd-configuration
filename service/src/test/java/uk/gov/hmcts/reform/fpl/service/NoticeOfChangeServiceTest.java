package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.AuditEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class NoticeOfChangeServiceTest {

    private static final Long CASE_ID = 10L;
    private static final String USER_ID = "User1";
    private static final String NOC_REQUEST_EVENT = "nocRequest";

    @Mock
    private UserService userService;

    @Mock
    private AuditEventService auditEventService;

    @Mock
    private RespondentRepresentationService respondentRepresentationService;

    @InjectMocks
    private NoticeOfChangeService underTest;

    @Test
    void shouldUpdateRespondentRepresentation() {

        final Element<Respondent> respondent = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("George")
                .lastName("West")
                .build())
            .solicitor(RespondentSolicitor.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@test.com")
                .build())
            .build());

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(List.of(respondent))
            .build();

        final AuditEvent auditEvent = AuditEvent.builder()
            .id("nocRequest")
            .userFirstName("Johnny")
            .userLastName("Smithy")
            .userId(USER_ID)
            .build();

        final UserDetails solicitorUser = UserDetails.builder()
            .email("john.smith@test.com")
            .forename("John")
            .surname("Smith")
            .build();

        when(auditEventService.getLatestAuditEventByName(caseData.getId().toString(), NOC_REQUEST_EVENT))
            .thenReturn(Optional.of(auditEvent));

        when(userService.getUserDetailsById(USER_ID))
            .thenReturn(solicitorUser);

        when(respondentRepresentationService.updateRepresentation(caseData, solicitorUser))
            .thenReturn(List.of(respondent));

        final List<Element<Respondent>> updatedRespondents = underTest.updateRepresentation(caseData);

        assertThat(updatedRespondents).containsExactly(respondent);

        verify(auditEventService).getLatestAuditEventByName(CASE_ID.toString(), NOC_REQUEST_EVENT);
        verify(userService).getUserDetailsById(USER_ID);
        verify(respondentRepresentationService).updateRepresentation(caseData, solicitorUser);
        verifyNoMoreInteractions(auditEventService, userService, respondentRepresentationService);
    }

    @Test
    void shouldThrowExceptionWhenNoPreviousNoticeOfChangeRequestInEventAudit() {

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        when(auditEventService.getLatestAuditEventByName(caseData.getId().toString(), NOC_REQUEST_EVENT))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.updateRepresentation(caseData))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Could not find nocRequest event in audit");

        verify(auditEventService).getLatestAuditEventByName(CASE_ID.toString(), NOC_REQUEST_EVENT);
        verifyNoMoreInteractions(auditEventService, userService, respondentRepresentationService);
    }

}
