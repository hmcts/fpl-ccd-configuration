package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SupplementsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded.AdditionalApplicationsUploadedTemplate;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.OTHER_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {AdditionalApplicationsUploadedEmailContentProvider.class,
    FixedTimeConfiguration.class})
class AdditionalApplicationsUploadedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private AdditionalApplicationsUploadedEmailContentProvider additionalApplicationsUploadedEmailContentProvider;

    private static final byte[] C2_DOCUMENT_BINARY = testDocumentBinaries();
    private final DocumentReference uploadedC2 = testDocumentReference();

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

        AdditionalApplicationsUploadedTemplate expectedParameters =
            getAdditionalApplicationsUploadedTemplateParameters();
        AdditionalApplicationsUploadedTemplate actualParameters = additionalApplicationsUploadedEmailContentProvider
            .getNotifyData(caseData, uploadedC2);

        assertThat(actualParameters).usingRecursiveComparison().isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedPbaPaymentNotTakenNotification() {
        CaseData caseData = buildCaseData();

        BaseCaseNotifyData expectedParameters = BaseCaseNotifyData.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
            .build();

        BaseCaseNotifyData actualParameters = additionalApplicationsUploadedEmailContentProvider
            .getPbaPaymentNotTakenNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    private AdditionalApplicationsUploadedTemplate getAdditionalApplicationsUploadedTemplateParameters() {
        return AdditionalApplicationsUploadedTemplate.builder()
            .callout("^Smith, 12345, " + HEARING_CALLOUT)
            .respondentLastName("Smith")
            .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
            .documentUrl("http://fake-url/documents/b28f859b-7521-4c84-9057-47e56afd773f/binary")
            .applicationTypes(Arrays.asList("C2", "C13A - Special guardianship order"))
            .build();
    }

    private CaseData buildCaseData() {
        SupplementsBundle supplementsBundle = SupplementsBundle.builder().name(C13A_SPECIAL_GUARDIANSHIP).build();

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .supplementsBundle(wrapElements(Collections.singletonList(
                supplementsBundle)))
            .build();

        return CaseData.builder()
            .id(12345L)
            .familyManCaseNumber(CASE_REFERENCE)
            .respondents1(wrapElements(Respondent.builder().party(RespondentParty.builder()
                .firstName("John")
                .lastName("Smith")
                .build()).build()))
            .hearingDetails(wrapElements(HearingBooking.builder().startDate((HEARING_DATE)).build()))
            .c2DocumentBundle(wrapElements(c2DocumentBundle))
            .build();
    }
}
