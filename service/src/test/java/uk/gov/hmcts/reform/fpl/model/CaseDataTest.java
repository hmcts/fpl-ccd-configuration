package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.NEXT_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createUnassignedDirection;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, FixedTimeConfiguration.class})
class CaseDataTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Time time;

    @Test
    void shouldSerialiseCaseManagementOrderToCorrectStringValueWhenInSelfReview() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(SELF_REVIEW).build())
            .build());

        JSONAssert.assertEquals(String.format("{%s:{status: %s}}",
            CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), SELF_REVIEW.name()), serialised, false);
    }

    @Test
    void shouldSerialiseCaseManagementOrderToCorrectStringValueWhenInSendToJudge() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build())
            .build());

        JSONAssert.assertEquals(
            String.format("{%s:{status: %s}}", CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), SEND_TO_JUDGE.name()),
            serialised, false);
    }

    @Test
    void shouldDeserialiseCaseDataWhenCaseManagementOrderWithSelfReviewState() throws JsonProcessingException {
        String content = String.format("{\"%s\":{\"status\": \"%s\"}}",
            CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), SELF_REVIEW.name());

        CaseData deserialised = mapper.readValue(content, CaseData.class);

        assertThat(deserialised).isEqualTo(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(SELF_REVIEW).build())
            .build());
    }

    @Test
    void shouldDeserialiseCaseDataWhenCaseManagementOrderWithSendToJudgeState() throws JsonProcessingException {
        String content = String.format("{\"%s\":{\"status\": \"%s\"}}",
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), SEND_TO_JUDGE.name());

        CaseData deserialised = mapper.readValue(content, CaseData.class);

        assertThat(deserialised).isEqualTo(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build())
            .build());
    }

    @Test
    void shouldGetAllOthersWhenFirstAndAdditionalOthersExist() {
        Other other1 = otherWithName("John");
        Other other2 = otherWithName("Sam");

        CaseData caseData = caseData(Others.builder().firstOther(other1).additionalOthers(wrapElements(other2)));

        assertThat(caseData.getAllOthers().get(0).getValue()).isEqualTo(other1);
        assertThat(caseData.getAllOthers().get(1).getValue()).isEqualTo(other2);
    }

    @Test
    void shouldGetEmptyListOfOthersWhenOthersIsNull() {
        CaseData caseData = CaseData.builder().build();

        assertThat(caseData.getAllOthers()).isEmpty();
    }

    @Test
    void shouldGetEmptyListOfOthersWhenOthersAreEmpty() {
        CaseData caseData = caseData(Others.builder());

        assertThat(caseData.getAllOthers()).isEmpty();
    }

    @Test
    void shouldGetEmptyListOfPlacementsWhenPlacementsIsNull() {
        CaseData caseData = CaseData.builder().build();
        assertThat(caseData.getPlacements()).isEmpty();
    }

    @Test
    void shouldGetFirstOtherWhenNoAdditionalOthers() {
        Other other1 = otherWithName("John");
        CaseData caseData = caseData(Others.builder().firstOther(other1));

        assertThat(caseData.getAllOthers().get(0).getValue()).isEqualTo(other1);
    }

    @Test
    void shouldFindFirstOther() {
        Other other1 = otherWithName("John");
        CaseData caseData = caseData(Others.builder().firstOther(other1));

        assertThat(caseData.findOther(0)).isEqualTo(Optional.of(other1));
    }

    @Test
    void shouldNotFindNonExistingOther() {
        Other other1 = otherWithName("John");
        CaseData caseData = caseData(Others.builder().firstOther(other1));

        assertThat(caseData.findOther(1)).isEqualTo(Optional.empty());
    }

    @Test
    void shouldFindExistingOther() {
        Other other1 = otherWithName("John");
        Other other2 = otherWithName("Sam");
        CaseData caseData = CaseData.builder().others(Others.builder()
            .firstOther(other1)
            .additionalOthers(wrapElements(other2))
            .build())
            .build();

        assertThat(caseData.findOther(1)).isEqualTo(Optional.of(other2));
    }

    @Test
    void shouldFindExistingRespondent() {
        Respondent respondent = Respondent.builder().build();
        CaseData caseData = CaseData.builder().respondents1(wrapElements(respondent)).build();

        assertThat(caseData.findRespondent(0)).isEqualTo(Optional.of(respondent));
    }

    @Test
    void shouldNotFindNonExistingRespondent() {
        Respondent respondent = Respondent.builder().build();
        CaseData caseData = CaseData.builder().respondents1(wrapElements(respondent)).build();

        assertThat(caseData.findRespondent(1)).isEqualTo(Optional.empty());
    }

    @Test
    void shouldFindExistingApplicant() {
        Applicant applicant = Applicant.builder().build();
        CaseData caseData = CaseData.builder().applicants(wrapElements(applicant)).build();

        assertThat(caseData.findApplicant(0)).isEqualTo(Optional.of(applicant));
    }

    @Test
    void shouldNotFindNonExistingApplicant() {
        Applicant applicant = Applicant.builder().build();
        CaseData caseData = CaseData.builder().applicants(wrapElements(applicant)).build();

        assertThat(caseData.findApplicant(1)).isEqualTo(Optional.empty());
    }

    @Test
    void shouldNotFindApplicantWhenNull() {
        CaseData caseData = CaseData.builder().build();

        assertThat(caseData.findApplicant(0)).isEqualTo(Optional.empty());
    }

    private CaseData caseData(Others.OthersBuilder othersBuilder) {
        return CaseData.builder().others(othersBuilder.build()).build();
    }

    private Other otherWithName(String name) {
        return Other.builder().name(name).build();
    }

    @Nested
    class GetFurtherDirectionsText {
        private FurtherDirections furtherDirections;
        private CaseData caseData;

        @Test
        void shouldReturnDirectionTextWhenFurtherDirectionIsPopulated() {
            furtherDirections = FurtherDirections.builder().directions("some text").build();
            caseData = CaseData.builder().orderFurtherDirections(furtherDirections).build();

            assertThat(caseData.getFurtherDirectionsText()).isEqualTo("some text");
        }

        @Test
        void shouldReturnEmptyStringWhenFurtherDirectionIsNotPopulated() {
            furtherDirections = FurtherDirections.builder().build();
            caseData = CaseData.builder().orderFurtherDirections(furtherDirections).build();

            assertThat(caseData.getFurtherDirectionsText()).isEmpty();
        }

        @Test
        void shouldReturnEmptyStringWhenFurtherDirectionIsNull() {
            caseData = CaseData.builder().build();

            assertThat(caseData.getFurtherDirectionsText()).isEmpty();
        }
    }

    @Nested
    class GetLastC2DocumentBundle {
        private CaseData caseData;

        @Test
        void shouldReturnLastC2DocumentBundleWhenC2DocumentBundleIsPopulated() {
            C2DocumentBundle c2DocumentBundle1 = C2DocumentBundle.builder()
                .description("Mock bundle 1")
                .build();

            C2DocumentBundle c2DocumentBundle2 = C2DocumentBundle.builder()
                .description("Mock bundle 2")
                .build();

            List<Element<C2DocumentBundle>> c2DocumentBundle = wrapElements(c2DocumentBundle1, c2DocumentBundle2);

            caseData = CaseData.builder().c2DocumentBundle(c2DocumentBundle).build();
            assertThat(caseData.getLastC2DocumentBundle()).isEqualTo(c2DocumentBundle2);
        }

        @Test
        void shouldReturnNullIfC2DocumentBundleIsNotPopulated() {
            caseData = CaseData.builder().c2DocumentBundle(null).build();
            assertThat(caseData.getLastC2DocumentBundle()).isEqualTo(null);
        }
    }

    @Nested
    class PrepareCaseManagementOrder {
        final Schedule schedule = Schedule.builder().includeSchedule("Yes").build();
        final List<Element<Recital>> recitals = wrapElements(Recital.builder().title("example title").build());
        final OrderAction action = baseOrderActionWithType().build();

        @Test
        void shouldReturnCaseManagementOrderWhenFullDetailsButNoPreviousOrder() {
            CaseData caseData = getCaseData();

            assertThat(caseData.getCaseManagementOrder())
                .isEqualToComparingFieldByField(buildOrder(schedule, recitals, createCmoDirections(), action));
        }

        @Test
        void shouldReturnCaseManagementOrderAndEmptyListDirectionsWhenOnlyPreviousOrder() {
            Map<String, Object> data = new HashMap<>();
            String key = CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey();
            data.put(key, buildOrder(schedule, recitals, createCmoDirections(), action));

            CaseData caseData = mapper.convertValue(data, CaseData.class);

            assertThat(caseData.getCaseManagementOrder())
                .isEqualToComparingFieldByField(buildOrder(schedule, recitals, emptyList(), action));
        }

        @Test
        void shouldReturnCaseManagementOrderWhenNoCaseData() {
            CaseData caseData = CaseData.builder().build();

            assertThat(caseData.getCaseManagementOrder()).isEqualTo(CaseManagementOrder.builder()
                .directions(emptyList())
                .build());
        }

        @Test
        void shouldOverwriteRecitalsWithEmptyListWhenRemovingAllRecitals() {
            Map<String, Object> data = new HashMap<>();
            String key = CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey();
            data.put(key, buildOrder(schedule, recitals, emptyList(), action));
            data.put(RECITALS.getKey(), emptyList());

            CaseData caseData = mapper.convertValue(data, CaseData.class);

            assertThat(caseData.getCaseManagementOrder().getRecitals()).isEmpty();
        }

        //TODO: test fails due to custom setter for directionsForCaseManagementOrder being null.
        // get directions from fields and add to order, i.e mid event / about to submit when we want prepped cmo
        // if order is not null, we want to take directions from the order, i.e about to start
        // if all directions fields are empty, overwrite directions on order i.e about to submit removed everything.
        // directions fields are never null, always return empty list...
        @Disabled
        @Test
        void shouldOverwriteDirectionsWithEmptyListWhenAllDirectionsRemoved() {
            Map<String, Object> data = new HashMap<>();
            String key = CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey();
            data.put(key, buildOrder(schedule, recitals, createCmoDirections(), action));

            Stream.of(DirectionAssignee.values()).forEach(assignee ->
                data.put(assignee.toCustomDirectionField().concat("CMO"), emptyList()));

            CaseData caseData = mapper.convertValue(data, CaseData.class);

            assertThat(caseData.getCaseManagementOrder().getDirections()).isEmpty();
        }

        private CaseManagementOrder buildOrder(Schedule schedule,
                                               List<Element<Recital>> recitals,
                                               List<Element<Direction>> directions,
                                               OrderAction action) {
            return CaseManagementOrder.builder()
                .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                .hearingDate(formatLocalDateToMediumStyle(5))
                .directions(directions)
                .action(action)
                .nextHearing(NextHearing.builder()
                    .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                    .date(formatLocalDateToMediumStyle(5))
                    .build())
                .dateOfIssue(formatLocalDateToString(time.now().toLocalDate(), DATE))
                .schedule(schedule)
                .recitals(recitals)
                .build();
        }

        private CaseData getCaseData() {
            Map<String, Object> caseData = new HashMap<>();

            Stream.of(DirectionAssignee.values()).forEach(direction -> {
                Direction unassignedDirection = createUnassignedDirection();
                caseData.put(direction.toCustomDirectionField().concat("CMO"), wrapElements(unassignedDirection));
            });

            caseData.put(HEARING_DATE_LIST.getKey(), getDynamicList());
            caseData.put(NEXT_HEARING_DATE_LIST.getKey(), getDynamicList());
            caseData.put(ORDER_ACTION.getKey(), action);
            caseData.put("dateOfIssue", time.now());
            caseData.put("schedule", schedule);
            caseData.put("recitals", recitals);

            return mapper.convertValue(caseData, CaseData.class);
        }

        private OrderAction.OrderActionBuilder baseOrderActionWithType() {
            return OrderAction.builder().type(ActionType.SEND_TO_ALL_PARTIES);
        }

        private DynamicList getDynamicList() {
            DynamicListElement listElement = DynamicListElement.builder()
                .label(formatLocalDateToMediumStyle(5))
                .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                .build();

            return DynamicList.builder()
                .listItems(List.of(
                    listElement,
                    DynamicListElement.builder()
                        .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                        .label(formatLocalDateToMediumStyle(2))
                        .build(),
                    DynamicListElement.builder()
                        .code(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                        .label(formatLocalDateToMediumStyle(0))
                        .build()))
                .value(listElement)
                .build();
        }

        private String formatLocalDateToMediumStyle(int i) {
            return formatLocalDateToString(time.now().plusDays(i).toLocalDate(), FormatStyle.MEDIUM);
        }
    }

    @Test
    void shouldSerialiseDirectionsAsNullWhenEmptyDirections() {
        assertThat(CaseData.builder().build()).hasFieldOrPropertyWithValue("directionsForCaseManagementOrder", null);
    }
}
