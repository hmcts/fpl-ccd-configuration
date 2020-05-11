package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.NextHearing;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.NEXT_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.values;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createElementCollection;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingsFromInitialDate;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createUnassignedDirection;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, CommonDirectionService.class, DraftCMOService.class, FixedTimeConfiguration.class
})
class DraftCMOServiceTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DraftCMOService service;

    @Autowired
    private Time time;

    private CaseManagementOrder caseManagementOrder;
    private List<Element<HearingBooking>> hearingDetails;

    @BeforeEach
    void setUp() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now());
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenCaseDetailsHasHearingDate() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now().plusDays(5));
        caseManagementOrder = CaseManagementOrder.builder().build();

        DynamicList data = service.getHearingDateDynamicList(hearingDetails, caseManagementOrder);

        assertThat(data.getListItems())
            .containsAll(Arrays.asList(
                DynamicListElement.builder()
                    .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                    .label(formatLocalDateToMediumStyle(10))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                    .label(formatLocalDateToMediumStyle(7))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                    .label(formatLocalDateToMediumStyle(5))
                    .build()));
    }

    @Test
    void shouldNotReturnHearingDatesWhenHearingDateIsInThePast() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now().minusDays(10));
        caseManagementOrder = CaseManagementOrder.builder().build();

        DynamicList data = service.getHearingDateDynamicList(hearingDetails, caseManagementOrder);

        assertThat(data.getListItems()).isEmpty();
    }

    @Test
    void shouldReturnHearingDatesWhenHearingDateIsSameDayButLaterTime() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now().plusMinutes(5));
        caseManagementOrder = CaseManagementOrder.builder().build();

        DynamicList data = service.getHearingDateDynamicList(hearingDetails, caseManagementOrder);

        assertThat(data.getListItems())
            .containsAll(Arrays.asList(
                DynamicListElement.builder()
                    .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                    .label(formatLocalDateToMediumStyle(5))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                    .label(formatLocalDateToMediumStyle(2))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                    .label(formatLocalDateToMediumStyle(0))
                    .build()));
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenCmoHasPreviousSelectedValue() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now());
        caseManagementOrder = createCaseManagementOrder();

        DynamicList data = service.getHearingDateDynamicList(hearingDetails, caseManagementOrder);

        assertThat(data.getValue())
            .isEqualTo(DynamicListElement.builder()
                .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                .label(formatLocalDateToMediumStyle(2))
                .build());
    }

    @Test
    void shouldReturnCaseManagementOrderWhenProvidedCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();

        Stream.of(values()).forEach(direction ->
            caseData.put(direction.toCustomDirectionField().concat("CMO"),
                createElementCollection(createUnassignedDirection()))
        );

        caseData.put(HEARING_DATE_LIST.getKey(), getDynamicList());
        caseData.put(NEXT_HEARING_DATE_LIST.getKey(), getDynamicList());
        caseData.put(ORDER_ACTION.getKey(), baseOrderActionWithType().document(buildFromDocument(document())).build());

        CaseManagementOrder caseManagementOrder = service.prepareCaseManagementOrder(
            mapper.convertValue(caseData, CaseData.class));

        assertThat(caseManagementOrder).isEqualToComparingFieldByField(CaseManagementOrder.builder()
            .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
            .hearingDate(formatLocalDateToMediumStyle(5))
            .directions(createCmoDirections())
            .action(baseOrderActionWithType().build())
            .nextHearing(NextHearing.builder()
                .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                .date(formatLocalDateToMediumStyle(5))
                .build())
            .build());
    }

    @Test
    void shouldNotOverwriteHearingDateWithNullWhenHearingDateListIsEmptyAndValueAlreadyExistsInCmo() {
        CaseManagementOrder orderWithHearingDate = CaseManagementOrder.builder()
            .status(CMOStatus.SEND_TO_JUDGE)
            .hearingDate("1 May 2020")
            .directions(emptyList())
            .build();

        CaseData caseData = CaseData.builder().caseManagementOrder(orderWithHearingDate).build();

        CaseManagementOrder updatedOrder = service.prepareCaseManagementOrder(caseData);

        assertThat(updatedOrder).isEqualToComparingFieldByField(orderWithHearingDate);
    }

    @Test
    void shouldMoveDirectionsToCaseDetailsWhenCMOExistsWithDirections() {
        Map<String, Object> caseData = new HashMap<>();

        service.prepareCustomDirections(CaseDetails.builder().data(caseData).build(),
            CaseManagementOrder.builder()
                .directions(createCmoDirections())
                .build());

        assertThat(caseData).containsKeys("allParties", "localAuthorityDirections", "cafcassDirections",
            "courtDirections", "otherPartiesDirections", "respondentDirections");
    }

    @Test
    void shouldRemoveCustomDirectionsWhenCMODoesNotExistOnCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();

        Stream.of(values()).forEach(direction ->
            caseData.put(direction.toCustomDirectionField().concat("CMO"),
                createElementCollection(createUnassignedDirection()))
        );

        service.prepareCustomDirections(CaseDetails.builder().data(caseData).build(), null);

        assertThat(caseData).doesNotContainKeys("allPartiesCustomCMO", "localAuthorityDirectionsCustomCMO",
            "cafcassDirectionsCustomCMO", "courtDirectionsCustomCMO", "otherPartiesDirections", "respondentDirections");
    }

    private DynamicList getDynamicList() {
        DynamicList dynamicList = service.buildDynamicListFromHearingDetails(
            createHearingBookingsFromInitialDate(time.now()));

        DynamicListElement listElement = DynamicListElement.builder()
            .label(formatLocalDateToMediumStyle(5))
            .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
            .build();

        dynamicList.setValue(listElement);
        return dynamicList;
    }

    private CaseManagementOrder createCaseManagementOrder() {
        return CaseManagementOrder.builder()
            .hearingDate(formatLocalDateToMediumStyle(2))
            .id(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
            .recitals(List.of(Element.<Recital>builder()
                .value(Recital.builder().build())
                .build()))
            .schedule(Schedule.builder().build())
            .status(SELF_REVIEW)
            .orderDoc(DocumentReference.builder().build())
            .build();
    }

    private String formatLocalDateToMediumStyle(int i) {
        return formatLocalDateToString(time.now().plusDays(i).toLocalDate(), FormatStyle.MEDIUM);
    }

    private OrderAction.OrderActionBuilder baseOrderActionWithType() {
        return OrderAction.builder().type(ActionType.SEND_TO_ALL_PARTIES);
    }

    @Nested
    class PrepareCaseDetailsTest {
        private final String[] keys = {
            HEARING_DATE_LIST.getKey(),
            RECITALS.getKey(),
            SCHEDULE.getKey()};

        private HashMap<String, Object> data; // Tries to use an ImmutableMap unless specified

        @Test
        void shouldRemoveScheduleAndRecitalsAndHearingDateListFromCaseData() {
            data = new HashMap<>();

            Arrays.stream(keys).forEach(key -> data.put(key, ""));

            service.removeTransientObjectsFromCaseData(data);

            assertThat(data).doesNotContainKeys(keys);
        }
    }
}
