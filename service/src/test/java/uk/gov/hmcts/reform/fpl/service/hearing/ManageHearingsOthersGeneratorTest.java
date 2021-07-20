package uk.gov.hmcts.reform.fpl.service.hearing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.OthersService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
class ManageHearingsOthersGeneratorTest {
    private static final String OTHER_LABEL = "Other label";

    @Mock
    private OthersService othersService;

    @Mock
    private FeatureToggleService toggleService;

    @InjectMocks
    private ManageHearingsOthersGenerator underTest;

    @Test
    void shouldGenerateFieldsWhenOthersInCaseAndHearingBooking() {
        Other other = Other.builder().build();

        CaseData caseData = CaseData.builder().others(Others.builder().firstOther(other).build()).build();
        HearingBooking hearingBooking = HearingBooking.builder().others(wrapElements(other)).build();

        when(othersService.getOthersLabel(any())).thenReturn(OTHER_LABEL);
        when(toggleService.isServeOrdersAndDocsToOthersEnabled()).thenReturn(true);

        Map<String, Object> generatedData = underTest.generate(caseData, hearingBooking);
        Map<String, Object> expectedData = Map.of(
            "hasOthers", YES.getValue(),
            "othersSelector", Selector.builder().selected(List.of(0)).build().setNumberOfOptions(1),
            "others_label", OTHER_LABEL,
            "sendOrderToAllOthers", YES.getValue()
        );

        assertThat(generatedData).isEqualTo(expectedData);
    }

    @Test
    void shouldGenerateFieldsWhenOthersInCaseAndNotHearingBooking() {
        Other other = Other.builder().build();

        CaseData caseData = CaseData.builder().others(Others.builder().firstOther(other).build()).build();
        HearingBooking hearingBooking = HearingBooking.builder().build();

        when(othersService.getOthersLabel(any())).thenReturn(OTHER_LABEL);
        when(toggleService.isServeOrdersAndDocsToOthersEnabled()).thenReturn(true);

        Map<String, Object> generatedData = underTest.generate(caseData, hearingBooking);
        Map<String, Object> expectedData = Map.of(
            "hasOthers", YES.getValue(),
            "othersSelector", Selector.builder().build().setNumberOfOptions(1),
            "others_label", OTHER_LABEL,
            "sendOrderToAllOthers", NO.getValue()
        );

        assertThat(generatedData).isEqualTo(expectedData);
    }

    @Test
    void shouldGenerateFieldsWhenOthersIsEmptyInHearingBooking() {
        Other other = Other.builder().build();

        CaseData caseData = CaseData.builder().others(Others.builder().firstOther(other).build()).build();
        HearingBooking hearingBooking = HearingBooking.builder().others(Collections.emptyList()).build();

        when(othersService.getOthersLabel(any())).thenReturn(OTHER_LABEL);
        when(toggleService.isServeOrdersAndDocsToOthersEnabled()).thenReturn(true);

        Map<String, Object> generatedData = underTest.generate(caseData, hearingBooking);
        Map<String, Object> expectedData = Map.of(
            "hasOthers", YES.getValue(),
            "othersSelector", Selector.builder().build().setNumberOfOptions(1),
            "others_label", OTHER_LABEL,
            "sendOrderToAllOthers", NO.getValue()
        );

        assertThat(generatedData).isEqualTo(expectedData);
    }

    @Test
    void shouldNotGenerateFieldsWhenNoOthersInCase() {
        CaseData caseData = CaseData.builder().build();
        HearingBooking hearingBooking = HearingBooking.builder().build();

        when(toggleService.isServeOrdersAndDocsToOthersEnabled()).thenReturn(true);

        Map<String, Object> generatedData = underTest.generate(caseData, hearingBooking);

        assertThat(generatedData).isEmpty();
    }

    @Test
    void shouldNotGenerateFieldsWhenToggledOff() {
        CaseData caseData = CaseData.builder().build();
        HearingBooking hearingBooking = HearingBooking.builder().build();

        when(toggleService.isServeOrdersAndDocsToOthersEnabled()).thenReturn(false);

        Map<String, Object> generatedData = underTest.generate(caseData, hearingBooking);

        assertThat(generatedData).isEmpty();
    }
}
