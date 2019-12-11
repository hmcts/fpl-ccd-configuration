package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.io.IOException;
import java.util.Map;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.PARTIES_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class CaseManagementOrderProgressionServiceTest {
    private static final java.util.UUID UUID = randomUUID();

    @Autowired
    private ObjectMapper mapper;

    private CaseManagementOrderProgressionService service;

    @BeforeEach
    void setUp() {
        this.service = new CaseManagementOrderProgressionService(mapper);
    }

    @Test
    void shouldPopulateCmoToActionWhenLocalAuthoritySendsToJudge() throws IOException {
        CaseData caseData = caseDataWithCaseManagementOrder(SEND_TO_JUDGE).build();
        CaseDetails caseDetails = getCaseDetails(caseData);

        service.handleCaseManagementOrderProgression(caseDetails);

        CaseData updatedCaseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        assertThat(updatedCaseData.getCmoToAction()).isEqualTo(caseData.getCaseManagementOrder());
        assertThat(caseDetails.getData().get("caseManagementOrder")).isNull();
    }

    @Test
    void shouldPopulateSharedDocumentWhenOrderIsReadyForPartiesReview() throws IOException {
        CaseData caseData = caseDataWithCaseManagementOrder(PARTIES_REVIEW).build();
        CaseDetails caseDetails = getCaseDetails(caseData);

        service.handleCaseManagementOrderProgression(caseDetails);

        CaseData updatedCaseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        assertThat(updatedCaseData.getCaseManagementOrder()).isEqualTo(caseData.getCaseManagementOrder());
        assertThat(caseDetails.getData().get("sharedDraftCMODocument")).isNotNull();
        assertThat(caseDetails.getData().get("cmoToAction")).isNull();
    }

    @Test
    void shouldRemoveSharedDraftDocumentWhenStatusIsSelfReview() throws IOException {
        CaseData caseData = caseDataWithCaseManagementOrder(SELF_REVIEW)
            .sharedDraftCMODocument(DocumentReference.builder().build())
            .build();

        CaseDetails caseDetails = getCaseDetails(caseData);

        service.handleCaseManagementOrderProgression(caseDetails);

        CaseData updatedCaseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        assertThat(updatedCaseData.getCaseManagementOrder()).isEqualTo(caseData.getCaseManagementOrder());
        assertThat(caseDetails.getData().get("sharedDraftCMODocument")).isNull();
    }

    @Test
    void shouldPopulateDraftCaseManagementOrderWhenJudgeRequestsChange() {
        CaseData caseData = caseDataWithCmoToAction(JUDGE_REQUESTED_CHANGE).build();
        CaseDetails caseDetails = getCaseDetails(caseData);

        service.handleCaseManagementOrderProgression(caseDetails);

        CaseData updatedCaseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        assertThat(updatedCaseData.getCaseManagementOrder()).isEqualTo(caseData.getCmoToAction());
        assertThat(caseDetails.getData().get("cmoToAction")).isNull();
    }

    @Test
    void shouldDoNothingWhenJudgeLeavesInSelfReview() {
        CaseData caseData = caseDataWithCmoToAction(ActionType.SELF_REVIEW).build();
        CaseDetails caseDetails = getCaseDetails(caseData);

        service.handleCaseManagementOrderProgression(caseDetails);

        CaseData updatedCaseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        assertThat(updatedCaseData.getCmoToAction()).isEqualTo(caseData.getCmoToAction());
        assertThat(caseDetails.getData().get("caseManagementOrder")).isNull();
    }

    private CaseData.CaseDataBuilder caseDataWithCaseManagementOrder(CMOStatus status) throws IOException {
        return CaseData.builder().caseManagementOrder(
            CaseManagementOrder.builder()
                .status(status)
                .orderDoc(buildFromDocument(document()))
                .build());
    }

    @SuppressWarnings("unchecked")
    private CaseDetails getCaseDetails(CaseData caseData) {
        Map<String, Object> data = mapper.convertValue(caseData, Map.class);
        return CaseDetails.builder().data(data).build();
    }

    private CaseData.CaseDataBuilder caseDataWithCmoToAction(ActionType type) {
        return CaseData.builder()
            .cmoToAction(CaseManagementOrder.builder()
                .id(UUID)
                .action(OrderAction.builder()
                    .type(type)
                    .build())
                .build());
    }
}
