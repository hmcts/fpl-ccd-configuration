package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.SupplementsBundle;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C12_WARRANT_TO_ASSIST_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C15_CONTACT_WITH_CHILD_IN_CARE;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C16_CHILD_ASSESSMENT;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C18_RECOVERY_ORDER;
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
        assertThat(caseDetailsMap).containsAllEntriesOf(expectedData);
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
            WITHOUT_NOTICE, C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION,
            List.of(C16_CHILD_ASSESSMENT, C18_RECOVERY_ORDER)))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.valueOf(215)).build());

        feeCalculator.getAdditionalApplicationsFee(caseDetailsMap, data);

        Map<String, Object> expectedData = Map.of(
            "amountToPay", "21500",
            "displayAmountToPay", YES.getValue());

        verify(feeService).getFeesDataForAdditionalApplications(
            WITHOUT_NOTICE, C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION,
            List.of(C16_CHILD_ASSESSMENT, C18_RECOVERY_ORDER));

        assertThat(caseDetailsMap).containsAllEntriesOf(expectedData);
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
            C12_WARRANT_TO_ASSIST_PERSON, List.of(C15_CONTACT_WITH_CHILD_IN_CARE)))
            .thenReturn(FeesData.builder().totalAmount(BigDecimal.valueOf(55)).build());

        feeCalculator.getOtherApplicationsFee(caseDetailsMap, data);

        Map<String, Object> expectedData = Map.of(
            "amountToPay", "5500",
            "displayAmountToPay", YES.getValue());

        verify(feeService).getFeesDataForOtherApplications(
            C12_WARRANT_TO_ASSIST_PERSON, List.of(C15_CONTACT_WITH_CHILD_IN_CARE));
        assertThat(caseDetailsMap).containsAllEntriesOf(expectedData);
    }

    //TODO: add tests
}
