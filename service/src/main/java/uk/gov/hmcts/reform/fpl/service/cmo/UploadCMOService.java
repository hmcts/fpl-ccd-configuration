package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder.from;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadCMOService {
    private static final String CMO_HEARING_INFO_FIELD = "cmoHearingInfo";
    private static final String CMO_JUDGE_INFO_FIELD = "cmoJudgeInfo";
    private static final String PAST_HEARING_LIST_FIELD = "hearingsWithoutApprovedCMO";
    private static final String NUM_HEARINGS_FIELD = "numHearingsWithoutCMO";

    private static final String SINGLE = "SINGLE";
    private static final String MULTI = "MULTI";
    private static final String NONE = "NONE";

    private final ObjectMapper mapper;
    private final Time time;

    public Map<String, Object> getInitialPageData(List<Element<HearingBooking>> hearings,
                                                  List<Element<CaseManagementOrder>> unsealedOrders) {

        List<Element<HearingBooking>> hearingsWithoutCMOs = getHearingsWithoutCMO(hearings, unsealedOrders);

        Map<String, Object> data = new HashMap<>();
        String textAreaKey = null;
        String numHearingsWithoutCMO;
        String showTextAreaKey = null;

        switch (hearingsWithoutCMOs.size()) {
            case 0:
                numHearingsWithoutCMO = NONE;
                break;
            case 1:
                numHearingsWithoutCMO = SINGLE;
                textAreaKey = "singleHearingWithCMO";
                showTextAreaKey = "showHearingsSingleTextArea";

                Map<String, Object> details = new HashMap<>(getJudgeAndHearingDetails(
                    hearingsWithoutCMOs.get(0).getValue()));
                String updated = format(
                    "Send agreed CMO for %s.%nThis must have been discussed by all parties at the hearing.",
                    details.get(CMO_HEARING_INFO_FIELD)
                );

                details.put(CMO_HEARING_INFO_FIELD, updated);
                data.putAll(details);
                break;
            default:
                numHearingsWithoutCMO = MULTI;
                textAreaKey = "multiHearingsWithCMOs";
                showTextAreaKey = "showHearingsMultiTextArea";
                data.put(PAST_HEARING_LIST_FIELD, buildDynamicList(hearingsWithoutCMOs));
        }

        String textAreaContent = buildHearingsWithCMOsText(unsealedOrders, hearings);

        if (textAreaContent.length() != 0 && textAreaKey != null) {
            data.put(textAreaKey, textAreaContent);
            data.put(showTextAreaKey, "YES");
        }

        data.put(NUM_HEARINGS_FIELD, numHearingsWithoutCMO);

        return data;
    }

    public Map<String, Object> prepareJudgeAndHearingDetails(Object dynamicList,
                                                             List<Element<HearingBooking>> hearings,
                                                             List<Element<CaseManagementOrder>> unsealedOrders) {
        // When dynamic lists are fixed unsealedOrders shouldn't need passing, we can just remove the statement below
        // as hearings is just a superset of hearingsWithoutCMO.
        // Currently it is used to get the hearings to rebuild the dynamic list and as a byproduct the filtered list of
        // hearings has a reduced search space for get getSelectedHearing.
        List<Element<HearingBooking>> hearingsWithoutCMO = getHearingsWithoutCMO(hearings, unsealedOrders);
        UUID selectedHearingId = getSelectedHearingId(dynamicList, hearingsWithoutCMO);
        HearingBooking selectedHearing = getSelectedHearing(selectedHearingId, hearingsWithoutCMO);
        Map<String, Object> data = new HashMap<>(getJudgeAndHearingDetails(selectedHearing));

        if (!(dynamicList instanceof DynamicList)) {
            // reconstruct dynamic list
            //see RDM-5696 and RDM-6651
            data.put(PAST_HEARING_LIST_FIELD, buildDynamicList(hearingsWithoutCMO, selectedHearingId));
        }

        return data;
    }

    public void updateHearingsAndUnsealedCMOs(List<Element<HearingBooking>> hearings,
                                              List<Element<CaseManagementOrder>> unsealedOrders,
                                              DocumentReference uploadedOrder,
                                              Object dynamicList) {
        List<Element<HearingBooking>> filteredHearings = getHearingsWithoutCMO(hearings, unsealedOrders);
        UUID selectedHearingId = getSelectedHearingId(dynamicList, filteredHearings);
        HearingBooking hearing = getSelectedHearing(selectedHearingId, filteredHearings);

        Element<CaseManagementOrder> element = element(from(uploadedOrder, hearing, time.now().toLocalDate()));

        Optional<UUID> uuid = updateHearingWithCmoId(selectedHearingId, hearings, element);

        if (uuid.isPresent()) {
            // overwrite old draft CMO
            int index = -1;
            for (int i = 0; i < unsealedOrders.size(); i++) {
                if (unsealedOrders.get(i).getId().equals(uuid.get())) {
                    index = i;
                    break;
                }
            }
            unsealedOrders.set(index, element);
        } else {
            unsealedOrders.add(element);
        }
    }

    public boolean isNewCmoUploaded(List<Element<CaseManagementOrder>> cmosCurrent,
                                    List<Element<CaseManagementOrder>> cmosBefore) {
        // Duplicate to not effect passed list
        List<Element<CaseManagementOrder>> current = new ArrayList<>(cmosCurrent);

        current.removeAll(cmosBefore);

        return !current.isEmpty();
    }

    private List<Element<HearingBooking>> getHearingsWithoutCMO(List<Element<HearingBooking>> hearings,
                                                                List<Element<CaseManagementOrder>> unsealedOrders) {
        return hearings.stream()
            .filter(hearing -> associatedToReturnedCMO(hearing, unsealedOrders)
                || !hearing.getValue().hasCMOAssociation())
            .collect(toList());
    }

    private Optional<UUID> updateHearingWithCmoId(UUID selectedHearing, List<Element<HearingBooking>> hearings,
                                                  Element<CaseManagementOrder> cmo) {
        HearingBooking hearing = getSelectedHearing(selectedHearing, hearings);
        UUID previousCMOId = hearing.getCaseManagementOrderId();
        hearing.setCaseManagementOrderId(cmo.getId());
        return Optional.ofNullable(previousCMOId);
    }

    private UUID getSelectedHearingId(Object dynamicList, List<Element<HearingBooking>> hearings) {
        if (hearings.size() == 1) {
            return hearings.get(0).getId();
        }

        //see RDM-5696 and RDM-6651
        if (dynamicList instanceof String) {
            return UUID.fromString(dynamicList.toString());
        }
        return mapper.convertValue(dynamicList, DynamicList.class).getValueCode();
    }

    private HearingBooking getSelectedHearing(UUID id, List<Element<HearingBooking>> hearings) {
        return findElement(id, hearings)
            .orElseThrow(() -> new HearingNotFoundException("No hearing found with id: " + id))
            .getValue();
    }

    private DynamicList buildDynamicList(List<Element<HearingBooking>> hearings) {
        return buildDynamicList(hearings, null);
    }

    private DynamicList buildDynamicList(List<Element<HearingBooking>> hearings, UUID selected) {
        return asDynamicList(hearings, selected, HearingBooking::toLabel);
    }

    private Map<String, Object> getJudgeAndHearingDetails(HearingBooking hearing) {
        return Map.of(
            CMO_JUDGE_INFO_FIELD, formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()),
            CMO_HEARING_INFO_FIELD, hearing.toLabel()
        );
    }

    private boolean associatedToReturnedCMO(Element<HearingBooking> hearing,
                                            List<Element<CaseManagementOrder>> unsealedCMOs) {
        return unsealedCMOs.stream()
            .filter(cmo -> cmo.getValue().getStatus() == RETURNED)
            .anyMatch(cmo -> cmo.getId().equals(hearing.getValue().getCaseManagementOrderId()));
    }

    private String buildHearingsWithCMOsText(List<Element<CaseManagementOrder>> unsealedOrders,
                                             List<Element<HearingBooking>> hearings) {

        List<HearingBooking> filtered = new ArrayList<>();
        hearings.forEach(hearing -> unsealedOrders.stream()
            .filter(order -> RETURNED != order.getValue().getStatus()
                && order.getId().equals(hearing.getValue().getCaseManagementOrderId()))
            .map(order -> hearing.getValue())
            .findFirst()
            .ifPresent(filtered::add)
        );

        filtered.sort(Comparator.comparing(HearingBooking::getStartDate));
        return filtered.stream().map(HearingBooking::toLabel).collect(Collectors.joining("\n"));
    }
}
