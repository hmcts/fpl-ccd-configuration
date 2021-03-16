package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.C2_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.OTHER_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested.APPOINTMENT_OF_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested.CHANGE_SURNAME_OR_REMOVE_JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType.WALES;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C16_CHILD_ASSESSMENT;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C20_SECURE_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class ApplicationsFeeCalculatorTest {

    @Mock
    private FeeService feeService;

    @InjectMocks
    private ApplicationsFeeCalculator feeCalculator;

    @Test
    void shouldCalculateFeeForC2DocumentBundle() {
        C2DocumentBundle c2Document = buildC2Document();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(C2_ORDER))
            .temporaryC2Document(c2Document).build();

        when(feeService.getFeesDataForAdditionalApplications(
            c2Document, null, List.of(C16_CHILD_ASSESSMENT), List.of()))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.TEN).build());

        Map<String, Object> actualData = feeCalculator.calculateFee(caseData);

        Map<String, Object> expectedData = Map.of(
            "amountToPay", "1000",
            "displayAmountToPay", YES.getValue());

        verify(feeService).getFeesDataForAdditionalApplications(
            c2Document, null, List.of(C16_CHILD_ASSESSMENT), List.of());

        assertThat(actualData).containsExactlyInAnyOrderEntriesOf(expectedData);
    }

    @Test
    void shouldNotCalculateFeeWhenC2AndOtherApplicationsAreSelectedAndOnlyC2DocumentBundleExists() {
        C2DocumentBundle c2Document = buildC2Document();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(C2_ORDER, OTHER_ORDER))
            .temporaryC2Document(c2Document).build();

        Map<String, Object> actualData = feeCalculator.calculateFee(caseData);

        verifyNoInteractions(feeService);
        assertThat(actualData).isEmpty();
    }

    @Test
    void shouldNotCalculateFeeWhenC2AndOtherApplicationsAreSelectedAndOtherApplicationsBundleDocumentIsNull() {
        C2DocumentBundle c2Document = C2DocumentBundle.builder().type(WITH_NOTICE)
            .c2AdditionalOrdersRequested(List.of(APPOINTMENT_OF_GUARDIAN, CHANGE_SURNAME_OR_REMOVE_JURISDICTION))
            .build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(C2_ORDER, OTHER_ORDER))
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .temporaryC2Document(c2Document).build();

        Map<String, Object> actualData = feeCalculator.calculateFee(caseData);

        verifyNoInteractions(feeService);
        assertThat(actualData).isEmpty();
    }

    @Test
    void shouldCalculateFeeForOtherDocumentBundle() {
        OtherApplicationsBundle otherApplicationsBundle = buildOtherApplicationsBundle();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(OTHER_ORDER))
            .temporaryOtherApplicationsBundle(otherApplicationsBundle).build();

        when(feeService.getFeesDataForAdditionalApplications(
            null, otherApplicationsBundle, List.of(C13A_SPECIAL_GUARDIANSHIP), List.of()))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.valueOf(20)).build());

        Map<String, Object> actualData = feeCalculator.calculateFee(caseData);

        Map<String, Object> expectedData = Map.of(
            "amountToPay", "2000",
            "displayAmountToPay", YES.getValue());

        verify(feeService).getFeesDataForAdditionalApplications(
            null, otherApplicationsBundle, List.of(C13A_SPECIAL_GUARDIANSHIP), List.of());

        assertThat(actualData).containsExactlyInAnyOrderEntriesOf(expectedData);
    }

    @Test
    void shouldCalculateFeeForC2DocumentBundleAndOtherApplicationsBundle() {
        C2DocumentBundle c2Document = buildC2Document();

        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .applicationType(OtherApplicationType.C1_PARENTAL_RESPONSIBILITY)
            .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_FATHER)
            .document(DocumentReference.builder().build())
            .supplementsBundle(List.of(
                element(Supplement.builder().name(C13A_SPECIAL_GUARDIANSHIP).build()),
                element(Supplement.builder().name(C20_SECURE_ACCOMMODATION)
                    .secureAccommodationType(WALES).build())))
            .build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(C2_ORDER, OTHER_ORDER))
            .temporaryC2Document(c2Document)
            .temporaryOtherApplicationsBundle(otherApplicationsBundle).build();

        when(feeService.getFeesDataForAdditionalApplications(c2Document, otherApplicationsBundle,
            List.of(C16_CHILD_ASSESSMENT, C13A_SPECIAL_GUARDIANSHIP), List.of(WALES)))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.valueOf(50)).build());

        Map<String, Object> actualData = feeCalculator.calculateFee(caseData);

        Map<String, Object> expectedData = Map.of(
            "amountToPay", "5000",
            "displayAmountToPay", YES.getValue());

        verify(feeService).getFeesDataForAdditionalApplications(c2Document, otherApplicationsBundle,
            List.of(C16_CHILD_ASSESSMENT, C13A_SPECIAL_GUARDIANSHIP), List.of(WALES));

        assertThat(actualData).containsExactlyInAnyOrderEntriesOf(expectedData);
    }

    @Test
    void shouldIgnoreFeeRegisterExceptionWhenGettingFeeForAdditionalApplications() {
        OtherApplicationsBundle applicationsBundle = OtherApplicationsBundle.builder()
            .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN).build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(OTHER_ORDER))
            .temporaryOtherApplicationsBundle(applicationsBundle).build();

        when(feeService.getFeesDataForAdditionalApplications(
            null, applicationsBundle, emptyList(), emptyList()))
            .thenThrow(new FeeRegisterException(404, "message", new RuntimeException()));

        Map<String, Object> actualData = feeCalculator.calculateFee(caseData);

        assertThat(actualData).containsExactlyEntriesOf(Map.of("displayAmountToPay", "No"));
    }

    @Test
    void shouldCalculateFeeForAdditionalApplicationsBundleWithC2Document() {
        C2DocumentBundle c2Document = buildC2Document();

        AdditionalApplicationsBundle bundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(c2Document)
            .build();

        when(feeService.getFeesDataForAdditionalApplications(
            c2Document, null, List.of(C16_CHILD_ASSESSMENT), List.of()))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.TEN).build());

        FeesData actualFeesData = feeCalculator.getFeeDataForAdditionalApplications(bundle);

        verify(feeService).getFeesDataForAdditionalApplications(
            c2Document, null, List.of(C16_CHILD_ASSESSMENT), List.of());

        assertThat(actualFeesData).isEqualTo(FeesData.builder().totalAmount(BigDecimal.TEN).build());
    }

    @Test
    void shouldCalculateFeeForAdditionalApplicationsBundleWithC2DocumentAndOtherApplications() {
        C2DocumentBundle c2Document = buildC2Document();
        OtherApplicationsBundle otherApplicationsBundle = buildOtherApplicationsBundle();

        AdditionalApplicationsBundle bundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(c2Document)
            .otherApplicationsBundle(otherApplicationsBundle)
            .build();

        when(feeService.getFeesDataForAdditionalApplications(
            c2Document, otherApplicationsBundle,
            List.of(C16_CHILD_ASSESSMENT, C13A_SPECIAL_GUARDIANSHIP), List.of()))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.TEN).build());

        FeesData actualFeesData = feeCalculator.getFeeDataForAdditionalApplications(bundle);

        verify(feeService).getFeesDataForAdditionalApplications(
            c2Document, otherApplicationsBundle, List.of(C16_CHILD_ASSESSMENT, C13A_SPECIAL_GUARDIANSHIP), List.of());

        assertThat(actualFeesData).isEqualTo(FeesData.builder().totalAmount(BigDecimal.TEN).build());
    }

    @Test
    void shouldCalculateFeeForAdditionalApplicationsBundleWithOtherApplications() {
        OtherApplicationsBundle otherApplicationsBundle = buildOtherApplicationsBundle();

        AdditionalApplicationsBundle bundle = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(otherApplicationsBundle)
            .build();

        when(feeService.getFeesDataForAdditionalApplications(
            null, otherApplicationsBundle, List.of(C13A_SPECIAL_GUARDIANSHIP), List.of()))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.valueOf(20)).build());

        FeesData actualFeesData = feeCalculator.getFeeDataForAdditionalApplications(bundle);

        verify(feeService).getFeesDataForAdditionalApplications(
            null, otherApplicationsBundle, List.of(C13A_SPECIAL_GUARDIANSHIP), List.of());

        assertThat(actualFeesData).isEqualTo(FeesData.builder().totalAmount(BigDecimal.valueOf(20)).build());
    }

    private C2DocumentBundle buildC2Document() {
        return C2DocumentBundle.builder().type(WITH_NOTICE)
            .c2AdditionalOrdersRequested(List.of(APPOINTMENT_OF_GUARDIAN, CHANGE_SURNAME_OR_REMOVE_JURISDICTION))
            .supplementsBundle(List.of(element(Supplement.builder().name(C16_CHILD_ASSESSMENT).build())))
            .build();
    }

    private OtherApplicationsBundle buildOtherApplicationsBundle() {
        return OtherApplicationsBundle.builder()
            .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN)
            .supplementsBundle(List.of(element(Supplement.builder().name(C13A_SPECIAL_GUARDIANSHIP).build())))
            .build();
    }
}
