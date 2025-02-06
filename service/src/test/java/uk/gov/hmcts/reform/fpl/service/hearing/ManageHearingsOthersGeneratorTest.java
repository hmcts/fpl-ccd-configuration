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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
class ManageHearingsOthersGeneratorTest {
    private static final Other OTHER = Other.builder().name("John").build();
    private static final String OTHER_LABEL = "Other label";
    private static final Selector OTHER_SELECTOR = Selector.builder().build();

    @Mock
    private OthersService othersService;

    @Mock
    private FeatureToggleService toggleService;

    @InjectMocks
    private ManageHearingsOthersGenerator underTest;

    @Test
    void shouldGenerateFieldsWhenOthersInCaseAndHearingBooking() {
        CaseData caseData = CaseData.builder().others(Others.builder().firstOther(OTHER).build()).build();
        HearingBooking hearingBooking = HearingBooking.builder().others(wrapElements(OTHER)).build();

        when(othersService.buildOtherSelector(unwrapElements(caseData.getOthersV2()),
            unwrapElements(hearingBooking.getOthers()))).thenReturn(OTHER_SELECTOR);
        when(othersService.getOthersLabel(any())).thenReturn(OTHER_LABEL);

        Map<String, Object> generatedData = underTest.generate(caseData, hearingBooking);
        Map<String, Object> expectedData = Map.of(
            "hasOthers", YES.getValue(),
            "othersSelector", OTHER_SELECTOR,
            "others_label", OTHER_LABEL,
            "sendNoticeOfHearing", YES.getValue(),
            "sendOrderToAllOthers", YES.getValue()
        );

        assertThat(generatedData).isEqualTo(expectedData);
    }

    @Test
    void shouldGenerateFieldsWhenOthersInCaseAndNotHearingBooking() {
        CaseData caseData = CaseData.builder().others(Others.builder().firstOther(OTHER).build()).build();
        HearingBooking hearingBooking = HearingBooking.builder().build();

        when(othersService.buildOtherSelector(unwrapElements(caseData.getOthersV2()),
            unwrapElements(hearingBooking.getOthers()))).thenReturn(OTHER_SELECTOR);
        when(othersService.getOthersLabel(any())).thenReturn(OTHER_LABEL);

        Map<String, Object> generatedData = underTest.generate(caseData, hearingBooking);
        Map<String, Object> expectedData = Map.of(
            "hasOthers", YES.getValue(),
            "othersSelector", OTHER_SELECTOR,
            "others_label", OTHER_LABEL,
            "sendNoticeOfHearing", NO.getValue(),
            "sendOrderToAllOthers", NO.getValue()
        );

        assertThat(generatedData).isEqualTo(expectedData);
    }

    @Test
    void shouldGenerateFieldsWhenOthersIsEmptyInHearingBooking() {
        CaseData caseData = CaseData.builder().others(Others.builder().firstOther(OTHER).build()).build();
        HearingBooking hearingBooking = HearingBooking.builder().others(Collections.emptyList()).build();

        when(othersService.buildOtherSelector(unwrapElements(caseData.getOthersV2()),
            unwrapElements(hearingBooking.getOthers()))).thenReturn(OTHER_SELECTOR);
        when(othersService.getOthersLabel(any())).thenReturn(OTHER_LABEL);

        Map<String, Object> generatedData = underTest.generate(caseData, hearingBooking);
        Map<String, Object> expectedData = Map.of(
            "hasOthers", YES.getValue(),
            "othersSelector", OTHER_SELECTOR,
            "others_label", OTHER_LABEL,
            "sendNoticeOfHearing", NO.getValue(),
            "sendOrderToAllOthers", NO.getValue()
        );

        assertThat(generatedData).isEqualTo(expectedData);
    }

    @Test
    void shouldNotGenerateSomeFieldsWhenNewHearingType() {
        CaseData caseData = CaseData.builder()
            .others(Others.builder().firstOther(OTHER).build())
            .hearingOption(NEW_HEARING)
            .build();
        HearingBooking hearingBooking = HearingBooking.builder().others(wrapElements(OTHER)).build();

        when(othersService.buildOtherSelector(unwrapElements(caseData.getOthersV2()),
            unwrapElements(hearingBooking.getOthers()))).thenReturn(OTHER_SELECTOR);
        when(othersService.getOthersLabel(any())).thenReturn(OTHER_LABEL);

        Map<String, Object> generatedData = underTest.generate(caseData, hearingBooking);

        assertThat(generatedData).doesNotContainKeys("sendNoticeOfHearing", "sendOrderToAllOthers");
    }

    @Test
    void shouldNotGenerateFieldsWhenNoOthersInCase() {
        CaseData caseData = CaseData.builder().build();
        HearingBooking hearingBooking = HearingBooking.builder().build();

        Map<String, Object> generatedData = underTest.generate(caseData, hearingBooking);

        assertThat(generatedData).isEmpty();
    }

}
