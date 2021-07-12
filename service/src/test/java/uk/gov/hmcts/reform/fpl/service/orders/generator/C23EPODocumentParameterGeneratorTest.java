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
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C23EPODocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.EPO_V2;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class C23EPODocumentParameterGeneratorTest {

    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Local Authority Name";
    private static final String FURTHER_DIRECTIONS = "further directions";
    private static final Order ORDER_TYPE = C23_EMERGENCY_PROTECTION_ORDER;

    public static final String CHILDREN_DESCRIPTION = "Children description";
    public static final LocalDateTime APPROVAL_DATE_TIME = LocalDateTime.of(2021, 4, 20, 10, 0, 0);
    public static final LocalDateTime END_DATE_TIME = LocalDateTime.of(2021, 4, 25, 10, 0, 0);
    public static final String EXPECTED_APPROVAL_DATE_TIME = "20 April 2021, 10:00am";
    public static final LocalDate EXCLUSION_DATE = LocalDate.now().plusDays(2);
    public static final String EXCLUDE_PERSON = "Test user1";
    public static final Address REMOVAL_ADDRESS = Address.builder()
        .addressLine1("12 street").postcode("SW1").country("UK").build();

    @Mock
    private static Child CHILD;
    @Mock
    private ChildrenSmartSelector childrenSmartSelector;
    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @InjectMocks
    private C23EPODocumentParameterGenerator underTest;

    @BeforeEach
    void setUp() {
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
    }

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(ORDER_TYPE);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(EPO_V2);
    }

    @Test
    void testOrderTemplateForEPOTypeRemoveToAccommodation() {
        final CaseData CASE_DATA = CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .manageOrdersEventData(getManageOrdersEventData(EPOType.REMOVE_TO_ACCOMMODATION, null, false))
            .build();
        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenSmartSelector.getSelectedChildren(CASE_DATA)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA);
        DocmosisParameters expectedParameters = expectedParameters(EPOType.REMOVE_TO_ACCOMMODATION, false, false);

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void testOrderTemplateForEPOTypePreventRemoval() {
        final CaseData CASE_DATA = CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .manageOrdersEventData(getManageOrdersEventData(EPOType.PREVENT_REMOVAL, REMOVAL_ADDRESS, true))
            .build();

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenSmartSelector.getSelectedChildren(CASE_DATA)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA);
        DocmosisParameters expectedParameters = expectedParameters(EPOType.PREVENT_REMOVAL, true, true);

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    private DocmosisParameters expectedParameters(EPOType type,
                                                  boolean exclusionRequired,
                                                  boolean powerOfArrestFileAttached) {
        final String exclusionRequirement = String.format("The Court directs that %s be excluded from %s from %s "
                + "so that the child may continue to live "
                + "there, consent to the exclusion requirement having been given by %s.",
            EXCLUDE_PERSON, REMOVAL_ADDRESS.getAddressAsString(", "),
            formatLocalDateToString(EXCLUSION_DATE, DATE), EXCLUDE_PERSON);

        return C23EPODocmosisParameters.builder()
            .orderType(EMERGENCY_PROTECTION_ORDER)
            .orderTitle("Emergency protection order")
            .dateOfIssue(EXPECTED_APPROVAL_DATE_TIME)
            .furtherDirections(FURTHER_DIRECTIONS)
            .orderDetails(format("It is ordered that the child is placed in the care of %s.", LA_NAME))
            .localAuthorityName(LA_NAME)
            .epoType(type)
            .includePhrase("Yes")
            .childrenDescription(CHILDREN_DESCRIPTION)
            .epoStartDateTime(formatLocalDateTimeBaseUsingFormat(APPROVAL_DATE_TIME, DATE_TIME_AT))
            .epoEndDateTime(formatLocalDateTimeBaseUsingFormat(END_DATE_TIME, DATE_TIME_AT))
            .removalAddress(exclusionRequired ? REMOVAL_ADDRESS.getAddressAsString(", ") : EMPTY)
            .exclusionRequirement(exclusionRequired ? exclusionRequirement : null)
            .powerOfArrestFileAttached(powerOfArrestFileAttached)
            .build();
    }

    private ManageOrdersEventData getManageOrdersEventData(EPOType type, Address address, boolean exclusionRequired) {
        return ManageOrdersEventData.builder()
            .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
            .manageOrdersEpoType(type)
            .manageOrdersApprovalDateTime(APPROVAL_DATE_TIME)
            .manageOrdersEndDateTime(END_DATE_TIME)
            .manageOrdersIncludePhrase("Yes")
            .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
            .manageOrdersChildrenDescription(CHILDREN_DESCRIPTION)
            .manageOrdersType(ORDER_TYPE)
            .manageOrdersEpoRemovalAddress(address)
            .manageOrdersExclusionRequirement(exclusionRequired ? "Yes" : "No")
            .manageOrdersWhoIsExcluded(exclusionRequired ? EXCLUDE_PERSON : null)
            .manageOrdersExclusionStartDate(exclusionRequired ? EXCLUSION_DATE : null)
            .manageOrdersPowerOfArrest(type.equals(EPOType.PREVENT_REMOVAL) ? testDocumentReference() : null)
            .build();
    }

}
