package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.Section;
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
    CaseValidatorService validationService;

    @Autowired
    ObjectMapper objectMapper;

    public String getStatus(CaseDetails caseDetails) {

        List<String> messages = new ArrayList<>();

        String state = caseDetails.getState();

        messages.add(getCaseProgressMessage(state));

        if (caseDetails.getState().equals("Open")) {
            CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

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

    private String buildLink(Long caseId, String event, String label, boolean completed) {
        if (completed) {
            return String.format("[%s](/case/%s/%s/%s/trigger/%s)<img align='right' src='%s'>\r\n___", label, JURISDICTION, CASE_TYPE, caseId, event, getImageUrl("completed.png"));
        } else {
            return String.format("[%s](/case/%s/%s/%s/trigger/%s)\r\n___", label, JURISDICTION, CASE_TYPE, caseId, event);
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
