package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.interfaces.SelectableItem;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.String.join;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

public abstract class MessageJudgeService {
    @Autowired
    protected Time time;

    protected String getNextHearingLabel(CaseData caseData) {
        return caseData.getNextHearingAfter(time.now())
            .map(hearing -> String.format("Next hearing in the case: %s", hearing.toLabel()))
            .orElse("");
    }

    public List<Element<JudicialMessage>> sortJudicialMessages(List<Element<JudicialMessage>> judicialMessages) {
        judicialMessages.sort(Comparator.comparing(judicialMessageElement
            -> judicialMessageElement.getValue().getUpdatedTime(), Comparator.reverseOrder()));

        return judicialMessages;
    }

    protected String buildMessageHistory(String message, String history, String sender) {
        String formattedLatestMessage = String.format("%s - %s", sender, message);

        if (history.isBlank()) {
            return formattedLatestMessage;
        }

        return join("\n \n", history, formattedLatestMessage);
    }

    protected List<Element<SelectableItem>> getApplications(CaseData caseData) {

        final List<Element<SelectableItem>> applications = new ArrayList<>();

        ofNullable(caseData.getC2DocumentBundle())
            .ifPresent(c2s -> c2s
                .forEach(application -> applications.add(element(application.getId(), application.getValue()))));

        unwrapElements(caseData.getAdditionalApplicationsBundle()).forEach(bundle -> {
            ofNullable(bundle.getC2DocumentBundle())
                .ifPresent(application -> applications.add(element(application.getId(), application)));
            ofNullable(bundle.getOtherApplicationsBundle())
                .ifPresent(application -> applications.add(element(application.getId(), application)));
        });

        ofNullable(caseData.getPlacementEventData())
            .map(PlacementEventData::getPlacements)
            .ifPresent(placement -> placement
                .forEach(application -> applications.add(element(application.getId(), application.getValue()))));

        return applications;
    }
}
