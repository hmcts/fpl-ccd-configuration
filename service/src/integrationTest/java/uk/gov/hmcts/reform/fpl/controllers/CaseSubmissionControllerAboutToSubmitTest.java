package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.NoticeOfChangeAnswersData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentPolicyData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.casesubmission.CaseSubmissionService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Map.of;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.SecureDocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerAboutToSubmitTest extends AbstractCallbackTest {

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CaseSubmissionService caseSubmissionService;

    private final Document document = document();
    private final LocalDate respondentDOB = LocalDate.now();

    CaseSubmissionControllerAboutToSubmitTest() {
        super("case-submission");
    }

    @BeforeEach
    void mocking() {
        givenCurrentUserWithName("Emma Taylor");

        doReturn(document).when(caseSubmissionService).generateC110aSubmittedFormPDF(any(), eq(false));

        given(uploadDocumentService.uploadPDF(DOCUMENT_CONTENT, "2313.pdf"))
            .willReturn(document);
        given(featureToggleService.isRestrictedFromCaseSubmission("FPLA"))
            .willReturn(true);
    }

    @Test
    void shouldReturnUnsuccessfulResponseWithNoData() {
        postAboutToSubmitEvent(new byte[] {}, SC_BAD_REQUEST);
    }

    @Test
    void shouldReturnUnsuccessfulResponseWithMalformedData() {
        postAboutToSubmitEvent("malformed json".getBytes(), SC_BAD_REQUEST);
    }

    @Test
    void shouldSetCtscPropertyToYesRegardlessOfLDVariable() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent("fixtures/case.json");

        assertThat(callbackResponse.getData())
            .containsEntry("caseLocalAuthority", LOCAL_AUTHORITY_1_CODE)
            .containsEntry("sendToCtsc", "Yes")
            .containsEntry("submittedForm", ImmutableMap.<String, String>builder()
                .put("document_url", document.links.self.href)
                .put("document_binary_url", document.links.binary.href)
                .put("document_filename", document.originalDocumentName)
                .build());
    }

    @Test
    void shouldSetCtscPropertyToNoWhenCtscLaunchDarklyVariableIsDisabled() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent("fixtures/case.json");

        assertThat(callbackResponse.getData()).containsEntry("sendToCtsc", "Yes");
    }

    @Test
    void shouldSetCtscPropertyToYesRegardlessOfIfCaseLocalAuthorityIsNotSet() {

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(CaseDetails.builder()
            .id(2313L)
            .data(Map.of(
                "dateSubmitted", dateNow(),
                "orders", Orders.builder().orderType(List.of(CARE_ORDER)).build(),
                "amountToPay", "233300",
                "displayAmountToPay", "Yes",
                "applicants", wrapElements(buildApplicant()),
                "respondents1", wrapElements(Respondent.builder().party(buildRespondentParty()).build())
            ))
            .build());

        assertThat(callbackResponse.getData())
            .containsEntry("sendToCtsc", "Yes");
    }

    @Test
    void shouldRetainPaymentInformationInCase() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(CaseDetails.builder()
            .id(2313L)
            .data(Map.of(
                "dateSubmitted", dateNow(),
                "orders", Orders.builder().orderType(List.of(CARE_ORDER)).build(),
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
                "amountToPay", "233300",
                "displayAmountToPay", "Yes",
                "applicants", wrapElements(buildApplicant()),
                "respondents1", wrapElements(Respondent.builder().party(buildRespondentParty()).build())
            ))
            .build());

        assertThat(callbackResponse.getData())
            .containsEntry("amountToPay", "233300")
            .containsEntry("displayAmountToPay", YES.getValue());
    }

    @Nested
    class LocalAuthorityValidation {

        final String localAuthority = LOCAL_AUTHORITY_1_CODE;
        final CaseDetails caseDetails = CaseDetails.builder()
            .data(of("caseLocalAuthority", localAuthority,
                "applicants", wrapElements(buildApplicant()),
                "respondents1", wrapElements(Respondent.builder().party(buildRespondentParty()).build())
            ))
            .build();

        @Test
        void shouldReturnErrorWhenCaseSubmissionIsBlockedForLocalAuthority() {
            given(featureToggleService.isRestrictedFromCaseSubmission(localAuthority)).willReturn(true);

            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

            assertThat(callbackResponse.getData()).containsEntry("caseLocalAuthority", localAuthority);
            assertThat(callbackResponse.getErrors()).contains("You cannot submit this application online yet."
                + " Ask your FPL administrator for your local authority's enrolment date");
        }

        @Test
        void shouldReturnNoErrorsWhenCaseSubmissionIsAllowedForLocalAuthority() {
            given(featureToggleService.isRestrictedFromCaseSubmission(localAuthority)).willReturn(false);

            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

            assertThat(callbackResponse.getData()).containsEntry("caseLocalAuthority", localAuthority);
            assertThat(callbackResponse.getErrors()).isEmpty();
        }
    }

    @Test
    void shouldMapCaseDetailsToNoticeOfChangeAnswersAndRespondentPolicies() {
        UUID respondentElementOneId = UUID.randomUUID();
        UUID respondentElementTwoId = UUID.randomUUID();

        RespondentParty respondentParty = buildRespondentParty();

        Organisation solicitorOrganisation = Organisation.builder()
            .organisationName("Summers Inc")
            .organisationID("12345")
            .build();

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName("Ben")
            .lastName("Summers")
            .email("bensummers@gmail.com")
            .organisation(solicitorOrganisation)
            .build();

        Respondent respondentOne = Respondent.builder()
            .party(respondentParty)
            .legalRepresentation("Yes")
            .solicitor(respondentSolicitor)
            .build();

        Respondent respondentTwo = Respondent.builder()
            .party(respondentParty)
            .legalRepresentation("No")
            .build();

        List<Element<Respondent>> respondents = List.of(
            element(respondentElementOneId, respondentOne),
            element(respondentElementTwoId, respondentTwo));

        Map<String, Object> data = new HashMap<>(Map.of(
            "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
            "respondents1", respondents,
            "applicants", List.of(element(buildApplicant()))
        ));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);
        CaseData updatedCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        OrganisationPolicy expectedRespondentPolicyOne = OrganisationPolicy.builder()
            .organisation(solicitorOrganisation)
            .orgPolicyCaseAssignedRole(SolicitorRole.SOLICITORA.getCaseRoleLabel())
            .build();

        NoticeOfChangeAnswers expectedNoticeOfChangeAnswers = NoticeOfChangeAnswers.builder()
            .respondentFirstName("Joe")
            .respondentLastName("Bloggs")
            .build();

        RespondentPolicyData respondentPolicyData = updatedCaseData.getRespondentPolicyData();
        NoticeOfChangeAnswersData noticeOfChangeAnswersData = updatedCaseData.getNoticeOfChangeAnswersData();

        assertThat(updatedCaseData.getRespondents1()).isEqualTo(respondents);
        assertThat(noticeOfChangeAnswersData.getNoticeOfChangeAnswers0()).isEqualTo(expectedNoticeOfChangeAnswers);
        assertThat(noticeOfChangeAnswersData.getNoticeOfChangeAnswers1()).isEqualTo(expectedNoticeOfChangeAnswers);

        assertThat(respondentPolicyData).isEqualTo(RespondentPolicyData.builder()
            .respondentPolicy0(expectedRespondentPolicyOne)
            .respondentPolicy1(buildOrganisationPolicy(SolicitorRole.SOLICITORB))
            .respondentPolicy2(buildOrganisationPolicy(SolicitorRole.SOLICITORC))
            .respondentPolicy3(buildOrganisationPolicy(SolicitorRole.SOLICITORD))
            .respondentPolicy4(buildOrganisationPolicy(SolicitorRole.SOLICITORE))
            .respondentPolicy5(buildOrganisationPolicy(SolicitorRole.SOLICITORF))
            .respondentPolicy6(buildOrganisationPolicy(SolicitorRole.SOLICITORG))
            .respondentPolicy7(buildOrganisationPolicy(SolicitorRole.SOLICITORH))
            .respondentPolicy8(buildOrganisationPolicy(SolicitorRole.SOLICITORI))
            .respondentPolicy9(buildOrganisationPolicy(SolicitorRole.SOLICITORJ))
            .build());
    }

    @Test
    void shouldRemoveTransientFields() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(CaseDetails.builder()
            .id(2313L)
            .data(Map.of(
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
                "applicants", List.of(element(buildApplicant())),
                "respondents1", wrapElements(Respondent.builder().party(buildRespondentParty()).build()),
                "draftApplicationDocument", DocumentReference.buildFromDocument(document),
                "submissionConsentLabel", "Test",
                "temporaryApplicationDocuments", buildApplicationDocuments()
            ))
            .build());

        assertThat(callbackResponse.getData()).doesNotContainKeys(
            "draftApplicationDocument",
            "submissionConsentLabel",
            "temporaryApplicationDocuments"
        );
    }

    private RespondentParty buildRespondentParty() {
        return RespondentParty.builder()
            .firstName("Joe")
            .lastName("Bloggs")
            .relationshipToChild("Father")
            .dateOfBirth(respondentDOB)
            .telephoneNumber(Telephone.builder()
                .contactDirection("By telephone")
                .telephoneNumber("02838882333")
                .telephoneUsageType("Personal home number")
                .build())
            .gender("Male")
            .placeOfBirth("Newry")
            .build();
    }

    private List<Element<ApplicationDocument>> buildApplicationDocuments() {
        return List.of(element(ApplicationDocument.builder()
            .documentType(ApplicationDocumentType.CHECKLIST_DOCUMENT)
            .document(DocumentReference.builder().filename("ABC").build())
            .build()));
    }

    private Applicant buildApplicant() {
        return Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName("Test organisation")
                .build())
            .build();
    }

    private OrganisationPolicy buildOrganisationPolicy(SolicitorRole solicitorRole) {
        return OrganisationPolicy.builder()
            .organisation(Organisation.builder().build())
            .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
            .build();
    }
}
