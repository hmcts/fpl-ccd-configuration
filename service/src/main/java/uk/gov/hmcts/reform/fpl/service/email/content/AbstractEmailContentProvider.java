package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.getFirstRespondentLastName;

public abstract class AbstractEmailContentProvider {

    final String uiBaseUrl;
    private final DateFormatterService dateFormatterService;
    private final HearingBookingService hearingBookingService;

    protected AbstractEmailContentProvider(String uiBaseUrl,
                                           DateFormatterService dateFormatterService,
                                           HearingBookingService hearingBookingService) {
        this.uiBaseUrl = uiBaseUrl;
        this.dateFormatterService = dateFormatterService;
        this.hearingBookingService = hearingBookingService;
    }

    @SuppressWarnings("unchecked")
    ImmutableMap.Builder<String, Object> getCasePersonalisationBuilder(CaseDetails caseDetails, CaseData caseData) {
        List<String> ordersAndDirections = buildOrdersAndDirections(
            (Map<String, Object>) caseDetails.getData().get("orders"));

        Optional<String> timeFrame = Optional.ofNullable((Map<String, Object>) caseDetails.getData().get("hearing"))
            .map(hearing -> (String) hearing.get("timeFrame"));

        return ImmutableMap.<String, Object>builder()
            .put("ordersAndDirections", !ordersAndDirections.isEmpty() ? ordersAndDirections : "")
            .put("dataPresent", !ordersAndDirections.isEmpty() ? "Yes" : "No")
            .put("fullStop", !ordersAndDirections.isEmpty() ? "No" : "Yes")
            .put("timeFramePresent", timeFrame.isPresent() ? "Yes" : "No")
            .put("timeFrameValue", uncapitalize(timeFrame.orElse("")))
            .put("urgentHearing", timeFrame.isPresent() && timeFrame.get().equals("Same day") ? "Yes" : "No")
            .put("nonUrgentHearing", timeFrame.isPresent() && !timeFrame.get().equals("Same day") ? "Yes" : "No")
            .put("firstRespondentName", getFirstRespondentLastName(caseData))
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId());
    }

    ImmutableMap.Builder<String, Object> getSDOPersonalisationBuilder(CaseDetails caseDetails, CaseData caseData) {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber",
                isNull(caseData.getFamilyManCaseNumber()) ? "" : caseData.getFamilyManCaseNumber() + ",")
            .put("leadRespondentsName", capitalize(caseData.getRespondents1()
                .get(0)
                .getValue()
                .getParty()
                .getLastName()) + ",")
            .put("hearingDate", getHearingBooking(caseData))
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId());
    }

    private String getHearingBooking(CaseData data) {
        if (!isNull(data.getHearingDetails())) {
            return dateFormatterService.formatLocalDateToString(
                hearingBookingService.getMostUrgentHearingBooking(
                    data.getHearingDetails()).getStartDate().toLocalDate(), FormatStyle.LONG);
        }
        return "";
    }

    private List<String> buildOrdersAndDirections(Map<String, Object> optionalOrders) {
        ImmutableList.Builder<String> ordersAndDirectionsBuilder = ImmutableList.builder();

        Optional.ofNullable(optionalOrders).ifPresent(orders -> {
            appendOrders(orders, ordersAndDirectionsBuilder);
            appendDirections(orders, ordersAndDirectionsBuilder);
        });

        return ordersAndDirectionsBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private void appendOrders(Map<String, Object> orders, ImmutableList.Builder<String> builder) {
        Optional.ofNullable(orders.get("orderType")).ifPresent(orderTypes -> {
            for (String typeString : (List<String>) orderTypes) {
                builder.add(OrderType.valueOf(typeString).getLabel());
            }
        });

        Optional.ofNullable(orders.get("emergencyProtectionOrders")).ifPresent(emergencyProtectionOrders -> {
            for (String typeString : (List<String>) emergencyProtectionOrders) {
                builder.add(EmergencyProtectionOrdersType.valueOf(typeString).getLabel());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void appendDirections(Map<String, Object> orders, ImmutableList.Builder<String> builder) {
        Optional.ofNullable(orders.get("emergencyProtectionOrderDirections")).ifPresent(
            emergencyProtectionOrderDirections -> {
                for (String typeString : (List<String>) emergencyProtectionOrderDirections) {
                    builder.add(EmergencyProtectionOrderDirectionsType.valueOf(typeString).getLabel());
                }
            });
    }
}
