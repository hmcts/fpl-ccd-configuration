package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ConfidentialRefusedOrders;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.event.C2AdditionalApplicationEventData;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ReviewAdditionalApplicationService;
import uk.gov.hmcts.reform.fpl.service.cmo.ApproveDraftOrdersService;
import uk.gov.hmcts.reform.fpl.service.cmo.HearingOrderGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class ReviewAdditionalApplicationServiceTest {

    @Mock
    private ApproveDraftOrdersService approveDraftOrdersService;

    @Mock
    private HearingOrderGenerator hearingOrderGenerator;

    @InjectMocks
    private ReviewAdditionalApplicationService reviewAdditionalApplicationService;

    private static final Element<AdditionalApplicationsBundle> REVIEWED_BUNDLE =
        element(AdditionalApplicationsBundle.builder()
           .uploadedDateTime("1 January 2021, 12:00pm")
           .author("TEST_REVIEWED")
           .c2DocumentBundle(C2DocumentBundle.builder().uploadedDateTime("1 January 2021, 12:00pm").build())
           .applicationReviewed(YES)
           .build());

    private static final Element<AdditionalApplicationsBundle> NEW_BUNDLE_1 =
        element(AdditionalApplicationsBundle.builder()
            .uploadedDateTime("1 January 2021, 12:00pm")
            .author("TESTING1")
            .c2DocumentBundle(C2DocumentBundle.builder()
                .uploadedDateTime("1 January 2021, 12:00pm").build())
            .applicationReviewed(NO)
            .build());

    private static final Element<AdditionalApplicationsBundle> NEW_BUNDLE_2 =
        element(AdditionalApplicationsBundle.builder()
            .uploadedDateTime("1 January 2021, 12:00pm")
            .author("TESTING2")
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                .applicationType(OtherApplicationType.C1_TERMINATION_OF_APPOINTMENT_OF_A_GUARDIAN)
                .uploadedDateTime("1 January 2021, 12:00pm").build())
            .applicationReviewed(NO)
            .build());

    @Test
    void shouldInitEventFieldWithListOfBundlesToBeReviewed() {
        when(approveDraftOrdersService.getJudgeTitleAndNameOfCurrentUser(any())).thenReturn("District Judge Example");

        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE, NEW_BUNDLE_1, NEW_BUNDLE_2))
            .build();

        Map<String, Object> resultMap = reviewAdditionalApplicationService.initEventField(caseData);

        Map<String, Object> expectedMap = Map.of(
            "hasApplicationToBeReviewed", YES,
            "onlyOneApplicationToBeReviewed", NO,
            "additionalApplicationToBeReviewedList",
            asDynamicList(List.of(NEW_BUNDLE_1, NEW_BUNDLE_2), AdditionalApplicationsBundle::toLabel),
            "reviewOrderUrgency", NO,
            "addCoverSheet", NO,
            "judgeNameAndTitle", "District Judge Example"
        );

        assertThat(resultMap).isEqualTo(expectedMap);
    }

    @Test
    void shouldInitEventFieldWithOutBundlesToBeReviewed() {
        when(approveDraftOrdersService.getJudgeTitleAndNameOfCurrentUser(any())).thenReturn("District Judge Example");

        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE))
            .build();

        Map<String, Object> resultMap = reviewAdditionalApplicationService.initEventField(caseData);

        Map<String, Object> expectedMap = Map.of(
            "hasApplicationToBeReviewed", NO,
            "onlyOneApplicationToBeReviewed", NO,
            "reviewOrderUrgency", NO,
            "addCoverSheet", NO,
            "judgeNameAndTitle", "District Judge Example"
            );

        assertThat(resultMap).isEqualTo(expectedMap);
    }

    @Test
    void shouldInitEventFieldWithOneBundleToBeReviewed() {
        when(approveDraftOrdersService.getJudgeTitleAndNameOfCurrentUser(any())).thenReturn("District Judge Example");

        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE, NEW_BUNDLE_1))
            .build();

        Map<String, Object> resultMap = reviewAdditionalApplicationService.initEventField(caseData);

        assertThat(resultMap)
            .containsEntry("hasApplicationToBeReviewed", YES)
            .containsEntry("onlyOneApplicationToBeReviewed", YES)
            .containsEntry("hasC2ToBeReview", YES)
            .containsEntry("hasOtherToBeReview", NO)
            .containsEntry("uploadedDraftOrder", null)
            .containsEntry("reviewOrderUrgency", NO)
            .containsEntry("addCoverSheet", NO)
            .containsEntry("judgeNameAndTitle", "District Judge Example")
            .containsEntry("c2AdditionalApplicationToBeReview",
                buildReviewC2AdditionalApplicationEventData(NEW_BUNDLE_1.getValue()));
    }

    @Test
    void shouldGetSelectedApplicationsToBeReviewed() {
        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE, NEW_BUNDLE_1, NEW_BUNDLE_2))
            .confirmApplicationReviewedEventData(ConfirmApplicationReviewedEventData.builder()
                .additionalApplicationToBeReviewedList(asDynamicList(List.of(NEW_BUNDLE_1, NEW_BUNDLE_2),
                    NEW_BUNDLE_1.getId(), AdditionalApplicationsBundle::toLabel))
                .build())
            .build();

        Element<AdditionalApplicationsBundle> result =
            reviewAdditionalApplicationService.getSelectedApplicationsToBeReviewed(caseData);

        assertThat(result).isEqualTo(NEW_BUNDLE_1);
    }

    @Test
    void shouldThrowExceptionWhenSelectedBundleNotFound() {
        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE, NEW_BUNDLE_1, NEW_BUNDLE_2))
            .confirmApplicationReviewedEventData(ConfirmApplicationReviewedEventData.builder()
                .additionalApplicationToBeReviewedList(asDynamicList(List.of(NEW_BUNDLE_1, NEW_BUNDLE_2),
                    UUID.randomUUID(), AdditionalApplicationsBundle::toLabel))
                .build())
            .build();

        assertThatThrownBy(() -> reviewAdditionalApplicationService.getSelectedApplicationsToBeReviewed(caseData))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No value present");
    }

    @Test
    void shouldMarkSelectedApplicationsAsReviewed() {
        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE, NEW_BUNDLE_1, NEW_BUNDLE_2))
            .confirmApplicationReviewedEventData(ConfirmApplicationReviewedEventData.builder()
                .additionalApplicationToBeReviewedList(asDynamicList(List.of(NEW_BUNDLE_1, NEW_BUNDLE_2),
                    NEW_BUNDLE_1.getId(), AdditionalApplicationsBundle::toLabel))
                .build())
            .build();

        List<Element<AdditionalApplicationsBundle>> resultList =
            reviewAdditionalApplicationService.markSelectedBundleAsReviewed(caseData);

        List<Element<AdditionalApplicationsBundle>> expectedList =
            List.of(REVIEWED_BUNDLE,
                element(NEW_BUNDLE_1.getId(), NEW_BUNDLE_1.getValue().toBuilder().applicationReviewed(YES).build()),
                NEW_BUNDLE_2);

        assertThat(resultList).isEqualTo(expectedList);
    }

    @Test
    void shouldThrowWhenMarkingUnknownApplicationAsReviewed() {
        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(List.of(REVIEWED_BUNDLE, NEW_BUNDLE_1, NEW_BUNDLE_2))
            .confirmApplicationReviewedEventData(ConfirmApplicationReviewedEventData.builder()
                .additionalApplicationToBeReviewedList(asDynamicList(List.of(NEW_BUNDLE_1, NEW_BUNDLE_2),
                    UUID.randomUUID(), AdditionalApplicationsBundle::toLabel))
                .build())
            .build();

        assertThatThrownBy(() -> reviewAdditionalApplicationService.markSelectedBundleAsReviewed(caseData))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No value present");
    }

    @Test
    void shouldReturnDraftOrderToApplicantAndUpdateRefusedOrders() {
        UUID draftOrderId = UUID.randomUUID();
        Element<HearingOrder> draftOrder = element(draftOrderId, HearingOrder.builder()
            .order(DocumentReference.builder().filename("draft-order.docx").build())
            .build());
        Element<HearingOrder> rejectedOrder = element(draftOrderId,
            draftOrder.getValue().toBuilder().status(RETURNED).requestedChanges("Applicant needs to make changes")
                .build());
        Element<HearingOrdersBundle> hearingBundle = element(HearingOrdersBundle.builder()
            .orders(new ArrayList<>(List.of(draftOrder)))
            .ordersCTSC(new ArrayList<>())
            .build());

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(new ArrayList<>(List.of(hearingBundle)))
            .build();

        when(hearingOrderGenerator.buildRejectedHearingOrder(draftOrder,
            "Applicant needs to make changes to the order")).thenReturn(rejectedOrder);
        when(approveDraftOrdersService.updateHearingDraftOrdersBundle(caseData, hearingBundle))
            .thenReturn(Map.of("hearingOrdersBundlesDrafts", List.of()));

        Map<String, Object> result = reviewAdditionalApplicationService.returnDraftOrderToApplicant(caseData,
            hearingBundle, draftOrderId, null);

        assertThat(result.get("refusedHearingOrders")).isEqualTo(List.of(rejectedOrder));
        assertThat(result.get("hearingOrdersBundlesDrafts")).isEqualTo(List.of());
        verify(approveDraftOrdersService).updateHearingDraftOrdersBundle(caseData, hearingBundle);
    }

    @Test
    void shouldReturnConfidentialDraftOrderAndUpdateMatchingRefusedSuffix() {
        UUID draftOrderId = UUID.randomUUID();
        Element<HearingOrder> draftOrder = element(draftOrderId, HearingOrder.builder()
            .orderConfidential(DocumentReference.builder().filename("draft-order.docx").build())
            .build());
        Element<HearingOrder> rejectedOrder = element(draftOrderId,
            draftOrder.getValue().toBuilder().status(RETURNED).requestedChanges("Applicant needs to make changes")
                .build());
        Element<HearingOrdersBundle> hearingBundle = element(HearingOrdersBundle.builder()
            .orders(new ArrayList<>())
            .ordersCTSC(new ArrayList<>())
            .ordersLA(new ArrayList<>(List.of(draftOrder)))
            .build());

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(new ArrayList<>(List.of(hearingBundle)))
            .confidentialRefusedOrders(ConfidentialRefusedOrders.builder().build())
            .build();

        when(hearingOrderGenerator.buildRejectedHearingOrder(draftOrder,
            "Applicant needs to make changes to the order")).thenReturn(rejectedOrder);
        when(approveDraftOrdersService.updateHearingDraftOrdersBundle(caseData, hearingBundle))
            .thenReturn(Map.of("hearingOrdersBundlesDrafts", List.of()));

        Map<String, Object> result = reviewAdditionalApplicationService.returnDraftOrderToApplicant(caseData,
            hearingBundle, draftOrderId, null);

        assertThat(result.get("refusedHearingOrdersLA")).isEqualTo(List.of(rejectedOrder));
        assertThat(result).doesNotContainKey("refusedHearingOrders");
        assertThat(result.get("hearingOrdersBundlesDrafts")).isEqualTo(List.of());
        verify(approveDraftOrdersService).updateHearingDraftOrdersBundle(caseData, hearingBundle);
    }

    @Test
    void shouldUseProvidedRequestedChanges() {
        UUID draftOrderId = UUID.randomUUID();
        String requestedChanges = "Please amend paragraph 3 and correct child DOB";

        Element<HearingOrder> draftOrder = element(draftOrderId, HearingOrder.builder()
            .order(DocumentReference.builder().filename("draft-order.docx").build())
            .build());
        Element<HearingOrder> rejectedOrder = element(draftOrderId,
            draftOrder.getValue().toBuilder().status(RETURNED).requestedChanges(requestedChanges).build());
        Element<HearingOrdersBundle> hearingBundle = element(HearingOrdersBundle.builder()
            .orders(new ArrayList<>(List.of(draftOrder)))
            .ordersCTSC(new ArrayList<>())
            .build());

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(new ArrayList<>(List.of(hearingBundle)))
            .build();

        when(hearingOrderGenerator.buildRejectedHearingOrder(draftOrder, requestedChanges)).thenReturn(rejectedOrder);
        when(approveDraftOrdersService.updateHearingDraftOrdersBundle(caseData, hearingBundle))
            .thenReturn(Map.of("hearingOrdersBundlesDrafts", List.of()));

        Map<String, Object> result = reviewAdditionalApplicationService.returnDraftOrderToApplicant(
            caseData,
            hearingBundle,
            draftOrderId,
            requestedChanges
        );

        assertThat(result.get("refusedHearingOrders")).isEqualTo(List.of(rejectedOrder));
        verify(hearingOrderGenerator).buildRejectedHearingOrder(eq(draftOrder), eq(requestedChanges));
        verify(approveDraftOrdersService).updateHearingDraftOrdersBundle(caseData, hearingBundle);
    }

    private static C2AdditionalApplicationEventData buildReviewC2AdditionalApplicationEventData(
            AdditionalApplicationsBundle bundle) {
        boolean isC2Confidential = YES.equals(bundle.getHasConfidentialC2());
        C2DocumentBundle c2ToBeReviewed = (isC2Confidential)
            ? bundle.getC2DocumentBundleConfidential() : bundle.getC2DocumentBundle();
        return C2AdditionalApplicationEventData.builder()
            .routeType(c2ToBeReviewed.getRouteType())
            .applicantName(c2ToBeReviewed.getApplicantName())
            .type(c2ToBeReviewed.getType())
            .confidentialApplication((isC2Confidential)
                ? YES.getValue() + " - only HMCTS will be able to view this application" : NO.getValue())
            .document(c2ToBeReviewed.getDocument())
            .applicationPermissionType(c2ToBeReviewed.getApplicationPermissionType())
            .applicationRelatesToAllChildren(c2ToBeReviewed.getApplicationRelatesToAllChildren())
            .childrenOnApplication(c2ToBeReviewed.getChildrenOnApplication())
            .applicationSummary(c2ToBeReviewed.getApplicationSummary())
            .hasSafeguardingRisk(c2ToBeReviewed.getHasSafeguardingRisk())
            .isHearingAdjournmentRequired(c2ToBeReviewed.getIsHearingAdjournmentRequired())
            .requestedHearingToAdjourn(c2ToBeReviewed.getRequestedHearingToAdjourn())
            .canBeConsideredAtNextHearing(c2ToBeReviewed.getCanBeConsideredAtNextHearing())
            .draftOrdersBundle(c2ToBeReviewed.getDraftOrdersBundle())
            .supplementsBundle(c2ToBeReviewed.getSupplementsBundle())
            .supportingEvidenceBundle(c2ToBeReviewed.getSupportingEvidenceBundle())
            .build();
    }
}
