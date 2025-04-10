package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.NoticeOfChangeAnswersData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.LegalCounsellorTestHelper.buildLegalCounsellor;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final String SOLICITOR_ORG_ID = "Organisation ID";
    private static final ApplicantParty LEGACY_APPLICANT = ApplicantParty.builder()
        .organisationName("Applicant org name")
        .build();
    private static final List<Element<Applicant>> APPLICANTS = wrapElements(Applicant.builder()
        .party(LEGACY_APPLICANT).build());
    private static final RespondentSolicitor RESPONDENT_SOLICITOR = RespondentSolicitor.builder()
        .organisation(Organisation.builder()
            .organisationID(SOLICITOR_ORG_ID)
            .build())
        .build();

    RespondentControllerAboutToSubmitTest() {
        super("enter-respondents");
    }

    @Test
    void shouldGenerateRespondentPoliciesAndAnswersWhenToggledOnAndStateIsNotOpen() {
        final Respondent respondentWithRepresentative = respondent(dateNow()).toBuilder()
            .legalRepresentation(YES.getValue())
            .party(RespondentParty.builder()
                .firstName("Alex")
                .lastName("Brown")
                .build())
            .solicitor(RESPONDENT_SOLICITOR)
            .build();

        final LocalAuthority localAuthority = LocalAuthority.builder()
            .name("Local authority name")
            .designated("Yes")
            .build();

        final CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .localAuthorities(wrapElements(localAuthority))
            .applicants(APPLICANTS)
            .respondents1(wrapElements(respondentWithRepresentative))
            .build();

        final CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getRespondentPolicyData().getRespondentPolicy0().getOrganisation().getOrganisationID())
            .isEqualTo(SOLICITOR_ORG_ID);

        final NoticeOfChangeAnswersData expectedAnswers = NoticeOfChangeAnswersData.builder()
            .noticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentFirstName(respondentWithRepresentative.getParty().getFirstName())
                .respondentLastName(respondentWithRepresentative.getParty().getLastName())
                .build())
            .build();
        assertThat(responseData.getNoticeOfChangeAnswersData()).isEqualTo(expectedAnswers);
    }

    @Test
    void shouldGenerateRespondentWithLegacyApplicantPoliciesWhenToggleOnAndStateIsNotOpen() {
        Respondent respondentWithRepresentative = respondent(dateNow()).toBuilder()
            .legalRepresentation(YES.getValue())
            .solicitor(RESPONDENT_SOLICITOR)
            .build();

        final CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .applicants(APPLICANTS)
            .respondents1(wrapElements(respondentWithRepresentative))
            .build();

        final CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getRespondentPolicyData().getRespondentPolicy0().getOrganisation().getOrganisationID())
            .isEqualTo(SOLICITOR_ORG_ID);

        final NoticeOfChangeAnswersData expectedAnswers = NoticeOfChangeAnswersData.builder()
            .noticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentFirstName(respondentWithRepresentative.getParty().getFirstName())
                .respondentLastName(respondentWithRepresentative.getParty().getLastName())
                .build())
            .build();
        assertThat(responseData.getNoticeOfChangeAnswersData()).isEqualTo(expectedAnswers);
    }

    @Test
    void shouldRemoveLegalCounselFromRespondentWhenRepresentativeIsRemoved() {
        List<Element<LegalCounsellor>> counsel = wrapElements(buildLegalCounsellor("1"));
        Respondent respondent1 = respondent(dateNow()).toBuilder()
            .solicitor(RESPONDENT_SOLICITOR)
            .legalCounsellors(counsel)
            .build();
        Respondent respondent2 = respondent(dateNow()).toBuilder()
            .solicitor(RESPONDENT_SOLICITOR)
            .legalCounsellors(counsel)
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .applicants(APPLICANTS)
            .respondents1(wrapElements(respondent1, respondent2))
            .build();
        CaseData caseDataAfter = CaseData.builder()
            .applicants(APPLICANTS)
            .respondents1(wrapElements(respondent1.toBuilder().solicitor(null).build(), respondent2))
            .build();

        CallbackRequest callbackRequest = toCallBackRequest(caseDataAfter, caseDataBefore);
        CaseData returnedCaseData = extractCaseData(postAboutToSubmitEvent(callbackRequest));

        assertThat(returnedCaseData.getAllRespondents().get(0).getValue().getLegalCounsellors()).isEmpty();
        assertThat(returnedCaseData.getAllRespondents().get(1).getValue().getLegalCounsellors()).isEqualTo(counsel);
    }

    @Test
    void shouldPersistRepresentativeAssociation() {
        List<Element<UUID>> association = List.of(element(UUID.randomUUID()));
        Element<Respondent> oldRespondent = element(respondent(dateNow()));
        oldRespondent.getValue().setRepresentedBy(association);

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(
                element(oldRespondent.getId(), respondent(dateNow())),
                element(respondent(dateNow()))
            ))
            .state(OPEN)
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .respondents1(List.of(oldRespondent))
            .build();

        CallbackRequest callbackRequest = toCallBackRequest(caseData, caseDataBefore);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);

        CaseData responseData = extractCaseData(response);

        Respondent firstRespondent = responseData.getRespondents1().get(0).getValue();
        Respondent secondRespondent = responseData.getRespondents1().get(1).getValue();

        assertThat(firstRespondent.getRepresentedBy()).isEqualTo(association);
        assertThat(secondRespondent.getRepresentedBy()).isNullOrEmpty();
    }

    @Test
    void shouldAddConfidentialRespondentsToCaseDataWhenConfidentialRespondentsExist() {
        CallbackRequest callbackRequest = callbackRequest();
        CaseData caseData = extractCaseData(postAboutToSubmitEvent(callbackRequest));
        CaseData initialData = extractCaseData(callbackRequest);

        assertThat(caseData.getConfidentialRespondents())
            .containsOnly(retainConfidentialDetails(initialData.getAllRespondents().get(0)));

        assertThat(caseData.getRespondents1().get(0).getValue().getParty().getAddress()).isNull();
        assertThat(caseData.getRespondents1().get(1).getValue().getParty().getAddress()).isNotNull();
    }

    private Respondent respondent(LocalDate dateOfBirth) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(dateOfBirth)
                .build())
            .build();
    }

    private Element<Respondent> retainConfidentialDetails(Element<Respondent> respondent) {
        return element(respondent.getId(), Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(respondent.getValue().getParty().getFirstName())
                .lastName(respondent.getValue().getParty().getLastName())
                .address(respondent.getValue().getParty().getAddress())
                .telephoneNumber(respondent.getValue().getParty().getTelephoneNumber())
                .email(respondent.getValue().getParty().getEmail())
                .hideAddress("Yes")
                .hideTelephone("Yes")
                .build())
            .build());
    }
}
