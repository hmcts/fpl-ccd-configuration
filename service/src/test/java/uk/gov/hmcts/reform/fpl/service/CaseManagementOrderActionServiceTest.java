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
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, DocmosisDocumentGeneratorService.class,
    DocmosisConfiguration.class, UploadDocumentService.class, JsonOrdersLookupService.class, RestTemplate.class,
    HearingVenueLookUpService.class, CommonCaseDataExtractionService.class, DateFormatterService.class,
    HearingBookingService.class, DirectionHelperService.class})
public class CaseManagementOrderActionServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String COURT_EMAIL_ADDRESS = "FamilyPublicLaw+test@gmail.com";
    private static final String COURT_NAME = "Test court";

    private final ObjectMapper objectMapper;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final OrdersLookupService ordersLookupService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final CommonCaseDataExtractionService commonCaseDataExtractionService;
    private final DateFormatterService dateFormatterService;
    private final HearingBookingService hearingBookingService;
    private final DirectionHelperService directionHelperService;

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(
        String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL_ADDRESS));
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration =
        new LocalAuthorityEmailLookupConfiguration(String.format("%s=>%s",
            LOCAL_AUTHORITY_CODE, LOCAL_AUTHORITY_EMAIL_ADDRESS));
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration =
        new LocalAuthorityNameLookupConfiguration(String.format("%s=>%s", LOCAL_AUTHORITY_CODE, LOCAL_AUTHORITY_NAME));

    private DraftCMOService draftCMOService;
    private CaseManageOrderActionService caseManageOrderActionService;

    @Autowired
    public CaseManagementOrderActionServiceTest(ObjectMapper objectMapper,
                                                DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                                                UploadDocumentService uploadDocumentService,
                                                OrdersLookupService ordersLookupService,
                                                HearingVenueLookUpService hearingVenueLookUpService,
                                                CommonCaseDataExtractionService commonCaseDataExtractionService,
                                                DateFormatterService dateFormatterService,
                                                HearingBookingService hearingBookingService,
                                                DirectionHelperService directionHelperService) {
        this.objectMapper = objectMapper;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.ordersLookupService = ordersLookupService;
        this.hearingVenueLookUpService = hearingVenueLookUpService;
        this.commonCaseDataExtractionService = commonCaseDataExtractionService;
        this.dateFormatterService = dateFormatterService;
        this.hearingBookingService = hearingBookingService;
        this.directionHelperService = directionHelperService;
    }

    @BeforeEach
    void setUp() {
        CaseDataExtractionService caseDataExtractionService = new CaseDataExtractionService(dateFormatterService,
            hearingBookingService, hmctsCourtLookupConfiguration, ordersLookupService, directionHelperService,
            hearingVenueLookUpService, commonCaseDataExtractionService, docmosisDocumentGeneratorService);

        draftCMOService = new DraftCMOService(objectMapper, dateFormatterService, directionHelperService,
            caseDataExtractionService, hmctsCourtLookupConfiguration, localAuthorityEmailLookupConfiguration,
            localAuthorityNameLookupConfiguration, ordersLookupService, docmosisDocumentGeneratorService,
            commonCaseDataExtractionService);

        caseManageOrderActionService = new CaseManageOrderActionService(objectMapper, draftCMOService,
            docmosisDocumentGeneratorService, uploadDocumentService);
    }

    @Test
    void shouldReturnDraftCaseManagementOrderForAction() {

    }
}
