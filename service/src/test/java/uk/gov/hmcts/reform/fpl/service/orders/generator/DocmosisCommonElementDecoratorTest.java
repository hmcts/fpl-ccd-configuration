package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.CourtLookUpService;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C32CareOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class DocmosisCommonElementDecoratorTest {

    private static final long CASE_NUMBER = 1234123412341234L;
    private static final String FORMATTED_CASE_NUMBER = "1234-1234-1234-1234";
    private static final String LA_CODE = "LA_CODE";
    private static final String COURT_NAME = "Court Name";
    private static final String FAM_MAN_CASE_NUM = "case number";
    private static final String TITLE = "Order title";
    private static final String CHILDREN_ACT = "Children act for order";
    private static final String CREST = "[userImage:crest.png]";
    private static final String WATERMARK = "[userImage:draft-watermark.png]";
    private static final String SEAL = "[userImage:familycourtseal.png]";
    private static final LocalDate APPROVAL_DATE = LocalDate.of(2021, 4, 20);
    private static final String EXPECTED_APPROVAL_DATE = "20 April 2021";
    private static final JudgeAndLegalAdvisor JUDGE = mock(JudgeAndLegalAdvisor.class);
    private static final DocmosisJudgeAndLegalAdvisor DOCMOSIS_JUDGE = mock(DocmosisJudgeAndLegalAdvisor.class);
    private static final List<Element<Child>> CHILDREN = wrapElements(mock(Child.class));
    private static final List<DocmosisChild> DOCMOSIS_CHILDREN = List.of(mock(DocmosisChild.class));
    private static final DocmosisParameters DOCMOSIS_PARAMETERS = C32CareOrderDocmosisParameters.builder().build();
    private static final Order ORDER_TYPE = mock(Order.class);
    private static final CaseData CASE_DATA = CaseData.builder()
        .familyManCaseNumber(FAM_MAN_CASE_NUM)
        .id(CASE_NUMBER)
        .caseLocalAuthority(LA_CODE)
        .judgeAndLegalAdvisor(JUDGE)
        .manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersApprovalDate(APPROVAL_DATE)
            .build())
        .build();

    private final ChildrenSmartSelector childrenSmartSelector = mock(ChildrenSmartSelector.class);
    private final CaseDataExtractionService extractionService = mock(CaseDataExtractionService.class);
    private final CourtService courtService = mock(CourtService.class);

    private final DocmosisCommonElementDecorator underTest = new DocmosisCommonElementDecorator(
        childrenSmartSelector, extractionService, courtService);

    @BeforeEach
    void setUp() {
        when(extractionService.getCourtName(CASE_DATA)).thenReturn(COURT_NAME);
        when(extractionService.getJudgeAndLegalAdvisor(JUDGE)).thenReturn(DOCMOSIS_JUDGE);
        when(childrenSmartSelector.getSelectedChildren(CASE_DATA)).thenReturn(CHILDREN);
        when(extractionService.getChildrenDetails(CHILDREN)).thenReturn(DOCMOSIS_CHILDREN);

        when(ORDER_TYPE.getTitle()).thenReturn(TITLE);
        when(ORDER_TYPE.getChildrenAct()).thenReturn(CHILDREN_ACT);
    }

    @Test
    void decorateDraft() {
        DocmosisParameters decorated = underTest.decorate(DOCMOSIS_PARAMETERS, CASE_DATA, DRAFT, ORDER_TYPE);
        DocmosisParameters expectedParameters = expectedCommonParameters(EXPECTED_APPROVAL_DATE)
            .draftbackground(WATERMARK)
            .build();

        assertThat(decorated).isEqualTo(expectedParameters);
    }

    @Test
    void decorateSealed() {
        when(courtService.getCourtSeal(CASE_DATA, SEALED))
                .thenReturn(COURT_SEAL.getValue(CASE_DATA.getImageLanguage()));

        DocmosisParameters decorated = underTest.decorate(DOCMOSIS_PARAMETERS, CASE_DATA, SEALED, ORDER_TYPE);
        DocmosisParameters expectedParameters = expectedCommonParameters(EXPECTED_APPROVAL_DATE)
            .courtseal(COURT_SEAL.getValue(Language.ENGLISH))
            .build();

        assertThat(decorated).isEqualTo(expectedParameters);
    }

    @Test
    void decorateHighCourtSealed() {
        CaseData caseData = CASE_DATA.toBuilder()
                .court(Court.builder()
                        .code(CourtLookUpService.RCJ_HIGH_COURT_CODE)
                        .build())
                .build();

        when(courtService.getCourtSeal(caseData, SEALED))
                .thenReturn(null);
        when(extractionService.getCourtName(caseData)).thenReturn(COURT_NAME);
        when(childrenSmartSelector.getSelectedChildren(caseData)).thenReturn(CHILDREN);

        DocmosisParameters decorated = underTest.decorate(DOCMOSIS_PARAMETERS, caseData, SEALED, ORDER_TYPE);
        DocmosisParameters expectedParameters = expectedCommonParameters(EXPECTED_APPROVAL_DATE)
                .courtseal(null)
                .build();

        assertThat(decorated).isEqualTo(expectedParameters);
    }

    @Test
    void shouldNotUpdateDateOfIssueWhenDateOfIssueIsAlreadySet() {
        final String expectedDateOfIssue = "25 April 2021, 9:00am";
        final LocalDateTime dateOfIssue = LocalDateTime.of(2021, 4, 25, 9, 0, 0);

        C32CareOrderDocmosisParameters docmosisParametersWithIssueDate = C32CareOrderDocmosisParameters.builder()
            .dateOfIssue(DateFormatterHelper.formatLocalDateTimeBaseUsingFormat(dateOfIssue, DATE_TIME))
            .build();

        when(courtService.getCourtSeal(CASE_DATA, SEALED))
                .thenReturn(COURT_SEAL.getValue(CASE_DATA.getImageLanguage()));

        DocmosisParameters decorated = underTest.decorate(
            docmosisParametersWithIssueDate, CASE_DATA, SEALED, ORDER_TYPE);

        DocmosisParameters expectedParameters = expectedCommonParameters(expectedDateOfIssue)
            .courtseal(SEAL)
            .build();

        assertThat(decorated).isEqualTo(expectedParameters);
    }

    @Test
    void shouldNotUpdateChildActWhenAlreadySet() {
        DocmosisParameters decorated = underTest.decorate(
            C32CareOrderDocmosisParameters.builder().childrenAct("Custom child act").build(),
            CASE_DATA,
            SEALED,
            ORDER_TYPE);

        assertThat(decorated.getChildrenAct()).isEqualTo("Custom child act");
    }

    private C32CareOrderDocmosisParameters.C32CareOrderDocmosisParametersBuilder<?, ?> expectedCommonParameters(
        String dateOfIssue) {

        return C32CareOrderDocmosisParameters.builder()
            .familyManCaseNumber(FAM_MAN_CASE_NUM)
            .ccdCaseNumber(FORMATTED_CASE_NUMBER)
            .childrenAct(CHILDREN_ACT)
            .judgeAndLegalAdvisor(DOCMOSIS_JUDGE)
            .courtName(COURT_NAME)
            .dateOfIssue(dateOfIssue)
            .children(DOCMOSIS_CHILDREN)
            .crest(CREST);
    }
}
