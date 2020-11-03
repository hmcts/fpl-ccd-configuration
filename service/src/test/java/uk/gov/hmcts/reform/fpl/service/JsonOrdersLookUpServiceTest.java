package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class JsonOrdersLookUpServiceTest {

    private JsonOrdersLookupService jsonOrdersLookupService;

    @Autowired
    private ObjectMapper mapper;

    @Mock
    private ObjectMapper mockedMapper;

    @BeforeEach
    void before() {
        jsonOrdersLookupService = new JsonOrdersLookupService(mapper);
    }

    @Test
    void shouldPopulateOrderDefinitionForStandardDirectionOrder() {
        OrderDefinition expectedOrderDefinition = OrderDefinition.builder()
            .type("standardDirectionOrder")
            .language(ENGLISH)
            .service("FPL")
            .directions(ImmutableList.of(
                DirectionConfiguration.builder()
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
                    .build(),
                DirectionConfiguration.builder()
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
                    .build(),
                DirectionConfiguration.builder()
                    .title("Test SDO type 3")
                    .text("Test body 3\n")
                    .assignee(LOCAL_AUTHORITY)
                    .display(Display.builder()
                        .due(Display.Due.BY)
                        .templateDateFormat("h:mma, d MMMM yyyy")
                        .directionRemovable(true)
                        .showDateOnly(false)
                        .delta("-2")
                        .time("16:00:00")
                        .build())
                    .build()))
            .build();

        OrderDefinition orderDefinition = jsonOrdersLookupService.getStandardDirectionOrder();

        assertThat(orderDefinition).isEqualToComparingFieldByField(expectedOrderDefinition);
    }

    @Test
    void shouldThrowExceptionIfMappingToOrderDefinitionFails() throws Exception {
        when(mockedMapper.readValue(anyString(), eq(OrderDefinition.class)))
            .thenThrow(new JsonEOFException(null, null, null));

        final JsonOrdersLookupService jsonOrdersLookupService = new JsonOrdersLookupService(mockedMapper);

        assertThrows(UncheckedIOException.class, jsonOrdersLookupService::getStandardDirectionOrder);
    }

}
