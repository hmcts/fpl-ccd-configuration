package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.PARTIES_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_SHARED;
import static uk.gov.hmcts.reform.fpl.enums.Event.ACTION_CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.Event.DRAFT_CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class CaseManagementOrderProgressionServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final UUID UUID = randomUUID();

    @Autowired
    private ObjectMapper mapper;

    private ApplicationEventPublisher applicationEventPublisher;

    private RequestData requestData;

    private CaseManagementOrderProgressionService service;

    @BeforeEach
    void setUp() {
        this.service = new CaseManagementOrderProgressionService(mapper, applicationEventPublisher, requestData);
    }

    @Test
    void shouldPopulateCmoToActionWhenLocalAuthoritySendsToJudge() throws IOException {
        CaseData caseData = caseDataWithCaseManagementOrder(SEND_TO_JUDGE).build();
        CaseDetails caseDetails = getCaseDetails(caseData);

        service.handleCaseManagementOrderProgression(caseDetails, DRAFT_CASE_MANAGEMENT_ORDER.getId());

        CaseData updatedCaseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        assertThat(updatedCaseData.getCaseManagementOrder()).isEqualTo(caseData.getCaseManagementOrder().toBuilder()
            .status(SEND_TO_JUDGE).build());
        assertThat(caseDetails.getData().get(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey())).isNull();
    }

    @Test
    void shouldPopulateSharedDocumentWhenOrderIsReadyForPartiesReview() throws IOException {
        CaseData caseData = caseDataWithCaseManagementOrder(PARTIES_REVIEW).build();
        CaseDetails caseDetails = getCaseDetails(caseData);

        service.handleCaseManagementOrderProgression(caseDetails, DRAFT_CASE_MANAGEMENT_ORDER.getId());

        CaseData updatedCaseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        assertThat(updatedCaseData.getCaseManagementOrder()).isEqualTo(caseData.getCaseManagementOrder());
        assertThat(caseDetails.getData().get(CASE_MANAGEMENT_ORDER_SHARED.getKey())).isNotNull();
        assertThat(caseDetails.getData().get(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey())).isNull();
    }

    @Test
    void shouldRemoveSharedDraftDocumentWhenStatusIsSelfReview() throws IOException {
        CaseData caseData = caseDataWithCaseManagementOrder(CMOStatus.SELF_REVIEW)
            .sharedDraftCMODocument(DocumentReference.builder().build())
            .build();

        CaseDetails caseDetails = getCaseDetails(caseData);

        service.handleCaseManagementOrderProgression(caseDetails, DRAFT_CASE_MANAGEMENT_ORDER.getId());

        CaseData updatedCaseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        assertThat(updatedCaseData.getCaseManagementOrder()).isEqualTo(caseData.getCaseManagementOrder());
        assertThat(caseDetails.getData().get(CASE_MANAGEMENT_ORDER_SHARED.getKey())).isNull();
    }

    @Test
    void shouldPopulateServedCaseManagementOrdersWhenTryingToSendToAllPartiesAndHearingIsComplete() {
        CaseData caseData = caseDataWithCaseManagementOrderAction(SEND_TO_ALL_PARTIES)
            .hearingDetails(hearingBookingWithStartDateInPast())
            .build();

        CaseDetails caseDetails = getCaseDetails(caseData);

        service.handleCaseManagementOrderProgression(caseDetails, ACTION_CASE_MANAGEMENT_ORDER.getId());

        CaseData updatedCaseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        assertThat(updatedCaseData.getServedCaseManagementOrders()).hasSize(1);
        assertThat(updatedCaseData.getServedCaseManagementOrders().get(0).getValue())
            .isEqualTo(caseData.getCaseManagementOrder());
        assertThat(caseDetails.getData().get(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey())).isNull();
    }

    @Test
    void shouldPopulateFirstElementOfServedCaseManagementOrdersWhenTryingToSendToAllPartiesAndHearingIsComplete() {
        List<Element<CaseManagementOrder>> orders = orderListWithOneElement();

        CaseData caseData = caseDataWithCaseManagementOrderAction(SEND_TO_ALL_PARTIES)
            .servedCaseManagementOrders(orders)
            .hearingDetails(hearingBookingWithStartDateInPast())
            .build();

        CaseDetails caseDetails = getCaseDetails(caseData);

        service.handleCaseManagementOrderProgression(caseDetails, ACTION_CASE_MANAGEMENT_ORDER.getId());

        CaseData updatedCaseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        assertThat(updatedCaseData.getServedCaseManagementOrders()).hasSize(2);
        assertThat(updatedCaseData.getServedCaseManagementOrders().get(0).getValue())
            .isEqualTo(caseData.getCaseManagementOrder());
        assertThat(caseDetails.getData().get(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey())).isNull();
    }

    @Test
    void shouldPopulateDraftCaseManagementOrderWhenJudgeRequestsChange() {
        CaseData caseData = caseDataWithCaseManagementOrderAction(JUDGE_REQUESTED_CHANGE).build();
        CaseDetails caseDetails = getCaseDetails(caseData);

        service.handleCaseManagementOrderProgression(caseDetails, ACTION_CASE_MANAGEMENT_ORDER.getId());

        CaseData updatedCaseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        assertThat(updatedCaseData.getCaseManagementOrder())
            .isEqualTo(caseData.getCaseManagementOrder().toBuilder().status(CMOStatus.SELF_REVIEW).build());

        assertThat(caseDetails.getData().get(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey())).isNull();
    }

    @Test
    void shouldDoNothingWhenJudgeLeavesInSelfReview() {
        CaseData caseData = caseDataWithCaseManagementOrderAction(SELF_REVIEW).build();
        CaseDetails caseDetails = getCaseDetails(caseData);

        service.handleCaseManagementOrderProgression(caseDetails, ACTION_CASE_MANAGEMENT_ORDER.getId());

        CaseData updatedCaseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        assertThat(updatedCaseData.getCaseManagementOrder()).isEqualTo(caseData.getCaseManagementOrder());
        assertThat(caseDetails.getData().get(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey())).isNull();
    }

    private CaseData.CaseDataBuilder caseDataWithCaseManagementOrder(CMOStatus status) throws IOException {
        return CaseData.builder().caseManagementOrder(
            CaseManagementOrder.builder()
                .status(status)
                .orderDoc(buildFromDocument(document()))
                .build());
    }

    private CaseData.CaseDataBuilder caseDataWithCaseManagementOrderAction(ActionType type) {
        return CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder()
                .id(UUID)
                .status(SEND_TO_JUDGE)
                .action(OrderAction.builder()
                    .type(type)
                    .build())
                .build());
    }

    @SuppressWarnings("unchecked")
    private CaseDetails getCaseDetails(CaseData caseData) {
        Map<String, Object> data = mapper.convertValue(caseData, Map.class);
        return CaseDetails.builder().data(data).build();
    }

    private List<Element<HearingBooking>> hearingBookingWithStartDateInPast() {
        return ImmutableList.of(Element.<HearingBooking>builder()
            .id(UUID)
            .value(HearingBooking.builder()
                .startDate(NOW.plusDays(-1))
                .build())
            .build());
    }

    private List<Element<CaseManagementOrder>> orderListWithOneElement() {
        List<Element<CaseManagementOrder>> orders = new ArrayList<>();
        orders.add(Element.<CaseManagementOrder>builder().build());
        return orders;
    }
}
