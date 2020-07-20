package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Directions;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.docmosis.CaseManagementOrderGenerationService;

import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.CMO;
import static uk.gov.hmcts.reform.fpl.model.Directions.getAssigneeToDirectionMapping;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderService {
    private final CaseManagementOrderGenerationService templateDataGenerationService;
    private final DocumentService documentService;
    private final ObjectMapper mapper;

    private static final String SINGLE = "SINGLE";
    private static final String MULTI = "MULTI";
    private static final String NONE = "NONE";
    public static final String[] TRANSIENT_FIELDS = {
        "uploadedCaseManagementOrder", "pastHearingSelector", "cmoJudgeInfo", "cmoHearingInfo", "numHearings",
        "singleHearingsWithCMOs", "multiHearingsWithCMOs", "showHearingsTextArea"
    };

    public Map<String, Object> getInitialPageData(
        List<Element<HearingBooking>> hearings,
        List<Element<uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder>> unsealedOrders) {

        List<Element<HearingBooking>> pastHearings = getHearingsWithoutCMO(hearings);

        Map<String, Object> data = new HashMap<>();
        String textAreaKey = null;
        String numHearings;
        String showTextArea = null;

        switch (pastHearings.size()) {
            case 0:
                numHearings = NONE;
                break;
            case 1:
                numHearings = SINGLE;
                textAreaKey = "singleHearingsWithCMOs";
                showTextArea = SINGLE;
                data.putAll(getJudgeAndHearingDetailsSingle(pastHearings.get(0).getId(), pastHearings));
                break;
            default:
                numHearings = MULTI;
                textAreaKey = "multiHearingsWithCMOs";
                showTextArea = MULTI;
                data.put("pastHearingSelector", buildDynamicList(pastHearings));
        }

        String textAreaContent = buildHearingsWithCMOsText(unsealedOrders, hearings);

        if (textAreaContent.length() != 0 && textAreaKey != null) {
            data.put(textAreaKey, textAreaContent);
            data.put("showHearingsTextArea", showTextArea);
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
                             Element<uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder> cmo) {
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
        List<Element<uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder>> unsealedOrders,
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

    @Deprecated
    public Document getOrderDocument(CaseData caseData) {
        DocmosisCaseManagementOrder templateData = templateDataGenerationService.getTemplateData(caseData);
        return documentService.getDocumentFromDocmosisOrderTemplate(templateData, CMO);
    }

    @Deprecated
    public void removeTransientObjectsFromCaseData(Map<String, Object> caseData) {
        final Set<String> keysToRemove = Set.of(HEARING_DATE_LIST.getKey(), SCHEDULE.getKey(), RECITALS.getKey());

        keysToRemove.forEach(caseData::remove);
    }

    @Deprecated
    public void prepareCustomDirections(CaseDetails caseDetails, CaseManagementOrder caseManagementOrder) {
        ofNullable(caseManagementOrder).ifPresentOrElse(
            order -> addDirections(caseDetails, order.getDirections()), () -> removeDirections(caseDetails));
    }

    @Deprecated
    public DynamicList getHearingDateDynamicList(CaseData caseData, CaseManagementOrder order) {
        List<DynamicListElement> values = getDateElements(caseData, false);

        DynamicListElement selectedValue = ofNullable(order)
            .map(x -> getPreselectedDate(values, x.getId()))
            .orElse(DynamicListElement.EMPTY);

        return DynamicList.builder()
            .listItems(values)
            .value(selectedValue)
            .build();
    }

    @Deprecated
    public DynamicList getNextHearingDateDynamicList(CaseData caseData) {
        return DynamicList.builder()
            .listItems(getDateElements(caseData, true))
            .value(DynamicListElement.EMPTY)
            .build();
    }

    @Deprecated
    private void addDirections(CaseDetails caseDetails, List<Element<Direction>> directions) {
        getAssigneeToDirectionMapping(directions)
            .forEach((key, value) -> caseDetails.getData().put(key.toCaseManagementOrderDirectionField(), value));
    }

    @Deprecated
    private void removeDirections(CaseDetails caseDetails) {
        Stream.of(Directions.class.getDeclaredFields()).forEach(field -> caseDetails.getData().remove(field.getName()));
    }

    @Deprecated
    private List<DynamicListElement> getDateElements(CaseData caseData, boolean excludePastDates) {
        var sealedCmoHearingDateIds = getSealedCmoHearingDateIds(caseData);

        var hearingDetailsStream = caseData.getHearingDetails().stream()
            .filter(hearingBooking -> !sealedCmoHearingDateIds.contains(hearingBooking.getId()));
        if (excludePastDates) {
            hearingDetailsStream = hearingDetailsStream
                .filter(hearingBooking -> hearingBooking.getValue().startsAfterToday());
        }

        return hearingDetailsStream.map(this::buildDynamicListElement).collect(toList());
    }

    @Deprecated
    private Set<UUID> getSealedCmoHearingDateIds(CaseData caseData) {
        return caseData.getServedCaseManagementOrders()
            .stream()
            .map(e -> e.getValue().getId())
            .collect(toSet());
    }

    @Deprecated
    private DynamicListElement buildDynamicListElement(Element<HearingBooking> element) {
        return DynamicListElement.builder()
            .label(formatLocalDateToString(element.getValue().getStartDate().toLocalDate(), FormatStyle.MEDIUM))
            .code(element.getId())
            .build();
    }

    @Deprecated
    private DynamicListElement getPreselectedDate(List<DynamicListElement> list, UUID id) {
        return list.stream()
            .filter(item -> item.getCode().equals(id))
            .findFirst()
            .orElse(DynamicListElement.EMPTY);
    }
}
