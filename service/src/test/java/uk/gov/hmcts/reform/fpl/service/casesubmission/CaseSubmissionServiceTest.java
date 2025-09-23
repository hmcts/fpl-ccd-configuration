package uk.gov.hmcts.reform.fpl.service.casesubmission;

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
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC14Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC15Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC16Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC17Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC18Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC20Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.CaseSubmissionGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C110A;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C14_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C15_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C16_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C17_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C18_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C20_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisC14Supplement;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisC15Supplement;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisC16Supplement;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisC17Supplement;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisC18Supplement;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisC20Supplement;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisCaseSubmission;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.SecureDocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseSubmissionService.class, JacksonAutoConfiguration.class})
class CaseSubmissionServiceTest {
    private static final byte[] PDF = TestDataHelper.DOCUMENT_CONTENT;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private CaseSubmissionGenerationService templateDataGenerationService;

    @Captor
    private ArgumentCaptor<DocmosisCaseSubmission> caseSubmissionDataCaptor;

    @Captor
    private ArgumentCaptor<DocmosisData> caseSubmissionSupplementDataCaptor;


    @Autowired
    private CaseSubmissionService caseSubmissionService;

    private CaseData givenCaseData;
    private DocmosisCaseSubmission expectedCaseSubmission;
    private DocmosisC14Supplement expectedC14Supplement;
    private DocmosisC15Supplement expectedC15Supplement;
    private DocmosisC16Supplement expectedC16Supplement;
    private DocmosisC17Supplement expectedC17Supplement;
    private DocmosisC18Supplement expectedC18Supplement;
    private DocmosisC20Supplement expectedC20Supplement;

    @BeforeEach
    void setup() {
        expectedCaseSubmission = expectedDocmosisCaseSubmission();
        expectedC14Supplement = expectedDocmosisC14Supplement();
        expectedC15Supplement = expectedDocmosisC15Supplement();
        expectedC16Supplement = expectedDocmosisC16Supplement();
        expectedC17Supplement = expectedDocmosisC17Supplement();
        expectedC18Supplement = expectedDocmosisC18Supplement();
        expectedC20Supplement = expectedDocmosisC20Supplement();
        given(templateDataGenerationService.getTemplateData(any())).willReturn(expectedCaseSubmission);
        given(templateDataGenerationService.getC14SupplementData(any(), anyBoolean()))
            .willReturn(expectedC14Supplement);
        given(templateDataGenerationService.getC15SupplementData(any(), anyBoolean()))
            .willReturn(expectedC15Supplement);
        given(templateDataGenerationService.getC16SupplementData(any(), anyBoolean()))
            .willReturn(expectedC16Supplement);
        given(templateDataGenerationService.getC17SupplementData(any(), anyBoolean()))
            .willReturn(expectedC17Supplement);
        given(templateDataGenerationService.getC18SupplementData(any(), anyBoolean()))
            .willReturn(expectedC18Supplement);
        given(templateDataGenerationService.getC20SupplementData(any(), anyBoolean()))
            .willReturn(expectedC20Supplement);

        given(documentGeneratorService
            .generateDocmosisDocument(any(DocmosisData.class), any(), any(), any()))
            .willReturn(new DocmosisDocument("case_submission_c110a.pdf", PDF));

        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document());

