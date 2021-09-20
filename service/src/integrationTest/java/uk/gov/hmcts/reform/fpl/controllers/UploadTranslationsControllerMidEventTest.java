package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.UploadTranslationsEventData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.CASE_DATA_WITH_ALL_ORDERS;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.RENDERED_DYNAMIC_LIST;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.UUID_3;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.dlElement;

@WebMvcTest(UploadTranslationsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadTranslationsControllerMidEventTest extends AbstractCallbackTest {

    UploadTranslationsControllerMidEventTest() {
        super("upload-translations");
    }

    @Test
    void shouldDisplayOriginalDocumentMidEvent() {
        CaseData updatedCaseData = extractCaseData(postMidEvent(
            CASE_DATA_WITH_ALL_ORDERS.toBuilder()
                .uploadTranslationsEventData(UploadTranslationsEventData.builder()
                    .uploadTranslationsRelatedToDocument(RENDERED_DYNAMIC_LIST.toBuilder()
                        .value(dlElement(UUID_3, "Notice of proceedings (C6)"))
                        .build())
                    .build())
                .build(),
            "select-document"
        ));

        assertThat(updatedCaseData.getUploadTranslationsEventData().getUploadTranslationsOriginalDoc()).isEqualTo(
            DocumentReference.builder()
                .filename("noticeo_c6.pdf")
                .build());

    }

}
