package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ChaseMissingCMOsTemplate;
import uk.gov.hmcts.reform.fpl.service.cmo.SendOrderReminderService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ChaseMissingCMOEmailContentProvider extends AbstractEmailContentProvider {
    private final SendOrderReminderService sendOrderReminderService;

    public ChaseMissingCMOsTemplate buildTemplate(CaseData caseData) {
        String message = sendOrderReminderService.getPastHearingBookingsWithoutCMOs(caseData).stream()
            .map(HearingBooking::toLabel)
            .collect(joining(lineSeparator()));

        return ChaseMissingCMOsTemplate.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .caseUrl(getCaseUrl(caseData.getId()))
            .subjectLine(String.format("%s, %s, %s", caseData.getCaseName(), caseData.getId(),
                caseData.getFamilyManCaseNumber()))
            .listOfHearingsMissingOrders(message)
            .build();
    }
}
