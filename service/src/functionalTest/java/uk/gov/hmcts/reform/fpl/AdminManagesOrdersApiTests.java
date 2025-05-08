package uk.gov.hmcts.reform.fpl;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import uk.gov.hmcts.reform.fpl.enums.C29ActionsPermitted;
import uk.gov.hmcts.reform.fpl.enums.EnglandOffices;
import uk.gov.hmcts.reform.fpl.enums.PlacedUnderOrder;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.NUMBER_OF_MONTHS;
import static uk.gov.hmcts.reform.fpl.model.order.Order.valueOf;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

public class AdminManagesOrdersApiTests extends AbstractApiTest {

    public static final String INPUT_FILE = "admin-manage-orders/%s/input-case-details.json";
    public static final String EXPECTED_FILE = "admin-manage-orders/%s/expected.txt";
    private final LocalDate todaysDate = LocalDate.now();
    private final LocalDateTime currentDateTime = LocalDateTime.now();

    @Autowired
    private DocumentService documentService;

    @Test
    public void doNothing() {
        assertTrue(true);
    }

    // @Test
    public void adminManagesOrderTest32a() {
        parametrizedTests("c32a", "C32A_CARE_ORDER");
    }

    // @Test
    public void adminManagesOrderTest32b() {
        parametrizedTests("c32b", "C32B_DISCHARGE_OF_CARE_ORDER");
    }

    // @Test
    public void adminManagesOrderTest23() {
        parametrizedTests("c23", "C23_EMERGENCY_PROTECTION_ORDER");
    }

    // @Test
    public void adminManagesOrderTest33() {
        parametrizedTests("c33", "C33_INTERIM_CARE_ORDER");
    }

    // @Test
    public void adminManagesOrderTest35a() {
        parametrizedTests("c35a", "C35A_SUPERVISION_ORDER");
    }

    // @Test
    public void adminManagesOrderTest35b() {
        parametrizedTests("c35b", "C35B_INTERIM_SUPERVISION_ORDER");
    }

    // @Test
    public void adminManagesOrderTest43a() {
        parametrizedTests("c43a", "C43A_SPECIAL_GUARDIANSHIP_ORDER");
    }

    // @Test
    public void adminManagesOrderTest29() {
        parametrizedTests("c29","C29_RECOVERY_OF_A_CHILD");
    }

    // @Test
    public void adminManagesOrderTest47a() {
        parametrizedTests("c47a", "C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN");
    }

    public void parametrizedTests(String inputFileDirectory, String orderType) {
        CaseData startingCaseData = createCase(format(INPUT_FILE, inputFileDirectory), LA_SWANSEA_USER_1);
        CaseData caseData = callAboutToSubmit(startingCaseData, orderType, format(EXPECTED_FILE, inputFileDirectory));
        assertEquals(orderType, getGeneratedOrderType(caseData));
    }

    private CaseData callAboutToSubmit(CaseData caseData, String orderType, String outputFilePath) {
        CaseData updatedCase = caseData.toBuilder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(valueOf(orderType))
                .manageOrdersTitle("Order title")
                .manageOrdersDirections("Order details")
                .manageOrdersApprovalDate(todaysDate)
                .manageOrdersCareOrderIssuedDate(todaysDate) // c32b
                .manageOrdersEndDateTime(currentDateTime) // c23
                .manageOrdersApprovalDateTime(currentDateTime) // c23
                .manageOrdersExclusionStartDate(todaysDate) // c23
                .manageOrdersOrderCreatedDate(todaysDate) // c29
                .manageOrdersSetDateEndDate(todaysDate) //c33
                .manageOrdersEndDateTypeWithEndOfProceedings(END_OF_PROCEEDINGS) //c33
                .manageOrdersEndDateTypeWithMonth(NUMBER_OF_MONTHS) //c35a
                .manageOrdersSetMonthsEndDate(12)  //c35a
                .manageOrdersCafcassOfficesEngland(EnglandOffices.BOURNEMOUTH)  //c47a
                .manageOrdersCafcassRegion("ENGLAND")
                .manageOrdersPlacedUnderOrder(PlacedUnderOrder.CARE_ORDER)
                .manageOrdersActionsPermitted(List.of(C29ActionsPermitted.ENTRY))
                .build())
            .build();

        CallbackResponse response = callback(updatedCase, COURT_ADMIN, "manage-orders/about-to-submit");

        GeneratedOrder order = unwrapElements(response.getCaseData().getOrderCollection()).get(0);

        String actualOrderContent = documentService.getPdfContent(order.getDocument(), COURT_ADMIN);

        String ordinalSuffix = getDayOfMonthSuffix(todaysDate.getDayOfMonth());

        String expectedOrderContent = readString(outputFilePath,
            Map.of(
                "id", formatCCDCaseNumber(caseData.getId()),
                "issueDate", formatLocalDateToString(todaysDate, DATE),
                "dateSuffix", formatLocalDateBaseUsingFormat(todaysDate,
                    format(DATE_WITH_ORDINAL_SUFFIX, ordinalSuffix)),
                "oneYearLaterTodaySuffix", formatLocalDateBaseUsingFormat(todaysDate.plusYears(1),
                    format(DATE_WITH_ORDINAL_SUFFIX, ordinalSuffix)),
                "dateTimeAt", formatLocalDateTimeBaseUsingFormat(currentDateTime, DATE_TIME_AT),
                "dateTimeComma", formatLocalDateTimeBaseUsingFormat(currentDateTime, DATE_TIME)
            ));

        assertThat(actualOrderContent).isEqualToNormalizingWhitespace(expectedOrderContent);

        return response.getCaseData();
    }

    @NotNull
    @Valid
    private String getGeneratedOrderType(CaseData caseData) {
        return caseData.getOrderCollection().get(0).getValue().getOrderType();
    }
}

