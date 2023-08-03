package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.fpl.controllers.orders.ApproveDraftOrdersController;
import uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.SendLetterService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;
import uk.gov.hmcts.reform.fpl.service.translation.TranslationRequestFormCreationService;
import uk.gov.service.notify.NotificationClient;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ApproveDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApproveDraftOrdersControllerSubmittedTest extends AbstractCallbackTest {

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    ApproveDraftOrdersControllerSubmittedTest() {
        super("approve-draft-orders");
    }

    @Test
    public void shouldTriggerPostHandlingEvent() {
        postSubmittedEvent(CaseData.builder().id(1L).build());
        verify(coreCaseDataService)
            .performPostSubmitCallbackWithoutChange(1L, "internal-change-approve-order");
    }
}
