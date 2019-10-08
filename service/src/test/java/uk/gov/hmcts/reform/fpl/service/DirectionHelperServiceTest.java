package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

@ExtendWith(SpringExtension.class)
class DirectionHelperServiceTest {

    private final DirectionHelperService directionHelperService = new DirectionHelperService();

    @Test
    void shouldAddRoleDirectionsIntoOrderObject() {
        CaseData caseData = populateCaseDataWithFixedDirections()
            .allPartiesCustom(buildCustomDirections())
            .localAuthorityDirectionsCustom(buildCustomDirections())
            .parentsAndRespondentsCustom(buildCustomDirections())
            .cafcassDirectionsCustom(buildCustomDirections())
            .otherPartiesDirectionsCustom(buildCustomDirections())
            .courtDirectionsCustom(buildCustomDirections())
            .build();

        Order order = directionHelperService.createOrder(caseData);

        assertThat(order.getDirections()).size().isEqualTo(12);
    }

    @Test
    void shouldAllowNullCustomDirectionValues() {
        CaseData caseData = populateCaseDataWithFixedDirections().build();

        Order order = directionHelperService.createOrder(caseData);

        assertThat(order.getDirections()).size().isEqualTo(6);
    }

    @Test
    void shouldAddCustomFlagOnlyToCustomDirection() {
        CaseData caseData = populateCaseDataWithFixedDirections()
            .courtDirectionsCustom(buildCustomDirections())
            .build();

        Order order = directionHelperService.createOrder(caseData);

        List<Element<Direction>> directionWithCustomFlag = order.getDirections().stream()
            .filter(element -> element.getValue().getCustom() != null && element.getValue().getCustom().equals("Yes"))
            .collect(toList());

        assertThat(directionWithCustomFlag).hasSize(1);
    }

    @Test
    void shouldAssignCustomDirectionToCorrectAssignee() {
        CaseData caseData = populateCaseDataWithFixedDirections()
            .courtDirectionsCustom(buildCustomDirections())
            .build();

        Order order = directionHelperService.createOrder(caseData);

        List<Element<Direction>> courtDirections = order.getDirections().stream()
            .filter(element -> element.getValue().getAssignee().equals(COURT))
            .collect(toList());

        assertThat(courtDirections).hasSize(2);
    }

    @Test
    void shouldNumberDirectionsStartingAtTwo() {
        CaseData caseData = populateCaseDataWithFixedDirections().build();

        Order order = directionHelperService.createOrder(caseData);

        List<String> numberedDirectionTypes = directionHelperService.numberDirections(order.getDirections()).stream()
            .map(direction -> direction.getValue().getType())
            .collect(toList());

        List<String> expectedDirectionsTypes = IntStream.range(0, numberedDirectionTypes.size())
            .mapToObj(x -> (x + 2) + ". direction")
            .collect(toList());

        assertThat(numberedDirectionTypes).isEqualTo(expectedDirectionsTypes);
    }

    @Test
    void shouldConstructDirectionFromDirectionConfigurationAsExpectedWhenCompleteByDateIsRealDate() {
        LocalDateTime today = LocalDateTime.now();

        DirectionConfiguration directionConfig = DirectionConfiguration.builder()
            .assignee(LOCAL_AUTHORITY)
            .title("direction title")
            .text("direction text")
            .display(Display.builder()
                .due(Display.Due.BY)
                .templateDateFormat("h:mma, d MMMM yyyy")
                .directionRemovable(false)
                .showDateOnly(false)
                .build())
            .build();

        Element<Direction> actualDirection = directionHelperService.constructDirectionForCCD(directionConfig, today);

        assertThat(actualDirection.getValue()).isEqualTo(Direction.builder()
            .type("direction title")
            .text("direction text")
            .readOnly("No")
            .directionRemovable("No")
            .completeBy(today)
            .assignee(LOCAL_AUTHORITY)
            .build());
    }

    @Test
    void shouldConstructDirectionFromDirectionConfigurationAsExpectedWhenCompleteByDateIsNull() {
        DirectionConfiguration directionConfig = DirectionConfiguration.builder()
            .assignee(LOCAL_AUTHORITY)
            .title("direction title")
            .text("direction text")
            .display(Display.builder()
                .due(Display.Due.BY)
                .templateDateFormat("h:mma, d MMMM yyyy")
                .directionRemovable(false)
                .showDateOnly(false)
                .build())
            .build();

        Element<Direction> actualDirection =
            directionHelperService.constructDirectionForCCD(directionConfig, null);

        assertThat(actualDirection.getValue()).isEqualTo(Direction.builder()
            .type("direction title")
            .text("direction text")
            .readOnly("No")
            .directionRemovable("No")
            .completeBy(null)
            .assignee(LOCAL_AUTHORITY)
            .build());
    }

    private CaseData.CaseDataBuilder populateCaseDataWithFixedDirections() {
        return CaseData.builder()
            .allParties(buildDirections(ALL_PARTIES))
            .localAuthorityDirections(buildDirections(LOCAL_AUTHORITY))
            .parentsAndRespondentsDirections(buildDirections(PARENTS_AND_RESPONDENTS))
            .cafcassDirections(buildDirections(CAFCASS))
            .otherPartiesDirections(buildDirections(OTHERS))
            .courtDirections(buildDirections(COURT));
    }

    private List<Element<Direction>> buildDirections(DirectionAssignee assignee) {
        return ImmutableList.of(Element.<Direction>builder()
            .value(Direction.builder()
                .type("direction")
                .text("example direction text")
                .assignee(assignee)
                .build())
            .build());
    }

    private List<Element<Direction>> buildCustomDirections() {
        return ImmutableList.of(Element.<Direction>builder()
            .value(Direction.builder()
                .type("direction")
                .text("example direction text")
                .build())
            .build());
    }
}
