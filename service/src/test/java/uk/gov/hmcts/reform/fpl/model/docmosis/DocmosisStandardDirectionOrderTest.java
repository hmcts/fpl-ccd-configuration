package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

class DocmosisStandardDirectionOrderTest {
    private static final Set<String> DOCMOSIS_KEYS = Set.of("judgeAndLegalAdvisor", "courtName",
        "familyManCaseNumber", "ccdCaseNumber", "dateOfIssue", "complianceDeadline", "respondents", "children",
        "respondentsProvided", "applicantName", "hearingBooking", "allParties", "localAuthorityDirections",
        "respondentDirections", "cafcassDirections", "otherPartiesDirections", "courtDirections", "crest",
        "draftbackground", "courtseal");

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldConvertStandardDirectionOrderDirectionsToExpectedMapWhenDirectionsForAllAssignees() {
        List<DocmosisDirection> directions = directionsForAllAssignees();

        DocmosisStandardDirectionOrder order = order(directions);

        assertThat(order.toMap(mapper)).containsExactlyInAnyOrderEntriesOf(expectedMap());
    }

    @Test
    void shouldContainRequiredFieldsForDocmosisWhenAllDirectionAssigneesHaveDirections() {
        List<DocmosisDirection> directions = directionsForAllAssignees();

        DocmosisStandardDirectionOrder order = DocmosisStandardDirectionOrder.builder()
            .directions(directions)
            .build();

        assertThat(order.toMap(mapper)).containsOnlyKeys(DOCMOSIS_KEYS);
    }

    @Test
    void shouldContainRequiredFieldsForDocmosisWhenNoDirections() {
        DocmosisStandardDirectionOrder order = DocmosisStandardDirectionOrder.builder().build();

        Set<String> modifiedKeys = newHashSet(DOCMOSIS_KEYS);

        Stream.of(DirectionAssignee.values()).forEach(assignee -> modifiedKeys.remove(assignee.getValue()));

        assertThat(order.toMap(mapper)).containsOnlyKeys(modifiedKeys);
    }

    private List<DocmosisDirection> directionsForAllAssignees() {
        return Stream.of(DirectionAssignee.values())
            .map(this::direction)
            .collect(toList());
    }

    private DocmosisDirection direction(DirectionAssignee assignee) {
        return DocmosisDirection.builder()
            .assignee(assignee)
            .title("Direction title")
            .build();
    }

    private Map<String, Object> expectedMap() {
        ImmutableMap.Builder<String, Object> expectedMap = ImmutableMap.builder();

        expectedMap
            .put("judgeAndLegalAdvisor", Map.of(
                "judgeTitleAndName", "Mr Judge",
                "legalAdvisorName", "Mrs Judge"))
            .put("courtName", "Court name")
            .put("familyManCaseNumber", "123")
            .put("ccdCaseNumber", "1234-1234-1234-1234")
            .put("dateOfIssue", "29 November 2019")
            .put("complianceDeadline", "this other date")
            .put("respondents", List.of(Map.of(
                "name", "Respondent",
                "relationshipToChild", "Father")))
            .put("children", List.of(Map.of(
                "name", "child name",
                "dateOfBirth", "date of birth")))
            .put("respondentsProvided", true)
            .put("applicantName", "Swansea Local Authority")
            .put("hearingBooking", Map.of(
                "hearingTime", "hearing time",
                "hearingJudgeTitleAndName", "Mr Judge",
                "hearingLegalAdvisorName", "Mrs Judge",
                "preHearingAttendance", "hearing preAttendance",
                "hearingVenue", "hearingVenue",
                "hearingDate", "hearingDate"))
            .put("allParties", List.of(direction(ALL_PARTIES)))
            .put("localAuthorityDirections", List.of(direction(LOCAL_AUTHORITY)))
            .put("respondentDirections", List.of(direction(PARENTS_AND_RESPONDENTS)))
            .put("cafcassDirections", List.of(direction(CAFCASS)))
            .put("otherPartiesDirections", List.of(direction(OTHERS)))
            .put("courtDirections", List.of(direction(COURT)))
            .put("crest", "crest")
            .put("draftbackground", "draft background")
            .put("courtseal", "court seal");

        return expectedMap.build();
    }

    private DocmosisStandardDirectionOrder order(List<DocmosisDirection> directions) {
        return DocmosisStandardDirectionOrder.builder()
            .judgeAndLegalAdvisor(DocmosisJudgeAndLegalAdvisor.builder()
                .judgeTitleAndName("Mr Judge")
                .legalAdvisorName("Mrs Judge")
                .build())
            .courtName("Court name")
            .familyManCaseNumber("123")
            .ccdCaseNumber("1234-1234-1234-1234")
            .dateOfIssue("29 November 2019")
            .complianceDeadline("this other date")
            .respondents(List.of(DocmosisRespondent.builder()
                .name("Respondent")
                .relationshipToChild("Father")
                .build()))
            .children(List.of(DocmosisChild.builder()
                .name("child name")
                .dateOfBirth("date of birth")
                .build()))
            .respondentsProvided(true)
            .applicantName("Swansea Local Authority")
            .hearingBooking(DocmosisHearingBooking.builder()
                .hearingTime("hearing time")
                .hearingJudgeTitleAndName("Mr Judge")
                .hearingLegalAdvisorName("Mrs Judge")
                .preHearingAttendance("hearing preAttendance")
                .hearingVenue("hearingVenue")
                .hearingDate("hearingDate")
                .build())
            .directions(directions)
            .crest("crest")
            .draftbackground("draft background")
            .courtseal("court seal")
            .build();
    }
}
