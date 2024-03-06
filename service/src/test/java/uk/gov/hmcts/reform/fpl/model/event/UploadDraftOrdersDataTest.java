package uk.gov.hmcts.reform.fpl.model.event;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderKind;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOType.AGREED;
import static uk.gov.hmcts.reform.fpl.enums.CMOType.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.CMO;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData.builder;

class UploadDraftOrdersDataTest {

    @Test
    void shouldReturnTemporaryFields() {
        assertThat(UploadDraftOrdersData.temporaryFields())
            .containsExactlyInAnyOrder(
                "hearingOrderDraftKind",
                "currentHearingOrderDrafts",
                "uploadedCaseManagementOrder",
                "replacementCMO",
                "pastHearingsForCMO",
                "futureHearingsForCMO",
                "hearingsForHearingOrderDrafts",
                "cmoUploadType",
                "cmosSentToJudge",
                "cmoHearingInfo",
                "previousCMO",
                "cmoJudgeInfo",
                "cmoToSend",
                "showCMOsSentToJudge",
                "showReplacementCMO",
                "cmoToSendTranslationRequirements",
                "orderToSend0",
                "orderToSendTranslationRequirements0",
                "orderToSend1",
                "orderToSendTranslationRequirements1",
                "orderToSend2",
                "orderToSendTranslationRequirements2",
                "orderToSend3",
                "orderToSendTranslationRequirements3",
                "orderToSend4",
                "orderToSendTranslationRequirements4",
                "orderToSend5",
                "orderToSendTranslationRequirements5",
                "orderToSend6",
                "orderToSendTranslationRequirements6",
                "orderToSend7",
                "orderToSendTranslationRequirements7",
                "orderToSend8",
                "orderToSendTranslationRequirements8",
                "orderToSend9",
                "orderToSendTranslationRequirements9",
                "orderToSendOptionCount",
                "uploadCMOMessageAcknowledge"
            );
    }

    @Test
    void shouldReturnIsCmoAgreedBasedOnCmoUploadType() {
        assertThat(builder().cmoUploadType(AGREED).build().isCmoAgreed()).isTrue();
        assertThat(builder().cmoUploadType(DRAFT).build().isCmoAgreed()).isFalse();
        assertThat(builder().cmoUploadType(null).build().isCmoAgreed()).isFalse();
    }

    @Nested
    class GetOrderToSendTranslationRequirements {

        @Test
        void testEmpty() {

            UploadDraftOrdersData underTest = builder().build();

            IntStream.range(0, 10).forEach(i ->
                assertThat(underTest.getOrderToSendTranslationRequirements(i)).isNull()
            );

        }

