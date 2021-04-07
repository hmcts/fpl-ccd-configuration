package uk.gov.hmcts.reform.fpl.service.casesubmission;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.components.NoticeOfChangeRespondentConverter;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeRespondent;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.CaseSubmissionGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C110A;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORB;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisCaseSubmission;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseSubmissionService.class,
    JacksonAutoConfiguration.class,
    NoticeOfChangeRespondentConverter.class})
class CaseSubmissionServiceTest {
    private static final byte[] PDF = TestDataHelper.DOCUMENT_CONTENT;
    private static final LocalDate RESPONDENT_DOB = LocalDate.now();

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private CaseSubmissionGenerationService templateDataGenerationService;

    @Captor
    private ArgumentCaptor<DocmosisCaseSubmission> caseSubmissionDataCaptor;

    @Autowired
    private CaseSubmissionService caseSubmissionService;

    private CaseData givenCaseData;
    private DocmosisCaseSubmission expectedCaseSubmission;

    @BeforeEach
    void setup() {
        expectedCaseSubmission = expectedDocmosisCaseSubmission();
        given(templateDataGenerationService.getTemplateData(any())).willReturn(expectedCaseSubmission);

        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(new DocmosisDocument("case_submission_c110a.pdf", PDF));

        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document());

        givenCaseData = populatedCaseData();
    }

    @Test
    void shouldGenerateCaseSubmissionDocumentSuccessfully() {
        caseSubmissionService.generateSubmittedFormPDF(givenCaseData, false);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionDataCaptor.capture(), eq(C110A));
        DocmosisCaseSubmission caseSubmission = caseSubmissionDataCaptor.getValue();
        assertThat(caseSubmission).isEqualTo(expectedCaseSubmission);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }

    @Test
    void shouldMapRespondentsToNoticeOfChangeRespondentsWhenExisting() {
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
            .build();

        NoticeOfChangeAnswers noticeOfChange = NoticeOfChangeAnswers.builder()
            .respondentFirstName("Joe")
            .respondentLastName("Bloggs")
            .respondentDOB(RESPONDENT_DOB)
            .applicantName("Test organisation")
            .build();

        List<Element<Respondent>> respondents = List.of(
            element(respondentElementOneId, respondentOne),
            element(respondentElementTwoId, respondentTwo));

        NoticeOfChangeRespondent expectedRespondentOne = NoticeOfChangeRespondent.builder()
            .respondentId(respondentElementOneId)
            .noticeOfChangeAnswers(noticeOfChange)
            .organisationPolicy(OrganisationPolicy.builder()
                .organisation(solicitorOrganisation)
                .orgPolicyCaseAssignedRole(SOLICITORA.getCaseRoleLabel())
                .build())
            .build();

        NoticeOfChangeRespondent expectedRespondentTwo = NoticeOfChangeRespondent.builder()
            .respondentId(respondentElementTwoId)
            .noticeOfChangeAnswers(noticeOfChange)
            .organisationPolicy(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(SOLICITORB.getCaseRoleLabel())
                .build())
            .build();

        Map<String, Object> data = new HashMap<>(Map.of(
            "respondents1", respondents,
            "applicants", List.of(element(buildApplicant()))
        ));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();

        CaseDetails updatedCaseDetails = caseSubmissionService.setNoticeOfChangeRespondents(caseDetails);

        Assertions.assertThat(updatedCaseDetails.getData())
            .extracting("respondents1", "respondent1", "respondent2")
            .containsExactly(respondents, expectedRespondentOne, expectedRespondentTwo);
    }

    @Test
    void shouldNotMapRespondentsToNoticeOfChangeRespondentsWhenApplicantsDoNotExist() {
        Respondent respondentOne = Respondent.builder()
            .party(RespondentParty.builder().build())
            .legalRepresentation("Yes")
            .build();

        Respondent respondentTwo = Respondent.builder()
            .party(RespondentParty.builder().build())
            .build();

        List<Element<Respondent>> respondents = List.of(
            element(respondentOne),
            element(respondentTwo));

        CaseDetails caseDetails = CaseDetails.builder().data(Map.of(
            "respondents1", respondents,
            "familyManCaseNumber", "12345"
        )).build();

        CaseDetails updatedCaseDetails = caseSubmissionService.setNoticeOfChangeRespondents(caseDetails);

        assertThat(updatedCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    void shouldNotMapRespondentsToNoticeOfChangeRespondentsWhenRespondentsDoNotExist() {
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of(
            "applicants", List.of(element(buildApplicant())),
            "familyManCaseNumber", "12345"
        )).build();

        CaseDetails updatedCaseDetails = caseSubmissionService.setNoticeOfChangeRespondents(caseDetails);

        assertThat(updatedCaseDetails).isEqualTo(caseDetails);
    }

    private RespondentParty buildRespondentParty() {
        return RespondentParty.builder()
            .firstName("Joe")
            .lastName("Bloggs")
            .relationshipToChild("Father")
            .dateOfBirth(RESPONDENT_DOB)
            .telephoneNumber(Telephone.builder()
                .contactDirection("By telephone")
                .telephoneNumber("02838882333")
                .telephoneUsageType("Personal home number")
                .build())
            .gender("Male")
            .placeOfBirth("Newry")
            .build();
    }

    private Applicant buildApplicant() {
        return Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName("Test organisation")
                .build())
            .build();
    }
}
