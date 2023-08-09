package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList.INTERNATIONAL_ASPECT;

@WebMvcTest(CaseExtensionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseExtensionControllerAboutToStartTest extends AbstractCallbackTest {

    CaseExtensionControllerAboutToStartTest() {
        super("case-extension");
    }

    @Test
    void shouldPopulateShouldBeCompletedByDateWith26WeekTimeline() {
        List<Child> children = List.of(
                getChild(null, "Daisy", "French"),
                getChild(null, "Archie", "Turner"),
                getChild(null, "Julie", "Jane")
        );

        LocalDate dateSubmitted = LocalDate.of(2030, 11, 12);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("dateSubmitted", dateSubmitted,
                "caseCompletionDate", "",
                    "children1", ElementUtils.wrapElements(children)))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        String label = join(lineSeparator(),
                "Child 1: Daisy French: 13 May 2031",
                "Child 2: Archie Turner: 13 May 2031",
                "Child 3: Julie Jane: 13 May 2031");

        assertThat(callbackResponse.getData().get("shouldBeCompletedByDate"))
            .isEqualTo("13 May 2031");
        assertThat(callbackResponse.getData().get("childSelectorForExtension"))
            .isEqualTo(Map.of("optionCount", "123"));
        assertThat(callbackResponse.getData().get("childCaseCompletionDateLabel"))
            .isEqualTo(label);
    }

    @Test
    void shouldPopulateShouldBeCompletedByDateWithMaxChildCaseCompletionDate() {
        List<Child> children = List.of(
                getChild(LocalDate.of(2024, 7, 2), "Daisy", "French"),
                getChild(null, "Archie", "Turner"),
                getChild(LocalDate.of(2024, 10, 8), "Julie", "Jane")
        );

        LocalDate dateSubmitted = LocalDate.of(2024, 1, 12);

        CaseDetails caseDetails = CaseDetails.builder()
                .data(Map.of("dateSubmitted", dateSubmitted,
                        "caseCompletionDate", "",
                        "children1", ElementUtils.wrapElements(children)))
                .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        String label = join(lineSeparator(),
                "Child 1: Daisy French: 2 July 2024",
                "Child 2: Archie Turner: 12 July 2024",
                "Child 3: Julie Jane: 8 October 2024");

        assertThat(callbackResponse.getData().get("shouldBeCompletedByDate"))
                .isEqualTo("8 October 2024");
        assertThat(callbackResponse.getData().get("childSelectorForExtension"))
                .isEqualTo(Map.of("optionCount", "123"));
        assertThat(callbackResponse.getData().get("childCaseCompletionDateLabel"))
                .isEqualTo(label);
    }

    private Child getChild(LocalDate completionDate,
                           String firstName,
                           String lastName) {
        ChildParty childParty = ChildParty.builder()
                .completionDate(completionDate)
                .extensionReason(INTERNATIONAL_ASPECT)
                .firstName(firstName)
                .lastName(lastName)
                .build();
        return Child.builder()
                .party(childParty)
                .build();
    }
}