        @Test
        void testNotEmpty() {

            UploadDraftOrdersData underTest = builder()
                .orderToSendTranslationRequirements0(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements1(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements2(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements3(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements4(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements5(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements6(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements7(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements8(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements9(ENGLISH_TO_WELSH)
                .build();

            IntStream.range(0, 10).forEach(i ->
                assertThat(underTest.getOrderToSendTranslationRequirements(i)).isEqualTo(ENGLISH_TO_WELSH)
            );
        }

        @Test
        void testPartialOverBoundary() {

            UploadDraftOrdersData underTest = builder()
                .orderToSendTranslationRequirements0(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements1(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements2(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements3(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements4(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements5(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements6(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements7(ENGLISH_TO_WELSH)
                .orderToSendTranslationRequirements8(ENGLISH_TO_WELSH)
                .build();

            IntStream.range(0, 9).forEach(i ->
                assertThat(underTest.getOrderToSendTranslationRequirements(i)).isEqualTo(ENGLISH_TO_WELSH)
            );
            assertThat(underTest.getOrderToSendTranslationRequirements(9)).isNull();

        }
    }

    @Nested
    class HearingDynamicList {

        final DynamicList pastHearing = dynamicList();
        final DynamicList futureHearing = dynamicList();
        final DynamicList allHearings = dynamicList();

        @Test
        void shouldReturnPastHearingsWhenAgreedCmoSelected() {
            final UploadDraftOrdersData uploadDraftOrdersData = UploadDraftOrdersData.builder()
                .pastHearingsForCMO(pastHearing)
                .futureHearingsForCMO(futureHearing)
                .hearingsForHearingOrderDrafts(allHearings)
                .hearingOrderDraftKind(List.of(CMO))
                .cmoUploadType(AGREED)
                .build();

            assertThat(uploadDraftOrdersData.getHearingDynamicList()).isEqualTo(pastHearing);
        }

        @Test
        void shouldReturnFutureHearingsWhenDraftCmoSelected() {
            final UploadDraftOrdersData uploadDraftOrdersData = UploadDraftOrdersData.builder()
                .pastHearingsForCMO(pastHearing)
                .futureHearingsForCMO(futureHearing)
                .hearingsForHearingOrderDrafts(allHearings)
                .hearingOrderDraftKind(List.of(CMO))
                .cmoUploadType(DRAFT)
                .build();

            assertThat(uploadDraftOrdersData.getHearingDynamicList()).isEqualTo(futureHearing);
        }

        @Test
        void shouldReturnAllHearingsWhenDraftOrderSelected() {
            final UploadDraftOrdersData uploadDraftOrdersData = UploadDraftOrdersData.builder()
                .pastHearingsForCMO(pastHearing)
                .futureHearingsForCMO(futureHearing)
                .hearingsForHearingOrderDrafts(allHearings)
                .hearingOrderDraftKind(List.of(C21))
                .build();

            assertThat(uploadDraftOrdersData.getHearingDynamicList()).isEqualTo(allHearings);
        }

        @Test
        void shouldReturnPastHearingsWhenAgreedCmoAndDraftOrdersSelected() {
            final UploadDraftOrdersData uploadDraftOrdersData = UploadDraftOrdersData.builder()
                .pastHearingsForCMO(pastHearing)
                .futureHearingsForCMO(futureHearing)
                .hearingsForHearingOrderDrafts(allHearings)
                .hearingOrderDraftKind(List.of(CMO, C21))
                .cmoUploadType(AGREED)
                .build();

            assertThat(uploadDraftOrdersData.getHearingDynamicList()).isEqualTo(pastHearing);
        }

        @Test
        void shouldReturnPastHearingsWhenDraftCmoAndDraftOrdersSelected() {
            final UploadDraftOrdersData uploadDraftOrdersData = UploadDraftOrdersData.builder()
                .pastHearingsForCMO(pastHearing)
                .futureHearingsForCMO(futureHearing)
                .hearingsForHearingOrderDrafts(allHearings)
                .hearingOrderDraftKind(List.of(CMO, C21))
                .cmoUploadType(DRAFT)
                .build();

            assertThat(uploadDraftOrdersData.getHearingDynamicList()).isEqualTo(futureHearing);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnNullWhenOrderTypeNotKnown(List<HearingOrderKind> kinds) {
            final UploadDraftOrdersData uploadDraftOrdersData = UploadDraftOrdersData.builder()
                .pastHearingsForCMO(pastHearing)
                .futureHearingsForCMO(futureHearing)
                .hearingsForHearingOrderDrafts(allHearings)
                .hearingOrderDraftKind(kinds)
                .cmoUploadType(DRAFT)
                .build();

            assertThat(uploadDraftOrdersData.getHearingDynamicList()).isNull();
        }

        @Test
        void shouldReturnYesIfC21Order() {
            final UploadDraftOrdersData uploadDraftOrdersData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(C21))
                .build();

            assertThat(uploadDraftOrdersData.hasDraftOrderBeenUploadedThatNeedsApproval()).isEqualTo(YesNo.YES);
        }

        @Test
        void shouldReturnYesIfApprovedCMO() {
            final UploadDraftOrdersData uploadDraftOrdersData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(CMO))
                .cmoUploadType(AGREED)
                .build();

            assertThat(uploadDraftOrdersData.hasDraftOrderBeenUploadedThatNeedsApproval()).isEqualTo(YesNo.YES);
        }

        @Test
        void shouldReturnNoIfDraftCMO() {
            final UploadDraftOrdersData uploadDraftOrdersData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(CMO))
                .cmoUploadType(DRAFT)
                .build();

            assertThat(uploadDraftOrdersData.hasDraftOrderBeenUploadedThatNeedsApproval()).isEqualTo(YesNo.NO);
        }

        private DynamicList dynamicList() {
            return DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(randomAlphanumeric(10))
                    .build())
                .build();
        }
    }

}
