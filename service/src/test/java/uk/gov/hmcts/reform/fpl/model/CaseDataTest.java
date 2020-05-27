package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.NEXT_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createUnassignedDirection;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;

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

        JSONAssert.assertEquals(format("{%s:{status: %s}}",
            CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), SELF_REVIEW.name()), serialised, false);
    }

    @Test
    void shouldSerialiseCaseManagementOrderToCorrectStringValueWhenInSendToJudge() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build())
            .build());

        JSONAssert.assertEquals(
            format("{%s:{status: %s}}", CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), SEND_TO_JUDGE.name()),
            serialised, false);
    }

    @Test
    void shouldDeserialiseCaseDataWhenCaseManagementOrderWithSelfReviewState() throws JsonProcessingException {
        String content = format("{\"%s\":{\"status\": \"%s\"}}",
            CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), SELF_REVIEW.name());

        CaseData deserialised = mapper.readValue(content, CaseData.class);

        assertThat(deserialised).isEqualTo(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(SELF_REVIEW).build())
            .directionsForCaseManagementOrder(Directions.builder().build())
            .build());
    }

    @Test
    void shouldDeserialiseCaseDataWhenCaseManagementOrderWithSendToJudgeState() throws JsonProcessingException {
        String content = format("{\"%s\":{\"status\": \"%s\"}}",
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), SEND_TO_JUDGE.name());

        CaseData deserialised = mapper.readValue(content, CaseData.class);

        assertThat(deserialised).isEqualTo(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build())
            .directionsForCaseManagementOrder(Directions.builder().build())
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

    @Test
    void shouldGetOrderAppliesToAllChildrenWithValueAsYesWhenOnlyOneChildOnCase() {
        CaseData caseData = CaseData.builder().children1(List.of(testChild())).build();
        assertThat(caseData.getOrderAppliesToAllChildren()).isEqualTo("Yes");
    }

    @Test
    void shouldGetOrderAppliesToAllChildrenWithCustomValueWhenMultipleChildrenOnCase() {
        CaseData caseData = CaseData.builder().children1(testChildren()).orderAppliesToAllChildren("No").build();
        assertThat(caseData.getOrderAppliesToAllChildren()).isEqualTo("No");
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
            assertThat(getCaseData().getCaseManagementOrder()).isEqualTo(orderWithDirections(createCmoDirections()));
        }

        @Test
        void shouldReturnCaseManagementOrderWithOrderDirectionsWhenOnlyPreviousOrder() {
            Map<String, Object> data = new HashMap<>();
            data.put(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), orderWithDirections(createCmoDirections()));

            CaseData caseData = mapper.convertValue(data, CaseData.class);

            assertThat(caseData.getCaseManagementOrder()).isEqualTo(orderWithDirections(createCmoDirections()));
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
            data.put(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), orderWithDirections(emptyList()));
            data.put(RECITALS.getKey(), emptyList());

            CaseData caseData = mapper.convertValue(data, CaseData.class);

            assertThat(caseData.getCaseManagementOrder().getRecitals()).isEmpty();
        }

        @Test
        void shouldOverwriteDirectionsWithEmptyListWhenAllDirectionsRemoved() {
            Map<String, Object> data = new HashMap<>();
            data.put(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), orderWithDirections(createCmoDirections()));

            Stream.of(DirectionAssignee.values()).forEach(assignee ->
                data.put(assignee.toCaseManagementOrderDirectionField(), emptyList()));

            CaseData caseData = mapper.convertValue(data, CaseData.class);

            assertThat(caseData.getCaseManagementOrder().getDirections()).isEmpty();
        }

        private CaseManagementOrder orderWithDirections(List<Element<Direction>> directions) {
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
                caseData.put(direction.toCaseManagementOrderDirectionField(), wrapElements(unassignedDirection));
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

    @Test
    void shouldDeserialiseIndividualCmoDirectionsToDirectionsObject() throws JsonProcessingException {
        UUID id = randomUUID();

        CaseData deserialised = mapper.readValue(buildJsonDirections(id), CaseData.class);

        assertThat(deserialised).isEqualToComparingFieldByField(caseDataWithPopulatedDirections(id));
    }

    @Test
    void shouldSerialiseDirectionsObjectToIndividualDirections() throws JsonProcessingException {
        UUID id = randomUUID();

        String serialised = mapper.writeValueAsString(caseDataWithPopulatedDirections(id));

        Stream.of(DirectionAssignee.values())
            .forEach(assignee -> JSONAssert.assertEquals(getExpectedString(assignee, id), serialised, false));
    }

    private String buildJsonDirections(UUID id) throws JsonProcessingException {
        List<Element<Direction>> directions = List.of(element(id, Direction.builder().directionType("title").build()));
        String directionString = mapper.writeValueAsString(directions);

        StringBuilder builder = new StringBuilder().append("{");

        DirectionAssignee[] values = DirectionAssignee.values();
        for (int i = 0; i < values.length; i++) {
            DirectionAssignee assignee = values[i];
            builder.append(getJsonDirectionForParty(assignee.toCaseManagementOrderDirectionField(), directionString));

            if (i != values.length - 1) {
                builder.append(",");
            }
        }

        return builder.append("}").toString();
    }

    private Direction directionForParty(DirectionAssignee assignee) {
        return Direction.builder().assignee(assignee).custom("Yes").readOnly("No").directionType("title").build();
    }

    private String getJsonDirectionForParty(String key, String directionString) {
        return format("\"%s\": %s", key, directionString);
    }

    private CaseData caseDataWithPopulatedDirections(UUID id) {
        return CaseData.builder()
            .directionsForCaseManagementOrder(Directions.builder()
                .allPartiesCustomCMO(List.of(element(id, directionForParty(ALL_PARTIES))))
                .localAuthorityDirectionsCustomCMO(List.of(element(id, directionForParty(LOCAL_AUTHORITY))))
                .respondentDirectionsCustomCMO(List.of(element(id, directionForParty(PARENTS_AND_RESPONDENTS))))
                .cafcassDirectionsCustomCMO(List.of(element(id, directionForParty(CAFCASS))))
                .otherPartiesDirectionsCustomCMO(List.of(element(id, directionForParty(OTHERS))))
                .courtDirectionsCustomCMO(List.of(element(id, directionForParty(COURT))))
                .build())
            .build();
    }

    private String getExpectedString(DirectionAssignee assignee, UUID id) {
        String key = assignee.toCaseManagementOrderDirectionField();

        return format("{\"%s\": [{\"id\":\"%s\",\"value\":{\"directionType\":\"title\",\"assignee\":\"%s\","
            + "\"readOnly\":\"No\",\"custom\":\"Yes\",\"responses\":[]}}]}", key, id, assignee.toString());
    }
}
