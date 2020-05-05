package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.NextHearing;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FixedTimeConfiguration.class, CaseManagementOrderService.class})
class CaseManagementOrderServiceTest {
    private static final Document DOCUMENT = document();
    private static final UUID ID = fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31");

    @MockBean
    private DocumentService documentService;

    @MockBean
    private CaseManagementOrderGenerationService templateDataService;

    @MockBean
    private DraftCMOService draftCMOService;

    @Autowired
    private Time time;

    @Autowired
    private CaseManagementOrderService service;

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
    void shouldGetCaseManagementOrderWithDocumentReference() {
        initMocks();

        LocalDateTime dateTime = LocalDateTime.of(2099, 1, 1, 10, 0, 0);
        CaseManagementOrder order = service.getOrder(buildCaseDataForCMODocmosisGeneration(dateTime));

        assertThat(order).isEqualToComparingFieldByField(expectedCaseManagementOrder());
    }

    @Test
    void shouldGetDocumentObjectWhenCalled() {
        initMocks();

        assertThat(service.getDocument(buildCaseDataForCMODocmosisGeneration(time.now()))).isEqualTo(DOCUMENT);
    }

    private void initMocks() {
        DocmosisOrder order = any(DocmosisOrder.class);
        given(documentService.getDocumentFromDocmosisOrderTemplate(order, eq(CMO))).willReturn(DOCUMENT);
        given(templateDataService.getTemplateData(any())).willReturn(DocmosisCaseManagementOrder.builder().build());
        given(draftCMOService.prepareCaseManagementOrder(any())).willReturn(baseOrder().build());
    }

    private CaseManagementOrder expectedCaseManagementOrder() {
        return baseOrder().orderDoc(buildFromDocument(DOCUMENT)).build();
    }

    private CaseManagementOrder.CaseManagementOrderBuilder baseOrder() {
        return CaseManagementOrder.builder()
            .id(ID)
            .hearingDate("6 Jan 2099")
            .dateOfIssue(formatLocalDateToString(time.now().toLocalDate(), "d MMMM yyyy"))
            .directions(expectedDirections())
            .schedule(createSchedule(true))
            .recitals(createRecitals())
            .action(OrderAction.builder().type(SEND_TO_ALL_PARTIES).build())
            .nextHearing(NextHearing.builder().id(CaseManagementOrderServiceTest.ID).build());
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
