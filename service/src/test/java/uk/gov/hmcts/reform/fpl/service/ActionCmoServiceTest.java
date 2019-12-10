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
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingDynmaicList;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
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
    private static final LocalDateTime MOCK_DATE = LocalDateTime.of(2018, 2, 12, 9, 30);

    private final DraftCMOService draftCMOService;
    private final HearingBookingService hearingBookingService;
    private final DateFormatterService dateFormatterService;

    private ActionCmoService service;

    @Autowired
    ActionCmoServiceTest(DraftCMOService draftCMOService,
                         HearingBookingService hearingBookingService,
                         DateFormatterService dateFormatterService) {
        this.draftCMOService = draftCMOService;
        this.hearingBookingService = hearingBookingService;
        this.dateFormatterService = dateFormatterService;
    }

    @BeforeEach
    void setUp() {
        service = new ActionCmoService(draftCMOService, dateFormatterService, hearingBookingService);
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

        service.progressCMOToAction(caseDetails, CaseManagementOrder.builder().build(), true);

        assertThat(caseDetails.getData()).containsOnlyKeys("orderAction", "caseManagementOrder");
    }

    @Test
    void shouldAddOrderActionToCaseDataButNotCaseManagementOrderWhenNotApproved() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        service.progressCMOToAction(caseDetails, CaseManagementOrder.builder().build(), true);

        assertThat(caseDetails.getData()).containsOnlyKeys("orderAction");
    }

    @Test
    void shouldReturnEmptyStringWhenOrderActionIsNotPresentOnCMO() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings(MOCK_DATE);
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder().build();

        String label = service.createNextHearingDateLabel(caseManagementOrder, hearingBookings);

        assertThat(label).isEqualTo("");
    }

    @Test
    void shouldFormatNextHearingBookingLabelWhenCMOOrderActionContainsMatchingUUID() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings(MOCK_DATE);

        CaseManagementOrder caseManagementOrder =
            createCMOWithNextHearing(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"));

        String label = service.createNextHearingDateLabel(caseManagementOrder, hearingBookings);

        assertThat(label).isEqualTo("The next hearing date is on 12 February at 9:30am");
    }

    @Test
    void shouldSetOrderActionNextHearingDateWhenProvidedNextHearingDateList() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder().build();

        CaseManagementOrder updatedCaseManagementOrder =
            service.appendNextHearingDateToCMO(createHearingBookingDynmaicList(), caseManagementOrder);

        OrderAction orderAction = updatedCaseManagementOrder.getAction();

        assertThat(orderAction.getNextHearingId()).isEqualTo(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"));
        assertThat(orderAction.getNextHearingDate()).isEqualTo("15th Dec 2019");
    }

    @Test
    void shouldPreserveCMOWhenNextHearingDateListIsNotProvided() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
            .hearingDate("Test date")
            .build();

        CaseManagementOrder updatedCaseManagementOrder = service.appendNextHearingDateToCMO(null,
            caseManagementOrder);

        assertThat(updatedCaseManagementOrder.getHearingDate()).isEqualTo("Test date");
    }

    private CaseManagementOrder createCMOWithNextHearing(UUID hearingID) {
        return CaseManagementOrder.builder()
            .action(OrderAction.builder()
                .nextHearingId(hearingID)
                .build())
            .build();
    }
}
