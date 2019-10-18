package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class JsonOrdersLookUpServiceTest {

    private JsonOrdersLookupService jsonOrdersLookupService;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void before() {
        jsonOrdersLookupService = new JsonOrdersLookupService(mapper);
    }

    @Test
    void shouldPopulateOrderDefinitionForStandardDirectionOrder() throws IOException {
        OrderDefinition expectedOrderDefinition = OrderDefinition.builder()
            .type("standardDirectionOrder")
            .language(ENGLISH)
            .service("FPL")
            .directions(ImmutableList.of(
                DirectionConfiguration.builder()
                    .title("Test SDO type 1")
                    .text("• Test body 1 \n\n• Two")
                    .assignee(ALL_PARTIES)
                    .display(Display.builder()
                        .due(Display.Due.ON)
                        .templateDateFormat("d MMMM yyyy 'at' h:mma")
                        .directionRemovable(false)
                        .showDateOnly(true)
                        .build())
                    .build(),
                DirectionConfiguration.builder()
                    .title("Test SDO type 2")
                    .text("Test body 2")
                    .assignee(LOCAL_AUTHORITY)
                    .display(Display.builder()
                        .due(Display.Due.BY)
                        .templateDateFormat("h:mma, d MMMM yyyy")
                        .directionRemovable(false)
                        .showDateOnly(false)
                        .build())
                    .build()))
            .build();

        OrderDefinition orderDefinition = jsonOrdersLookupService.getStandardDirectionOrder();

        assertThat(orderDefinition).isEqualToComparingFieldByField(expectedOrderDefinition);
    }
}
