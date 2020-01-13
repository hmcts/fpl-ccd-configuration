package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

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
    ImmutableMap.Builder<String, Object> getCasePersonalisationBuilder(Long caseDetailsId, CaseData caseData) {
        List<String> ordersAndDirections = buildOrdersAndDirections(caseData.getOrders());
        String timeFrame = "";
        if (caseData.getHearing() != null) {
            timeFrame = caseData.getHearing().getTimeFrame();
        }

        return ImmutableMap.<String, Object>builder()
            .put("ordersAndDirections", !ordersAndDirections.isEmpty() ? ordersAndDirections : "")
            .put("dataPresent", !ordersAndDirections.isEmpty() ? "Yes" : "No")
            .put("fullStop", !ordersAndDirections.isEmpty() ? "No" : "Yes")
            .put("timeFramePresent", isNotBlank(timeFrame) ? "Yes" : "No")
            .put("timeFrameValue", uncapitalize(defaultIfBlank(timeFrame, "")))
            .put("urgentHearing", isNotBlank(timeFrame) && timeFrame.equals("Same day") ? "Yes" : "No")
            .put("nonUrgentHearing", isNotBlank(timeFrame) && !timeFrame.equals("Same day") ? "Yes" : "No")
            .put("firstRespondentName", getFirstRespondentLastName(caseData.getRespondents1()))
            .put("reference", String.valueOf(caseDetailsId))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetailsId);
    }

    ImmutableMap.Builder<String, Object> getSDOPersonalisationBuilder(Long caseDetailsId, CaseData caseData) {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber",
                isNull(caseData.getFamilyManCaseNumber()) ? "" : caseData.getFamilyManCaseNumber() + ",")
            .put("leadRespondentsName", capitalize(caseData.getRespondents1()
                .get(0)
                .getValue()
                .getParty()
                .getLastName()) + ",")
            .put("hearingDate", getHearingBooking(caseData))
            .put("reference", String.valueOf(caseDetailsId))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetailsId);
    }

    private String getHearingBooking(CaseData data) {
        if (!isNull(data.getHearingDetails())) {
            return dateFormatterService.formatLocalDateToString(
                hearingBookingService.getMostUrgentHearingBooking(
                    data.getHearingDetails()).getStartDate().toLocalDate(), FormatStyle.LONG);
        }
        return "";
    }

    private List<String> buildOrdersAndDirections(Orders orders) {
        ImmutableList.Builder<String> ordersAndDirectionsBuilder = ImmutableList.builder();

        Optional.ofNullable(orders).ifPresent(ords -> {
            appendOrders(ords, ordersAndDirectionsBuilder);
            appendDirections(ords, ordersAndDirectionsBuilder);
        });

        return ordersAndDirectionsBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private void appendOrders(Orders orders, ImmutableList.Builder<String> builder) {
        Optional.ofNullable(orders.getOrderType()).ifPresent(orderTypes -> {
            for (OrderType type : orderTypes) {
                builder.add(type.getLabel());
            }
        });

        Optional.ofNullable(orders.getEmergencyProtectionOrders()).ifPresent(emergencyProtectionOrders -> {
            for (EmergencyProtectionOrdersType epoType : emergencyProtectionOrders) {
                builder.add(epoType.getLabel());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void appendDirections(Orders orders, ImmutableList.Builder<String> builder) {
        Optional.ofNullable(orders.getEmergencyProtectionOrderDirections()).ifPresent(
            emergencyProtectionOrderDirections -> {
                for (EmergencyProtectionOrderDirectionsType epoDirectionsType : emergencyProtectionOrderDirections) {
                    builder.add(epoDirectionsType.getLabel());
                }
            });
    }
}
