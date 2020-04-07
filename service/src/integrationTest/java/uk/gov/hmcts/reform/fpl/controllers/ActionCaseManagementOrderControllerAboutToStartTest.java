package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCaseManagementOrder;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingsFromInitialDate;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCaseManagementOrderController.class)
@OverrideAutoConfiguration(enabled = true)
public class ActionCaseManagementOrderControllerAboutToStartTest extends AbstractControllerTest {

    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDate(
        FormatStyle.MEDIUM).localizedBy(Locale.UK);

    @Autowired
    private Time time;

    ActionCaseManagementOrderControllerAboutToStartTest() {
        super("action-cmo");
    }

    @Test
    void shouldAddCurrentTimeAsDateOfIssuedWhenNotInCaseManagementOrder() {
        Map<String, Object> data = Map.of(HEARING_DETAILS_KEY, createHearingBookingsFromInitialDate(time.now()),
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), createCaseManagementOrder(SEND_TO_JUDGE));

        CaseDetails caseDetails = buildCaseDetails(data);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getData()).containsEntry("dateOfIssue", time.now().toLocalDate().toString());
    }

    @Test
    void shouldAddPreviousTimeAsDateOfIssuedWhenInCaseManagementOrder() {
        final CaseManagementOrder order = createCaseManagementOrder(SEND_TO_JUDGE).toBuilder()
            .dateOfIssue("20 March 2019")
            .build();

        Map<String, Object> data = Map.of(HEARING_DETAILS_KEY, createHearingBookingsFromInitialDate(time.now()),
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order);

        CaseDetails caseDetails = buildCaseDetails(data);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getData()).containsEntry("dateOfIssue", LocalDate.of(2019, 3, 20).toString());
    }

    @Test
    void shouldExtractIndividualCaseManagementOrderFieldsWithFutureHearingDates() {
        final CaseManagementOrder order = createCaseManagementOrder(SEND_TO_JUDGE);

        Map<String, Object> data = Map.of(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order,
            HEARING_DETAILS_KEY, createHearingBookingsFromInitialDate(LocalDateTime.now()));

        CaseDetails caseDetails = buildCaseDetails(data);
        List<String> expected = List.of(
            NOW.plusDays(5).format(DATE_TIME_FORMATTER),
            NOW.plusDays(2).format(DATE_TIME_FORMATTER));

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(getHearingDates(response)).isEqualTo(expected);
        assertThat(getHearingDates(response)).doesNotContain(NOW.format(DATE_TIME_FORMATTER));
        assertThat(caseData.getOrderAction()).isNull();
        assertThat(caseData.getSchedule()).isEqualTo(order.getSchedule());
        assertThat(caseData.getRecitals()).isEqualTo(order.getRecitals());
    }

    @Test
    void shouldNotProgressOrderWhenOrderActionIsNotSet() {
        CaseDetails caseDetails = createCaseDetailsWithEmptyCMO();
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getCaseManagementOrder()).isEqualTo(CaseManagementOrder.builder().build());
    }

    private CaseDetails buildCaseDetails(Map<String, Object> data) {
        return CaseDetails.builder()
            .id(12345L)
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .data(data)
            .build();
    }

    private List<String> getHearingDates(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        return caseData.getNextHearingDateList().getListItems().stream()
            .map(element -> mapper.convertValue(element, DynamicListElement.class))
            .map(DynamicListElement::getLabel)
            .collect(toList());
    }

    private CaseDetails createCaseDetailsWithEmptyCMO() {
        Map<String, Object> data = new HashMap<>();
        final CaseManagementOrder order = CaseManagementOrder.builder().build();

        data.put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order);
        return buildCaseDetails(data);
    }
}
