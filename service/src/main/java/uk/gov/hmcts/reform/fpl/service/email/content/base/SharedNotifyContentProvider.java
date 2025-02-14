package uk.gov.hmcts.reform.fpl.service.email.content.base;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

public abstract class SharedNotifyContentProvider extends AbstractEmailContentProvider {

    @Autowired
    protected EmailNotificationHelper helper;

    protected <T extends SharedNotifyTemplate> T buildNotifyTemplate(T template, CaseData caseData) {
        final Long caseId = caseData.getId();
        List<String> ordersAndDirections = buildOrdersAndDirections(caseData.getOrders());
        Optional<String> timeFrame = Optional.ofNullable(caseData.getHearing())
            .map(hearing -> nonNull(hearing.getHearingUrgencyType())
                ? hearing.getHearingUrgencyType().getLabel() : hearing.getTimeFrame())
            .filter(StringUtils::isNotBlank);

        template.setOrdersAndDirections(ordersAndDirections);
        template.setTimeFramePresent(timeFrame.isPresent() ? YES.getValue() : NO.getValue());
        template.setTimeFrameValue(uncapitalize(timeFrame.orElse("")));
        template.setUrgentHearing(timeFrame.isPresent() && timeFrame.get().equals("Same day")
                                  ? YES.getValue() : NO.getValue()
        );
        template.setNonUrgentHearing(timeFrame.isPresent() && !timeFrame.get().equals("Same day")
                                     ? YES.getValue() : NO.getValue()
        );

        //When hearing is not filled in put make the subject line Application Received - hearing other
        if (NO.getValue().equals(template.getUrgentHearing()) && NO.getValue().equals(template.getNonUrgentHearing())) {
            template.setTimeFrameValue("other");
            template.setTimeFramePresent(YES.getValue());
            template.setNonUrgentHearing(YES.getValue());
        }

        template.setFirstRespondentName(getFirstRespondentLastName(caseData.getRespondents1()));
        template.setReference(String.valueOf(caseId));
        template.setCaseUrl(getCaseUrl(caseId));
        template.setChildLastName(helper.getEldestChildLastName(caseData.getAllChildren()));

        return template;
    }


    protected List<String> buildOrdersAndDirections(Orders optionalOrders) {
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
