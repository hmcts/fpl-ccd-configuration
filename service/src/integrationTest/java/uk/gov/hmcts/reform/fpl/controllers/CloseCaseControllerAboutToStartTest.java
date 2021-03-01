package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(CloseCaseController.class)
@OverrideAutoConfiguration(enabled = true)
public class CloseCaseControllerAboutToStartTest extends AbstractControllerTest {

    public static final String EXPECTED_LABEL_TEXT = "The case will remain open for 21 days to allow for appeal.\n\n"
        + "In a closed case, you can still:\n"
        + "   •  add a case note\n"
        + "   •  upload a document\n"
        + "   •  issue a C21 (blank order)\n"
        + "   •  submit a C2 application\n";

    CloseCaseControllerAboutToStartTest() {
        super("close-case");
    }

    @Test
    void shouldAddLabelStringToCaseDataMap() {
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of()).build();
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getData()).extracting("close_case_label")
            .isEqualTo(EXPECTED_LABEL_TEXT);
    }

    @Test
    void shouldSetShowFullReasonToNoWhenAllChildrenDoNotHaveFinalOrderMarked() {
        CaseData caseData = CaseData.builder()
            .children1(createChildren(false, "jim", "dave", "steve", "bob"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        // Due to how the field is deserialised we have to check the map as otherwise it will be ignored
        assertThat(response.getData()).extracting("closeCase").extracting("showFullReason").isEqualTo("NO");
    }

    @Test
    void shouldSetShowFullReasonToYesWhenAllChildrenHaveFinalOrderMarked() {
        CaseData caseData = CaseData.builder()
            .children1(createChildren(true, "jim", "dave", "steve", "bob"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getData()).extracting("closeCase").extracting("showFullReason").isEqualTo("YES");
    }

    private List<Element<Child>> createChildren(boolean allMarked, String... firstNames) {
        Child[] children = new Child[firstNames.length];
        for (int i = 0; i < firstNames.length; i++) {
            children[i] = Child.builder()
                .finalOrderIssued(i != 0 || allMarked ? "Yes" : "No")
                .party(ChildParty.builder()
                    .firstName(firstNames[i])
                    .build())
                .build();
        }
        return wrapElements(children);
    }
}
