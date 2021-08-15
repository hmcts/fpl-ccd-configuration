package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.C29ActionsPermitted;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C29RecoveryOfAChildDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER_V2;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C29_RECOVERY_OF_A_CHILD;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class C29RecoveryOfAChildDocumentParameterGeneratorTest {

    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Local Authority Name";
    private static final String FURTHER_DIRECTIONS = "further directions";
    private static final String OFFICER_NAME = "Officer Barbrady";
    private static final LocalDate ORDER_CREATED_DATE = LocalDate.of(2021, 8, 20);
    private static final String dayOrdinalSuffix = getDayOfMonthSuffix(ORDER_CREATED_DATE.getDayOfMonth());

    private static final String formattedDate = formatLocalDateToString(
        ORDER_CREATED_DATE,
        String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
    );
    private static final Address REMOVAL_ADDRESS = Address.builder()
        .addressLine1("12 street").postcode("SW1").country("UK").build();
    private static final String ORDER_HEADER = "Warning \n";
    private static final String ORDER_MESSAGE = "It is an offence intentionally to obstruct the "
        + "person from removing the child (Section 50(9) Children Act 1989).";

    @Mock
    private static Child CHILD;

    @Mock
    private ChildrenService childrenService;

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @InjectMocks
    private C29RecoveryOfAChildDocumentParameterGenerator underTest;

    @BeforeEach
    void setUp() {
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
    }

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(C29_RECOVERY_OF_A_CHILD);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(ORDER_V2);
    }

    @Test
    void shouldGenerateDocumentWithEntryMessage() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .caseLocalAuthorityName(LA_NAME)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOrderCreatedDate(ORDER_CREATED_DATE)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersActionsPermitted(List.of(C29ActionsPermitted.INFORM))
                .manageOrdersIsExParte("No")
                .build())
            .build();

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(getInformMessage() + getIsExparteMessage(false))
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    private C29RecoveryOfAChildDocmosisParameters.C29RecoveryOfAChildDocmosisParametersBuilder
        <?,?> expectedCommonParameters() {
        return C29RecoveryOfAChildDocmosisParameters.builder()
            .furtherDirections(FURTHER_DIRECTIONS)
            .localAuthorityName(LA_NAME)
            .dateOfIssue(formattedDate)
            .orderTitle(C29_RECOVERY_OF_A_CHILD.getTitle());
    }

    private CaseData getCaseData() {
        return CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .caseLocalAuthorityName(LA_NAME)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOrderCreatedDate(ORDER_CREATED_DATE)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .build())
            .build();
    }

    private String getIsExparteMessage(boolean isExparte) {
        return isExparte ? "This order has been made exparte." : "This order has not been made exparte.";
    }

    private String getInformMessage() {
        return "The court requires any person who has information about where the child is, "
            + "or may be, to give that information to a police constable or an officer of the court, "
            + "if asked to do so.\n\n";
    }
}
