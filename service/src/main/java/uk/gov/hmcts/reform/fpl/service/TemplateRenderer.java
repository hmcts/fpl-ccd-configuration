package uk.gov.hmcts.reform.fpl.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.Application;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static freemarker.template.Configuration.VERSION_2_3_33;

@Slf4j
@Service
public class TemplateRenderer {

    private final Configuration cfg;

    public TemplateRenderer() {
        this.cfg = new Configuration(VERSION_2_3_33);
        cfg.setClassForTemplateLoading(Application.class, "/templates/html");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public String renderTemplate(String templateName, Map<String, Object> data) throws IOException, TemplateException {
        Template template = cfg.getTemplate(templateName);
        StringWriter stringWriter = new StringWriter();
        template.process(data, stringWriter);
        return stringWriter.toString();
    }

    public String renderTaskList(Map<String, Object> data) {
        try {
            return this.renderTemplate("taskList.ftlh", data);
        } catch (Exception e) {
            log.error("Error rendering task list", e);
            return "";
        }
    }

}
