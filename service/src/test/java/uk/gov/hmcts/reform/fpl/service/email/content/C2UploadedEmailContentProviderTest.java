package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForC2;
import uk.gov.hmcts.reform.fpl.model.notify.c2uploaded.C2UploadedTemplate;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.C2;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {C2UploadedEmailContentProvider.class, FixedTimeConfiguration.class})
class C2UploadedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    private static final byte[] C2_DOCUMENT_BINARY = testDocumentBinaries();
    private DocumentReference uploadedC2 = testDocumentReference();

    private static final LocalDateTime HEARING_DATE = LocalDateTime.now().plusMonths(3);

    private static final String HEARING_CALLOUT = "hearing " + HEARING_DATE
        .toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(Locale.UK));

    @BeforeEach
    void init() {
        when(documentDownloadService.downloadDocument(uploadedC2.getBinaryUrl()))
            .thenReturn(C2_DOCUMENT_BINARY);
    }

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetails() {
        DocumentReference uploadedC2 = DocumentReference.builder()
            .filename(randomAlphanumeric(10))
            .url(randomAlphanumeric(10))
            .binaryUrl("http://dm-store:8080/documents/b28f859b-7521-4c84-9057-47e56afd773f/binary")
            .build();

        CaseData caseData = buildCaseData();

        C2UploadedTemplate expectedParameters = getC2UploadedTemplateParameters();
        C2UploadedTemplate actualParameters = c2UploadedEmailContentProvider
            .getNotifyData(caseData, uploadedC2);

        assertThat(actualParameters).usingRecursiveComparison().isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedPbaPaymentNotTakenNotification() {
        CaseData caseData = buildCaseData();

        BaseCaseNotifyData expectedParameters = BaseCaseNotifyData.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, C2))
            .build();

        BaseCaseNotifyData actualParameters = c2UploadedEmailContentProvider
            .getPbaPaymentNotTakenNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedMapWithAllocatedJudgeDetails() {
        CaseData caseData = buildCaseData();

        AllocatedJudgeTemplateForC2 expectedData = getAllocatedJudgeParametersForC2();
        AllocatedJudgeTemplateForC2 actualData = c2UploadedEmailContentProvider
            .getNotifyDataForAllocatedJudge(caseData);

        assertThat(actualData).usingRecursiveComparison().isEqualTo(expectedData);
    }

    private AllocatedJudgeTemplateForC2 getAllocatedJudgeParametersForC2() {
        return AllocatedJudgeTemplateForC2.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, C2))
            .callout("^Smith, 12345, " + HEARING_CALLOUT)
            .judgeTitle("Her Honour Judge")
            .judgeName("Moley")
            .respondentLastName("Smith")
            .build();
    }

    private C2UploadedTemplate getC2UploadedTemplateParameters() {
        return C2UploadedTemplate.builder()
            .callout("Smith, 12345, " + HEARING_CALLOUT)
            .respondentLastName("Smith")
            .caseUrl(caseUrl(CASE_REFERENCE, C2))
            .documentUrl("http://fake-url/documents/b28f859b-7521-4c84-9057-47e56afd773f/binary")
            .build();
    }

    private CaseData buildCaseData() {
        return CaseData.builder()
            .id(12345L)
            .familyManCaseNumber(CASE_REFERENCE)
            .respondents1(wrapElements(Respondent.builder().party(RespondentParty.builder()
                .firstName("John")
                .lastName("Smith")
                .build()).build()))
            .hearingDetails(wrapElements(HearingBooking.builder().startDate((HEARING_DATE)).build()))
            .allocatedJudge(Judge.builder().judgeTitle(HER_HONOUR_JUDGE).judgeLastName("Moley").build())
            .build();
    }
}
