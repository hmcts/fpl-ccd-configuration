package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.UploadCMOEventData;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder.from;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListValueCode;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadCMOService {

    private final ObjectMapper mapper;
    private final Time time;

    public UploadCMOEventData getInitialPageData(List<Element<HearingBooking>> hearings,
                                                 List<Element<CaseManagementOrder>> unsealedOrders) {

        List<Element<HearingBooking>> hearingsWithoutCMOs = getHearingsWithoutCMO(hearings, unsealedOrders);
        UploadCMOEventData.UploadCMOEventDataBuilder eventBuilder = UploadCMOEventData.builder();
        String textAreaContent = buildHearingsWithCMOsText(unsealedOrders, hearings);

        switch (hearingsWithoutCMOs.size()) {
            case 0:
                eventBuilder.numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.NONE);
                break;
            case 1:
                eventBuilder.numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.SINGLE);

                if (textAreaContent.length() != 0) {
                    eventBuilder.singleHearingWithCMO(textAreaContent);
                    eventBuilder.showHearingsSingleTextArea(YesNo.YES);
                }

                addJudgeAndHearingDetails(hearingsWithoutCMOs.get(0).getValue(), eventBuilder, true);
                break;
            default:
                eventBuilder.numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.MULTI);

                if (textAreaContent.length() != 0) {
                    eventBuilder.multiHearingsWithCMOs(textAreaContent);
                    eventBuilder.showHearingsMultiTextArea(YesNo.YES);
                }

                eventBuilder.hearingsWithoutApprovedCMO(buildDynamicList(hearingsWithoutCMOs));
        }

        return eventBuilder.build();
    }

    public UploadCMOEventData prepareJudgeAndHearingDetails(Object dynamicList,
                                                            List<Element<HearingBooking>> hearings,
                                                            List<Element<CaseManagementOrder>> unsealedOrders) {
        /*
         When dynamic lists are fixed unsealedOrders shouldn't need passing, we can just remove the statement below
         as hearings is just a superset of hearingsWithoutCMO.
         Currently it is used to get the hearings to rebuild the dynamic list and as a byproduct the filtered list of
         hearings has a reduced search space for get getSelectedHearing.
        */
        List<Element<HearingBooking>> hearingsWithoutCMO = getHearingsWithoutCMO(hearings, unsealedOrders);
        UUID selectedHearingId = getSelectedHearingId(dynamicList, hearingsWithoutCMO);
        HearingBooking selectedHearing = getSelectedHearing(selectedHearingId, hearingsWithoutCMO);

        UploadCMOEventData.UploadCMOEventDataBuilder eventBuilder = UploadCMOEventData.builder();

        addJudgeAndHearingDetails(selectedHearing, eventBuilder, false);

        if (!(dynamicList instanceof DynamicList)) {
            /*
             reconstruct dynamic list
             see RDM-5696 and RDM-6651
            */
            eventBuilder.hearingsWithoutApprovedCMO(buildDynamicList(hearingsWithoutCMO, selectedHearingId));
        }

        return eventBuilder.build();
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

        return getDynamicListValueCode(dynamicList, mapper);
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
        return asDynamicList(hearings, selected, hearing -> hearing.toLabel(DATE));
    }

    private void addJudgeAndHearingDetails(HearingBooking hearing,
                                           UploadCMOEventData.UploadCMOEventDataBuilder builder,
                                           boolean initialPage) {

        builder.cmoJudgeInfo(formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()));

        if (initialPage) {
            builder.cmoHearingInfo(format(
                "Send agreed CMO for %s.%nThis must have been discussed by all parties at the hearing.",
                hearing.toLabel(DATE)
            ));
        } else {
            builder.cmoHearingInfo(hearing.toLabel(DATE));
        }

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
        return filtered.stream().map(value -> value.toLabel(DATE)).collect(Collectors.joining("\n"));
    }
}
