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
import uk.gov.hmcts.reform.fpl.enums.PlacedUnderOrder;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C29RecoveryOfAChildDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C29RecoveryOfAChildDocmosisParameters.C29RecoveryOfAChildDocmosisParametersBuilder;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.time.LocalDate;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.isNull;
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
    private static final LocalDate ORDER_CREATED_DATE = LocalDate.of(2020, 8, 20);

    private static final String FORMAT_LOCAL_DATE_TO_STRING = formatLocalDateToString(
        ORDER_CREATED_DATE,
        format(DATE_WITH_ORDINAL_SUFFIX, getDayOfMonthSuffix(ORDER_CREATED_DATE.getDayOfMonth()))
    );
    private static final Address REMOVAL_ADDRESS = Address.builder()
        .addressLine1("12 street").postcode("SW1").country("UK").build();
    private static final String ORDER_HEADER = "Warning\n";
    private static final String ORDER_MESSAGE = "It is an offence intentionally to obstruct the "
        + "person from removing the child (Section 50(9) Children Act 1989).";
    private static final String paragraphBreak = "\n\n";

    @Mock
    private Child child;

    @Mock
    private ChildrenService childrenService;

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @Mock
    private ManageOrderDocumentService manageOrderDocumentService;

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
                .manageOrdersActionsPermittedAddress(REMOVAL_ADDRESS)
                .manageOrdersOfficerName(OFFICER_NAME)
                .manageOrdersActionsPermitted(List.of(C29ActionsPermitted.ENTRY))
                .manageOrdersIsExParte("No")
                .manageOrdersPlacedUnderOrder(PlacedUnderOrder.CARE_ORDER)
                .build())
            .build();

        List<Element<Child>> selectedChildren = wrapElements(child);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(manageOrderDocumentService.getChildGrammar(selectedChildren.size())).thenReturn("child");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(getStandardMessage(
                manageOrderDocumentService.getChildGrammar(selectedChildren.size()),
                PlacedUnderOrder.CARE_ORDER)
                + getEntryMessage(selectedChildren.size())
                + getIsExparteMessage(false))
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldGenerateDocumentWithInformMessage() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .caseLocalAuthorityName(LA_NAME)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOrderCreatedDate(ORDER_CREATED_DATE)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersActionsPermitted(List.of(C29ActionsPermitted.INFORM))
                .manageOrdersIsExParte("No")
                .manageOrdersPlacedUnderOrder(PlacedUnderOrder.CARE_ORDER)
                .build())
            .build();

        List<Element<Child>> selectedChildren = wrapElements(child);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(manageOrderDocumentService.getChildGrammar(selectedChildren.size())).thenReturn("child");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(getStandardMessage(
                manageOrderDocumentService.getChildGrammar(selectedChildren.size()),
                PlacedUnderOrder.CARE_ORDER)
                + getInformMessage(caseData.getManageOrdersEventData())
                + getIsExparteMessage(false))
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldGenerateDocumentWithProduceMessage() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .caseLocalAuthorityName(LA_NAME)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOrderCreatedDate(ORDER_CREATED_DATE)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersOfficerName(OFFICER_NAME)
                .manageOrdersActionsPermitted(List.of(C29ActionsPermitted.PRODUCE))
                .manageOrdersIsExParte("No")
                .manageOrdersPlacedUnderOrder(PlacedUnderOrder.CARE_ORDER)
                .build())
            .build();

        List<Element<Child>> selectedChildren = wrapElements(child);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(manageOrderDocumentService.getChildGrammar(selectedChildren.size())).thenReturn("child");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(getStandardMessage(
                manageOrderDocumentService.getChildGrammar(selectedChildren.size()),
                PlacedUnderOrder.CARE_ORDER)
                + getProduceMessage()
                + getIsExparteMessage(false))
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }


    @Test
    void shouldGenerateDocumentWithRemoveMessage() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .caseLocalAuthorityName(LA_NAME)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOrderCreatedDate(ORDER_CREATED_DATE)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersOfficerName(OFFICER_NAME)
                .manageOrdersActionsPermitted(List.of(C29ActionsPermitted.REMOVE))
                .manageOrdersIsExParte("No")
                .manageOrdersPlacedUnderOrder(PlacedUnderOrder.CARE_ORDER)
                .build())
            .build();

        List<Element<Child>> selectedChildren = wrapElements(child);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(manageOrderDocumentService.getChildGrammar(selectedChildren.size())).thenReturn("child");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(getStandardMessage(
                manageOrderDocumentService.getChildGrammar(selectedChildren.size()),
                PlacedUnderOrder.CARE_ORDER)
                + getRemoveMessage()
                + getIsExparteMessage(false))
            .orderHeader(ORDER_HEADER)
            .orderMessage(ORDER_MESSAGE)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldGenerateDocumentWithAllActionsPermittedOfficerNameEnteredAndIsExParte() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .caseLocalAuthorityName(LA_NAME)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOrderCreatedDate(ORDER_CREATED_DATE)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersActionsPermittedAddress(REMOVAL_ADDRESS)
                .manageOrdersOfficerName(OFFICER_NAME)
                .manageOrdersActionsPermitted(List.of(
                    C29ActionsPermitted.ENTRY,
                    C29ActionsPermitted.INFORM,
                    C29ActionsPermitted.PRODUCE,
                    C29ActionsPermitted.REMOVE
                ))
                .manageOrdersPlacedUnderOrder(PlacedUnderOrder.EMERGENCY_PROTECTION_ORDER)
                .manageOrdersIsExParte("Yes")
                .build())
            .build();

        List<Element<Child>> selectedChildren = wrapElements(child);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(manageOrderDocumentService.getChildGrammar(selectedChildren.size())).thenReturn("child");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(getStandardMessage(
                manageOrderDocumentService.getChildGrammar(selectedChildren.size()),
                PlacedUnderOrder.EMERGENCY_PROTECTION_ORDER)
                + getEntryMessage(selectedChildren.size())
                + getInformMessage(caseData.getManageOrdersEventData())
                + getProduceMessage()
                + getRemoveMessage()
                + getIsExparteMessage(true))
            .orderHeader(ORDER_HEADER)
            .orderMessage(ORDER_MESSAGE)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    private C29RecoveryOfAChildDocmosisParametersBuilder<?,?> expectedCommonParameters() {
        return C29RecoveryOfAChildDocmosisParameters.builder()
            .furtherDirections(FURTHER_DIRECTIONS)
            .localAuthorityName(LA_NAME)
            .dateOfIssue(FORMAT_LOCAL_DATE_TO_STRING)
            .orderTitle(C29_RECOVERY_OF_A_CHILD.getTitle());
    }

    private String getStandardMessage(String childOrChildren, PlacedUnderOrder order) {
        String orderGrammar = order.equals(PlacedUnderOrder.EMERGENCY_PROTECTION_ORDER) ? "an " : "a ";

        return format(
            "The Court is satisfied that %s has parental responsibility for the %s by virtue of %s made on %s.%s",
            LA_NAME,
            childOrChildren,
            orderGrammar + order.getLabel(),
            FORMAT_LOCAL_DATE_TO_STRING,
            paragraphBreak);
    }

    private String getEntryMessage(int numberOfChildren) {
        String childrenGrammar = numberOfChildren > 1 ? "children" : "child";
        return format("The court authorises %s to enter the premises known as %s, "
                + "and search for the %s, using reasonable force if necessary.%s",
            OFFICER_NAME,
            REMOVAL_ADDRESS.getAddressAsString(", "),
            childrenGrammar,
            paragraphBreak
        );
    }

    private String getInformMessage(ManageOrdersEventData eventData) {
        String policeAssignee = isNull(eventData.getManageOrdersOfficerName())
            ? "a police constable"
            : eventData.getManageOrdersOfficerName();
        return format(
            "The court requires any person who has information about where the child is, "
            + "or may be, to give that information to %s or an officer of the court, "
            + "if asked to do so.%s", policeAssignee, paragraphBreak
        );
    }

    private String getProduceMessage() {
        return format(
            "The court directs that any person who can produce the child when asked to by %s to do so.%s",
            OFFICER_NAME, paragraphBreak
        );
    }

    private String getRemoveMessage() {
        return format("The Court authorises %s to remove the child.%s", OFFICER_NAME, paragraphBreak);
    }

    private String getIsExparteMessage(boolean isExparte) {
        return isExparte ? "This order has been made exparte." : "This order has not been made exparte.";
    }
}
