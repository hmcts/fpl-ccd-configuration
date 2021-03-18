package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded.AdditionalApplicationsUploadedTemplate;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C20_SECURE_ACCOMMODATION;
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
            .callout("Smith, 12345, " + HEARING_CALLOUT)
            .respondentLastName("Smith")
            .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
            .documentUrl("http://fake-url/documents/b28f859b-7521-4c84-9057-47e56afd773f/binary")
            .applicationTypes(Arrays.asList("C2 (With notice) - Appointment of a guardian",
                "C13A - Special guardianship order",
                "C20 - Secure accommodation (England)",
                "C1 - Parental responsibility by the father",
                "C13A - Special guardianship order",
                "C20 - Secure accommodation (England)"))
            .build();
    }

    private CaseData buildCaseData() {
        List<Supplement> supplements = Arrays.asList(
            Supplement.builder().name(C13A_SPECIAL_GUARDIANSHIP).build(),
            Supplement.builder()
                .name(C20_SECURE_ACCOMMODATION)
                .secureAccommodationType(SecureAccommodationType.ENGLAND)
                .build()
        );

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .type(C2ApplicationType.WITH_NOTICE)
            .supplementsBundle(wrapElements(supplements))
            .c2AdditionalOrdersRequested(Collections.singletonList(C2AdditionalOrdersRequested.APPOINTMENT_OF_GUARDIAN))
            .build();

        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .applicationType(OtherApplicationType.C1_PARENTAL_RESPONSIBILITY)
            .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_FATHER)
            .supplementsBundle(wrapElements(supplements))
            .build();

        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(c2DocumentBundle)
            .otherApplicationsBundle(otherApplicationsBundle)
            .build();

        return CaseData.builder()
            .id(12345L)
            .familyManCaseNumber(CASE_REFERENCE)
            .respondents1(wrapElements(Respondent.builder().party(RespondentParty.builder()
                .firstName("John")
                .lastName("Smith")
                .build()).build()))
            .hearingDetails(wrapElements(HearingBooking.builder().startDate((HEARING_DATE)).build()))
            .additionalApplicationsBundle(wrapElements(additionalApplicationsBundle))
            .build();
    }
}
