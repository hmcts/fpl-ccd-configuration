package uk.gov.hmcts.reform.fpl.service.email.content.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyCaseContent;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

public abstract class CasePersonalisedContentProvider extends AbstractEmailContentProvider {
    protected CasePersonalisedContentProvider(String uiBaseUrl, ObjectMapper mapper) {
        super(uiBaseUrl, mapper);
    }

    protected NotifyCaseContent.NotifyCaseContentBuilder getCasePersonalisationBuilder(Long caseId, CaseData caseData) {
        List<String> ordersAndDirections = buildOrdersAndDirections(caseData.getOrders());

        Optional<String> timeFrame = Optional.ofNullable(caseData.getHearing())
            .map(Hearing::getTimeFrame)
            .filter(StringUtils::isNotBlank);

        return NotifyCaseContent.builder()
            .ordersAndDirections(!ordersAndDirections.isEmpty() ? ordersAndDirections : List.of(""))
            .dataPresent(!ordersAndDirections.isEmpty() ? YES : NO)
            .fullStop(!ordersAndDirections.isEmpty() ? NO : YES)
            .timeFramePresent(timeFrame.isPresent() ? YES : NO)
            .urgentHearing(timeFrame.isPresent() && timeFrame.get().equals("Same day") ? YES : NO)
            .nonUrgentHearing(timeFrame.isPresent() && !timeFrame.get().equals("Same day") ? YES : NO)
            .firstRespondentName(getFirstRespondentLastName(caseData.getRespondents1()))
            .reference(String.valueOf(caseId))
            .caseUrl(formatCaseUrl(uiBaseUrl, caseId));
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
