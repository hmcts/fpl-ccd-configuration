package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.AuditEvent;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.noc.UpdateRepresentationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@ExtendWith(MockitoExtension.class)
class NoticeOfChangeServiceTest {

    private static final Long CASE_ID = 10L;
    private static final String USER_ID = "User1";
    private static final String NOC_REQUEST_EVENT = "nocRequest";
    private static final Map<String, Object> UPDATED_REPRESENTATION = Map.of("some", "stuff");

    @Mock
    private UserService userService;

    @Mock
    private AuditEventService auditEventService;

    @Mock
    private RespondentService respondentService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private UpdateRepresentationService updateRepresentationService;

    @InjectMocks
    private NoticeOfChangeService underTest;

    @Nested
    class UpdateRepresentation {

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

            when(updateRepresentationService.updateRepresentation(caseData, solicitorUser))
                .thenReturn(UPDATED_REPRESENTATION);

            final Map<String, Object> actual = underTest.updateRepresentation(caseData);

            assertThat(actual).isEqualTo(UPDATED_REPRESENTATION);

            verify(auditEventService).getLatestAuditEventByName(CASE_ID.toString(), NOC_REQUEST_EVENT);
            verify(userService).getUserDetailsById(USER_ID);
            verifyNoMoreInteractions(auditEventService, userService, updateRepresentationService);
        }

        @Test
        void shouldUpdateRepresentationForThirdPartyOutsourcing() {
            Organisation newOrg = Organisation.builder().organisationID("Test123").build();

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .outsourcingPolicy(OrganisationPolicy.builder()
                    .organisation(newOrg)
                    .orgPolicyCaseAssignedRole("[EPSMANAGING]")
                    .build())
                .changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                    .organisationToAdd(newOrg)
                    .caseRoleId(caseRoleDynamicList("[EPSMANAGING]"))
                    .build())
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

            when(updateRepresentationService.updateRepresentationThirdPartyOutsourcing(caseData, solicitorUser))
                .thenReturn(UPDATED_REPRESENTATION);

            final Map<String, Object> actual = underTest.updateRepresentation(caseData);

            assertThat(actual).isEqualTo(UPDATED_REPRESENTATION);

            verify(auditEventService).getLatestAuditEventByName(CASE_ID.toString(), NOC_REQUEST_EVENT);
            verify(userService).getUserDetailsById(USER_ID);
            verifyNoMoreInteractions(auditEventService, userService, updateRepresentationService);
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
            verifyNoMoreInteractions(auditEventService, userService, updateRepresentationService);
        }
    }

    @Nested
    @SuppressWarnings({"unchecked", "rawtypes"})
    class UpdateRepresentationAccess {

        @Test
        void shouldNotApplyDecisionWhenRepresentationsHaveNotChanged() {

            final Element<Respondent> respondent1 = respondent(organisation("ORG1"));

            final List<Element<Respondent>> previousRespondents = List.of(respondent1);
            final List<Element<Respondent>> newRespondents = List.of(respondent1);

            final CaseData caseDataBefore = CaseData.builder()
                .id(CASE_ID)
                .respondents1(previousRespondents)
                .build();

            final CaseData caseData = caseDataBefore.toBuilder()
                .respondents1(newRespondents)
                .build();

            when(respondentService.getRepresentationChanges((List)newRespondents,(List) previousRespondents,
                SolicitorRole.Representing.RESPONDENT))
                .thenReturn(emptyList());

            underTest.updateRepresentativesAccess(caseData, caseDataBefore, SolicitorRole.Representing.RESPONDENT);

            verifyNoInteractions(coreCaseDataService);
        }

        @Test
        void shouldApplyNoCDecisionForEachChangedOrganisation() {

            Organisation organisation1 = organisation("ORG1");
            Organisation organisation2 = organisation("ORG2");
            Organisation organisation3 = organisation("ORG3");

            Element<Respondent> respondent1 = respondent(organisation1);
            Element<Respondent> respondent2 = respondent(organisation2);
            Element<Respondent> respondent1Updated = respondent(respondent1, organisation3);
            Element<Respondent> respondent2Updated = respondent(respondent2, null);

            List<Element<Respondent>> previousRespondents = List.of(respondent1, respondent2);
            List<Element<Respondent>> newRespondents = List.of(respondent1Updated, respondent2Updated);

            CaseData caseDataBefore = CaseData.builder()
                .id(CASE_ID)
                .respondents1(previousRespondents)
                .build();

            CaseData caseData = caseDataBefore.toBuilder()
                .respondents1(newRespondents)
                .build();

            ChangeOrganisationRequest changeOrganisationRequest1 = ChangeOrganisationRequest.builder()
                .organisationToAdd(organisation3)
                .organisationToRemove(organisation1)
                .build();

            ChangeOrganisationRequest changeOrganisationRequest2 = ChangeOrganisationRequest.builder()
                .organisationToRemove(organisation2)
                .build();

            List<ChangeOrganisationRequest> changes = List.of(changeOrganisationRequest1, changeOrganisationRequest2);

            when(respondentService.getRepresentationChanges((List)newRespondents, (List) previousRespondents,
                SolicitorRole.Representing.RESPONDENT)).thenReturn(changes);

            underTest.updateRepresentativesAccess(caseData, caseDataBefore, SolicitorRole.Representing.RESPONDENT);

            verify(coreCaseDataService, times(2))
                .performPostSubmitCallback(eq(CASE_ID), eq("updateRepresentation"), any(), eq(true));
            verify(coreCaseDataService, times(2))
                .performPostSubmitCallback(eq(CASE_ID), eq("internal-change-UPDATE_CASE"), any());
        }

        private Element<Respondent> respondent(Organisation organisation) {
            return element(Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .organisation(organisation)
                    .build())
                .build());
        }

        private Element<Respondent> respondent(Element<Respondent> respondent, Organisation organisation) {
            return element(respondent.getId(), respondent.getValue().toBuilder()
                .solicitor(RespondentSolicitor.builder()
                    .organisation(organisation)
                    .build())
                .build());
        }
    }

}
