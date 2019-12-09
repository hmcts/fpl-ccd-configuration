package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, JsonOrdersLookupService.class, LookupTestConfig.class,
    JsonOrdersLookupService.class, DateFormatterService.class, DirectionHelperService.class,
    DocmosisConfiguration.class, RestTemplate.class, CaseDataExtractionService.class,
    DocmosisDocumentGeneratorService.class, CommonCaseDataExtractionService.class, HearingBookingService.class,
    HearingVenueLookUpService.class, DraftCMOService.class
})
class ActionCmoServiceTest {
    private final DraftCMOService draftCMOService;

    private ActionCmoService service;

    @Autowired
    ActionCmoServiceTest(DraftCMOService draftCMOService) {
        this.draftCMOService = draftCMOService;
    }

    @BeforeEach
    void setUp() {
        service = new ActionCmoService(draftCMOService);
    }

    @Test
    void shouldAddDocumentToOrderWhenOrderAndDocumentExist() throws IOException {
        Document document = document();
        CaseManagementOrder orderWithDocument = service.addDocument(CaseManagementOrder.builder().build(), document);

        assertThat(orderWithDocument.getOrderDoc()).isEqualTo(DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build());
    }

    @Test
    void shouldAddOrderActionToCaseDataAndCaseManagementOrderWhenApproved() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        service.prepareCaseDetailsForSubmission(caseDetails, CaseManagementOrder.builder().build(), true);

        assertThat(caseDetails.getData()).containsOnlyKeys("orderAction", "caseManagementOrder");
    }

    @Test
    void shouldAddOrderActionToCaseDataButNotCaseManagementOrderWhenNotApproved() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        service.prepareCaseDetailsForSubmission(caseDetails, CaseManagementOrder.builder().build(), false);

        assertThat(caseDetails.getData()).containsOnlyKeys("orderAction");
    }
}
