package uk.gov.hmcts.reform.fpl.model.event;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderKind;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOType.AGREED;
import static uk.gov.hmcts.reform.fpl.enums.CMOType.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.CMO;
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
                "cmoSupportingDocs",
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
                "showReplacementCMO");
    }

    @Test
    void shouldReturnIsCmoAgreedBasedOnCmoUploadType() {
        assertThat(builder().cmoUploadType(AGREED).build().isCmoAgreed()).isTrue();
        assertThat(builder().cmoUploadType(DRAFT).build().isCmoAgreed()).isFalse();
        assertThat(builder().cmoUploadType(null).build().isCmoAgreed()).isFalse();
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

        private DynamicList dynamicList() {
            return DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(randomAlphanumeric(10))
                    .build())
                .build();
        }
    }

}
