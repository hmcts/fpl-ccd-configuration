package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.group.C110A;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;

@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
class LanguageSelectionControllerAboutToSubmitTest extends AbstractCallbackTest {

    LanguageSelectionControllerAboutToSubmitTest() {
        super("language-selection");
    }

    @Test
    void aboutToSubmitShouldPopulateWhenLanguageIsSelected() {
        C110A c110A = C110A.builder()
            .languageRequirementApplication(Language.ENGLISH)
            .languageRequirementApplicationNeedWelsh("Yes")
            .build();

        CaseData caseData = CaseData.builder()
            .languageRequirement("Yes")
            .c110A(c110A)
            .build();

        CaseData responseCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseCaseData.getC110A()).isEqualTo(
            c110A.toBuilder()
                .submittedFormTranslationRequirements(ENGLISH_TO_WELSH)
                .build()
        );
    }
}
