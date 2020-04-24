package uk.gov.hmcts.reform.fpl.service.email.content.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang.StringUtils.uncapitalize;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

public abstract class NotifyTemplateContentProvider extends AbstractEmailContentProvider {
    protected NotifyTemplateContentProvider(String uiBaseUrl, ObjectMapper mapper) {
        super(uiBaseUrl, mapper);
    }

    protected <T extends SharedNotifyTemplate> T buildNotifyTemplate(T template,
                                                                     Long caseId,
                                                                     Orders orders,
                                                                     Hearing hearing,
                                                                     List<Element<Respondent>> respondents1) {
        List<String> ordersAndDirections = buildOrdersAndDirections(orders);

        Optional<String> timeFrame = Optional.ofNullable(hearing)
            .map(Hearing::getTimeFrame)
            .filter(StringUtils::isNotBlank);

        // What if we just add the list no matter if it is empty or not,
        // surely the parts below determine if it is going to be shown or not
        template.setOrdersAndDirections(!ordersAndDirections.isEmpty() ? ordersAndDirections : List.of(""));
        template.setDataPresent(!ordersAndDirections.isEmpty() ? YES.getValue() : NO.getValue());
        template.setFullStop(!ordersAndDirections.isEmpty() ? NO.getValue() : YES.getValue());
        template.setTimeFramePresent(timeFrame.isPresent() ? YES.getValue() : NO.getValue());
        template.setTimeFrameValue(uncapitalize(timeFrame.orElse("")));
        template.setUrgentHearing(
            timeFrame.isPresent() && timeFrame.get().equals("Same day") ? YES.getValue() : NO.getValue());
        template.setNonUrgentHearing(
            timeFrame.isPresent() && !timeFrame.get().equals("Same day") ? YES.getValue() : NO.getValue());
        template.setFirstRespondentName(getFirstRespondentLastName(respondents1));
        template.setReference(String.valueOf(caseId));
        template.setCaseUrl(formatCaseUrl(uiBaseUrl, caseId));

        return template;
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
