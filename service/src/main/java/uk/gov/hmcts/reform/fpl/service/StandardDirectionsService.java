package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CustomDirection;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.Directions.getAssigneeToDirectionMapping;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionsService {
    private final CalendarService calendarService;
    private final OrdersLookupService ordersLookupService;
    private final CommonDirectionService commonDirectionService;
    private final ObjectMapper objectMapper;

    public boolean hasEmptyDates(CaseData caseData) {
        return Stream.of(caseData.getAllParties(),
            caseData.getLocalAuthorityDirections(),
            caseData.getRespondentDirections(),
            caseData.getCafcassDirections(),
            caseData.getOtherPartiesDirections(),
            caseData.getCourtDirections())
            .flatMap(Collection::stream)
            .map(Element::getValue)
            .map(Direction::getDateToBeCompletedBy)
            .anyMatch(Objects::isNull);
    }

    public boolean hasEmptyDirections(CaseData caseData) {
        return Stream.of(caseData.getAllParties(),
            caseData.getLocalAuthorityDirections(),
            caseData.getRespondentDirections(),
            caseData.getCafcassDirections(),
            caseData.getOtherPartiesDirections(),
            caseData.getCourtDirections())
            .allMatch(ObjectUtils::isEmpty);
    }

    public Map<String, List<Element<Direction>>> populateStandardDirections(CaseData caseData) {
        List<Element<Direction>> directions = getDirections(caseData.getFirstHearingOfType(CASE_MANAGEMENT));

        return getAssigneeToDirectionMapping(directions).entrySet().stream()
            .collect(toMap(pair -> pair.getKey().getValue(), Map.Entry::getValue));
    }

    public List<Element<Direction>> getDirections(HearingBooking hearingBooking) {
        return getDirections(Optional.ofNullable(hearingBooking));
    }


    private List<Direction> getDirections(CaseData caseData) {
        return Stream.of(caseData.getAllParties(),
            caseData.getLocalAuthorityDirections(),
            caseData.getRespondentDirections(),
            caseData.getCafcassDirections(),
            caseData.getOtherPartiesDirections(),
            caseData.getCourtDirections())
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .map(Element::getValue)
            .collect(Collectors.toList());
    }


    private StandardDirection getFromTemplate(DirectionType directionType, List<Direction> templateDirections) {
        DirectionConfiguration directionConf = ordersLookupService.getDirectionConfiguration(directionType);

        Optional<StandardDirection> vv = templateDirections.stream()
            .filter(x -> x.getDirectionType().equals(directionConf.getTitle()))
            .findFirst()
            .map(d -> StandardDirection.builder()
                .type(directionType)
                .assignee(directionConf.getAssignee())
                .description(d.getDirectionText())
                .title(d.getDirectionType())
                .dateToBeCompletedBy(d.getDateToBeCompletedBy())
                .daysBeforeHearing(-1 * Integer.valueOf(directionConf.getDisplay().getDelta()))
                .showDateOnly(YesNo.from(directionConf.getDisplay().isShowDateOnly()))
                .build());

        return vv.orElseGet(() -> StandardDirection.builder()
            .type(directionType)
            .assignee(directionConf.getAssignee())
            .title(directionConf.getTitle())
            .description(directionConf.getText())
            .build());
    }


    public Map<String, Object> getRequestedDirections(CaseData caseData) {

        Map<String, Object> data = new HashMap<>();
        List<DirectionType> requested = caseData.getGatekeepingOrderEventData().getRequestedDirections();
        List<Direction> templateDirections = getDirections(caseData);
        List<StandardDirection> drafted = nullSafeList(caseData.getStandardDirections()).stream().map(Element::getValue).collect(toList());


        requested.forEach(requestedType -> {
            StandardDirection dir = drafted.stream()
                .filter(x -> x.getType().equals(requestedType)).findFirst()
                .orElseGet(() -> getFromTemplate(requestedType, templateDirections));

            data.put("sdoDirection-" + requestedType.name(), dir);
        });


//        List<Element<Direction>> customDirections = new ArrayList<>();
//
//        customDirections.addAll(defaultIfNull(caseData.getAllPartiesCustom(), new ArrayList<>()));
//        customDirections.addAll(defaultIfNull(caseData.getLocalAuthorityDirectionsCustom(), new ArrayList<>()));
//        customDirections.addAll(defaultIfNull(caseData.getRespondentDirectionsCustom(), new ArrayList<>()));
//        customDirections.addAll(defaultIfNull(caseData.getCafcassDirectionsCustom(), new ArrayList<>()));
//        customDirections.addAll(defaultIfNull(caseData.getOtherPartiesDirectionsCustom(), new ArrayList<>()));
//        customDirections.addAll(defaultIfNull(caseData.getCourtDirectionsCustom(), new ArrayList<>()));

//        List<Element<CustomDirection>> allCustomDirections = customDirections.stream()
//            .map(customDirection -> {
//                CustomDirection cc = CustomDirection.builder()
//                    .title(customDirection.getValue().getDirectionType())
//                    .description(customDirection.getValue().getDirectionText())
//                    .assignee(customDirection.getValue().getAssignee())
//                    .dateToBeCompletedBy(customDirection.getValue().getDateToBeCompletedBy())
//                    .build();
//                return element(customDirection.getId(), cc);
//            })
//            .collect(Collectors.toList());

//        data.put("sdoDirectionCustom", allCustomDirections);
//        data.remove("allPartiesCustom");
//        data.remove("localAuthorityDirectionsCustom");
//        data.remove("respondentDirectionsCustom");
//        data.remove("cafcassDirectionsCustom");
//        data.remove("otherPartiesDirectionsCustom");
//        data.remove("courtDirectionsCustom");

        return data;

    }


    @SuppressWarnings("unchecked")
    private List<DirectionType> directions(CaseDetails caseDetails, String filed) {
        return ((List<String>) caseDetails.getData().get(filed)).stream()
            .map(t -> EnumUtils.getEnum(DirectionType.class, t))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public StandardDirection find(DirectionType type, List<Element<Direction>> directions) {
        DirectionConfiguration conf = ordersLookupService.getStandardDirectionOrder().getDirections().stream()
            .filter(def -> def.getId().equals(type))
            .findFirst()
            .orElse(null);

        List<Element<Direction>> dirs = defaultIfNull(directions, new ArrayList<>());
        return dirs.stream().filter(d -> d.getValue().getDirectionType().equals(conf.getTitle()))
            .map(Element::getValue)
            .map(dir -> StandardDirection.builder()
                .type(type)
                .description(dir.getDirectionText())
                .dateToBeCompletedBy(dir.getDateToBeCompletedBy())
                .build())
            .findFirst()
            .orElse(null);
    }

    public void addOrUpdateDirections(CaseData caseData, CaseDetails caseDetails) {

        List<Element<Direction>> allDirections = commonDirectionService.combineAllDirections(caseData);
        allDirections.forEach(direction -> direction.getValue().setDirectionNeeded(YesNo.NO.getValue()));

        List<Element<CustomDirection>> customDirections = defaultIfNull(caseData.getSdoDirectionCustom(), new ArrayList<>());
        List<Element<StandardDirection>> directions = new ArrayList<>();

        caseData.setSdoDirectionCustom(customDirections);
        caseData.setStandardDirections(directions);

        Stream.of(DirectionType.values()).forEach(type -> {
            CustomDirection newDirection = objectMapper.convertValue(caseDetails.getData().get("sdoDirection-" + type), CustomDirection.class);
            DirectionConfiguration directionDef = ordersLookupService.getStandardDirectionOrder().getDirections().stream()
                .filter(def -> def.getId().equals(type))
                .findFirst()
                .orElse(null);

            if (newDirection != null) {
                directions.add(ElementUtils.element(newDirection.toBuilder()
                    .type(directionDef.getId())
                    .title(directionDef.getTitle())
                    .assignee(directionDef.getAssignee())
                    .description(directionDef.getDisplay().isShowDateOnly() ? directionDef.getText() : newDirection.getDescription())
                    .build()));
            }
        });
    }

    private List<Element<Direction>> getDirections(Optional<HearingBooking> hearingBooking) {
        LocalDateTime hearingStartDate = hearingBooking.map(HearingBooking::getStartDate).orElse(null);

        return ordersLookupService.getStandardDirectionOrder().getDirections()
            .stream()
            .map(configuration -> constructDirectionForCCD(hearingStartDate, configuration))
            .collect(toList());
    }

    private Element<Direction> constructDirectionForCCD(LocalDateTime hearingDate, DirectionConfiguration direction) {
        LocalDateTime dateToBeCompletedBy = ofNullable(hearingDate)
            .map(date -> getCompleteByDate(date, direction.getDisplay()))
            .orElse(null);

        return element(Direction.builder()
            .directionType(direction.getTitle())
            .directionText(direction.getText())
            .assignee(direction.getAssignee())
            .directionNeeded(YES.getValue())
            .directionRemovable(booleanToYesOrNo(direction.getDisplay().isDirectionRemovable()))
            .readOnly(booleanToYesOrNo(direction.getDisplay().isShowDateOnly()))
            .dateToBeCompletedBy(dateToBeCompletedBy)
            .build());
    }

    private LocalDateTime getCompleteByDate(LocalDateTime startDate, Display display) {
        return ofNullable(display.getDelta())
            .map(delta -> addDelta(startDate, parseInt(delta)))
            .map(date -> getLocalDateTime(display.getTime(), date))
            .orElse(null);
    }

    private LocalDateTime getLocalDateTime(String time, LocalDate date) {
        return ofNullable(time).map(item -> LocalDateTime.of(date, LocalTime.parse(item))).orElse(date.atStartOfDay());
    }

    private LocalDate addDelta(LocalDateTime date, int delta) {
        if (delta == 0) {
            return date.toLocalDate();
        }
        return calendarService.getWorkingDayFrom(date.toLocalDate(), delta);
    }

    private String booleanToYesOrNo(boolean value) {
        return value ? "Yes" : "No";
    }
}
