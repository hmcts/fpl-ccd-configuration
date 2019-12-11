package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, JsonOrdersLookupService.class, LookupTestConfig.class,
    JsonOrdersLookupService.class, DateFormatterService.class, DirectionHelperService.class,
    DocmosisConfiguration.class, RestTemplate.class, CaseDataExtractionService.class,
    DocmosisDocumentGeneratorService.class, CommonCaseDataExtractionService.class, HearingBookingService.class,
    HearingVenueLookUpService.class, ActionCmoService.class
})
class ActionCmoServiceTest {

    @Autowired
    private ActionCmoService service;

    @Test
    void shouldAddDocumentToOrderWhenOrderAndDocumentExist() throws IOException {
        Document document = document();
        CaseManagementOrder orderWithDocument = service.addDocument(CaseManagementOrder.builder().build(), document);

        assertThat(orderWithDocument.getOrderDoc()).isEqualTo(DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build());
    }

    @Test
    void shouldAddSharedDocumentToCaseDataAndCaseManagementOrderWhenApproved() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        final CaseManagementOrder order = CaseManagementOrder.builder()
            .action(OrderAction.builder()
                .type(SEND_TO_ALL_PARTIES)
                .build())
            .build();

        service.progressCMOToAction(caseDetails, order);

        assertThat(caseDetails.getData()).containsOnlyKeys("sharedDraftCMODocument");
    }

    @Test
    void shouldDoNothingToCaseDataWhenSelfReviewIsRequired() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        final CaseManagementOrder order = CaseManagementOrder.builder()
            .action(OrderAction.builder()
                .type(SELF_REVIEW)
                .build())
            .build();

        service.progressCMOToAction(caseDetails, order);

        assertThat(caseDetails.getData()).isEmpty();
    }

    @Test
    void shouldRemoveCMOToActionAndAddCaseManagementOrderToCaseDataWhenJudgeRequestsChange() {
        final HashMap<String, Object> data = new HashMap<>();
        data.put("cmoToAction", null);
        CaseDetails caseDetails = CaseDetails.builder().data(data).build();

        final CaseManagementOrder order = CaseManagementOrder.builder()
            .action(OrderAction.builder()
                .type(JUDGE_REQUESTED_CHANGE)
                .build())
            .build();

        service.progressCMOToAction(caseDetails, order);

        assertThat(caseDetails.getData()).doesNotContainKey("cmoToAction");
        assertThat(caseDetails.getData()).containsKey("caseManagementOrder");
        assertThat(caseDetails.getData().get("caseManagementOrder")).isEqualTo(order);
    }
}
