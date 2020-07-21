package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadCMOService {
    private final ObjectMapper mapper;

    private static final String SINGLE = "SINGLE";
    private static final String MULTI = "MULTI";
    private static final String NONE = "NONE";
    public static final String[] TRANSIENT_FIELDS = {
        "uploadedCaseManagementOrder", "pastHearingList", "cmoJudgeInfo", "cmoHearingInfo", "numHearings",
        "singleHearingsWithCMOs", "multiHearingsWithCMOs", "showHearingsSingleTextArea", "showHearingsMultiTextArea"
    };

    public Map<String, Object> getInitialPageData(List<Element<HearingBooking>> hearings,
                                                  List<Element<CaseManagementOrder>> unsealedOrders) {

        List<Element<HearingBooking>> pastHearings = getHearingsWithoutCMO(hearings);

        Map<String, Object> data = new HashMap<>();
        String textAreaKey = null;
        String numHearings;
        String showTextAreaKey = null;

        switch (pastHearings.size()) {
            case 0:
                numHearings = NONE;
                break;
            case 1:
                numHearings = SINGLE;
                textAreaKey = "singleHearingsWithCMOs";
                showTextAreaKey = "showHearingsSingleTextArea";
                data.putAll(getJudgeAndHearingDetailsSingle(pastHearings.get(0).getId(), pastHearings));
                break;
            default:
                numHearings = MULTI;
                textAreaKey = "multiHearingsWithCMOs";
                showTextAreaKey = "showHearingsMultiTextArea";
                data.put("pastHearingList", buildDynamicList(pastHearings));
        }

        String textAreaContent = buildHearingsWithCMOsText(unsealedOrders, hearings);

        if (textAreaContent.length() != 0 && textAreaKey != null) {
            data.put(textAreaKey, textAreaContent);
            data.put(showTextAreaKey, "YES");
        }

        data.put("numHearings", numHearings);

        return data;
    }

    public List<Element<HearingBooking>> getHearingsWithoutCMO(List<Element<HearingBooking>> hearings) {
        return hearings.stream()
            .filter(hearing -> !hearing.getValue().hasCMOAssociation())
            .collect(toList());
    }

    public Map<String, Object> getJudgeAndHearingDetails(UUID selectedHearing,
                                                         List<Element<HearingBooking>> hearings) {
        HearingBooking selected = getSelectedHearing(selectedHearing, hearings);
        return Map.of(
            "cmoJudgeInfo", formatJudgeTitleAndName(selected.getJudgeAndLegalAdvisor()),
            "cmoHearingInfo", selected.toLabel(DATE)
        );
    }

    public void mapToHearing(UUID selectedHearing, List<Element<HearingBooking>> hearings,
                             Element<CaseManagementOrder> cmo) {
        getSelectedHearing(selectedHearing, hearings).setCaseManagementOrderId(cmo.getId());
    }

    public UUID getSelectedHearingId(Object dynamicList, List<Element<HearingBooking>> hearings) {
        if (hearings.size() == 1) {
            return hearings.get(0).getId();
        }

        //see RDM-5696 and RDM-6651
        if (dynamicList instanceof String) {
            return UUID.fromString(dynamicList.toString());
        }
        return mapper.convertValue(dynamicList, DynamicList.class).getValueCode();
    }

    public HearingBooking getSelectedHearing(UUID id, List<Element<HearingBooking>> hearings) {
        return findElement(id, hearings)
            .orElseThrow(() -> new HearingNotFoundException("No hearing found with id: " + id))
            .getValue();
    }

    public DynamicList buildDynamicList(List<Element<HearingBooking>> hearings) {
        return buildDynamicList(hearings, null);
    }

    public DynamicList buildDynamicList(List<Element<HearingBooking>> hearings, UUID selected) {
        return asDynamicList(hearings, selected, (hearing) -> hearing.toLabel(DATE));
    }

    private Map<String, Object> getJudgeAndHearingDetailsSingle(UUID selectedHearing,
                                                                List<Element<HearingBooking>> hearings) {
        Map<String, Object> details = new HashMap<>(getJudgeAndHearingDetails(selectedHearing, hearings));
        String updated = format("Send agreed CMO for %s.\nThis must have been discussed by all hearings at the party.",
            details.get("cmoHearingInfo"));
        details.put("cmoHearingInfo", updated);
        return details;
    }

    private String buildHearingsWithCMOsText(
        List<Element<CaseManagementOrder>> unsealedOrders,
        List<Element<HearingBooking>> hearings) {

        StringBuilder builder = new StringBuilder();

        hearings.stream()
            .filter(hearing -> unsealedOrders.stream()
                .anyMatch(order -> order.getId().equals(hearing.getValue().getCaseManagementOrderId())))
            .map(Element::getValue)
            .sorted(Comparator.comparing(HearingBooking::getStartDate))
            .forEach(hearing -> builder.append(hearing.toLabel(DATE)).append("\n"));

        return builder.toString();
    }
}
