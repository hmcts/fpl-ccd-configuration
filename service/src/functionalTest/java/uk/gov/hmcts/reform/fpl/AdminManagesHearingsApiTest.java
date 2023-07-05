package uk.gov.hmcts.reform.fpl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.enums.C29ActionsPermitted;
import uk.gov.hmcts.reform.fpl.enums.EnglandOffices;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.PlacedUnderOrder;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
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

public class AdminManagesHearingsApiTest extends AbstractApiTest {

    public static final String INPUT_FILE = "admin-manage-hearings/input-case-details.json";
    // public static final String EXPECTED_FILE = "admin-manage-hearings/%s/expected.txt";
    private final LocalDate todaysDate = LocalDate.now();
    private final LocalDateTime currentDateTime = LocalDateTime.now();
    private CaseData startingCaseData;

    @Autowired
    private DocumentService documentService;

    @Test
    public void adminCreatesFirstHearing() {
        parametrizedTests("c32a");
    }

    public void parametrizedTests(String inputFileDirectory) {
        startingCaseData = createCase(INPUT_FILE, LA_SWANSEA_USER_1);
        callAboutToSubmit(startingCaseData);
    }

    private void callAboutToSubmit(CaseData caseData) {
        CaseData updatedCase = caseData.toBuilder()
            .hearingOption(HearingOptions.NEW_HEARING)
            .hearingType(HearingType.CASE_MANAGEMENT)
            .build();

        CallbackResponse response = callback(updatedCase, COURT_ADMIN, "manage-hearings/about-to-submit");

        assertThat(response.getCaseData().getHearingDetails()).doesNotContainNull();

        var test = response.getCaseData();
    }
}
