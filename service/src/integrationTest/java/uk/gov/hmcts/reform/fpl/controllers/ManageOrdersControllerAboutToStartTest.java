package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageOrdersControllerAboutToStartTest extends AbstractCallbackTest {

    ManageOrdersControllerAboutToStartTest() {
        super("manage-orders");
    }

    @Test
    void shouldNotPopulateManageOrdersAmendmentListWhenStateIsClosed() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .build();

        CaseData returnedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        assertThat(returnedCaseData.getManageOrdersEventData().getManageOrdersAmendmentList()).isNull();
    }

}
