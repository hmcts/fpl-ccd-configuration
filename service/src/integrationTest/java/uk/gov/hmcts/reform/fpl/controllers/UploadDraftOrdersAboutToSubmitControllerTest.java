package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.orders.UploadDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.CMOType;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(UploadDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadDraftOrdersAboutToSubmitControllerTest extends AbstractUploadDraftOrdersControllerTest {

    private static final DocumentReference DOCUMENT_REFERENCE = testDocumentReference();

    @MockBean
    private ManageDocumentService manageDocumentService;

    UploadDraftOrdersAboutToSubmitControllerTest() {
        super();
    }

    @BeforeEach
    void before() {
        when(manageDocumentService.getUploaderType(any())).thenReturn(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY);
        when(manageDocumentService.getUploaderCaseRoles(any())).thenReturn(List.of(CaseRole.LASOLICITOR));
    }

    @Test
    void shouldAddCMOToListWithDraftStatusAndNotMigrateDocs() {

        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(now().plusDays(3));

        UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
            .hearingOrderDraftKind(List.of(CMO))
            .futureHearingsForCMO(dynamicList(hearings))
            .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
            .cmoUploadType(CMOType.DRAFT)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(eventData)
            .hearingDetails(hearings)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        HearingOrder cmo = orderWithDocs(hearings.get(0).getValue(), HearingOrderType.DRAFT_CMO, DRAFT, null,
            hearings.get(0).getId());

        List<Element<HearingOrder>> unsealedCMOs = responseData.getDraftUploadedCMOs();

        assertThat(unsealedCMOs)
            .hasSize(1)
            .first()
            .extracting(Element::getValue)
            .isEqualTo(cmo);

        hearings.get(0).getValue().setCaseManagementOrderId(unsealedCMOs.get(0).getId());

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);
    }

    @Test
    void shouldAddCMOToListWithSendToJudgeStatusAndMigrateDocs() {
        givenCurrentUser(UserDetails.builder()
            .email("Test LA")
            .roles(List.of("caseworker-publiclaw-solicitor"))
            .build());

        UUID bundleId = UUID.randomUUID();

        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(now().minusDays(3));

        UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
            .hearingOrderDraftKind(List.of(CMO))
            .pastHearingsForCMO(dynamicList(hearings))
            .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
            .cmoUploadType(CMOType.AGREED)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(eventData)
            .hearingDetails(hearings)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        HearingOrder cmo = orderWithDocs(hearings.get(0).getValue(), AGREED_CMO, SEND_TO_JUDGE, null,
            hearings.get(0).getId());

        List<Element<HearingOrder>> unsealedCMOs = responseData.getDraftUploadedCMOs();

        assertThat(unsealedCMOs)
            .hasSize(1)
            .first()
            .extracting(Element::getValue)
            .isEqualTo(cmo);

        hearings.get(0).getValue().setCaseManagementOrderId(unsealedCMOs.get(0).getId());

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);
    }

    @Test
    void shouldReplaceSupportingDocumentInDraftCMOWhenANewSupportingDocumentIsUploaded() {
        UUID cmoId = UUID.randomUUID();

        Element<SupportingEvidenceBundle> existingSupportingDoc = element(
            SupportingEvidenceBundle.builder()
                .name("case summary doc1")
                .build()
        );

        List<Element<HearingBooking>> hearings = List.of(hearingWithCMOId(now().plusDays(3), cmoId));
        Element<HearingOrder> cmoElement = element(cmoId,
            orderWithDocs(hearings.get(0).getValue(), HearingOrderType.DRAFT_CMO, DRAFT,
                newArrayList(existingSupportingDoc), hearings.get(0).getId()));

        UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
            .previousCMO(cmoElement.getValue().getOrder())
            .hearingOrderDraftKind(List.of(CMO))
            .futureHearingsForCMO(dynamicLists.from(0,
                Pair.of(hearings.get(0).getValue().toLabel(), hearings.get(0).getId())))
            .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
            .cmoUploadType(CMOType.DRAFT)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(eventData)
            .hearingDetails(hearings)
            .draftUploadedCMOs(newArrayList(cmoElement))
            .hearingOrdersBundlesDrafts(List.of(element(UUID.randomUUID(), HearingOrdersBundle.builder()
                .hearingName(hearings.get(0).getValue().toLabel())
                .orders(newArrayList(cmoElement))
                .build())))
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        HearingOrder cmo = orderWithDocs(
            hearings.get(0).getValue(), HearingOrderType.DRAFT_CMO, DRAFT,
            null, hearings.get(0).getId());

        List<Element<HearingOrder>> unsealedCMOs = responseData.getDraftUploadedCMOs();

        assertThat(unsealedCMOs)
            .hasSize(1)
            .first()
            .extracting(Element::getValue)
            .isEqualTo(cmo);

        hearings.get(0).getValue().setCaseManagementOrderId(unsealedCMOs.get(0).getId());

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);
    }

    @Test
    void shouldRemoveTemporaryFields() {
        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2050, 3, 15, 10, 7));
        List<Element<HearingOrder>> draftCMOs = List.of();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(CMO))
                .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
                .futureHearingsForCMO(dynamicList(hearings))
                .cmoJudgeInfo("DUMMY DATA")
                .cmoHearingInfo("DUMMY DATA")
                .showReplacementCMO(YesNo.NO)
                .replacementCMO(DOCUMENT_REFERENCE)
                .previousCMO(DOCUMENT_REFERENCE)
                .cmoToSend(DOCUMENT_REFERENCE)
                .showCMOsSentToJudge(YesNo.NO)
                .cmosSentToJudge("DUMMY DATA")
                .cmoUploadType(CMOType.DRAFT)
                .build())
            .hearingDetails(hearings)
            .draftUploadedCMOs(draftCMOs)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        Set<String> keys = new HashSet<>(
            mapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {
            }).keySet());

        keys.removeAll(List.of(
            "showCMOsSentToJudge", "cmosSentToJudge", "cmoUploadType", "pastHearingsForCMO", "futureHearingsForCMO",
            "cmoHearingInfo", "showReplacementCMO", "previousCMO", "uploadedCaseManagementOrder", "replacementCMO",
            "cmoSupportingDocs", "cmoJudgeInfo", "cmoToSend", "hearingsForHearingOrderDrafts",
            "currentHearingOrderDrafts", "hearingOrderDraftKind",
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
        ));

        assertThat(response.getData().keySet()).isEqualTo(keys);
    }

    @Test
    void shouldSetWATaskFieldIfAgreedCMOUploaded() {
        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2020, 3, 15, 10, 7));
        List<Element<HearingBooking>> futureHearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2050, 3, 15, 10, 7));
        List<Element<HearingBooking>> allHearings = Stream.of(hearings, futureHearings)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        List<Element<HearingOrder>> draftCMOs = List.of();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(CMO))
                .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
                .pastHearingsForCMO(dynamicList(hearings))
                .futureHearingsForCMO(dynamicList(futureHearings))
                .cmoJudgeInfo("DUMMY DATA")
                .cmoHearingInfo("DUMMY DATA")
                .showReplacementCMO(YesNo.NO)
                .replacementCMO(DOCUMENT_REFERENCE)
                .previousCMO(DOCUMENT_REFERENCE)
                .cmoToSend(DOCUMENT_REFERENCE)
                .showCMOsSentToJudge(YesNo.NO)
                .cmosSentToJudge("DUMMY DATA")
                .cmoUploadType(CMOType.AGREED).build())
            .hearingDetails(allHearings)
            .draftUploadedCMOs(draftCMOs)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        assertThat(response.getData())
            .extracting("draftOrderNeedsReviewUploaded")
            .isEqualTo("Yes");
    }

    @Test
    void shouldSetWATaskFieldIfC21Uploaded() {
        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2020, 3, 15, 10, 7));
        List<Element<HearingBooking>> futureHearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2050, 3, 15, 10, 7));
        List<Element<HearingBooking>> allHearings = Stream.of(hearings, futureHearings)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        List<Element<HearingOrder>> draftOrders = List.of();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(C21))
                .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
                .pastHearingsForCMO(dynamicList(hearings))
                .futureHearingsForCMO(dynamicList(futureHearings))
                .cmoToSend(DOCUMENT_REFERENCE)
                .currentHearingOrderDrafts(draftOrders)
                .showReplacementCMO(YesNo.NO)
                .build())
            .hearingDetails(allHearings)
            .draftUploadedCMOs(draftOrders)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        assertThat(response.getData())
            .extracting("draftOrderNeedsReviewUploaded")
            .isEqualTo("Yes");
    }

    @Test
    void shouldSetWATaskFieldToNoIfDraftCMOUploaded() {
        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2020, 3, 15, 10, 7));
        List<Element<HearingBooking>> futureHearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2050, 3, 15, 10, 7));
        List<Element<HearingBooking>> allHearings = Stream.of(hearings, futureHearings)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        List<Element<HearingOrder>> draftCMOs = List.of();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(CMO))
                .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
                .pastHearingsForCMO(dynamicList(hearings))
                .futureHearingsForCMO(dynamicList(futureHearings))
                .cmoJudgeInfo("DUMMY DATA")
                .cmoHearingInfo("DUMMY DATA")
                .showReplacementCMO(YesNo.NO)
                .replacementCMO(DOCUMENT_REFERENCE)
                .previousCMO(DOCUMENT_REFERENCE)
                .cmoToSend(DOCUMENT_REFERENCE)
                .showCMOsSentToJudge(YesNo.NO)
                .cmosSentToJudge("DUMMY DATA")
                .cmoUploadType(CMOType.DRAFT).build())
            .hearingDetails(allHearings)
            .draftUploadedCMOs(draftCMOs)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        assertThat(response.getData())
            .extracting("draftOrderNeedsReviewUploaded")
            .isEqualTo("No");

    }

    private HearingOrder orderWithDocs(HearingBooking hearing, HearingOrderType type, CMOStatus status,
                                       List<Element<SupportingEvidenceBundle>> supportingDocs, UUID hearingId) {
        return HearingOrder.builder()
            .type(type)
            .title(type == AGREED_CMO ? "Agreed CMO discussed at hearing" : "Draft CMO from advocates' meeting")
            .status(status)
            .hearing(hearing.toLabel())
            .hearingId(hearingId)
            .order(DOCUMENT_REFERENCE)
            .dateSent(dateNow())
            .judgeTitleAndName(formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()))
            .supportingDocs(supportingDocs)
            .uploaderType(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY)
            .uploaderCaseRoles(List.of(CaseRole.LASOLICITOR))
            .build();
    }

    private DynamicList dynamicList(List<Element<HearingBooking>> hearings) {
        return dynamicLists.from(0,
            Pair.of(hearings.get(0).getValue().toLabel(), hearings.get(0).getId()),
            Pair.of(hearings.get(1).getValue().toLabel(), hearings.get(1).getId())
        );
    }

    private List<Element<HearingBooking>> hearingsOnDateAndDayAfter(LocalDateTime startDate) {
        return List.of(
            hearing(startDate),
            hearing(startDate.plusDays(1))
        );
    }
}
