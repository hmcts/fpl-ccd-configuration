package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.exceptions.OrderDefinitionNotFoundException;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ASK_FOR_DISCLOSURE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.CONTACT_ALTERNATIVE_CARERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.CUSTOM;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.REQUEST_HELP_TO_TAKE_PART_IN_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class JsonOrdersLookUpServiceTest {

    @Autowired
    private ObjectMapper mapper;

    @Mock
    private ObjectMapper mockedMapper;

    private final DirectionConfiguration order1Definition = DirectionConfiguration.builder()
        .type(REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE)
        .title("Test SDO type 1")
        .text("- Test body 1 \n\n- Two\n")
        .assignee(ALL_PARTIES)
        .display(Display.builder()
            .due(Display.Due.ON)
            .templateDateFormat("d MMMM yyyy 'at' h:mma")
            .directionRemovable(false)
            .showDateOnly(true)
            .delta("0")
            .build())
        .build();

    private final DirectionConfiguration order2Definition = DirectionConfiguration.builder()
        .type(REQUEST_HELP_TO_TAKE_PART_IN_PROCEEDINGS)
        .title("Test SDO type 2")
        .text("Test body 2\n")
        .assignee(LOCAL_AUTHORITY)
        .display(Display.builder()
            .due(Display.Due.BY)
            .templateDateFormat("h:mma, d MMMM yyyy")
            .directionRemovable(false)
            .showDateOnly(false)
            .delta("-3")
            .time("12:00:00")
            .build())
        .build();

    private final DirectionConfiguration order3Definition = DirectionConfiguration.builder()
        .type(ASK_FOR_DISCLOSURE)
        .title("Test SDO type 3")
        .text("Test body 3\n")
        .assignee(LOCAL_AUTHORITY)
        .display(Display.builder()
            .due(Display.Due.ON)
            .templateDateFormat("h:mma, d MMMM yyyy")
            .directionRemovable(true)
            .showDateOnly(false)
            .delta("-2")
            .time("16:00:00")
            .build())
        .build();

    private final DirectionConfiguration customOrderDefinition = DirectionConfiguration.builder()
        .type(CUSTOM)
        .display(Display.builder()
            .due(Display.Due.BY)
            .templateDateFormat("d MMMM yyyy 'at' h:mma")
            .showDateOnly(false)
            .build())
        .build();

    @Test
    void shouldPopulateOrderDefinitionForStandardDirectionOrder() {
        OrderDefinition expectedOrderDefinition = OrderDefinition.builder()
            .type("standardDirectionOrder")
            .language(ENGLISH)
            .service("FPL")
            .standardDirections(List.of(order1Definition, order2Definition, order3Definition))
            .customDirection(customOrderDefinition)
            .build();

        OrderDefinition orderDefinition = newLookupService().getStandardDirectionOrder();

        assertThat(orderDefinition).isEqualTo(expectedOrderDefinition);
    }

    @Test
    void shouldThrowExceptionIfMappingToOrderDefinitionFails() throws Exception {
        when(mockedMapper.readValue(anyString(), eq(OrderDefinition.class)))
            .thenThrow(new JsonEOFException(null, null, null));

        assertThatThrownBy(() -> new JsonOrdersLookupService(mockedMapper));
    }

    @Test
    void shouldFindDirectionConfiguration() {
        DirectionConfiguration configuration = newLookupService().getDirectionConfiguration(ASK_FOR_DISCLOSURE);

        assertThat(configuration).isEqualTo(order3Definition);
    }

    @Test
    void shouldFindCustomDirectionConfiguration() {
        DirectionConfiguration configuration = newLookupService().getDirectionConfiguration(CUSTOM);

        assertThat(configuration).isEqualTo(customOrderDefinition);
    }

    @Test
    void shouldThrowExceptionWhenDirectionConfigurationNotFound() {
        assertThatThrownBy(() -> newLookupService().getDirectionConfiguration(CONTACT_ALTERNATIVE_CARERS))
            .isInstanceOf(OrderDefinitionNotFoundException.class)
            .hasMessage("Order definition CONTACT_ALTERNATIVE_CARERS not found");
    }

    private OrdersLookupService newLookupService() {
        return new JsonOrdersLookupService(mapper);
    }

}
