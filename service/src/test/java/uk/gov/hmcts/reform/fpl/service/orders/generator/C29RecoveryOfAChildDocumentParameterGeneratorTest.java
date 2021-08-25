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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER_V2;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C29_RECOVERY_OF_A_CHILD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class C29RecoveryOfAChildDocumentParameterGeneratorTest {

    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Local Authority Name";
    private static final String FURTHER_DIRECTIONS = "further directions";
    private static final String OFFICER_NAME = "Officer Barbrady";
    private static final LocalDate ORDER_CREATED_DATE = LocalDate.of(2020, 8, 20);

    private static final String FORMAT_LOCAL_DATE_TO_STRING = "20th August 2020";
    private static final Address REMOVAL_ADDRESS = Address.builder()
        .addressLine1("12 street").postcode("SW1").country("UK").build();
    private static final String ORDER_HEADER = "Warning\n";
    private static final String ORDER_MESSAGE = "It is an offence intentionally to obstruct the "
        + "person from removing the child (Section 50(9) Children Act 1989).";
    private static final String PARAGRAPH_BREAK = "\n\n";

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
            .orderDetails(getStandardMessage()
                + getEntryMessage()
                + getIsNotExparteMessage())
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
            .orderDetails(getStandardMessage()
                + getInformMessage()
                + getIsNotExparteMessage())
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
            .orderDetails(getStandardMessage()
                + getProduceMessage()
                + getIsNotExparteMessage())
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
            .orderDetails(getStandardMessage()
                + getRemoveMessage()
                + getIsNotExparteMessage())
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
            .orderDetails(getStandardMessageWithEPO()
                + getEntryMessage()
                + getInformMessageWithPoliceOfficer()
                + getProduceMessage()
                + getRemoveMessage()
                + getIsExparteMessage())
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

    private String getStandardMessage() {
        return format("The Court is satisfied that %s has parental responsibility for the child by virtue "
            + "of a Care order made on %s.%s", LA_NAME, FORMAT_LOCAL_DATE_TO_STRING, PARAGRAPH_BREAK);

    }

    private String getStandardMessageWithEPO() {
        return format("The Court is satisfied that %s has parental responsibility for the child by virtue "
            + "of an Emergency protection order made on %s.%s", LA_NAME, FORMAT_LOCAL_DATE_TO_STRING, PARAGRAPH_BREAK);
    }

    private String getEntryMessage() {
        return format("The court authorises %s to enter the premises known as %s, "
                + "and search for the child, using reasonable force if necessary.%s",
            OFFICER_NAME,
            REMOVAL_ADDRESS.getAddressAsString(", "),
            PARAGRAPH_BREAK
        );
    }

    private String getInformMessage() {
        return format(
            "The court requires any person who has information about where the child is, "
            + "or may be, to give that information to a police constable or an officer of the court, "
            + "if asked to do so.%s", PARAGRAPH_BREAK
        );
    }

    private String getInformMessageWithPoliceOfficer() {
        return format(
            "The court requires any person who has information about where the child is, "
                + "or may be, to give that information to " + OFFICER_NAME + " or an officer of the court, "
                + "if asked to do so.%s", PARAGRAPH_BREAK
        );
    }

    private String getProduceMessage() {
        return format(
            "The court directs that any person who can produce the child when asked to by %s to do so.%s",
            OFFICER_NAME, PARAGRAPH_BREAK
        );
    }

    private String getRemoveMessage() {
        return format("The Court authorises %s to remove the child.%s", OFFICER_NAME, PARAGRAPH_BREAK);
    }

    private String getIsExparteMessage() {
        return "This order has been made exparte.";
    }

    private String getIsNotExparteMessage() {
        return "This order has not been made exparte.";
    }
}
