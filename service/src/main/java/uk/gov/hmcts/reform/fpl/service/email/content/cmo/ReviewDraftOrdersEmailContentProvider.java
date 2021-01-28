package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ApprovedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
public class ReviewDraftOrdersEmailContentProvider extends AbstractEmailContentProvider {

    //TODO multiple document links
    public ApprovedOrdersTemplate buildOrdersApprovedContent(CaseData caseData,
                                                             HearingBooking hearing,
                                                             List<HearingOrder> orders,
                                                             RepresentativeServingPreferences servingPreference) {
        return ApprovedOrdersTemplate.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .subjectLineWithHearingDate(subject(hearing, caseData.getAllRespondents(),
                caseData.getFamilyManCaseNumber()))
            .orderList(formatOrders(orders))
            .documentLinks(List.of(new JSONObject()))
            .digitalPreference(hasDigitalServingPreference(servingPreference) ? "Yes" : "No")
            .caseUrl((hasDigitalServingPreference(servingPreference) ? getCaseUrl(caseData.getId(), ORDERS) : ""))
            .build();
    }

    public RejectedOrdersTemplate buildOrdersRejectedContent(CaseData caseData, HearingBooking hearing,
                                                             List<HearingOrder> hearingOrders) {
        return RejectedOrdersTemplate.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .subjectLineWithHearingDate(subject(hearing, caseData.getAllRespondents(),
                caseData.getFamilyManCaseNumber()))
            .caseUrl(getCaseUrl(caseData.getId(), ORDERS))
            .ordersAndRequestedChanges(ordersAndRequestedChanges(hearingOrders))
            .build();
    }

    private boolean hasDigitalServingPreference(RepresentativeServingPreferences servingPreference) {
        return servingPreference == DIGITAL_SERVICE;
    }

    private String subject(HearingBooking hearing, List<Element<Respondent>> respondents, String familyManCaseNumber) {
        String subject = buildSubjectLine(familyManCaseNumber, respondents);
        if (hearing == null) {
            return subject;
        }
        return String.format("%s, %s", subject, uncapitalize(hearing.toLabel()));
    }

    private List<String> ordersAndRequestedChanges(List<HearingOrder> hearingOrders) {
        List<String> ordersAndRequestedChanges = new ArrayList<>();
        for (HearingOrder order : hearingOrders) {
            ordersAndRequestedChanges.add(format("%s - %s", order.getTitle(), order.getRequestedChanges()));
        }
        return ordersAndRequestedChanges;
    }

    private String formatOrders(List<HearingOrder> orders) {
        return orders.stream()
            .map(HearingOrder::getTitle)
            .collect(joining(lineSeparator()));
    }
}