        givenCaseData = populatedCaseData();
    }

    @Test
    void shouldGenerateCaseSubmissionDocumentSuccessfullyDefault() {
        caseSubmissionService.generateC110aSubmittedFormPDF(givenCaseData, false);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionDataCaptor.capture(),
            eq(C110A),
            eq(RenderFormat.PDF),
            eq(Language.ENGLISH));

        DocmosisCaseSubmission caseSubmission = caseSubmissionDataCaptor.getValue();
        assertThat(caseSubmission).isEqualTo(expectedCaseSubmission);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }

    @Test
    void shouldGenerateCaseSubmissionDocumentSuccessfullyIfWelsh() {
        caseSubmissionService.generateC110aSubmittedFormPDF(givenCaseData.toBuilder()
            .c110A(uk.gov.hmcts.reform.fpl.model.group.C110A.builder()
                .languageRequirementApplication(Language.WELSH)
                .build())
            .build(), false);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionDataCaptor.capture(),
            eq(C110A),
            eq(RenderFormat.PDF),
            eq(Language.WELSH));

        DocmosisCaseSubmission caseSubmission = caseSubmissionDataCaptor.getValue();
        assertThat(caseSubmission).isEqualTo(expectedCaseSubmission);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }

    @Test
    void shouldGenerateSupplementSuccessfullyIfC1Application() {
        caseSubmissionService.generateSupplementPDF(givenCaseData, false, C16_SUPPLEMENT, expectedC16Supplement);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionSupplementDataCaptor.capture(),
            eq(C16_SUPPLEMENT),
            eq(RenderFormat.PDF),
            eq(Language.ENGLISH));

        DocmosisData c16Supplement = caseSubmissionSupplementDataCaptor.getValue();
        assertThat(c16Supplement).isEqualTo(expectedC16Supplement);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }

    @Test
    void shouldGetSigneeName() {
        final CaseData caseData = CaseData.builder().build();

        given(templateDataGenerationService.getSigneeName(caseData)).willReturn("John Smith");

        final String actualSigneeName = caseSubmissionService.getSigneeName(caseData);

        assertThat(actualSigneeName).isEqualTo("John Smith");
    }

    @Test
    void shouldGenerateC14SupplementSuccessfully() {
        CaseData caseData = givenCaseData.toBuilder()
            .orders(givenCaseData.getOrders().toBuilder()
                .orderType(List.of(OrderType.REFUSE_CONTACT_WITH_CHILD))
                .build())
            .build();
        caseSubmissionService.generateC1SupplementPDF(caseData, false);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionSupplementDataCaptor.capture(),
            eq(C14_SUPPLEMENT),
            eq(RenderFormat.PDF),
            eq(Language.ENGLISH));

        DocmosisData c16Supplement = caseSubmissionSupplementDataCaptor.getValue();
        assertThat(c16Supplement).isEqualTo(expectedC14Supplement);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }

    @Test
    void shouldGenerateC15SupplementSuccessfully() {
        CaseData caseData = givenCaseData.toBuilder()
            .orders(givenCaseData.getOrders().toBuilder()
                .orderType(List.of(OrderType.CONTACT_WITH_CHILD_IN_CARE))
                .build())
            .build();
        caseSubmissionService.generateC1SupplementPDF(caseData, false);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionSupplementDataCaptor.capture(),
            eq(C15_SUPPLEMENT),
            eq(RenderFormat.PDF),
            eq(Language.ENGLISH));

        DocmosisData c15Supplement = caseSubmissionSupplementDataCaptor.getValue();
        assertThat(c15Supplement).isEqualTo(expectedC15Supplement);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }

    @Test
    void shouldGenerateC16SupplementSuccessfully() {
        CaseData caseData = givenCaseData.toBuilder()
            .orders(givenCaseData.getOrders().toBuilder()
                .orderType(List.of(OrderType.CHILD_ASSESSMENT_ORDER))
                .build())
            .build();
        caseSubmissionService.generateC1SupplementPDF(caseData, false);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionSupplementDataCaptor.capture(),
            eq(C16_SUPPLEMENT),
            eq(RenderFormat.PDF),
            eq(Language.ENGLISH));

        DocmosisData c16Supplement = caseSubmissionSupplementDataCaptor.getValue();
        assertThat(c16Supplement).isEqualTo(expectedC16Supplement);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }

    @Test
    void shouldGenerateC17SupplementSuccessfully() {
        CaseData caseData = givenCaseData.toBuilder()
            .orders(givenCaseData.getOrders().toBuilder()
                .orderType(List.of(OrderType.EDUCATION_SUPERVISION_ORDER))
                .build())
            .build();
        caseSubmissionService.generateC1SupplementPDF(caseData, false);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionSupplementDataCaptor.capture(),
            eq(C17_SUPPLEMENT),
            eq(RenderFormat.PDF),
            eq(Language.ENGLISH));

        DocmosisData c17Supplement = caseSubmissionSupplementDataCaptor.getValue();
        assertThat(c17Supplement).isEqualTo(expectedC17Supplement);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }

    @Test
    void shouldGenerateC18SupplementSuccessfully() {
        CaseData caseData = givenCaseData.toBuilder()
            .orders(givenCaseData.getOrders().toBuilder()
                .orderType(List.of(OrderType.CHILD_RECOVERY_ORDER))
                .build())
            .build();
        caseSubmissionService.generateC1SupplementPDF(caseData, false);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionSupplementDataCaptor.capture(),
            eq(C18_SUPPLEMENT),
            eq(RenderFormat.PDF),
            eq(Language.ENGLISH));

        DocmosisData c18Supplement = caseSubmissionSupplementDataCaptor.getValue();
        assertThat(c18Supplement).isEqualTo(expectedC18Supplement);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }

    @Test
    void shouldGenerateC20SupplementSuccessfully() {
        CaseData caseData = givenCaseData.toBuilder()
            .orders(givenCaseData.getOrders().toBuilder()
                .orderType(List.of(OrderType.SECURE_ACCOMMODATION_ORDER))
                .build())
            .build();
        caseSubmissionService.generateC1SupplementPDF(caseData, false);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionSupplementDataCaptor.capture(),
            eq(C20_SUPPLEMENT),
            eq(RenderFormat.PDF),
            eq(Language.ENGLISH));

        DocmosisData c20Supplement = caseSubmissionSupplementDataCaptor.getValue();
        assertThat(c20Supplement).isEqualTo(expectedC20Supplement);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }


    @Test
    void shouldReturnGeneratedCaseName() {
        final LocalAuthority localAuthority = LocalAuthority.builder()
            .name("Local authority 1")
            .build();

        final Respondent respondent1 = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Jim")
                .lastName("Test")
                .build())
            .build();

        final Respondent respondent2 = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Marina")
                .lastName("Test")
                .build())
            .build();

        final Respondent respondent3 = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Fred")
                .lastName("Smith")
                .build())
            .build();

        CaseData caseData = givenCaseData.toBuilder()
            .respondents1(wrapElements(respondent1, respondent2, respondent3))
            .localAuthorities(wrapElements(localAuthority))
            .caseName("Draft case name")
            .build();

        assertThat(caseSubmissionService.generateCaseName(caseData))
            .isEqualTo("Local authority 1 & Test, Smith");
    }
}
