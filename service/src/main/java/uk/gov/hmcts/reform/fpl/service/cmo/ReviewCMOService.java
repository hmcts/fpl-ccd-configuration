package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewCMOService {

    private final ObjectMapper mapper;
    private final FeatureToggleService featureToggleService;
    private final Time time;

    /**
     * That methods shouldn't be invoked without any cmo selected as the outcome is unexpected.
     * There is dedicated method below to support this functionality.
     */
    public DynamicList buildDynamicList(CaseData caseData) {
        List<Element<CaseManagementOrder>> cmosReadyForApproval = getCMOsReadyForApproval(caseData);
        Element<CaseManagementOrder> selectedCMO = getSelectedCMO(caseData);

        return asDynamicList(cmosReadyForApproval, selectedCMO.getId(), CaseManagementOrder::getHearing);
    }

    public DynamicList buildUnselectedDynamicList(CaseData caseData) {
        List<Element<CaseManagementOrder>> cmosReadyForApproval = getCMOsReadyForApproval(caseData);

        return asDynamicList(cmosReadyForApproval, null, CaseManagementOrder::getHearing);
    }

    public Map<String, Object> getPageDisplayControls(CaseData caseData) {
        List<Element<CaseManagementOrder>> cmosReadyForApproval = getCMOsReadyForApproval(caseData);
        Map<String, Object> data = new HashMap<>();
        String numDraftCMOs = "numDraftCMOs";

        switch (cmosReadyForApproval.size()) {
            case 0:
                data.put(numDraftCMOs, "NONE");
                break;
            case 1:
                CaseManagementOrder cmo = cmosReadyForApproval.get(0).getValue();
                data.put(numDraftCMOs, "SINGLE");
                data.put("reviewCMODecision",
                    ReviewDecision.builder().hearing(cmo.getHearing()).document(cmo.getOrder()).build());
                break;
            default:
                data.put(numDraftCMOs, "MULTI");
                data.put("cmoToReviewList", buildUnselectedDynamicList(caseData));
                break;
        }

        return data;
    }

    public Element<CaseManagementOrder> getCMOToSeal(CaseData caseData) {
        Element<CaseManagementOrder> cmo = getSelectedCMO(caseData);
        DocumentReference order;

        if (JUDGE_AMENDS_DRAFT.equals(caseData.getReviewCMODecision().getDecision())) {
            order = caseData.getReviewCMODecision().getJudgeAmendedDocument();
        } else {
            order = cmo.getValue().getOrder();
        }
        return element(cmo.getValue().toBuilder()
            .dateIssued(time.now().toLocalDate())
            .status(CMOStatus.APPROVED)
            .order(order)
            .build());
    }

    public List<Element<CaseManagementOrder>> getCMOsReadyForApproval(CaseData caseData) {
        return caseData.getDraftUploadedCMOs().stream()
            .filter(cmo -> cmo.getValue().getStatus().equals(SEND_TO_JUDGE))
            .collect(Collectors.toList());
    }

    public Element<CaseManagementOrder> getSelectedCMO(CaseData caseData) {
        List<Element<CaseManagementOrder>> readyForApproval = getCMOsReadyForApproval(caseData);
        if (readyForApproval.size() > 1) {
            UUID selectedCMOCode = getSelectedCMOId(caseData.getCmoToReviewList());

            return caseData.getDraftUploadedCMOs().stream()
                .filter(element -> element.getId().equals(selectedCMOCode))
                .findFirst()
                .orElseThrow(() -> new CMONotFoundException("Could not find draft cmo with id " + selectedCMOCode));
        } else {
            return readyForApproval.get(0);
        }
    }

    public CaseManagementOrder getLatestSealedCMO(CaseData caseData) {
        List<Element<CaseManagementOrder>> sealedCMOs = caseData.getSealedCMOs();
        if (!sealedCMOs.isEmpty()) {
            return sealedCMOs.get(sealedCMOs.size() - 1).getValue();
        } else {
            throw new CMONotFoundException("No sealed CMOS found");
        }
    }

    public State getStateBasedOnNextHearing(CaseData caseData, UUID cmoID) {
        State currentState = caseData.getState();
        if (featureToggleService.isNewCaseStateModelEnabled()) {
            Optional<HearingBooking> nextHearingBooking = caseData.getNextHearingAfterCmo(cmoID);

            if (nextHearingBooking.isPresent()
                && caseData.getReviewCMODecision().hasReviewOutcomeOf(SEND_TO_ALL_PARTIES)
                && nextHearingBooking.get().isOfType(HearingType.FINAL)) {
                return State.FINAL_HEARING;
            }
        }
        return currentState;
    }

    private UUID getSelectedCMOId(Object dynamicList) {
        //see RDM-5696 and RDM-6651
        if (dynamicList instanceof String) {
            return UUID.fromString(dynamicList.toString());
        }
        return mapper.convertValue(dynamicList, DynamicList.class).getValueCode();
    }
}
