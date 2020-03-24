package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

public abstract class AbstractEmailContentProvider {

    final String uiBaseUrl;
    private final HearingBookingService hearingBookingService;

    protected AbstractEmailContentProvider(String uiBaseUrl, HearingBookingService hearingBookingService) {
        this.uiBaseUrl = uiBaseUrl;
        this.hearingBookingService = hearingBookingService;
    }

    ImmutableMap.Builder<String, Object> getCasePersonalisationBuilder(Long caseId, CaseData caseData) {
        List<String> ordersAndDirections = buildOrdersAndDirections(caseData.getOrders());

        Optional<String> timeFrame = Optional.ofNullable(caseData.getHearing())
            .map(Hearing::getTimeFrame)
            .filter(StringUtils::isNotBlank);

        return ImmutableMap.<String, Object>builder()
            .put("ordersAndDirections", !ordersAndDirections.isEmpty() ? ordersAndDirections : "")
            .put("dataPresent", !ordersAndDirections.isEmpty() ? "Yes" : "No")
            .put("fullStop", !ordersAndDirections.isEmpty() ? "No" : "Yes")
            .put("timeFramePresent", timeFrame.isPresent() ? "Yes" : "No")
            .put("timeFrameValue", uncapitalize(timeFrame.orElse("")))
            .put("urgentHearing", timeFrame.isPresent() && timeFrame.get().equals("Same day") ? "Yes" : "No")
            .put("nonUrgentHearing", timeFrame.isPresent() && !timeFrame.get().equals("Same day") ? "Yes" : "No")
            .put("firstRespondentName", getFirstRespondentLastName(caseData.getRespondents1()))
            .put("reference", String.valueOf(caseId))
            .put("caseUrl", formatCaseUrl(uiBaseUrl, caseId));
    }

    ImmutableMap.Builder<String, Object> getSDOPersonalisationBuilder(Long caseId, CaseData caseData) {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber",
                isNull(caseData.getFamilyManCaseNumber()) ? "" : caseData.getFamilyManCaseNumber() + ",")
            .put("leadRespondentsName", capitalize(caseData.getRespondents1()
                .get(0)
                .getValue()
                .getParty()
                .getLastName()) + ",")
            .put("hearingDate", getHearingBooking(caseData))
            .put("reference", String.valueOf(caseId))
            .put("caseUrl", formatCaseUrl(uiBaseUrl, caseId));
    }

    private String getHearingBooking(CaseData data) {
        if (!isNull(data.getHearingDetails())) {
            return formatLocalDateToString(hearingBookingService.getMostUrgentHearingBooking(
                    data.getHearingDetails()).getStartDate().toLocalDate(), FormatStyle.LONG);
        }
        return "";
    }

    private List<String> buildOrdersAndDirections(Orders optionalOrders) {
        ImmutableList.Builder<String> ordersAndDirectionsBuilder = ImmutableList.builder();

        Optional.ofNullable(optionalOrders).ifPresent(orders -> {
            appendOrders(orders, ordersAndDirectionsBuilder);
            appendDirections(orders, ordersAndDirectionsBuilder);
        });

        return ordersAndDirectionsBuilder.build();
    }

    private void appendOrders(Orders orders, ImmutableList.Builder<String> builder) {
        defaultIfNull(orders.getOrderType(), Collections.<OrderType>emptyList()).stream()
            .map(OrderType::getLabel)
            .forEach(builder::add);

        defaultIfNull(orders.getEmergencyProtectionOrders(), Collections.<EmergencyProtectionOrdersType>emptyList())
            .stream()
            .map(EmergencyProtectionOrdersType::getLabel)
            .forEach(builder::add);
    }

    private void appendDirections(Orders orders, ImmutableList.Builder<String> builder) {
        defaultIfNull(orders.getEmergencyProtectionOrderDirections(),
            Collections.<EmergencyProtectionOrderDirectionsType>emptyList()).stream()
            .map(EmergencyProtectionOrderDirectionsType::getLabel)
            .forEach(builder::add);
    }
}
