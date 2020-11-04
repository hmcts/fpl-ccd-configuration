package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.CMOType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.cmo.AgreedCMOUploaded;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftCMOUploaded;
import uk.gov.hmcts.reform.fpl.events.cmo.UploadCMOEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.UploadCMOEventData;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FURTHER_CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class UploadCMOServiceTest {

    private static final DocumentReference DOCUMENT = DocumentReference.builder().build();

    private final Time time = new FixedTimeConfiguration().stoppedTime();
    private final UploadCMOService service = new UploadCMOService(new ObjectMapper(), time);

    @Test
    void shouldAddHearingTextThatHaveCMOsBeingReviewedByJudge() {
        List<Element<CaseManagementOrder>> unsealedCMOs = List.of(
            element(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build()),
            element(CaseManagementOrder.builder().status(DRAFT).build())
        );

        List<Element<HearingBooking>> hearings = List.of(
            element(hearing(CASE_MANAGEMENT, LocalDateTime.of(2020, 2, 1, 11, 30), unsealedCMOs.get(0).getId())),
            element(hearing(CASE_MANAGEMENT, time.now().plusDays(2), unsealedCMOs.get(1).getId())),
            element(hearing(CASE_MANAGEMENT, time.now().plusDays(3)))
        );

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .draftUploadedCMOs(unsealedCMOs)
            .build();

        UploadCMOEventData pageData = service.getInitialPageData(caseData);

        assertThat(pageData.getShowCMOsSentToJudge()).isEqualTo(YesNo.YES);
        assertThat(pageData.getCmosSentToJudge()).isEqualTo("Case management hearing, 1 February 2020");
    }

    @Test
    void shouldNotAddHearingTextWhenNoCMOsBeingReviewedByJudge() {
        List<Element<HearingBooking>> hearings = List.of(
            element(hearing(CASE_MANAGEMENT, LocalDateTime.of(2020, 2, 1, 11, 30)))
        );

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .build();

        UploadCMOEventData pageData = service.getInitialPageData(caseData);

        assertThat(pageData.getShowCMOsSentToJudge()).isEqualTo(YesNo.NO);
        assertThat(pageData.getCmosSentToJudge()).isNullOrEmpty();
    }

    @Test
    void shouldBuildDynamicListsFromHearings() {
        List<Element<HearingBooking>> hearings = new ArrayList<>(hearings());

        hearings.add(element(hearing(CASE_MANAGEMENT, LocalDateTime.of(3000, 12, 3, 11, 32))));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .build();

        UploadCMOEventData pageData = service.getInitialPageData(caseData);

        DynamicList pastList = dynamicList(hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId());
        DynamicList futureList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(DynamicListElement.builder()
                .code(hearings.get(3).getId())
                .label("Case management hearing, 3 December 3000")
                .build()))
            .build();

        assertThat(pageData.getPastHearingsForCMO()).isEqualTo(pastList);
        assertThat(pageData.getFutureHearingsForCMO()).isEqualTo(futureList);
    }

    @Test
    void shouldPullHearingInfoForNewCMO() {
        List<Element<HearingBooking>> hearings = hearings();

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(
                dynamicList(hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId(), true)
            )
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .uploadCMOEventData(eventData)
            .build();

        UploadCMOEventData cmoInfo = service.getCMOInfo(caseData);
        UploadCMOEventData expectedInfo = UploadCMOEventData.builder()
            .showReplacementCMO(YesNo.NO)
            .cmoHearingInfo("Case management hearing, 2 March 2020")
            .build();

        assertThat(cmoInfo).isEqualTo(expectedInfo);
    }

    @Test
    void shouldPullExistingInfoWhenDraftCMOAlreadyExisted() {
        List<Element<SupportingEvidenceBundle>> bundle = List.of(
            element(SupportingEvidenceBundle.builder().name("case summary").build())
        );

        List<Element<CaseManagementOrder>> unsealedCMOs = List.of(
            element(CaseManagementOrder.builder().status(DRAFT).order(DOCUMENT).supportingDocs(bundle).build())
        );

        List<Element<HearingBooking>> hearings = hearings();

        hearings.get(0).getValue().setCaseManagementOrderId(unsealedCMOs.get(0).getId());

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(
                dynamicList(hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId(), true)
            )
            .build();

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(unsealedCMOs)
            .hearingDetails(hearings)
            .uploadCMOEventData(eventData)
            .build();

        UploadCMOEventData cmoInfo = service.getCMOInfo(caseData);
        UploadCMOEventData expectedInfo = UploadCMOEventData.builder()
            .showReplacementCMO(YesNo.YES)
            .previousCMO(DOCUMENT)
            .cmoHearingInfo("Case management hearing, 2 March 2020")
            .cmoSupportingDocs(bundle)
            .build();

        assertThat(cmoInfo).isEqualTo(expectedInfo);
    }

    @Test
    void shouldRegenerateDynamicListsIfIdsPassedAsStrings() {
        Element<HearingBooking> futureHearing = element(hearing(CASE_MANAGEMENT,
            LocalDateTime.of(3000, 12, 3, 11, 32)));
        List<Element<HearingBooking>> hearings = new ArrayList<>(hearings());
        hearings.add(futureHearing);

        UUID pastHearingId = hearings.get(0).getId();
        UUID futureHearingId = futureHearing.getId();

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(pastHearingId.toString())
            .futureHearingsForCMO(futureHearingId.toString())
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .uploadCMOEventData(eventData)
            .build();

        UploadCMOEventData cmoInfo = service.getCMOInfo(caseData);

        DynamicList pastList = dynamicList(
            hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId(), true
        );

        DynamicListElement futureElement = DynamicListElement.builder()
            .code(futureHearingId)
            .label("Case management hearing, 3 December 3000")
            .build();

        DynamicList futureList = DynamicList.builder().value(futureElement).listItems(List.of(futureElement)).build();

        assertThat(cmoInfo)
            .extracting(UploadCMOEventData::getPastHearingsForCMO, UploadCMOEventData::getFutureHearingsForCMO)
            .containsOnly(pastList, futureList);
    }

    @Test
    void shouldPullJudgeAndUploadedDocForReview() {
        List<Element<HearingBooking>> hearings = hearings();
        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .uploadedCaseManagementOrder(DOCUMENT)
            .pastHearingsForCMO(dynamicList(
                hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId(), true
            ))
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .uploadCMOEventData(eventData)
            .build();

        UploadCMOEventData reviewData = service.getReviewData(caseData);

        UploadCMOEventData expectedData = UploadCMOEventData.builder()
            .cmoToSend(DOCUMENT)
            .cmoJudgeInfo("His Honour Judge Dredd")
            .build();

        assertThat(reviewData).isEqualTo(expectedData);
    }

    @Test
    void shouldPullReplacementDocumentWhenUploadedFieldIsNull() {
        List<Element<HearingBooking>> hearings = hearings();

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .replacementCMO(DOCUMENT)
            .pastHearingsForCMO(dynamicList(
                hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId(), true
            ))
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .uploadCMOEventData(eventData)
            .build();

        UploadCMOEventData reviewData = service.getReviewData(caseData);

        UploadCMOEventData expectedData = UploadCMOEventData.builder()
            .cmoToSend(DOCUMENT)
            .cmoJudgeInfo("His Honour Judge Dredd")
            .build();

        assertThat(reviewData).isEqualTo(expectedData);
    }

    @Test
    void shouldPullPreviousDocumentWhenReplacementAndMainAndUploadedFileFieldAreNull() {
        UUID orderID = UUID.randomUUID();
        List<Element<HearingBooking>> hearings = hearings();

        hearings.get(0).getValue().setCaseManagementOrderId(orderID);

        List<Element<CaseManagementOrder>> unsealedOrders = List.of(
            element(orderID, CaseManagementOrder.builder().order(DOCUMENT).status(DRAFT).build())
        );

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(dynamicList(
                hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId(), true
            ))
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .uploadCMOEventData(eventData)
            .draftUploadedCMOs(unsealedOrders)
            .build();

        UploadCMOEventData reviewData = service.getReviewData(caseData);

        UploadCMOEventData expectedData = UploadCMOEventData.builder()
            .cmoToSend(DOCUMENT)
            .cmoJudgeInfo("His Honour Judge Dredd")
            .build();

        assertThat(reviewData).isEqualTo(expectedData);
    }

    @Test
    void shouldAddNewCMOToListAndUpdateHearingIfCMOWasNotAlreadyInList() {
        List<Element<HearingBooking>> hearings = hearings();

        List<Element<SupportingEvidenceBundle>> bundle = List.of(
            element(SupportingEvidenceBundle.builder().name("name").build())
        );

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .uploadedCaseManagementOrder(DOCUMENT)
            .pastHearingsForCMO(dynamicList(
                hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId(), true)
            )
            .cmoSupportingDocs(bundle)
            .cmoUploadType(CMOType.DRAFT)
            .build();

        List<Element<CaseManagementOrder>> unsealedOrders = new ArrayList<>();
        List<Element<HearingFurtherEvidenceBundle>> bundles = new ArrayList<>();

        service.updateHearingsAndOrders(eventData, hearings, unsealedOrders, bundles);

        assertThat(bundles).isEmpty();

        assertThat(unsealedOrders).hasSize(1)
            .first()
            .extracting(Element::getValue)
            .isEqualTo(CaseManagementOrder.builder()
                .supportingDocs(bundle)
                .judgeTitleAndName("His Honour Judge Dredd")
                .hearing("Case management hearing, 2 March 2020")
                .dateSent(time.now().toLocalDate())
                .order(DOCUMENT)
                .status(DRAFT)
                .build());

        assertThat(hearings).hasSize(3)
            .first()
            .extracting(hearing -> hearing.getValue().getCaseManagementOrderId())
            .isEqualTo(unsealedOrders.get(0).getId());
    }

    @Test
    void shouldMigrateBundleWhenUploadedCMOIsAgreed() {
        List<Element<HearingBooking>> hearings = hearings();

        List<Element<SupportingEvidenceBundle>> bundle = List.of(
            element(SupportingEvidenceBundle.builder().name("name").build())
        );

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(dynamicList(
                hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId(), true
            ))
            .uploadedCaseManagementOrder(DOCUMENT)
            .cmoSupportingDocs(bundle)
            .cmoUploadType(CMOType.AGREED)
            .build();

        List<Element<CaseManagementOrder>> unsealedOrders = new ArrayList<>();
        List<Element<HearingFurtherEvidenceBundle>> bundles = new ArrayList<>();

        service.updateHearingsAndOrders(eventData, hearings, unsealedOrders, bundles);

        assertThat(bundles).hasSize(1)
            .isEqualTo(List.of(element(
                hearings.get(0).getId(),
                HearingFurtherEvidenceBundle.builder()
                    .hearingName("Case management hearing, 2 March 2020")
                    .supportingEvidenceBundle(bundle)
                    .build()
            )));
    }

    @Test
    void shouldUpdateExistingCMOWithNewOrderAndChangeStatus() {
        List<Element<HearingBooking>> hearings = hearings();
        List<Element<CaseManagementOrder>> unsealedOrders = new ArrayList<>();
        Element<CaseManagementOrder> oldOrder = element(CaseManagementOrder.builder().status(RETURNED).build());

        unsealedOrders.add(oldOrder);
        unsealedOrders.add(element(CaseManagementOrder.builder().build()));

        hearings.get(0).getValue().setCaseManagementOrderId(unsealedOrders.get(0).getId());

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .cmoUploadType(CMOType.AGREED)
            .pastHearingsForCMO(dynamicList(
                hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId(), true
            ))
            .uploadedCaseManagementOrder(DOCUMENT)
            .build();

        service.updateHearingsAndOrders(eventData, hearings, unsealedOrders, List.of());

        CaseManagementOrder expectedOrder = CaseManagementOrder.builder()
            .status(SEND_TO_JUDGE)
            .dateSent(time.now().toLocalDate())
            .order(DOCUMENT)
            .hearing("Case management hearing, 2 March 2020")
            .judgeTitleAndName("His Honour Judge Dredd")
            .supportingDocs(List.of())
            .build();

        assertThat(unsealedOrders).hasSize(2)
            .first()
            .extracting(Element::getValue)
            .isNotEqualTo(oldOrder.getValue())
            .isEqualTo(expectedOrder);

        assertThat(hearings).hasSize(3)
            .first()
            .extracting(hearing -> hearing.getValue().getCaseManagementOrderId())
            .isNotEqualTo(oldOrder.getId())
            .isEqualTo(unsealedOrders.get(0).getId());
    }

    @Test
    void shouldBuildAgreedEventWhenNewCMOIsAgreed() {
        List<Element<CaseManagementOrder>> unsealedOrders = List.of(
            element(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build())
        );

        List<Element<HearingBooking>> hearingsBefore = new ArrayList<>(hearings());

        CaseData caseDataBefore = CaseData.builder()
            .hearingDetails(hearingsBefore)
            .build();

        List<Element<HearingBooking>> hearingsAfter = new ArrayList<>(hearingsBefore);

        HearingBooking updatedHearing = hearingsAfter.get(0).getValue().toBuilder()
            .caseManagementOrderId(unsealedOrders.get(0).getId())
            .build();
        hearingsAfter.set(0, element(hearingsAfter.get(0).getId(), updatedHearing));


        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingsAfter)
            .draftUploadedCMOs(unsealedOrders)
            .build();

        UploadCMOEvent event = service.buildEventToPublish(caseData, caseDataBefore);

        assertThat(event).isEqualTo(new AgreedCMOUploaded(caseData, updatedHearing));
    }

    @Test
    void shouldBuildDraftEventWhenNewCMOIsDraft() {
        List<Element<CaseManagementOrder>> unsealedOrders = List.of(
            element(CaseManagementOrder.builder().status(DRAFT).build())
        );

        List<Element<HearingBooking>> hearingsBefore = new ArrayList<>(hearings());

        CaseData caseDataBefore = CaseData.builder()
            .hearingDetails(hearingsBefore)
            .build();

        List<Element<HearingBooking>> hearingsAfter = new ArrayList<>(hearingsBefore);

        HearingBooking updatedHearing = hearingsAfter.get(0).getValue().toBuilder()
            .caseManagementOrderId(unsealedOrders.get(0).getId())
            .build();
        hearingsAfter.set(0, element(hearingsAfter.get(0).getId(), updatedHearing));


        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingsAfter)
            .draftUploadedCMOs(unsealedOrders)
            .build();

        UploadCMOEvent event = service.buildEventToPublish(caseData, caseDataBefore);

        assertThat(event).isEqualTo(new DraftCMOUploaded(caseData, updatedHearing));
    }

    private DynamicList dynamicList(UUID uuid1, UUID uuid2, UUID uuid3, DynamicListElement... additional) {
        return dynamicList(uuid1, uuid2, uuid3, false, additional);
    }

    private DynamicList dynamicList(UUID uuid1, UUID uuid2, UUID uuid3, boolean withValue,
                                    DynamicListElement... additional) {
        DynamicListElement value;
        if (withValue) {
            value = DynamicListElement.builder()
                .code(uuid1)
                .label("Case management hearing, 2 March 2020")
                .build();
        } else {
            value = DynamicListElement.EMPTY;
        }

        List<DynamicListElement> listItems = new ArrayList<>(List.of(
            DynamicListElement.builder()
                .code(uuid1)
                .label("Case management hearing, 2 March 2020")
                .build(),
            DynamicListElement.builder()
                .code(uuid2)
                .label("Further case management hearing, 7 March 2020")
                .build(),
            DynamicListElement.builder()
                .code(uuid3)
                .label("Final hearing, 12 March 2020")
                .build()
        ));

        listItems.addAll(Arrays.asList(additional));

        return DynamicList.builder()
            .value(value)
            .listItems(listItems)
            .build();
    }

    private List<Element<HearingBooking>> hearings() {
        LocalTime time = LocalTime.now();
        return List.of(
            element(hearing(CASE_MANAGEMENT, LocalDateTime.of(LocalDate.of(2020, 3, 2), time))),
            element(hearing(FURTHER_CASE_MANAGEMENT, LocalDateTime.of(LocalDate.of(2020, 3, 7), time))),
            element(hearing(FINAL, LocalDateTime.of(LocalDate.of(2020, 3, 12), time)))
        );
    }

    private HearingBooking hearing(HearingType type, LocalDateTime startDate) {
        return hearing(type, startDate, null);
    }

    private HearingBooking hearing(HearingType type, LocalDateTime startDate, UUID cmoId) {
        return HearingBooking.builder()
            .type(type)
            .startDate(startDate)
            .caseManagementOrderId(cmoId)
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor())
            .build();
    }

    private JudgeAndLegalAdvisor judgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Dredd")
            .build();
    }
}
