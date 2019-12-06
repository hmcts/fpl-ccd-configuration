package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, JsonOrdersLookupService.class, LookupTestConfig.class,
    JsonOrdersLookupService.class,
    DateFormatterService.class,
    DirectionHelperService.class, DocmosisConfiguration.class,
    RestTemplate.class,
    CaseDataExtractionService.class, DocmosisDocumentGeneratorService.class, CommonCaseDataExtractionService.class,
    HearingBookingService.class,
    HearingVenueLookUpService.class,
    DraftCMOService.class
})
class ActionCmoServiceTest {
    private final ObjectMapper mapper;
    private final DraftCMOService draftCMOService;

    private ActionCmoService service;

    @Autowired
    ActionCmoServiceTest(ObjectMapper mapper, DraftCMOService draftCMOService) {
        this.mapper = mapper;
        this.draftCMOService = draftCMOService;
    }

    @BeforeEach
    void setUp() {
        service = new ActionCmoService(mapper, draftCMOService);
    }

    @Test
    void shouldAddDocumentToOrder() throws IOException {
        CaseManagementOrder orderWithDocument = service.addDocument(CaseManagementOrder.builder().build(), document());

        assertThat(orderWithDocument.getOrderDoc()).isNotNull();
    }
}
