package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.RemovalToolData;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.IdentityService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.controllers.RemovalToolController.APPLICATION_FORM_ALREADY_REMOVED_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationRemovalReason.DUPLICATE;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.RemovableType.ADDITIONAL_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.RemovableType.APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.RemovableType.ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RemovableType.SENT_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(RemovalToolController.class)
@OverrideAutoConfiguration(enabled = true)
class RemovalToolControllerAboutToSubmitTest extends AbstractCallbackTest {
    private static final String REASON = "The order was removed because the order was removed";
    private static final UUID SDO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID REMOVED_ORDER_ID = UUID.randomUUID();
    public static final String DUMMY_DATA = "dummy data";

    @MockBean
    private IdentityService identityService;

    private Element<GeneratedOrder> selectedOrder;

    RemovalToolControllerAboutToSubmitTest() {
        super("remove-order");
    }

    @BeforeEach
    void initialise() {
        selectedOrder = element(buildOrder());
    }

    //@Test
    void shouldUpdateGeneratedOrderCollectionAndHiddenGeneratedOrderCollection() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
            asCaseDetails(buildCaseData(selectedOrder))
        );

        CaseData responseData = extractCaseData(response);

        selectedOrder.getValue().setRemovalReason(REASON);

        assertThat(responseData.getOrderCollection()).isEmpty();
        assertThat(responseData.getRemovalToolData().getHiddenOrders()).hasSize(1).containsOnly(selectedOrder);
    }

    //@Test
    void shouldRemoveTemporaryFields() {
        Map<String, Object> fields = new HashMap<>();

        fields.put("removableType", ORDER);
        fields.put("removableApplicationList", DynamicList.builder().build());
        fields.put("orderTitleToBeRemoved", DUMMY_DATA);
        fields.put("applicationTypeToBeRemoved", DUMMY_DATA);
        fields.put("orderToBeRemoved", DUMMY_DATA);
        fields.put("c2ApplicationToBeRemoved", DUMMY_DATA);
        fields.put("otherApplicationToBeRemoved", DUMMY_DATA);
        fields.put("orderIssuedDateToBeRemoved", DUMMY_DATA);
        fields.put("orderDateToBeRemoved", DUMMY_DATA);
        fields.put("reasonToRemoveApplication", DUPLICATE);
        fields.put("applicationRemovalDetails", DUMMY_DATA);
        fields.put("hearingToUnlink", DUMMY_DATA);
        fields.put("showRemoveCMOFieldsFlag", DUMMY_DATA);
        fields.put("showRemoveSDOWarningFlag", DUMMY_DATA);
        fields.put("showReasonFieldFlag", DUMMY_DATA);
        fields.put("reasonToRemoveApplicationForm", DUMMY_DATA);
        fields.put("partyNameToBeRemoved", DUMMY_DATA);
        fields.put("sentAtToBeRemoved", DUMMY_DATA);
        fields.put("letterIdToBeRemoved", DUMMY_DATA);
        fields.put("sentDocumentToBeRemoved", DUMMY_DATA);
        fields.put("reasonToRemoveSentDocument", DUMMY_DATA);

        CaseDetails caseDetails = asCaseDetails(buildCaseData(selectedOrder));

        caseDetails.getData().putAll(fields);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).doesNotContainKeys(
            "removableOrderList",
            "removableApplicationList",
            "removableType",
            "orderTitleToBeRemoved",
            "applicationTypeToBeRemoved",
            "orderToBeRemoved",
            "c2ApplicationToBeRemoved",
            "otherApplicationToBeRemoved",
            "orderIssuedDateToBeRemoved",
            "orderDateToBeRemoved",
            "reasonToRemoveOrder",
            "reasonToRemoveApplication",
            "applicationRemovalDetails",
            "hearingToUnlink",
            "showRemoveCMOFieldsFlag",
            "showRemoveSDOWarningFlag",
            "showReasonFieldFlag",
            "reasonToRemoveApplicationForm",
            "partyNameToBeRemoved",
            "sentAtToBeRemoved",
            "letterIdToBeRemoved",
            "sentDocumentToBeRemoved",
            "reasonToRemoveSentDocument"
        );
    }

    //@Test
    void shouldUpdateChildrenPropertiesWhenRemovingAFinalOrder() {
        UUID childOneId = UUID.randomUUID();
        UUID childTwoId = UUID.randomUUID();

        List<Element<Child>> childrenList = List.of(
            element(childOneId, Child.builder()
                .finalOrderIssued("Yes")
                .finalOrderIssuedType("Some type")
                .party(ChildParty.builder().build())
                .build()),
            element(childTwoId, Child.builder()
                .finalOrderIssued("Yes")
                .finalOrderIssuedType("Some type")
                .party(ChildParty.builder().build())
                .build())
        );

        Element<GeneratedOrder> order1 = element(buildOrder(EMERGENCY_PROTECTION_ORDER, childrenList));

        CaseData caseData = CaseData.builder()
            .children1(childrenList)
            .orderCollection(List.of(order1))
            .removalToolData(RemovalToolData.builder()
                .removableType(ORDER)
                .removableOrderList(DynamicList.builder()
                    .value(buildListElement(order1.getId(), "order 1 - 15 June 2020"))
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData returnedCaseData = extractCaseData(response);
        List<Element<Child>> returnedChildren = returnedCaseData.getChildren1();

        List<Element<Child>> expectedChildrenList = List.of(
            element(childOneId, Child.builder()
                .party(ChildParty.builder().build())
                .build()),
            element(childTwoId, Child.builder()
                .party(ChildParty.builder().build())
                .build())
        );

        assertThat(returnedChildren).isEqualTo(expectedChildrenList);
    }

    //@Test
    void shouldNotUpdateChildrenPropertiesWhenRemovingANonFinalOrder() {
        List<Element<Child>> childrenList = List.of(
            element(UUID.randomUUID(), Child.builder()
                .finalOrderIssued("Yes")
                .finalOrderIssuedType("Some type")
                .party(ChildParty.builder().build())
                .build()),
            element(UUID.randomUUID(), Child.builder()
                .finalOrderIssued("Yes")
                .finalOrderIssuedType("Some type")
                .party(ChildParty.builder().build())
                .build())
        );

        Element<GeneratedOrder> order1 = element(buildOrder(BLANK_ORDER, childrenList));

        CaseData caseData = CaseData.builder()
            .children1(childrenList)
            .orderCollection(List.of(order1))
            .removalToolData(RemovalToolData.builder()
                .removableOrderList(DynamicList.builder()
                    .value(buildListElement(order1.getId(), "order 1 - 15 June 2020"))
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData returnedCaseData = extractCaseData(response);
        List<Element<Child>> returnedChildren = returnedCaseData.getChildren1();

        assertThat(returnedChildren).isEqualTo(childrenList);
    }

    //@Test
    void shouldRemoveSealedCaseManagementOrderAndRemoveHearingAssociation() {
        Element<HearingOrder> caseManagementOrder1 = element(REMOVED_ORDER_ID, HearingOrder.builder()
            .status(APPROVED)
            .type(AGREED_CMO)
            .build());

        List<Element<HearingOrder>> caseManagementOrders = List.of(
            caseManagementOrder1,
            element(HearingOrder.builder().build()));

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(HearingBooking.builder()
                .caseManagementOrderId(REMOVED_ORDER_ID)
                .build()));

        CaseData caseData = CaseData.builder()
            .sealedCMOs(caseManagementOrders)
            .hearingDetails(hearingBookings)
            .removalToolData(RemovalToolData.builder()
                .removableType(ORDER)
                .removableOrderList(DynamicList.builder()
                    .value(buildListElement(REMOVED_ORDER_ID, "Sealed case management order issued on 15 June 2020"))
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData responseData = extractCaseData(response);
        List<Element<HearingOrder>> hiddenCMOs = responseData.getRemovalToolData().getHiddenCMOs();
        HearingBooking unlinkedHearing = responseData.getHearingDetails().get(0).getValue();

        assertThat(hiddenCMOs).hasSize(1).first().isEqualTo(caseManagementOrder1);
        assertNull(unlinkedHearing.getCaseManagementOrderId());
    }

    //@Test
    void shouldRemoveDraftOrderFromTheHearingOrdersDraftBundles() {
        Element<HearingOrder> draftOrder = element(REMOVED_ORDER_ID, HearingOrder.builder()
            .status(SEND_TO_JUDGE)
            .type(C21)
            .dateSent(LocalDate.of(2020, 6, 15))
            .build());

        Element<HearingOrder> draftCMO = element(
            HearingOrder.builder().type(AGREED_CMO).status(SEND_TO_JUDGE).build());

        Element<HearingOrdersBundle> hearingOrdersBundle = element(HearingOrdersBundle.builder()
            .orders(newArrayList(draftOrder, draftCMO))
            .build());

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(HearingBooking.builder().build())))
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .removalToolData(RemovalToolData.builder()
                .removableType(ORDER)
                .removableOrderList(DynamicList.builder()
                    .value(buildListElement(REMOVED_ORDER_ID, "Draft order sent on 15 June 2020"))
                    .build())
                .build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        Element<HearingOrdersBundle> expectedHearingOrderBundle = element(hearingOrdersBundle.getId(),
            HearingOrdersBundle.builder().orders(newArrayList(draftCMO)).build());

        assertThat(responseData.getHearingOrdersBundlesDrafts())
            .hasSize(1)
            .first().isEqualTo(expectedHearingOrderBundle);
    }

    //@Test
    void shouldRemoveSDONoticeOfProceedingsAndSetStateToGatekeepingWhenRemovingASealedSDO() {
        UUID newSDOId = UUID.randomUUID();

        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Watson")
                .build())
            .orderStatus(SEALED)
            .build();

        CaseData caseData = CaseData.builder()
            .state(CASE_MANAGEMENT)
            .removalToolData(RemovalToolData.builder()
                .removableType(ORDER)
                .reasonToRemoveOrder(REASON)
                .removableOrderList(DynamicList.builder()
                    .value(buildListElement(SDO_ID, "Gatekeeping order - 15 June 2020"))
                    .build())
                .build())
            .standardDirectionOrder(standardDirectionOrder)
            .noticeOfProceedingsBundle(List.of(element(DocumentBundle.builder().build())))
            .build();

        StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .removalReason(REASON)
            .build();

        when(identityService.generateId()).thenReturn(newSDOId);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        CaseData responseData = extractCaseData(response);

        assertThat(responseData.getRemovalToolData().getHiddenStandardDirectionOrders())
            .isEqualTo(List.of(element(newSDOId, expectedSDO)));
        assertThat(responseData.getState()).isEqualTo(GATEKEEPING);
        assertNull(responseData.getNoticeOfProceedingsBundle());
    }

    //@Test
    void shouldRemoveDraftCaseManagementOrderFromHearingOrderBundleDraftsAndRemoveHearingAssociation() {
        UUID removedOrderId = UUID.randomUUID();
        UUID additionalOrderId = UUID.randomUUID();
        UUID hearingOrderBundleId = UUID.randomUUID();

        HearingOrder draftOrder = HearingOrder.builder()
                .status(DRAFT)
                .type(DRAFT_CMO)
                .build();

        HearingOrder additionalOrder = HearingOrder.builder()
                .status(SEND_TO_JUDGE)
                .type(AGREED_CMO)
                .build();

        List<Element<HearingOrder>> caseManagementOrdersDraft = newArrayList(
            element(removedOrderId, draftOrder)
        );

        List<Element<HearingOrder>> caseManagementOrders = newArrayList(
                element(additionalOrderId, additionalOrder));

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(HearingBooking.builder()
                .caseManagementOrderId(removedOrderId)
                .build()));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDraftReview(newArrayList(
                element(hearingOrderBundleId, HearingOrdersBundle.builder().orders(caseManagementOrdersDraft).build())
            ))
            .hearingOrdersBundlesDrafts(newArrayList(
                element(hearingOrderBundleId, HearingOrdersBundle.builder().orders(caseManagementOrders).build())
            ))
            .hearingDetails(hearingBookings)
            .removalToolData(RemovalToolData.builder()
                .removableType(ORDER)
                .removableOrderList(DynamicList.builder()
                    .value(buildListElement(removedOrderId, "Draft case management order - 15 June 2020"))
                    .build())
                .build())
                .draftUploadedCMOs(newArrayList(
                    element(removedOrderId, draftOrder),
                    element(additionalOrderId, additionalOrder))
                )
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData responseData = extractCaseData(response);
        HearingBooking unlinkedHearing = responseData.getHearingDetails().get(0).getValue();

        assertThat(responseData.getHearingOrdersBundlesDrafts()).isEqualTo(newArrayList(
            element(hearingOrderBundleId, HearingOrdersBundle.builder().orders(newArrayList(
                element(additionalOrderId, HearingOrder.builder()
                    .type(HearingOrderType.AGREED_CMO)
                    .status(SEND_TO_JUDGE)
                    .build())
            )).build())
        ));
        assertThat(responseData.getHearingOrdersBundlesDraftReview()).isNull();
        assertNull(unlinkedHearing.getCaseManagementOrderId());
    }

    //@Test
    void shouldRemoveDraftCaseManagementOrderFromDraftCaseManagementOrdersAndRemoveHearingAssociation() {
        UUID removedOrderId = UUID.randomUUID();
        UUID additionalOrderId = UUID.randomUUID();

        Element<HearingOrder> orderToBeRemoved = element(removedOrderId, HearingOrder.builder()
            .status(DRAFT)
            .type(HearingOrderType.DRAFT_CMO)
            .build());

        List<Element<HearingOrder>> caseManagementOrders = newArrayList(
            orderToBeRemoved,
            element(additionalOrderId, HearingOrder.builder().status(DRAFT).build()));

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(HearingBooking.builder()
                .caseManagementOrderId(removedOrderId)
                .build()));

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(caseManagementOrders)
            .hearingDetails(hearingBookings)
            .removalToolData(RemovalToolData.builder()
                .removableType(ORDER)
                .removableOrderList(DynamicList.builder()
                    .value(buildListElement(removedOrderId, "Draft case management order - 15 June 2020"))
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData responseData = extractCaseData(response);
        HearingBooking unlinkedHearing = responseData.getHearingDetails().get(0).getValue();

        Element<HearingOrder> expectedHearingOrderElement = element(additionalOrderId, HearingOrder.builder()
            .title("Draft CMO from advocates' meeting")
            .type(DRAFT_CMO)
            .status(DRAFT).build());

        assertThat(responseData.getDraftUploadedCMOs()).isEqualTo(newArrayList(expectedHearingOrderElement));

        assertNull(unlinkedHearing.getCaseManagementOrderId());
    }

    //@Test
    void shouldUpdateAdditionalApplicationsBundleCollection() {
        UUID applicationId = UUID.randomUUID();
        AdditionalApplicationsBundle application = buildCombinedApplication(C1_WITH_SUPPLEMENT, "6 May 2020");

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(applicationId)
                .label("C2, C1, 6 May 2020")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .removalToolData(RemovalToolData.builder()
                .removableType(ADDITIONAL_APPLICATION)
                .removableApplicationList(dynamicList)
                .reasonToRemoveApplication(DUPLICATE)
                .build())
            .additionalApplicationsBundle(List.of(element(applicationId, application)))
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getAdditionalApplicationsBundle()).isNull();
    }

    //@Test
    void shouldRemoveC110AApplicationForm() {
        DocumentReference c110a = testDocumentReference();
        String reasonToRemoveApplicationForm = "Confidential information disclosed.";
        CaseData caseData = CaseData.builder()
            .removalToolData(RemovalToolData.builder()
                .removableType(APPLICATION)
                .reasonToRemoveApplicationForm(reasonToRemoveApplicationForm)
                .build())
            .c110A(C110A.builder()
                .submittedForm(c110a)
                .build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getC110A().getDocument()).isNull();
        assertThat(responseData.getRemovalToolData().getHiddenApplicationForm().getSubmittedForm()).isEqualTo(c110a);
        assertThat(responseData.getRemovalToolData().getHiddenApplicationForm().getRemovalReason())
            .isEqualTo(reasonToRemoveApplicationForm);
    }

    //@Test
    void shouldRemoveC1ApplicationFormAndSupplement() {
        DocumentReference c1 = testDocumentReference();
        DocumentReference supplement = testDocumentReference();
        String reasonToRemoveApplicationForm = "Confidential information disclosed.";
        CaseData caseData = CaseData.builder()
            .removalToolData(RemovalToolData.builder()
                .removableType(APPLICATION)
                .reasonToRemoveApplicationForm(reasonToRemoveApplicationForm)
                .build())
            .c110A(C110A.builder()
                .submittedForm(c1)
                .supplementDocument(supplement)
                .build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getC110A().getDocument()).isNull();
        assertThat(responseData.getRemovalToolData().getHiddenApplicationForm().getSubmittedForm()).isEqualTo(c1);
        assertThat(responseData.getRemovalToolData().getHiddenApplicationForm().getSubmittedSupplement())
            .isEqualTo(supplement);
        assertThat(responseData.getRemovalToolData().getHiddenApplicationForm().getRemovalReason())
            .isEqualTo(reasonToRemoveApplicationForm);
    }

    //@Test
    void shouldErrorIfApplicationFormHasAlreadyBeenRemoved() {
        CaseData caseData = CaseData.builder()
            .c110A(C110A.builder()
                .submittedForm(null)
                .build())
            .removalToolData(RemovalToolData.builder()
                .removableType(APPLICATION)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        assertThat(response.getErrors()).containsExactly(APPLICATION_FORM_ALREADY_REMOVED_ERROR_MESSAGE);
    }

    //@Test
    void shouldRemoveSentDocument() {
        final String PARTY_NAME_1 = "Peter Pan";
        final String PARTY_NAME_2 = "Mickey Donald";

        DocumentReference file = DocumentReference.builder()
            .filename("file.pdf")
            .build();
        DocumentReference randomFile = DocumentReference.builder()
            .filename("randomFile.pdf")
            .build();

        final UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");

        final Element<SentDocument> removedTarget = element(id, SentDocument.builder()
            .partyName(PARTY_NAME_2)
            .document(file)
            .sentAt("04 JUNE 2020")
            .letterId("LETTER11-1111-1111-1111-111111111111")
            .build());

        List<Element<SentDocument>> sentDocumentsForPartyOne = new ArrayList<>();
        sentDocumentsForPartyOne.add(element(SentDocument.builder()
            .partyName(PARTY_NAME_1)
            .document(randomFile)
            .sentAt("12 June 2020")
            .build()));

        List<Element<SentDocument>> sentDocumentsForPartyTwo = new ArrayList<>();
        sentDocumentsForPartyTwo.add(removedTarget);

        List<Element<SentDocuments>> documentsSentToParties = List.of(
            element(SentDocuments.builder()
                .partyName(PARTY_NAME_1)
                .documentsSentToParty(sentDocumentsForPartyOne)
                .build()),
            element(SentDocuments.builder()
                .partyName(PARTY_NAME_2)
                .documentsSentToParty(sentDocumentsForPartyTwo)
                .build())
        );

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code("11111111-1111-1111-1111-111111111111")
                .label(PARTY_NAME_2 + " - file.pdf (04 JUNE 1989)")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .documentsSentToParties(documentsSentToParties)
            .removalToolData(RemovalToolData.builder()
                .removableType(SENT_DOCUMENT)
                .removableSentDocumentList(dynamicList)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        CaseData responseData = extractCaseData(response);
        assertThat(responseData.getDocumentsSentToParties()).hasSize(1);
        assertThat(responseData.getDocumentsSentToParties().get(0).getValue().getDocumentsSentToParty())
            .hasSize(1);
    }

    private CaseData buildCaseData(Element<GeneratedOrder> order) {
        return CaseData.builder()
            .orderCollection(List.of(order))
            .removalToolData(RemovalToolData.builder()
                .removableType(ORDER)
                .removableOrderList(buildRemovableOrderList(order.getId()))
                .reasonToRemoveOrder(REASON)
                .build())
            .build();
    }

    private DynamicList buildRemovableOrderList(UUID id) {
        return DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(id)
                .label("order - 12 March 1234")
                .build())
            .listItems(List.of(DynamicListElement.builder()
                .code(id)
                .label("order - 12 March 1234")
                .build()))
            .build();
    }

    private GeneratedOrder buildOrder() {
        return GeneratedOrder.builder()
            .type("Blank order (C21)")
            .title("order")
            .dateOfIssue("12 March 1234")
            .build();
    }

    private GeneratedOrder buildOrder(GeneratedOrderType type, List<Element<Child>> children) {
        return GeneratedOrder.builder()
            .type(getFullOrderType(type))
            .children(children)
            .build();
    }

    private DynamicListElement buildListElement(UUID id, String label) {
        return DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();
    }

    private AdditionalApplicationsBundle buildCombinedApplication(OtherApplicationType type, String date) {
        return AdditionalApplicationsBundle.builder()
            .uploadedDateTime(date)
            .c2DocumentBundle(C2DocumentBundle.builder()
                .document(testDocumentReference())
                .uploadedDateTime(date)
                .build())
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                .document(testDocumentReference())
                .applicationType(type)
                .uploadedDateTime(date)
                .build())
            .build();
    }
}
