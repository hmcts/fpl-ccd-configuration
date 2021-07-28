package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded.AdditionalApplicationsUploadedTemplate;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C20_SECURE_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.OTHER_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {AdditionalApplicationsUploadedEmailContentProvider.class})
class AdditionalApplicationsUploadedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final LocalDateTime PAST_HEARING_DATE = LocalDateTime.of(2000, 2, 12, 0, 0, 0);
    private static final LocalDateTime FUTURE_HEARING_DATE = LocalDateTime.of(2099, 2, 12, 0, 0, 0);
    private static final String HEARING_CALLOUT = "hearing 12 Feb 2099";

    @MockBean
    private Time time;
    @MockBean
    private EmailNotificationHelper helper;
    @Autowired
    private AdditionalApplicationsUploadedEmailContentProvider underTest;

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetails() {
        CaseData caseData = buildCaseData();

        when(helper.getSubjectLineLastName(caseData)).thenReturn("Davies");
        when(time.now()).thenReturn(FUTURE_HEARING_DATE.minusDays(1));

        AdditionalApplicationsUploadedTemplate expectedParameters = AdditionalApplicationsUploadedTemplate.builder()
            .callout("Smith, 12345, " + HEARING_CALLOUT)
            .lastName("Davies")
            .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
            .applicationTypes(Arrays.asList("C2 (With notice) - Appointment of a guardian",
                "C13A - Special guardianship order",
                "C20 - Secure accommodation (England)",
                "C1 - Parental responsibility by the father",
                "C13A - Special guardianship order",
                "C20 - Secure accommodation (England)"))
            .build();

        AdditionalApplicationsUploadedTemplate actualParameters = underTest.getNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetailsWhenRequestingC2WithParentalResponsibility() {
        CaseData caseData = buildCaseData().toBuilder()
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder().c2DocumentBundle(
                C2DocumentBundle.builder()
                    .type(C2ApplicationType.WITH_NOTICE)
                    .c2AdditionalOrdersRequested(List.of(C2AdditionalOrdersRequested.PARENTAL_RESPONSIBILITY))
                    .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_FATHER)
                    .supplementsBundle(List.of())
                    .build())
                .build()))
            .build();

        when(helper.getSubjectLineLastName(caseData)).thenReturn("Davies");
        when(time.now()).thenReturn(FUTURE_HEARING_DATE.minusDays(1));

        AdditionalApplicationsUploadedTemplate expectedParameters =
            AdditionalApplicationsUploadedTemplate.builder()
                .callout("Smith, 12345, " + HEARING_CALLOUT)
                .lastName("Davies")
                .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
                .applicationTypes(List.of("C2 (With notice) - Parental responsibility by the father"))
                .build();

        AdditionalApplicationsUploadedTemplate actualParameters = underTest.getNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedPbaPaymentNotTakenNotification() {
        CaseData caseData = buildCaseData();

        BaseCaseNotifyData expectedParameters = BaseCaseNotifyData.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
            .build();

        BaseCaseNotifyData actualParameters = underTest.getPbaPaymentNotTakenNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
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
            .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_FATHER)
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
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("John").lastName("Smith").build())
                .build()))
            .hearingDetails(wrapElements(
                    HearingBooking.builder().startDate((PAST_HEARING_DATE)).build(),
                    HearingBooking.builder().startDate((FUTURE_HEARING_DATE)).build()))
            .additionalApplicationsBundle(wrapElements(additionalApplicationsBundle))
            .build();
    }
}
