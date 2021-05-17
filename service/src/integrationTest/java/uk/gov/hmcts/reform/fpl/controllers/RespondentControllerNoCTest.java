package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORB;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerNoCTest extends AbstractCallbackTest {

    private final Organisation organisation1 = organisation("ORG_1");
    private final Organisation organisation2 = organisation("ORG_2");
    private final Organisation organisation3 = organisation("ORG_3");

    private final Element<Respondent> respondent1 = respondent(organisation1);
    private final Element<Respondent> respondent2 = respondent(organisation2);
    private final Element<Respondent> respondent1Updated = respondent(respondent1, organisation3);
    private final Element<Respondent> respondent2Updated = respondent(respondent2, null);

    private final List<Element<Respondent>> respondents = List.of(respondent1, respondent2);
    private final List<Element<Respondent>> updatedRespondents = List.of(respondent1Updated, respondent2Updated);

    private final CaseData caseDataBefore = CaseData.builder()
        .id(10L)
        .state(SUBMITTED)
        .respondents1(respondents)
        .build();

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    RespondentControllerNoCTest() {
        super("enter-respondents");
    }

    @BeforeEach
    void setUp() {
        givenFplService();
        givenSystemUser();
    }

    @Test
    void shouldUpdateRepresentativesAccess() {

        final ChangeOrganisationRequest expectedChange1 = ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .organisationToAdd(organisation3)
            .organisationToRemove(organisation1)
            .caseRoleId(caseRoleDynamicList(SOLICITORA))
            .requestTimestamp(now())
            .build();

        final ChangeOrganisationRequest expectedChange2 = ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .caseRoleId(caseRoleDynamicList(SOLICITORB))
            .organisationToRemove(organisation2)
            .requestTimestamp(now())
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .state(SUBMITTED)
            .respondents1(updatedRespondents)
            .build();

        givenEventStarted(2);

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        verifyEventsStarted(caseData, expectedChange1, expectedChange2);
    }

    @Test
    void shouldNotUpdateRepresentativesAccessWhenCaseNotSubmitted() {

        final CaseData caseData = caseDataBefore.toBuilder()
            .state(OPEN)
            .respondents1(updatedRespondents)
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        verifyNoEventStarted();
    }

    private void givenEventStarted(int times) {
        final List<StartEventResponse> startEventResponses = IntStream.range(0, times)
            .mapToObj(i -> RandomStringUtils.randomAlphanumeric(10))
            .map(token -> StartEventResponse.builder().eventId("updateRepresentation").token(token).build())
            .collect(toList());

        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(startEventResponses));
    }

    private void verifyEventsStarted(CaseData caseData, ChangeOrganisationRequest... requests) {
        final ArgumentCaptor<CaseDataContent> captor = ArgumentCaptor.forClass(CaseDataContent.class);

        final List<Map<String, ChangeOrganisationRequest>> update = Stream.of(requests)
            .map(changeRequest -> Map.of("changeOrganisationRequestField", changeRequest))
            .collect(toList());

        verify(coreCaseDataApi, times(update.size())).startEventForCaseWorker(
            USER_AUTH_TOKEN,
            SERVICE_AUTH_TOKEN,
            SYS_USER_ID,
            JURISDICTION,
            CASE_TYPE,
            caseData.getId().toString(),
            "updateRepresentation");

        verify(coreCaseDataApi, times(update.size())).submitEventForCaseWorker(
            eq(USER_AUTH_TOKEN),
            eq(SERVICE_AUTH_TOKEN),
            eq(SYS_USER_ID),
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(caseData.getId().toString()),
            eq(true),
            captor.capture());

        assertThat(captor.getAllValues())
            .extracting(CaseDataContent::getData)
            .containsExactlyInAnyOrderElementsOf(update);
    }

    private void verifyNoEventStarted() {
        verifyNoInteractions(coreCaseDataApi);
    }

    private static Element<Respondent> respondent(Organisation organisation) {
        return element(Respondent.builder()
            .solicitor(RespondentSolicitor.builder()
                .organisation(organisation)
                .build())
            .build());
    }

    private static Element<Respondent> respondent(Element<Respondent> respondent, Organisation organisation) {
        return element(respondent.getId(), respondent.getValue().toBuilder()
            .solicitor(RespondentSolicitor.builder()
                .organisation(organisation)
                .build())
            .build());
    }
}
