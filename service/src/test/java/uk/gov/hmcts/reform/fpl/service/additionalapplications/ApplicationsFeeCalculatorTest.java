package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.SupplementsBundle;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C12_WARRANT_TO_ASSIST_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_PARENTAL_RESPONSIBILITY;
import static uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType.PR_BY_FATHER;
import static uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType.SECTION_25_ENGLAND;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C15_CONTACT_WITH_CHILD_IN_CARE;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C16_CHILD_ASSESSMENT;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C18_RECOVERY_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C20_SECURE_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class ApplicationsFeeCalculatorTest {

    @Mock
    private FeeService feeService;

    @InjectMocks
    private ApplicationsFeeCalculator feeCalculator;

    @Test
    void shouldGetC2ApplicationFeeForC2WithNotice() {
        CaseData data = CaseData.builder()
            .c2ApplicationType(Map.of("type", WITH_NOTICE)).build();
        Map<String, Object> caseDetailsMap = new HashMap<>();

        when(feeService.getFeesDataForC2(WITH_NOTICE))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.TEN).build());

        feeCalculator.getC2ApplicationFee(caseDetailsMap, data);

        Map<String, Object> expectedData = Map.of(
            "amountToPay", "1000",
            "displayAmountToPay", YES.getValue());

        verify(feeService).getFeesDataForC2(WITH_NOTICE);
        assertThat(caseDetailsMap).containsExactlyInAnyOrderEntriesOf(expectedData);
    }

    @Test
    void shouldGetAdditionalApplicationsFee() {
        CaseData data = CaseData.builder()
            .c2ApplicationType(Map.of("type", WITHOUT_NOTICE))
            .temporaryOtherApplicationsBundle(
                OtherApplicationsBundle.builder()
                    .applicationType(C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION)
                    .supplementsBundle(List.of(
                        element(SupplementsBundle.builder().name(C16_CHILD_ASSESSMENT).build()),
                        element(SupplementsBundle.builder().name(C18_RECOVERY_ORDER).build()))).build())
            .build();

        Map<String, Object> caseDetailsMap = new HashMap<>();

        when(feeService.getFeesDataForAdditionalApplications(
            WITHOUT_NOTICE,
            C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION,
            null,
            List.of(C16_CHILD_ASSESSMENT, C18_RECOVERY_ORDER),
            emptyList()))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.valueOf(215)).build());

        feeCalculator.getAdditionalApplicationsFee(caseDetailsMap, data);

        Map<String, Object> expectedData = Map.of(
            "amountToPay", "21500",
            "displayAmountToPay", YES.getValue());

        verify(feeService).getFeesDataForAdditionalApplications(
            WITHOUT_NOTICE, C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION, null,
            List.of(C16_CHILD_ASSESSMENT, C18_RECOVERY_ORDER), emptyList());

        assertThat(caseDetailsMap).containsExactlyInAnyOrderEntriesOf(expectedData);
    }

    @Test
    void shouldGetFeesForParentalResponsibilityTypeWhenOtherApplicationTypeIsParentalResponsibility() {
        CaseData data = CaseData.builder()
            .temporaryOtherApplicationsBundle(
                OtherApplicationsBundle.builder()
                    .applicationType(C1_PARENTAL_RESPONSIBILITY)
                    .parentalResponsibilityType(PR_BY_FATHER)
                    .supplementsBundle(List.of(
                        element(SupplementsBundle.builder().name(C18_RECOVERY_ORDER).build()))).build())
            .build();

        Map<String, Object> caseDetailsMap = new HashMap<>();

        when(feeService.getFeesDataForOtherApplications(C1_PARENTAL_RESPONSIBILITY, PR_BY_FATHER,
            List.of(C18_RECOVERY_ORDER), emptyList()))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.valueOf(155)).build());

        feeCalculator.getOtherApplicationsFee(caseDetailsMap, data);

        Map<String, Object> expectedData = Map.of(
            "amountToPay", "15500",
            "displayAmountToPay", YES.getValue());

        verify(feeService).getFeesDataForOtherApplications(
            C1_PARENTAL_RESPONSIBILITY, PR_BY_FATHER,
            List.of(C18_RECOVERY_ORDER), emptyList());

        assertThat(caseDetailsMap).containsExactlyInAnyOrderEntriesOf(expectedData);
    }

    @Test
    void shouldGetFeesForSecureAccommodationTypesWhenSupplementTypeIsSecureAccommodation() {
        CaseData data = CaseData.builder()
            .c2ApplicationType(Map.of("type", WITHOUT_NOTICE))
            .temporaryOtherApplicationsBundle(
                OtherApplicationsBundle.builder()
                    .applicationType(C12_WARRANT_TO_ASSIST_PERSON)
                    .supplementsBundle(List.of(
                        element(SupplementsBundle.builder()
                            .name(C20_SECURE_ACCOMMODATION)
                            .secureAccommodationType(SECTION_25_ENGLAND).build()))).build()).build();

        Map<String, Object> caseDetailsMap = new HashMap<>();

        when(feeService.getFeesDataForAdditionalApplications(WITHOUT_NOTICE, C12_WARRANT_TO_ASSIST_PERSON, null,
            List.of(), List.of(SECTION_25_ENGLAND)))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.valueOf(100)).build());

        feeCalculator.getAdditionalApplicationsFee(caseDetailsMap, data);

        Map<String, Object> expectedData = Map.of(
            "amountToPay", "10000",
            "displayAmountToPay", YES.getValue());

        verify(feeService).getFeesDataForAdditionalApplications(WITHOUT_NOTICE,
            C12_WARRANT_TO_ASSIST_PERSON, null,
            List.of(), List.of(SECTION_25_ENGLAND));

        assertThat(caseDetailsMap).containsExactlyInAnyOrderEntriesOf(expectedData);
    }

    @Test
    void shouldGetOtherApplicationsFee() {
        CaseData data = CaseData.builder()
            .temporaryOtherApplicationsBundle(
                OtherApplicationsBundle.builder()
                    .applicationType(C12_WARRANT_TO_ASSIST_PERSON)
                    .supplementsBundle(List.of(
                        element(SupplementsBundle.builder()
                            .name(C15_CONTACT_WITH_CHILD_IN_CARE).build())))
                    .build())
            .build();

        Map<String, Object> caseDetailsMap = new HashMap<>();

        when(feeService.getFeesDataForOtherApplications(
            C12_WARRANT_TO_ASSIST_PERSON, null,
            List.of(C15_CONTACT_WITH_CHILD_IN_CARE), emptyList()))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.valueOf(55)).build());

        feeCalculator.getOtherApplicationsFee(caseDetailsMap, data);

        Map<String, Object> expectedData = Map.of(
            "amountToPay", "5500",
            "displayAmountToPay", YES.getValue());

        verify(feeService).getFeesDataForOtherApplications(
            C12_WARRANT_TO_ASSIST_PERSON, null,
            List.of(C15_CONTACT_WITH_CHILD_IN_CARE), emptyList());
        assertThat(caseDetailsMap).containsExactlyInAnyOrderEntriesOf(expectedData);
    }

    @Test
    void shouldIgnoreFeeRegisterExceptionWhenGettingFeeForC2Application() {
        CaseData data = CaseData.builder()
            .c2ApplicationType(Map.of("type", WITHOUT_NOTICE)).build();
        Map<String, Object> caseDetailsMap = new HashMap<>();

        when(feeService.getFeesDataForC2(WITHOUT_NOTICE))
            .thenThrow(new FeeRegisterException(400, "error message", new RuntimeException()));

        feeCalculator.getC2ApplicationFee(caseDetailsMap, data);

        assertThat(caseDetailsMap).containsExactlyEntriesOf(Map.of("displayAmountToPay", "No"));
    }

    @Test
    void shouldIgnoreFeeRegisterExceptionWhenGettingFeeForOtherApplications() {
        CaseData data = CaseData.builder()
            .temporaryOtherApplicationsBundle(
                OtherApplicationsBundle.builder()
                    .applicationType(C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION)
                    .supplementsBundle(List.of()).build())
            .build();

        Map<String, Object> caseDetailsMap = new HashMap<>();

        when(feeService.getFeesDataForOtherApplications(
            C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION, null, emptyList(), emptyList()))
            .thenThrow(new FeeRegisterException(400, "error", new RuntimeException()));

        feeCalculator.getOtherApplicationsFee(caseDetailsMap, data);

        assertThat(caseDetailsMap).containsExactlyEntriesOf(Map.of("displayAmountToPay", "No"));
    }

    @Test
    void shouldIgnoreFeeRegisterExceptionWhenGettingFeeForAdditionalApplications() {
        CaseData data = CaseData.builder()
            .c2ApplicationType(Map.of("type", WITH_NOTICE))
            .temporaryOtherApplicationsBundle(
                OtherApplicationsBundle.builder()
                    .applicationType(C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION)
                    .supplementsBundle(List.of()).build())
            .build();

        Map<String, Object> caseDetailsMap = new HashMap<>();

        when(feeService.getFeesDataForAdditionalApplications(
            WITH_NOTICE, C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION, null, emptyList(), emptyList()))
            .thenThrow(new FeeRegisterException(404, "message", new RuntimeException()));

        feeCalculator.getAdditionalApplicationsFee(caseDetailsMap, data);

        assertThat(caseDetailsMap).containsExactlyEntriesOf(Map.of("displayAmountToPay", "No"));
    }

    //TODO: add tests
}
