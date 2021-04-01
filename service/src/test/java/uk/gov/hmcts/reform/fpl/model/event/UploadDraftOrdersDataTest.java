package uk.gov.hmcts.reform.fpl.model.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOType.AGREED;
import static uk.gov.hmcts.reform.fpl.enums.CMOType.DRAFT;
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

}
