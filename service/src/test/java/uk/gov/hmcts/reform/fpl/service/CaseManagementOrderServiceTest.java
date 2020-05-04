package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.NextHearing;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisOrder;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.CMO;
import static uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee.OTHER_1;
import static uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee.RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.buildCaseDataForCMODocmosisGeneration;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRecitals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createSchedule;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

//TODO: this will be slightly improved when 1480 is merged to master
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    FixedTimeConfiguration.class, CaseManagementOrderService.class, HearingBookingService.class,
    DocumentService.class, DraftCMOService.class, CommonDirectionService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, StandardDirectionOrderGenerationService.class, CaseManagementOrderGenerationService.class,
    JsonOrdersLookupService.class, JacksonAutoConfiguration.class, HearingVenueLookUpService.class,
    DocmosisDocumentGeneratorService.class, RestTemplate.class, DocmosisConfiguration.class, UploadDocumentService.class
})
class CaseManagementOrderServiceTest {

    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private DocumentUploadClientApi documentUploadClient;
    @MockBean
    private RequestData requestData;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private CaseManagementOrderGenerationService templateDataService;

    @Autowired
    private Time time;

    @Autowired
    private CaseManagementOrderService service;

    @Test
    void shouldExtractExpectedMapFieldsWhenAllDataIsPresent() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(CaseManagementOrder.builder()
            .schedule(Schedule.builder().build())
            .recitals(emptyList())
            .action(OrderAction.builder().build())
            .build());

        assertThat(data).containsOnlyKeys(SCHEDULE.getKey(), RECITALS.getKey(), ORDER_ACTION.getKey());
    }

    @Test
    void shouldExtractMapFieldsWhenPartialDataIsPresent() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(CaseManagementOrder.builder()
            .schedule(Schedule.builder().build())
            .build());

        assertThat(data).containsOnlyKeys(SCHEDULE.getKey(), RECITALS.getKey(), ORDER_ACTION.getKey());
    }

    @Test
    void shouldExtractMapFieldsWhenCaseManagementOrderIsNull() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(null);

        assertThat(data).containsOnlyKeys(SCHEDULE.getKey(), RECITALS.getKey(), ORDER_ACTION.getKey());
    }

    @Test
    void shouldGetCMOIssueDate() {
        LocalDate expectedIssueDate = LocalDate.now().minusDays(1);
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
            .dateOfIssue(expectedIssueDate.format(DateTimeFormatter.ofPattern(DATE)))
            .build();

        LocalDate issueDate = service.getIssuedDate(caseManagementOrder);

        assertThat(issueDate).isEqualTo(expectedIssueDate);
    }

    @Test
    void shouldGetDefaultCMOIssueDate() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder().build();

        LocalDate issueDate = service.getIssuedDate(caseManagementOrder);

        assertThat(issueDate).isEqualTo(time.now().toLocalDate());
    }

    @Test
    void shouldGetDefaultCMOIssueDateWhenCMODoesNotExists() {
        LocalDate issueDate = service.getIssuedDate(null);

        assertThat(issueDate).isEqualTo(time.now().toLocalDate());
    }

    @Test
    void shouldGetCaseManagementOrderWhenMinimumViableData() {
        Document document = document();
        initMocks(document);

        UUID id = randomUUID();
        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(id, HearingBooking.builder().build())))
            .caseManagementOrder(CaseManagementOrder.builder().id(id).build())
            .build();

        CaseManagementOrder order = service.getOrder(caseData);

        assertThat(order).isEqualTo(CaseManagementOrder.builder()
            .id(id)
            .directions(emptyList())
            .orderDoc(buildFromDocument(document))
            .build());
    }

    @Test
    void shouldGetCaseManagementOrderWhenFullData() {
        Document document = document();
        initMocks(document);

        LocalDateTime dateTime = LocalDateTime.of(2099, 1, 1, 10, 0, 0);
        UUID id = fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31");
        CaseManagementOrder order = service.getOrder(buildCaseDataForCMODocmosisGeneration(dateTime));

        assertThat(order).isEqualToComparingFieldByField(expectedCaseManagementOrder(document, id));
    }

    @Test
    void shouldGetDocumentObjectWhenCalled() {
        Document document = document();
        initMocks(document);

        assertThat(service.getDocument(buildCaseDataForCMODocmosisGeneration(time.now()))).isEqualTo(document);
    }

    private void initMocks(Document document) {
        DocmosisOrder order = any(DocmosisOrder.class);
        given(documentService.getDocumentFromDocmosisOrderTemplate(order, eq(CMO))).willReturn(document);
        given(templateDataService.getTemplateData(any())).willReturn(DocmosisCaseManagementOrder.builder().build());
    }

    private CaseManagementOrder expectedCaseManagementOrder(Document document, UUID id) {
        return CaseManagementOrder.builder()
            .id(id)
            .hearingDate("6 Jan 2099")
            .dateOfIssue(formatLocalDateToString(time.now().toLocalDate(), "d MMMM yyyy"))
            .directions(expectedDirections())
            .schedule(createSchedule(true))
            .recitals(createRecitals())
            .orderDoc(buildFromDocument(document))
            .action(OrderAction.builder().type(SEND_TO_ALL_PARTIES).build())
            .nextHearing(NextHearing.builder().id(id).build())
            .build();
    }

    private List<Element<Direction>> expectedDirections() {
        return wrapElements(
            expectedDirection(ALL_PARTIES),
            expectedDirection(LOCAL_AUTHORITY),
            expectedDirection(RESPONDENT_1),
            expectedDirection(CAFCASS),
            expectedDirection(OTHER_1),
            expectedDirection(COURT)
        );
    }

    private Direction expectedDirection(ParentsAndRespondentsDirectionAssignee respondent) {
        return baseDirection(PARENTS_AND_RESPONDENTS, respondent, null);
    }

    private Direction expectedDirection(OtherPartiesDirectionAssignee other) {
        return baseDirection(OTHERS, null, other);
    }

    private Direction expectedDirection(DirectionAssignee assignee) {
        return baseDirection(assignee, null, null);
    }

    private Direction baseDirection(DirectionAssignee assignee,
                                    ParentsAndRespondentsDirectionAssignee respondent,
                                    OtherPartiesDirectionAssignee other) {
        return Direction.builder()
            .directionType("Direction title")
            .directionText("Mock direction text")
            .directionNeeded(YES.getValue())
            .assignee(assignee)
            .parentsAndRespondentsAssignee(respondent)
            .otherPartiesAssignee(other)
            .readOnly("No")
            .custom("Yes")
            .dateToBeCompletedBy(LocalDateTime.of(2099, 1, 1, 10, 0, 0))
            .build();
    }
}
