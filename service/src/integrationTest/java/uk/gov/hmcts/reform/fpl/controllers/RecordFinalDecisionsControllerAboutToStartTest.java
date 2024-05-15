package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.RecordFinalDecisionsService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@WebMvcTest(RecordFinalDecisionsController.class)
@OverrideAutoConfiguration(enabled = true)
class RecordFinalDecisionsControllerAboutToStartTest extends AbstractCallbackTest {

    RecordFinalDecisionsControllerAboutToStartTest() {
        super("record-final-decisions");
    }

    @MockBean
    private ChildrenService childrenService;

    @MockBean
    private OptionCountBuilder optionCountBuilder;

    @Test
    void testContextLoads() {
        assertNotNull(childrenService, "ChildrenService should be injected and not null");
    }

    @TestConfiguration
    static class RecordFinalDecisionsTestConfig {
        @Bean
        public RecordFinalDecisionsService recordFinalDecisionsService(ChildrenService childrenService, OptionCountBuilder optionCountBuilder) {
            return new RecordFinalDecisionsService(childrenService, optionCountBuilder);
        }
    }


    @WithMockUser
    @Test
    void shouldPrePopulateFields() {
        CaseData caseData = CaseData.builder().build();

        when(childrenService.getRemainingChildren(any())).thenReturn(Collections.emptyList());
        when(childrenService.getChildrenLabel(any(), anyBoolean())).thenReturn("Joe Bloggs");

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getData()).containsKeys("childSelector", "children_label", "close_case_label");
    }

}
