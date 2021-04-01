package uk.gov.hmcts.reform.fpl.service.tasklist;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Component
public class TaskListRenderElements {

    public TaskListRenderElements(@Value("${resources.images.baseUrl}") String imagesBaseUrl) {
        this.imagesBaseUrl = imagesBaseUrl;
    }

    private final String imagesBaseUrl;

    public String renderLink(Task event) {
        return format("<a href='/case/%s/%s/${[CASE_REFERENCE]}/trigger/%s'>%s</a>",
            JURISDICTION, CASE_TYPE, event.getEvent().getId(), event.getEvent().getName());
    }

    public String renderDisabledLink(Task event) {
        return format("<a>%s</a>", event.getEvent().getName());
    }

    public String renderImage(String imageName, String title) {
        return format("<img align='right' height='25px' src='%s%s' title='%s'/>", imagesBaseUrl, imageName, title);
    }

    public String renderHint(String text) {
        return format("<span class='govuk-hint govuk-!-font-size-14'>%s</span>", text);
    }

    public String renderInfo(String text) {
        return format("<div class='panel panel-border-wide govuk-!-font-size-16'>%s</div>", text);
    }

    public String renderHeader(String text) {
        return format("## %s", text);
    }


}
