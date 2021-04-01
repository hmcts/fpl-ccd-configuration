package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.CMOType;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderKind;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FURTHER_CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.DEFAULT_CODE;
import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.defaultListItem;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class DraftOrderServiceTest {

    private final Time time = new FixedTimeConfiguration().stoppedTime();

    private DraftOrderService service;

    @Mock
    private DocumentUploadHelper documentUploadHelper;

    @BeforeEach
    void init() {
        service = new DraftOrderService(new ObjectMapper(), time, documentUploadHelper);
    }

    @Nested
    class InitialData {

        @Test
        void shouldAddHearingTextThatHaveCMOsBeingReviewedByJudge() {
            List<Element<HearingOrder>> unsealedCMOs = newArrayList(
                element(HearingOrder.builder().status(SEND_TO_JUDGE).build()),
                element(HearingOrder.builder().status(DRAFT).build()));

            List<Element<HearingBooking>> hearings = newArrayList(
                element(hearing(CASE_MANAGEMENT, LocalDateTime.of(2020, 2, 1, 11, 30), unsealedCMOs.get(0).getId())),
                element(hearing(CASE_MANAGEMENT, time.now().plusDays(2), unsealedCMOs.get(1).getId())),
                element(hearing(CASE_MANAGEMENT, time.now().plusDays(3))));

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .draftUploadedCMOs(unsealedCMOs)
                .build();

            UploadDraftOrdersData pageData = service.getInitialData(caseData);

            assertThat(pageData.getShowCMOsSentToJudge()).isEqualTo(YES);
            assertThat(pageData.getCmosSentToJudge()).isEqualTo("Case management hearing, 1 February 2020");
        }

        @Test
        void shouldNotAddHearingTextWhenNoCMOsBeingReviewedByJudge() {
            List<Element<HearingBooking>> hearings = newArrayList(
                element(hearing(CASE_MANAGEMENT, LocalDateTime.of(2020, 2, 1, 11, 30))));

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .build();

            UploadDraftOrdersData pageData = service.getInitialData(caseData);

            assertThat(pageData.getShowCMOsSentToJudge()).isEqualTo(NO);
            assertThat(pageData.getCmosSentToJudge()).isNullOrEmpty();
        }

        @Test
        void shouldBuildDynamicListsFromHearings() {
            List<Element<HearingBooking>> hearings = hearings();

            hearings.add(element(hearing(CASE_MANAGEMENT, LocalDateTime.of(3000, 12, 3, 11, 32))));

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .build();

            UploadDraftOrdersData eventData = service.getInitialData(caseData);

            DynamicList pastHearingList = dynamicList(List.of(hearings.get(0), hearings.get(1)));
            DynamicList futureHearingList = dynamicList(List.of(hearings.get(2)));
            DynamicList allHearings = dynamicList(hearings, defaultListItem("No hearing"));

            assertThat(eventData.getPastHearingsForCMO()).isEqualTo(pastHearingList);
            assertThat(eventData.getFutureHearingsForCMO()).isEqualTo(futureHearingList);
            assertThat(eventData.getHearingsForHearingOrderDrafts()).isEqualTo(allHearings);
        }

        @Test
        void shouldNotSetDefaultHearingOrderKind() {
            UploadDraftOrdersData eventData = service.getInitialData(CaseData.builder().build());

            assertThat(eventData.getHearingOrderDraftKind()).isEmpty();
        }
    }

    @Nested
    class DraftsInfo {

        @Test
        void shouldPullHearingInfoForNewCMO() {
            List<Element<HearingBooking>> hearings = hearings();

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .pastHearingsForCMO(dynamicList(hearings.get(0).getId(), hearings))
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .build();

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .uploadDraftOrdersEventData(eventData)
                .build();

            UploadDraftOrdersData cmoInfo = service.getDraftsInfo(caseData);

            assertThat(cmoInfo.getShowReplacementCMO()).isEqualTo(NO);
            assertThat(cmoInfo.getCmoHearingInfo()).isEqualTo("Case management hearing, 2 March 2020");
        }

        @Test
        void shouldPullExistingInfoWhenDraftCMOAlreadyExisted() {
            List<Element<SupportingEvidenceBundle>> bundle = List.of(
                element(SupportingEvidenceBundle.builder().name("case summary").build())
            );

            Element<HearingOrder> previousCmo = element(HearingOrder.builder()
                .status(DRAFT)
                .order(testDocumentReference())
                .supportingDocs(bundle).build());

            List<Element<HearingBooking>> hearings = hearings();

            hearings.get(0).getValue().setCaseManagementOrderId(previousCmo.getId());

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .pastHearingsForCMO(dynamicList(hearings.get(0).getId(), hearings))
                .cmoSupportingDocs(bundle)
                .build();

            CaseData caseData = CaseData.builder()
                .draftUploadedCMOs(List.of(previousCmo))
                .hearingDetails(hearings)
                .uploadDraftOrdersEventData(eventData)
                .build();

            UploadDraftOrdersData cmoInfo = service.getDraftsInfo(caseData);

            UploadDraftOrdersData expectedInfo = eventData.toBuilder()
                .showReplacementCMO(YES)
                .previousCMO(previousCmo.getValue().getOrder())
                .cmoToSend(previousCmo.getValue().getOrder())
                .cmoHearingInfo("Case management hearing, 2 March 2020")
                .cmoJudgeInfo("His Honour Judge Dredd")
                .cmoSupportingDocs(bundle)
                .pastHearingsForCMO(dynamicList(hearings.get(0).getId(), hearings))
                .futureHearingsForCMO(dynamicList(emptyList()))
                .hearingsForHearingOrderDrafts(dynamicList(hearings, defaultListItem("No hearing")))
                .build();

            assertThat(cmoInfo).isEqualTo(expectedInfo);
        }

        @Test
        void shouldPullExistingInfoAndReplaceDocumentsWhenNewDocumentsAreUploaded() {
            Element<SupportingEvidenceBundle> supportingDoc = element(
                SupportingEvidenceBundle.builder().name("case summary").build());
            Element<SupportingEvidenceBundle> newSupportingDoc = element(
                SupportingEvidenceBundle.builder().name("new support document").build());

            DocumentReference previousOrder = testDocumentReference();
            DocumentReference newOrder = testDocumentReference();

            Element<HearingOrder> previousCmo = element(HearingOrder.builder()
                .status(DRAFT)
                .order(previousOrder)
                .supportingDocs(List.of(supportingDoc)).build());

            List<Element<HearingBooking>> futureHearings = wrapElements(
                hearing(CASE_MANAGEMENT, LocalDateTime.of(2050, 3, 2, 0, 0, 0), previousCmo.getId()),
                hearing(CASE_MANAGEMENT, LocalDateTime.of(2050, 3, 7, 0, 0, 0)));

            futureHearings.get(0).getValue().setCaseManagementOrderId(previousCmo.getId());

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .futureHearingsForCMO(dynamicList(futureHearings.get(0).getId(), futureHearings))
                .cmoSupportingDocs(List.of(newSupportingDoc))
                .cmoUploadType(CMOType.DRAFT)
                .previousCMO(previousOrder)
                .replacementCMO(newOrder)
                .build();

            CaseData caseData = CaseData.builder()
                .draftUploadedCMOs(List.of(previousCmo))
                .hearingDetails(futureHearings)
                .uploadDraftOrdersEventData(eventData)
                .build();

            UploadDraftOrdersData cmoInfo = service.getDraftsInfo(caseData);

            UploadDraftOrdersData expectedInfo = eventData.toBuilder()
                .showReplacementCMO(YES)
                .replacementCMO(null)
                .previousCMO(previousCmo.getValue().getOrder())
                .cmoToSend(newOrder)
                .cmoHearingInfo("Case management hearing, 2 March 2050")
                .cmoJudgeInfo("His Honour Judge Dredd")
                .cmoSupportingDocs(List.of(newSupportingDoc))
                .futureHearingsForCMO(dynamicList(futureHearings.get(0).getId(), futureHearings))
                .pastHearingsForCMO(dynamicList(emptyList()))
                .hearingsForHearingOrderDrafts(dynamicList(emptyList(), defaultListItem("No hearing")))
                .build();

            assertThat(cmoInfo).isEqualTo(expectedInfo);
        }

        @Test
        void shouldRemoveSupportingDocumentsWhenSupportedDocIsRemovedFromTheAssociatedDraftCMO() {
            Element<SupportingEvidenceBundle> supportingDoc = element(
                SupportingEvidenceBundle.builder().name("case summary").build());

            Element<HearingOrder> previousCmo = element(HearingOrder.builder()
                .status(DRAFT)
                .order(testDocumentReference())
                .supportingDocs(List.of(supportingDoc)).build());

            List<Element<HearingBooking>> hearings = wrapElements(
                hearing(CASE_MANAGEMENT, LocalDateTime.of(2050, 3, 2, 0, 0, 0), previousCmo.getId()),
                hearing(CASE_MANAGEMENT, LocalDateTime.of(2050, 3, 7, 0, 0, 0)));

            hearings.get(0).getValue().setCaseManagementOrderId(previousCmo.getId());

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .futureHearingsForCMO(dynamicList(hearings.get(0).getId(), hearings))
                .cmoSupportingDocs(null)
                .cmoUploadType(CMOType.DRAFT)
                .previousCMO(previousCmo.getValue().getOrder())
                .build();

            CaseData caseData = CaseData.builder()
                .draftUploadedCMOs(List.of(previousCmo))
                .hearingDetails(hearings)
                .uploadDraftOrdersEventData(eventData)
                .build();

            UploadDraftOrdersData cmoInfo = service.getDraftsInfo(caseData);

            UploadDraftOrdersData expectedInfo = eventData.toBuilder()
                .showReplacementCMO(YES)
                .previousCMO(previousCmo.getValue().getOrder())
                .cmoToSend(previousCmo.getValue().getOrder())
                .cmoHearingInfo("Case management hearing, 2 March 2050")
                .cmoJudgeInfo("His Honour Judge Dredd")
                .cmoSupportingDocs(List.of())
                .futureHearingsForCMO(dynamicList(hearings.get(0).getId(), hearings))
                .pastHearingsForCMO(dynamicList(emptyList()))
                .hearingsForHearingOrderDrafts(dynamicList(emptyList(), defaultListItem("No hearing")))
                .build();

            assertThat(cmoInfo).isEqualTo(expectedInfo);
        }

        @Test
        void shouldRegenerateDynamicListsIfIdsPassedAsStrings() {
            Element<HearingBooking> futureHearing = element(hearing(CASE_MANAGEMENT,
                LocalDateTime.of(3000, 12, 3, 11, 32)));
            List<Element<HearingBooking>> hearings = hearings();
            hearings.add(futureHearing);

            UUID pastHearingId = hearings.get(0).getId();
            UUID futureHearingId = futureHearing.getId();

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .pastHearingsForCMO(pastHearingId.toString())
                .futureHearingsForCMO(futureHearingId.toString())
                .hearingsForHearingOrderDrafts(DEFAULT_CODE.toString())
                .build();

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .uploadDraftOrdersEventData(eventData)
                .build();

            UploadDraftOrdersData cmoInfo = service.getDraftsInfo(caseData);

            DynamicList pastList = dynamicList(hearings.get(0).getId(), List.of(hearings.get(0), hearings.get(1)));
            DynamicList futureList = dynamicList(futureHearingId, List.of(hearings.get(2)));
            DynamicList allHearings = dynamicList(DEFAULT_CODE,
                List.of(hearings.get(0), hearings.get(1), hearings.get(2)), defaultListItem("No hearing"));

            assertThat(cmoInfo.getPastHearingsForCMO()).isEqualTo(pastList);
            assertThat(cmoInfo.getFutureHearingsForCMO()).isEqualTo(futureList);
            assertThat(cmoInfo.getHearingsForHearingOrderDrafts()).isEqualTo(allHearings);
        }

        @Test
        void shouldPullReplacementDocumentWhenUploadedFieldIsNull() {
            List<Element<HearingBooking>> hearings = hearings();
            Element<HearingOrder> cmo = element(HearingOrder.builder()
                .order(testDocumentReference())
                .status(DRAFT)
                .build());
            hearings.get(0).getValue().setCaseManagementOrderId(cmo.getId());

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .replacementCMO(testDocumentReference())
                .pastHearingsForCMO(dynamicList(hearings.get(0).getId(), hearings))
                .build();

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .uploadDraftOrdersEventData(eventData)
                .draftUploadedCMOs(List.of(cmo))
                .build();

            UploadDraftOrdersData reviewData = service.getDraftsInfo(caseData);

            UploadDraftOrdersData expectedData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .previousCMO(cmo.getValue().getOrder())
                .cmoToSend(eventData.getReplacementCMO())
                .cmoJudgeInfo("His Honour Judge Dredd")
                .cmoHearingInfo("Case management hearing, 2 March 2020")
                .showReplacementCMO(YES)
                .pastHearingsForCMO(dynamicList(hearings.get(0).getId(), hearings))
                .futureHearingsForCMO(dynamicList(emptyList()))
                .hearingsForHearingOrderDrafts(dynamicList(hearings, defaultListItem("No hearing")))
                .build();

            assertThat(reviewData).isEqualTo(expectedData);
        }

        @Test
        void shouldPullPreviousDocumentWhenReplacementAndMainAndUploadedFileFieldAreNull() {
            List<Element<HearingBooking>> hearings = hearings();
            Element<HearingOrder> cmo = element(HearingOrder.builder()
                .order(testDocumentReference())
                .status(DRAFT)
                .build());
            hearings.get(0).getValue().setCaseManagementOrderId(cmo.getId());

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .pastHearingsForCMO(dynamicList(hearings.get(0).getId(), hearings))
                .build();

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .uploadDraftOrdersEventData(eventData)
                .draftUploadedCMOs(List.of(cmo))
                .build();

            UploadDraftOrdersData reviewData = service.getDraftsInfo(caseData);

            UploadDraftOrdersData expectedData = eventData.toBuilder()
                .previousCMO(cmo.getValue().getOrder())
                .cmoToSend(cmo.getValue().getOrder())
                .cmoJudgeInfo("His Honour Judge Dredd")
                .cmoHearingInfo("Case management hearing, 2 March 2020")
                .showReplacementCMO(YES)
                .pastHearingsForCMO(dynamicList(hearings.get(0).getId(), hearings))
                .futureHearingsForCMO(dynamicList(emptyList()))
                .hearingsForHearingOrderDrafts(dynamicList(hearings, defaultListItem("No hearing")))
                .build();

            assertThat(reviewData).isEqualTo(expectedData);
        }

        @Test
        void shouldProvideEmptyDraftOrdersIfOrdersNotPresentForSelectedHearing() {
            List<Element<HearingBooking>> hearings = hearings();

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.C21))
                .hearingsForHearingOrderDrafts(dynamicList(hearings.get(0).getId(), hearings))
                .build();

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .uploadDraftOrdersEventData(eventData)
                .build();

            UploadDraftOrdersData reviewData = service.getDraftsInfo(caseData);

            assertThat(reviewData.getCurrentHearingOrderDrafts()).extracting(Element::getValue)
                .containsExactly(HearingOrder.builder().build());
        }

        @Test
        void shouldProvideNonCmoDraftOrdersForSelectedHearing() {
            List<Element<HearingBooking>> hearings = hearings();

            Element<HearingBooking> selectedHearing = hearings.get(0);
            Element<HearingBooking> otherHearing = hearings.get(1);

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.C21))
                .hearingsForHearingOrderDrafts(dynamicList(selectedHearing.getId(),
                    List.of(selectedHearing, otherHearing)))
                .build();

            Element<HearingOrder> hearingOrder1 = hearingOrder(AGREED_CMO);
            Element<HearingOrder> hearingOrder2 = hearingOrder(C21);
            Element<HearingOrder> hearingOrder3 = hearingOrder(C21);
            Element<HearingOrder> hearingOrder4 = hearingOrder(C21);

            HearingOrdersBundle selectedHearingOrdersBundle = HearingOrdersBundle.builder()
                .hearingId(selectedHearing.getId())
                .orders(newArrayList(hearingOrder1, hearingOrder2, hearingOrder3))
                .build();

            HearingOrdersBundle otherHearingOrdersBundle = HearingOrdersBundle.builder()
                .hearingId(otherHearing.getId())
                .orders(newArrayList(hearingOrder4))
                .build();

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .uploadDraftOrdersEventData(eventData)
                .hearingOrdersBundlesDrafts(wrapElements(selectedHearingOrdersBundle, otherHearingOrdersBundle))
                .build();

            UploadDraftOrdersData actualEventData = service.getDraftsInfo(caseData);

            assertThat(actualEventData.getCurrentHearingOrderDrafts()).containsExactly(hearingOrder2, hearingOrder3);
        }

        @Test
        void shouldProvideNonCmoDraftOrdersWhenNoHearingSelected() {
            List<Element<HearingBooking>> hearings = hearings();

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.C21))
                .hearingsForHearingOrderDrafts(dynamicList(DEFAULT_CODE, List.of(hearings.get(0), hearings.get(1)),
                    defaultListItem("No hearing")))
                .build();

            Element<HearingOrder> hearingOrder1 = hearingOrder(C21);
            Element<HearingOrder> hearingOrder2 = hearingOrder(C21);
            Element<HearingOrder> hearingOrder3 = hearingOrder(C21);

            HearingOrdersBundle hearingOrdersBundle = ordersBundle(hearings.get(0).getId(), hearingOrder1);
            HearingOrdersBundle noHearingOrdersBundle = ordersBundle(null, hearingOrder2, hearingOrder3);

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .uploadDraftOrdersEventData(eventData)
                .hearingOrdersBundlesDrafts(ElementUtils.wrapElements(hearingOrdersBundle, noHearingOrdersBundle))
                .build();

            UploadDraftOrdersData actualEventData = service.getDraftsInfo(caseData);

            assertThat(actualEventData.getCurrentHearingOrderDrafts()).containsExactly(hearingOrder2, hearingOrder3);
        }
    }

    @Nested
    class UpdateCase {

        @Test
        void shouldAddNewCMOToListAndUpdateHearingIfCMOWasNotAlreadyInList() {

            List<Element<HearingBooking>> hearings = hearings();

            List<Element<SupportingEvidenceBundle>> bundle = wrapElements(SupportingEvidenceBundle.builder()
                .name("name")
                .build());

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .uploadedCaseManagementOrder(testDocumentReference())
                .pastHearingsForCMO(dynamicList(hearings.get(0).getId(), List.of(hearings.get(0), hearings.get(1))))
                .cmoSupportingDocs(bundle)
                .cmoUploadType(CMOType.DRAFT)
                .build();

            List<Element<HearingOrder>> unsealedOrders = newArrayList();
            List<Element<HearingFurtherEvidenceBundle>> bundles = newArrayList();
            List<Element<HearingOrdersBundle>> ordersBundles = newArrayList();

            service.updateCase(eventData, hearings, unsealedOrders, bundles, ordersBundles);

            assertThat(bundles).isEmpty();

            assertThat(unsealedOrders).hasSize(1)
                .first()
                .extracting(Element::getValue)
                .isEqualTo(HearingOrder.builder()
                    .type(DRAFT_CMO)
                    .title("Draft CMO from advocates' meeting")
                    .supportingDocs(bundle)
                    .judgeTitleAndName("His Honour Judge Dredd")
                    .hearing("Case management hearing, 2 March 2020")
                    .dateSent(time.now().toLocalDate())
                    .order(eventData.getUploadedCaseManagementOrder())
                    .status(DRAFT)
                    .build());

            assertThat(hearings).hasSize(2)
                .first()
                .extracting(hearing -> hearing.getValue().getCaseManagementOrderId())
                .isEqualTo(unsealedOrders.get(0).getId());
        }

        @Test
        void shouldMigrateBundleWhenUploadedCMOIsAgreed() {
            when(documentUploadHelper.getUploadedDocumentUserDetails()).thenReturn("Test LA");

            List<Element<HearingBooking>> hearings = hearings();

            List<Element<SupportingEvidenceBundle>> bundle = List.of(
                buildSupportingEvidenceBundleElement("name"));

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .pastHearingsForCMO(dynamicList(hearings.get(0).getId(), hearings))
                .uploadedCaseManagementOrder(testDocumentReference())
                .cmoSupportingDocs(bundle)
                .cmoUploadType(CMOType.AGREED)
                .build();

            List<Element<HearingOrder>> unsealedOrders = newArrayList();
            List<Element<HearingFurtherEvidenceBundle>> bundles = newArrayList();
            List<Element<HearingOrdersBundle>> ordersBundles = newArrayList();

            service.updateCase(eventData, hearings, unsealedOrders, bundles, ordersBundles);

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
        void shouldUpdateExistingBundleWhenUploadedCMOIsAgreed() {
            when(documentUploadHelper.getUploadedDocumentUserDetails()).thenReturn("Test LA");

            List<Element<HearingBooking>> hearings = hearings();

            Element<SupportingEvidenceBundle> doc1 = buildSupportingEvidenceBundleElement("1");
            Element<SupportingEvidenceBundle> doc2 = buildSupportingEvidenceBundleElement("2");
            Element<SupportingEvidenceBundle> doc3 = buildSupportingEvidenceBundleElement("3");

            Element<SupportingEvidenceBundle> updatedDoc2 = element(doc2.getId(), doc2.getValue().toBuilder()
                .name("2 updated")
                .build());
            Element<SupportingEvidenceBundle> newDoc = buildSupportingEvidenceBundleElement("3");

            Element<HearingFurtherEvidenceBundle> existingHearingBundle = element(
                hearings.get(0).getId(),
                HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(newArrayList(doc1, doc2, doc3))
                    .build());

            List<Element<SupportingEvidenceBundle>> cmoSupportingDocuments = List.of(doc1, updatedDoc2, doc3, newDoc);

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .pastHearingsForCMO(dynamicList(hearings.get(0).getId(), hearings))
                .uploadedCaseManagementOrder(testDocumentReference())
                .cmoSupportingDocs(cmoSupportingDocuments)
                .cmoUploadType(CMOType.AGREED)
                .build();

            List<Element<HearingOrder>> unsealedOrders = newArrayList();
            List<Element<HearingFurtherEvidenceBundle>> bundles = newArrayList(existingHearingBundle);
            List<Element<HearingOrdersBundle>> ordersBundles = newArrayList();

            service.updateCase(eventData, hearings, unsealedOrders, bundles, ordersBundles);

            assertThat(bundles.get(0).getValue().getSupportingEvidenceBundle())
                .containsExactly(doc1, updatedDoc2, doc3, newDoc);
        }

        @Test
        void shouldUpdateExistingCMOWithNewOrderAndChangeStatus() {
            List<Element<HearingBooking>> hearings = hearings();
            List<Element<HearingOrder>> unsealedOrders = newArrayList();
            Element<HearingOrder> oldOrder = element(HearingOrder.builder().status(RETURNED).build());

            Element<HearingBooking> selectedHearing = hearings.get(0);

            unsealedOrders.add(oldOrder);
            unsealedOrders.add(element(HearingOrder.builder().build()));

            selectedHearing.getValue().setCaseManagementOrderId(unsealedOrders.get(0).getId());

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .cmoUploadType(CMOType.AGREED)
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .pastHearingsForCMO(dynamicList(selectedHearing.getId(), hearings))
                .uploadedCaseManagementOrder(testDocumentReference())
                .build();

            Element<HearingOrder> previousCmoOrder = hearingOrder(AGREED_CMO);
            Element<HearingOrder> c21Order = hearingOrder(C21);
            HearingOrdersBundle ordersBundle = ordersBundle(selectedHearing.getId(), previousCmoOrder, c21Order);
            List<Element<HearingOrdersBundle>> ordersBundles = wrapElements(ordersBundle);

            service.updateCase(eventData, hearings, unsealedOrders, List.of(), ordersBundles);

            HearingOrder expectedOrder = HearingOrder.builder()
                .title("Agreed CMO discussed at hearing")
                .type(AGREED_CMO)
                .status(SEND_TO_JUDGE)
                .dateSent(time.now().toLocalDate())
                .order(eventData.getUploadedCaseManagementOrder())
                .hearing("Case management hearing, 2 March 2020")
                .judgeTitleAndName("His Honour Judge Dredd")
                .supportingDocs(List.of())
                .build();

            assertThat(unsealedOrders).hasSize(2)
                .first()
                .extracting(Element::getValue)
                .isNotEqualTo(oldOrder.getValue())
                .isEqualTo(expectedOrder);

            assertThat(hearings).hasSize(2)
                .first()
                .extracting(hearing -> hearing.getValue().getCaseManagementOrderId())
                .isNotEqualTo(oldOrder.getId())
                .isEqualTo(unsealedOrders.get(0).getId());

            assertThat(ordersBundles).hasSize(1);
            assertThat(ordersBundles.get(0).getValue().getOrders()).extracting(Element::getValue)
                .containsExactly(expectedOrder, c21Order.getValue());
        }

        @Test
        void shouldUpdateExistingDocumentBundleWithDocsWhenPresent() {
            when(documentUploadHelper.getUploadedDocumentUserDetails()).thenReturn("Test LA");

            List<Element<HearingBooking>> hearings = hearings();

            Element<SupportingEvidenceBundle> newEvidenceBundle = buildSupportingEvidenceBundleElement("new");

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .pastHearingsForCMO(dynamicList(hearings.get(0).getId(), hearings))
                .uploadedCaseManagementOrder(testDocumentReference())
                .cmoSupportingDocs(List.of(newEvidenceBundle))
                .cmoUploadType(CMOType.AGREED)
                .build();

            List<Element<SupportingEvidenceBundle>> currentEvidenceBundles = newArrayList(
                element(SupportingEvidenceBundle.builder().name("current").build()));

            List<Element<HearingFurtherEvidenceBundle>> bundles = newArrayList(
                element(hearings.get(0).getId(), HearingFurtherEvidenceBundle.builder()
                    .hearingName("Case management hearing, 2 March 2020")
                    .supportingEvidenceBundle(currentEvidenceBundles)
                    .build()));

            List<Element<HearingOrder>> unsealedOrders = newArrayList();
            List<Element<HearingOrdersBundle>> ordersBundles = newArrayList();

            service.updateCase(eventData, hearings, unsealedOrders, bundles, ordersBundles);

            assertThat(bundles).hasSize(1)
                .first()
                .extracting(bundle -> bundle.getValue().getSupportingEvidenceBundle())
                .isEqualTo(List.of(currentEvidenceBundles.get(0), newEvidenceBundle));
        }

        @Test
        void shouldCreateOrderBundleIfDoesNotExists() {
            List<Element<HearingBooking>> hearings = hearings();

            Element<HearingOrder> hearingOrder1 = hearingOrder(C21);
            Element<HearingOrder> hearingOrder2 = hearingOrder(C21);

            HearingOrdersBundle ordersBundle = HearingOrdersBundle.builder()
                .hearingId(hearings.get(0).getId())
                .hearingName("Case management hearing, 2 March 2020")
                .judgeTitleAndName("His Honour Judge Dredd")
                .orders(newArrayList(hearingOrder1, hearingOrder2))
                .build();

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.C21))
                .hearingsForHearingOrderDrafts(dynamicList(hearings.get(0).getId(), hearings))
                .currentHearingOrderDrafts(newArrayList(hearingOrder1, hearingOrder2))
                .build();

            List<Element<HearingFurtherEvidenceBundle>> evidenceBundles = newArrayList();
            List<Element<HearingOrder>> unsealedOrders = newArrayList();
            List<Element<HearingOrdersBundle>> ordersBundles = newArrayList();

            service.updateCase(eventData, hearings, unsealedOrders, evidenceBundles, ordersBundles);

            assertThat(ordersBundles).hasSize(1);
            assertThat(ordersBundles.get(0)).extracting(Element::getValue).isEqualTo(ordersBundle);
        }

        @Test
        void shouldUpdateExistingOrdersBundle() {
            List<Element<HearingBooking>> hearings = hearings();

            Element<HearingBooking> selectedHearing = hearings.get(0);

            Element<HearingOrder> cmoOrder = hearingOrder(AGREED_CMO);
            Element<HearingOrder> hearingOrder1 = hearingOrder(C21);
            Element<HearingOrder> hearingOrder2 = hearingOrder(C21);
            Element<HearingOrder> hearingOrder3 = hearingOrder(C21);

            HearingOrdersBundle originalOrdersBundle = HearingOrdersBundle.builder()
                .hearingId(selectedHearing.getId())
                .hearingName("Case management hearing, 2 March 2020")
                .judgeTitleAndName("His Honour Judge Dredd")
                .orders(newArrayList(cmoOrder, hearingOrder1, hearingOrder2))
                .build();

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.C21))
                .hearingsForHearingOrderDrafts(dynamicList(selectedHearing.getId(), hearings))
                .currentHearingOrderDrafts(List.of(hearingOrder1, hearingOrder3))
                .build();

            List<Element<HearingOrdersBundle>> ordersBundles = wrapElements(originalOrdersBundle);

            service.updateCase(eventData, hearings, emptyList(), emptyList(), ordersBundles);

            HearingOrdersBundle expectedOrdersBundle = originalOrdersBundle.toBuilder()
                .orders(newArrayList(cmoOrder, hearingOrder1, hearingOrder3))
                .build();

            assertThat(ordersBundles).hasSize(1).first()
                .extracting(Element::getValue).isEqualTo(expectedOrdersBundle);
        }

        @Test
        void shouldRemoveOrderBundleIfNoOrdersPresent() {
            List<Element<HearingBooking>> hearings = hearings();

            Element<HearingBooking> selectedHearing = hearings.get(0);

            Element<HearingOrder> hearingOrder1 = hearingOrder(C21);
            Element<HearingOrder> hearingOrder2 = hearingOrder(C21);

            HearingOrdersBundle selectedHearingOrderBundle = ordersBundle(selectedHearing.getId(), hearingOrder1);
            HearingOrdersBundle otherOrdersBundle = ordersBundle(hearings.get(1).getId(), hearingOrder2);

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.C21))
                .hearingsForHearingOrderDrafts(dynamicList(selectedHearing.getId(), hearings))
                .currentHearingOrderDrafts(emptyList())
                .build();

            List<Element<HearingOrdersBundle>> ordersBundles = wrapElements(selectedHearingOrderBundle,
                otherOrdersBundle);

            service.updateCase(eventData, hearings, emptyList(), emptyList(), ordersBundles);

            assertThat(ordersBundles).extracting(Element::getValue).containsExactly(otherOrdersBundle);
        }

        @Test
        void shouldRemoveDraftCMOIfExistingWhenUploadingAgreedCMOForTheSameHearing() {
            List<Element<HearingBooking>> hearings = hearings();

            Element<HearingBooking> selectedHearing = hearings.get(0);

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .cmoUploadType(CMOType.AGREED)
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .pastHearingsForCMO(dynamicList(selectedHearing.getId(), hearings))
                .uploadedCaseManagementOrder(testDocumentReference())
                .build();

            Element<HearingOrder> previousDraftCmoOrder = hearingOrder(DRAFT_CMO);
            Element<HearingOrder> previousC21Order = hearingOrder(C21);
            HearingOrdersBundle ordersBundle = ordersBundle(
                selectedHearing.getId(), previousDraftCmoOrder, previousC21Order);
            List<Element<HearingOrdersBundle>> ordersBundles = wrapElements(ordersBundle);
            List<Element<HearingOrder>> unsealedOrders = newArrayList();

            service.updateCase(eventData, hearings, unsealedOrders, List.of(), ordersBundles);

            HearingOrder expectedOrder = HearingOrder.builder()
                .title("Agreed CMO discussed at hearing")
                .type(AGREED_CMO)
                .status(SEND_TO_JUDGE)
                .dateSent(time.now().toLocalDate())
                .order(eventData.getUploadedCaseManagementOrder())
                .hearing("Case management hearing, 2 March 2020")
                .judgeTitleAndName("His Honour Judge Dredd")
                .supportingDocs(List.of())
                .build();

            assertThat(ordersBundles).hasSize(1);
            assertThat(ordersBundles.get(0).getValue().getOrders().size()).isEqualTo(2);
            assertThat(ordersBundles.get(0).getValue().getOrders()).extracting(Element::getValue)
                .containsExactly(expectedOrder, previousC21Order.getValue());
        }

        @Test
        void shouldThrowsExceptionWhenHearingNotFoundForCMO() {
            List<Element<HearingBooking>> hearings = hearings();

            UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
                .hearingOrderDraftKind(List.of(HearingOrderKind.CMO))
                .hearingsForHearingOrderDrafts(dynamicList(hearings))
                .currentHearingOrderDrafts(emptyList())
                .build();

            Exception exception = assertThrows(Exception.class,
                () -> service.updateCase(eventData, hearings, newArrayList(), newArrayList(), newArrayList()));

            assertThat(exception).isInstanceOf(HearingNotFoundException.class);
        }

        private Element<SupportingEvidenceBundle> buildSupportingEvidenceBundleElement(String name) {
            return element(SupportingEvidenceBundle.builder().name(name)
                .uploadedBy("Test LA")
                .dateTimeUploaded(time.now())
                .build());
        }
    }

    @Nested
    class MigrateCMODrafts {

        @Test
        void shouldReturnEmptyListIfNoPreviousDraftOrders() {
            CaseData caseData = CaseData.builder().build();

            List<Element<HearingOrdersBundle>> actualOrderBundles = service.migrateCmoDraftToOrdersBundles(caseData);

            assertThat(actualOrderBundles).isEmpty();
        }

        @Test
        void shouldPreserveOriginalOrdersBundleIfNoUpdatedCMO() {
            HearingOrdersBundle originalOrdersBundle = HearingOrdersBundle.builder()
                .hearingId(randomUUID())
                .orders(wrapElements(HearingOrder.builder().title("test").type(C21).build()))
                .build();

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(wrapElements(originalOrdersBundle))
                .build();

            List<Element<HearingOrdersBundle>> actualOrdersBundles = service.migrateCmoDraftToOrdersBundles(caseData);

            assertThat(actualOrdersBundles).extracting(Element::getValue).containsExactly(originalOrdersBundle);
        }

        @Test
        void shouldAddCmoOrderToExistingOrdersBundle() {

            Element<HearingOrder> newHearing1CmoOrder = randomHearingOrder(AGREED_CMO);
            Element<HearingOrder> originalHearing1C21Order = randomHearingOrder(C21);
            Element<HearingOrder> originalHearing2C21Order = randomHearingOrder(C21);

            Element<HearingBooking> hearing1 = randomHearing(newHearing1CmoOrder.getId());
            Element<HearingBooking> hearing2 = randomHearing();

            HearingOrdersBundle originalHearing1OrdersBundle = ordersBundle(hearing1, originalHearing1C21Order);
            HearingOrdersBundle originalHearing2OrdersBundle = ordersBundle(hearing2, originalHearing2C21Order);

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(wrapElements(
                    List.of(originalHearing1OrdersBundle, originalHearing2OrdersBundle)))
                .hearingDetails(List.of(hearing1, hearing2))
                .draftUploadedCMOs(List.of(newHearing1CmoOrder))
                .build();

            List<Element<HearingOrdersBundle>> actualOrdersBundles = service.migrateCmoDraftToOrdersBundles(caseData);

            HearingOrdersBundle expectedHearing1OrderBundle = originalHearing1OrdersBundle.toBuilder()
                .orders(newArrayList(newHearing1CmoOrder, originalHearing1C21Order))
                .build();

            assertThat(actualOrdersBundles).extracting(Element::getValue)
                .containsExactly(expectedHearing1OrderBundle, originalHearing2OrdersBundle);
        }

        @Test
        void shouldAddCmoOrderToNewBundle() {

            Element<HearingOrder> cmoOrder = randomHearingOrder(AGREED_CMO);

            Element<HearingBooking> hearing = randomHearing(cmoOrder.getId());

            CaseData caseData = CaseData.builder()
                .hearingDetails(List.of(hearing))
                .draftUploadedCMOs(List.of(cmoOrder))
                .build();

            List<Element<HearingOrdersBundle>> actualOrdersBundles = service.migrateCmoDraftToOrdersBundles(caseData);

            HearingOrdersBundle expectedOrderBundle = HearingOrdersBundle.builder()
                .hearingId(hearing.getId())
                .hearingName("Issue resolution hearing, 10 January 2021")
                .judgeTitleAndName("Her Honour Judge Smith")
                .orders(newArrayList(cmoOrder))
                .build();

            assertThat(actualOrdersBundles).extracting(Element::getValue).containsExactly(expectedOrderBundle);
        }

        @Test
        void shouldUpdateCmoOrderInExistingOrdersBundle() {

            Element<HearingOrder> hearing1CmoOrder = randomHearingOrder(AGREED_CMO);
            Element<HearingOrder> newHearing1CmoOrder = randomHearingOrder(AGREED_CMO);
            Element<HearingOrder> hearing1C21Order = randomHearingOrder(C21);
            Element<HearingOrder> hearing2C21Order = randomHearingOrder(C21);

            Element<HearingBooking> hearing1 = randomHearing(newHearing1CmoOrder.getId());
            Element<HearingBooking> hearing2 = randomHearing();

            HearingOrdersBundle originalHearing1OrdersBundle = ordersBundle(hearing1, hearing1CmoOrder,
                hearing1C21Order);
            HearingOrdersBundle originalHearing2OrdersBundle = ordersBundle(hearing2, hearing2C21Order);

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(wrapElements(originalHearing1OrdersBundle, originalHearing2OrdersBundle))
                .hearingDetails(List.of(hearing1, hearing2))
                .draftUploadedCMOs(List.of(newHearing1CmoOrder))
                .build();

            List<Element<HearingOrdersBundle>> actualOrdersBundles = service.migrateCmoDraftToOrdersBundles(caseData);

            HearingOrdersBundle expectedOrderBundle = originalHearing1OrdersBundle.toBuilder()
                .orders(newArrayList(newHearing1CmoOrder, hearing1C21Order))
                .build();

            assertThat(actualOrdersBundles).extracting(Element::getValue)
                .containsExactly(expectedOrderBundle, originalHearing2OrdersBundle);
        }

        @Test
        void shouldRemoveCmoOrderFromBundle() {

            Element<HearingOrder> hearing1CmoOrder = randomHearingOrder(DRAFT_CMO);
            Element<HearingOrder> hearing1C21Order = randomHearingOrder(C21);
            Element<HearingOrder> hearing2C21Order = randomHearingOrder(C21);

            Element<HearingBooking> hearing1 = randomHearing(hearing1CmoOrder.getId());
            Element<HearingBooking> hearing2 = randomHearing();

            HearingOrdersBundle originalHearing1OrdersBundle = ordersBundle(hearing1, hearing1CmoOrder,
                hearing1C21Order);
            HearingOrdersBundle originalHearing2OrdersBundle = ordersBundle(hearing2, hearing2C21Order);

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(wrapElements(originalHearing1OrdersBundle, originalHearing2OrdersBundle))
                .hearingDetails(List.of(hearing1, hearing2))
                .draftUploadedCMOs(emptyList())
                .build();

            List<Element<HearingOrdersBundle>> actualOrdersBundles = service.migrateCmoDraftToOrdersBundles(caseData);

            HearingOrdersBundle expectedOrderBundle = originalHearing1OrdersBundle.toBuilder()
                .orders(newArrayList(hearing1C21Order))
                .build();

            assertThat(actualOrdersBundles).extracting(Element::getValue)
                .containsExactly(expectedOrderBundle, originalHearing2OrdersBundle);
        }

        private Element<HearingBooking> randomHearing(UUID cmoId) {
            return ElementUtils.element(randomUUID(), HearingBooking.builder()
                .startDate(LocalDateTime.of(2021, Month.JANUARY, 10, 0, 0, 0))
                .type(ISSUE_RESOLUTION)
                .caseManagementOrderId(cmoId)
                .venue(randomAlphanumeric(10))
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HER_HONOUR_JUDGE)
                    .judgeLastName("Smith")
                    .build())
                .build());
        }

        private Element<HearingBooking> randomHearing() {
            return randomHearing(null);
        }

        private Element<HearingOrder> randomHearingOrder(HearingOrderType hearingOrderType) {
            return ElementUtils.element(randomUUID(), HearingOrder.builder()
                .type(hearingOrderType)
                .title(randomAlphanumeric(10))
                .status(SEND_TO_JUDGE)
                .order(testDocumentReference())
                .build());
        }

        @SafeVarargs
        private HearingOrdersBundle ordersBundle(Element<HearingBooking> hearing, Element<HearingOrder>... orders) {
            return HearingOrdersBundle.builder()
                .orders(newArrayList(orders))
                .build()
                .updateHearing(hearing.getId(), hearing.getValue());
        }

    }

    private DynamicList dynamicList(List<Element<HearingBooking>> hearings, DynamicListElement... additional) {
        return dynamicList(null, hearings, additional);
    }

    private DynamicList dynamicList(UUID selected, List<Element<HearingBooking>> hearings,
                                    DynamicListElement... additionalItems) {

        List<DynamicListElement> listItems = hearings.stream()
            .map(hearing -> DynamicListElement.builder()
                .code(hearing.getId())
                .label(hearing.getValue().toLabel())
                .build())
            .collect(Collectors.toList());

        listItems.addAll(0, Arrays.asList(additionalItems));

        DynamicListElement selectedItem = listItems.stream()
            .filter(item -> item.hasCode(selected))
            .findFirst()
            .orElse(DynamicListElement.EMPTY);

        return DynamicList.builder()
            .value(selectedItem)
            .listItems(listItems)
            .build();
    }

    private List<Element<HearingBooking>> hearings() {
        LocalTime time = LocalTime.now();
        return newArrayList(
            element(hearing(CASE_MANAGEMENT, LocalDateTime.of(LocalDate.of(2020, 3, 2), time))),
            element(hearing(FURTHER_CASE_MANAGEMENT, LocalDateTime.of(LocalDate.of(2020, 3, 7), time)))
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
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Dredd")
                .build())
            .build();
    }

    private Element<HearingOrder> hearingOrder(HearingOrderType type) {
        return element(HearingOrder.builder()
            .type(type)
            .title(RandomStringUtils.randomAlphanumeric(5))
            .order(testDocumentReference())
            .build());
    }

    @SafeVarargs
    private HearingOrdersBundle ordersBundle(UUID hearingId, Element<HearingOrder>... orders) {
        return HearingOrdersBundle.builder()
            .hearingId(hearingId)
            .orders(newArrayList(orders))
            .build();
    }
}
