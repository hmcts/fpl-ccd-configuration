package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.exceptions.HearingOrdersBundleNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;

import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class DraftOrdersBundleHearingSelectorTest {

    private static final UUID BUNDLE_ID = UUID.randomUUID();
    private final DraftOrdersBundleHearingSelector underTest = new DraftOrdersBundleHearingSelector(new ObjectMapper());

    @Test
    void testIfNullBundles() {
        CaseData caseDa = CaseData.builder().build();
        Exception exception = assertThrows(IllegalStateException.class,
            () -> underTest.getSelectedHearingDraftOrdersBundle(caseDa));

        assertThat(exception).hasMessageContaining("Bundle not found");
    }

    @Test
    void testIfOnlyOneBundleSentToJudge() {
        Element<HearingOrdersBundle> hearingBundle = element(UUID.randomUUID(), HearingOrdersBundle.builder()
            .orders(newArrayList(element(
                UUID.randomUUID(), HearingOrder.builder()
                    .status(CMOStatus.SEND_TO_JUDGE)
                    .build()
            ))).build());

        Element<HearingOrdersBundle> actual = underTest.getSelectedHearingDraftOrdersBundle(
            CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of(hearingBundle))
                .build()
        );

        assertThat(actual).isEqualTo(hearingBundle);
    }

    @Test
    void testIfMultipleBundleSentToJudgeWhenSelected() {
        Element<HearingOrdersBundle> hearingBundle = element(BUNDLE_ID, HearingOrdersBundle.builder()
            .orders(newArrayList(element(
                UUID.randomUUID(), HearingOrder.builder()
                    .status(CMOStatus.SEND_TO_JUDGE)
                    .build()
            ))).build());

        Element<HearingOrdersBundle> anotherHearingBundle = element(UUID.randomUUID(), HearingOrdersBundle.builder()
            .orders(newArrayList(element(
                UUID.randomUUID(), HearingOrder.builder()
                    .status(CMOStatus.SEND_TO_JUDGE)
                    .build()
            ))).build());

        Element<HearingOrdersBundle> actual = underTest.getSelectedHearingDraftOrdersBundle(
            CaseData.builder()
                .cmoToReviewList(BUNDLE_ID.toString())
                .hearingOrdersBundlesDrafts(List.of(hearingBundle, anotherHearingBundle))
                .build()
        );

        assertThat(actual).isEqualTo(hearingBundle);
    }

    @Test
    void testIfMultipleBundleSentToJudgeNotMatchingId() {
        Element<HearingOrdersBundle> hearingBundle = element(UUID.randomUUID(), HearingOrdersBundle.builder()
            .orders(newArrayList(element(
                UUID.randomUUID(), HearingOrder.builder()
                    .status(CMOStatus.SEND_TO_JUDGE)
                    .build()
            ))).build());

        Element<HearingOrdersBundle> anotherHearingBundle = element(UUID.randomUUID(), HearingOrdersBundle.builder()
            .orders(newArrayList(element(
                UUID.randomUUID(), HearingOrder.builder()
                    .status(CMOStatus.SEND_TO_JUDGE)
                    .build()
            ))).build());

        CaseData caseData = CaseData.builder()
            .cmoToReviewList(BUNDLE_ID.toString())
            .hearingOrdersBundlesDrafts(List.of(hearingBundle, anotherHearingBundle))
            .build();

        HearingOrdersBundleNotFoundException exception = assertThrows(HearingOrdersBundleNotFoundException.class,
            () -> underTest.getSelectedHearingDraftOrdersBundle(caseData));

        assertThat(exception.getMessage()).isEqualTo(String.format(
            "Could not find hearing draft orders bundle with id %s",
            BUNDLE_ID.toString()));
    }

    @Test
    void testIfMultipleBundleSentToJudgeWhenDynamicList() {
        Element<HearingOrdersBundle> hearingBundle = element(BUNDLE_ID, HearingOrdersBundle.builder()
            .orders(newArrayList(element(
                UUID.randomUUID(), HearingOrder.builder()
                    .status(CMOStatus.SEND_TO_JUDGE)
                    .build()
            ))).build());

        Element<HearingOrdersBundle> anotherHearingBundle = element(UUID.randomUUID(), HearingOrdersBundle.builder()
            .orders(newArrayList(element(
                UUID.randomUUID(), HearingOrder.builder()
                    .status(CMOStatus.SEND_TO_JUDGE)
                    .build()
            ))).build());

        Element<HearingOrdersBundle> actual = underTest.getSelectedHearingDraftOrdersBundle(
            CaseData.builder()
                .cmoToReviewList(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code(BUNDLE_ID)
                        .build())
                    .build())
                .hearingOrdersBundlesDrafts(List.of(hearingBundle, anotherHearingBundle))
                .build()
        );

        assertThat(actual).isEqualTo(hearingBundle);
    }
}
