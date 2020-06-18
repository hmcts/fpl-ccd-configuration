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

import static org.apache.commons.lang.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.FplEvent.SUBMIT_APPLICATION;


@Service
public class CaseStateService {

    private static final String LINE_SEPARATOR = "\n\n";
    private static final String HORIZONTAL_RULE = "\n___";

    @Autowired
    AllEvents eventsService;

    @Autowired
    CaseValidatorService validationService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EventGuardProvider eventGuardProvider;

    String getStatusForLA(CaseDetails caseDetails) {

        List<String> messages = new ArrayList<>();

        String state = caseDetails.getState();

        messages.add(getCaseProgressMessage(state));

        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseDetails.getState().equals("Open")) {

            Set<Section> sectionWithErrors = validationService.validateCaseDetails2(caseData);
            List<String> validationErrors = validationService.validateCaseDetails3(caseData);

            messages.add("## Your application");
            messages.add("<div class='width-50'>");

            Map<Section, Boolean> completedBySection = new TreeMap<>();
            Stream.of(Section.values()).forEach(v -> completedBySection.put(v, true));

            sectionWithErrors.forEach(f -> completedBySection.put(f, false));

            completedBySection.forEach((k, v) -> {
                messages.add(buildLink(caseDetails.getId(), k.getEvent(), v));
                messages.add(HORIZONTAL_RULE);
            });

            messages.add("</div>");

            if (sectionWithErrors.isEmpty()) {
                messages.add("## Send your application");
                messages.add(buildLink(caseDetails.getId(), SUBMIT_APPLICATION, caseData, caseDetails));
            } else {
                messages.add("## Errors in application");
                messages.addAll(validationErrors);
            }
        }

        return String.join(LINE_SEPARATOR, messages);
    }

    String getStatusForAdmin(CaseDetails caseDetails) {

        List<String> messages = new ArrayList<>();

        String state = caseDetails.getState();

        messages.add(getCaseProgressMessage(state));

        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        Long caseId = caseDetails.getId();

        List<FplEvent> mandatoryEvents = eventsService.getMandatoryEvents(State.SUBMITTED, Roles.ADMIN);
        List<FplEvent> optionalEvents = eventsService.getOptionalEvents(State.SUBMITTED, Roles.ADMIN);

        if (!mandatoryEvents.isEmpty()) {
            messages.add("## To progress the case");
            messages.add("<div class='width-50'>");
            mandatoryEvents.forEach(event -> {
                messages.add(buildLink(caseId, event, caseData, caseDetails));
                messages.add(HORIZONTAL_RULE);
            });
            messages.add("</div>");
        }

        if (!optionalEvents.isEmpty()) {
            messages.add("## Other things you can do");
            messages.add("<div class='width-50'>");
            optionalEvents.forEach(event -> {
                messages.add(buildLink(caseId, event, caseData, caseDetails));
                messages.add(HORIZONTAL_RULE);
            });
            messages.add("</div>");
        }


        return String.join(LINE_SEPARATOR, messages);
    }

    private String buildLinkWithImage(Long caseId, FplEvent event, String image, String imageTitle) {
        return isEmpty(image) ? eventLink(caseId, event) : eventLink(caseId, event) + image(getImageUrl(image), imageTitle);
    }


    private String image(String image, String title) {
        return String.format("<img align='right' src='%s' title='%s'>", image, title);
    }

    private String eventLink(Long caseId, FplEvent event) {
        return String.format("[%s](/case/%s/%s/%s/trigger/%s)", event.getName(), JURISDICTION, CASE_TYPE, caseId, event.getId());
    }

    private String buildLink(Long caseId, FplEvent event, boolean completed) {
        if (completed) {
            return buildLinkWithImage(caseId, event, "completed.png", "Completed");
        } else {
            return buildLinkWithImage(caseId, event, null, null);
        }
    }

    private String buildLink(Long caseId, FplEvent event, CaseData caseData, CaseDetails caseDetails) {
        if (event.getCompletedPredicate().test(caseData)) {
            return buildLinkWithImage(caseId, event, "completed.png", "Completed");
        } else {
            List<String> errors = eventGuardProvider.getEventGuard(event).validate(caseDetails);
            if (errors.isEmpty()) {
                return buildLinkWithImage(caseId, event, null, null);
            } else {
                return buildLinkWithImage(caseId, event, "unavailable.png", String.join("\n", errors));
            }
        }
    }

    private String getImageUrl(String image) {
        return "https://raw.githubusercontent.com/tomaszpowroznik/proxypac/master/" + image;
    }

    private String getCaseProgressMessage(String state) {
        var image = state.equals("Open") ? "open.png" : "submitted.png";
        return String.format("## Case progress\n ![Submitted](%s)", getImageUrl(image));
    }
}
