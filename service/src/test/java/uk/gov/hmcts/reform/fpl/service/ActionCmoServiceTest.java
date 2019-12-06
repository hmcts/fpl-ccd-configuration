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
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, JsonOrdersLookupService.class, HearingVenueLookUpService.class,
    DocmosisDocumentGeneratorService.class, RestTemplate.class, DocmosisConfiguration.class,
    CommonCaseDataExtractionService.class, DateFormatterService.class, HearingBookingService.class,
    DirectionHelperService.class
})
class ActionCmoServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_EMAIL_ADDRESS = "FamilyPublicLaw+test@gmail.com";
    private static final String COURT_NAME = "Test court";
    private static final LocalDateTime MOCK_DATE = LocalDateTime.of(2018, 2, 12, 9, 30);
    private final ObjectMapper mapper;
    private final OrdersLookupService ordersLookupService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final CommonCaseDataExtractionService commonCaseDataExtractionService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final DateFormatterService dateFormatterService;
    private final HearingBookingService hearingBookingService;
    private final DirectionHelperService directionHelperService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(
        String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL_ADDRESS));

    private CaseManagementOrder caseManagementOrder;
    private List<Element<HearingBooking>> hearingDetails;
    private DraftCMOService draftCMOService;
    private ActionCmoService actionCmoService;
    private CaseDataExtractionService caseDataExtractionService;

    @Autowired
    public ActionCmoServiceTest(ObjectMapper mapper, OrdersLookupService ordersLookupService,
                                HearingVenueLookUpService hearingVenueLookUpService,
                                CommonCaseDataExtractionService commonCaseDataExtractionService,
                                DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                                DateFormatterService dateFormatterService,
                                HearingBookingService hearingBookingService,
                                DirectionHelperService directionHelperService) {
        this.mapper = mapper;
        this.ordersLookupService = ordersLookupService;
        this.hearingVenueLookUpService = hearingVenueLookUpService;
        this.commonCaseDataExtractionService = commonCaseDataExtractionService;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.dateFormatterService = dateFormatterService;
        this.hearingBookingService = hearingBookingService;
        this.directionHelperService = directionHelperService;
    }

    @BeforeEach
    void setUp() {
        CaseDataExtractionService caseDataExtractionService = new CaseDataExtractionService(dateFormatterService,
            hearingBookingService, hmctsCourtLookupConfiguration, ordersLookupService, directionHelperService,
            hearingVenueLookUpService, commonCaseDataExtractionService, docmosisDocumentGeneratorService);

        draftCMOService = new DraftCMOService(mapper, dateFormatterService, directionHelperService,
            caseDataExtractionService, hmctsCourtLookupConfiguration,
            docmosisDocumentGeneratorService, commonCaseDataExtractionService, hearingVenueLookUpService);

        actionCmoService = new ActionCmoService(mapper, draftCMOService, dateFormatterService,
            hearingVenueLookUpService, hearingBookingService);
    }

    @Test
    void shouldReturnEmptyStringWhenCMOActionIsNotPresent() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings(MOCK_DATE);
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder().build();

        String label = actionCmoService.createNextHearingDateLabel(caseManagementOrder, hearingBookings);

        assertThat(label).isEqualTo("");
    }

    @Test
    void shouldFormatNextHearingBookingLabelWhenCMOActionContainsMatchingUUID() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings(MOCK_DATE);

        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
            .action(OrderAction.builder()
                .nextHearingId(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                .build())
            .build();

        String label = actionCmoService.createNextHearingDateLabel(caseManagementOrder, hearingBookings);

        assertThat(label).isEqualTo(label).isEqualTo("The next hearing date is on 12 February at 9:30am");
    }

    @Test
    void shouldPreserveCMOWhenHearingDateListIsNull() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
            .hearingDate("Test date")
            .build();

        CaseManagementOrder updatedCaseManagemntOrder =
            actionCmoService.appendNextHearingDateToCMO(null, caseManagementOrder);

        assertThat(caseManagementOrder.getHearingDate()).isEqualTo("Test date");
    }

    @Test
    void shouldSetCMONextHearingDateWhenProvidedHearingDateList() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder().build();

        CaseManagementOrder updatedCaseManagemntOrder =
            actionCmoService.appendNextHearingDateToCMO(createHearingBookingDynmaicList(), caseManagementOrder);

        OrderAction orderAction = updatedCaseManagemntOrder.getAction();

        assertThat(orderAction.getNextHearingId()).isEqualTo(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"));
        assertThat(orderAction.getNextHearingDate()).isEqualTo("15th Dec 2019");
    }

    private DynamicList createHearingBookingDynmaicList() {
        return DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                .label("15th Dec 2019")
                .build())
            .listItems(List.of(DynamicListElement.builder().code(UUID.randomUUID()).label("test").build()))
            .build();
    }
}
