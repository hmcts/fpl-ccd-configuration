package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.CASE_DATA_WITH_ALL_ORDERS;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.RENDERED_DYNAMIC_LIST;

@WebMvcTest(UploadTranslationsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadTranslationsControllerAboutToStartTest extends AbstractCallbackTest {

    UploadTranslationsControllerAboutToStartTest() {
        super("upload-translations");
    }

    @Test
    void shouldDisplayAllTranslatableOrdersAboutToStart() {

        CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(CASE_DATA_WITH_ALL_ORDERS));

        assertThat(updatedCaseData.getUploadTranslationsEventData().getUploadTranslationsRelatedToDocument()).isEqualTo(
            RENDERED_DYNAMIC_LIST);
    }

}
