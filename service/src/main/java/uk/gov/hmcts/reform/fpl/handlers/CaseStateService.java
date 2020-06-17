package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.AllEvents;
import uk.gov.hmcts.reform.fpl.FplEvent;
import uk.gov.hmcts.reform.fpl.controllers.guards.EventGuardProvider;
import uk.gov.hmcts.reform.fpl.enums.Roles;
import uk.gov.hmcts.reform.fpl.enums.Section;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseValidatorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;


@Service
public class CaseStateService {

    @Autowired
    AllEvents eventsService;

    @Autowired
    CaseValidatorService validationService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EventGuardProvider eventGuardProvider;

    public String getStatusForLA(CaseDetails caseDetails) {

        List<String> messages = new ArrayList<>();

        String state = caseDetails.getState();

        messages.add(getCaseProgressMessage(state));

        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        Long caseId = caseDetails.getId();

        if (caseDetails.getState().equals("Open")) {

            Set<Section> sectionWithErrors = validationService.validateCaseDetails2(caseData);
            List<String> validationErrors = validationService.validateCaseDetails3(caseData);

            messages.add("## Your application\r\n\r\n<div class='width-50'>");

            Map<Section, Boolean> completedBySection = new TreeMap<>();
            Stream.of(Section.values()).forEach(v -> completedBySection.put(v, true));

            sectionWithErrors.forEach(f -> completedBySection.put(f, false));

            completedBySection.forEach((k, v) -> {
                messages.add(buildLink(caseDetails.getId(), k.getEvent(), k.getLabel(), v));
            });

            messages.add("</div>\r\n\r\n");

            if (sectionWithErrors.isEmpty()) {
                messages.add(String.format("## Send your application\r\n\r\n %s", buildLink(caseDetails.getId(), "submitApplication", "Send your application", false)));
            } else {
                messages.add("## Errors in application\r\n\r\n " + String.join("\r\n\r\n", validationErrors));
            }
        }

        return String.join("\r\n\r\n", messages);
    }

    public String getStatusForAdmin(CaseDetails caseDetails) {

        List<String> messages = new ArrayList<>();

        String state = caseDetails.getState();

        messages.add(getCaseProgressMessage(state));

        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        Long caseId = caseDetails.getId();

        List<FplEvent> mandatoryEvents = eventsService.getMandatoryEvents(State.SUBMITTED, Roles.ADMIN);
        List<FplEvent> optionalEvents = eventsService.getOptionalEvents(State.SUBMITTED, Roles.ADMIN);

        if (!mandatoryEvents.isEmpty()) {
            messages.add("## To progress the case\r\n\r\n<div class='width-50'>");
            mandatoryEvents.forEach(event -> messages.add(buildLink(caseId, event, caseData, caseDetails)));
            messages.add("</div>\r\n\r\n");
        }

        if (!optionalEvents.isEmpty()) {
            messages.add("## Other things you can do now\r\n\r\n<div class='width-50'>");
            optionalEvents.forEach(event -> messages.add(buildLink(caseId, event, caseData, caseDetails)));
            messages.add("</div>\r\n\r\n");
        }


        return String.join("\r\n\r\n", messages);
    }

    private String buildLink(Long caseId, String event, String label, boolean completed) {
        if (completed) {
            return String.format("[%s](/case/%s/%s/%s/trigger/%s)<img align='right' src='%s'>\r\n___", label, JURISDICTION, CASE_TYPE, caseId, event, getImageUrl("completed.png"));
        } else {
            return String.format("[%s](/case/%s/%s/%s/trigger/%s)\r\n___", label, JURISDICTION, CASE_TYPE, caseId, event);
        }
    }

    private String buildLink(Long caseId, FplEvent event, CaseData caseData, CaseDetails caseDetails) {
        if (event.getCompletedPredicate().test(caseData)) {
            return String.format("[%s](/case/%s/%s/%s/trigger/%s)<img align='right' src='%s'>\r\n___", event.getName(), JURISDICTION, CASE_TYPE, caseId, event.getId(), getImageUrl("completed.png"));
        } else {
            List<String> errors = eventGuardProvider.getEventGuard(event).validate(caseDetails);
            if (errors.isEmpty()) {
                return String.format("[%s](/case/%s/%s/%s/trigger/%s)\r\n___", event.getName(), JURISDICTION, CASE_TYPE, caseId, event.getId());
            } else {
                return String.format("%s<img align='right' src='%s' title='%s'>\r\n___", event.getName(), getImageUrl("unavailable.png"), String.join("\n", errors));
            }
        }
    }

    private String getImageUrl(String image) {
        return "https://raw.githubusercontent.com/tomaszpowroznik/proxypac/master/" + image;
    }

    private String getCaseProgressMessage(String state) {
        var image = state.equals("Open") ? "open.png" : "submitted.png";
        return String.format("## Case progress\r\n ![Submitted](%s)", getImageUrl(image));
    }
}
