package uk.gov.hmcts.reform.fpl.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class DocumentsListRenderer {

    private final String imagesBaseUrl;
    private final CaseUrlService caseUrlService;

    public DocumentsListRenderer(@Value("${resources.images.baseUrl}") String imagesBaseUrl,
                                 CaseUrlService caseUrlService) {
        this.imagesBaseUrl = imagesBaseUrl;
        this.caseUrlService = caseUrlService;
    }

    public String renderSearchResults(List<DocumentView> documents) {
        final List<String> lines = new LinkedList<>();
        lines.add("<div>");
        lines.add("<h1>Found "+documents.size()+" document(s)</h1>");
        for (DocumentView documentBundle : documents) {
            lines.add(render(documentBundle));
        }
        lines.add("</div>");
        return String.join("\n\n", lines);
    }

    //TODO consider templating solution like mustache
    public String render(List<DocumentBundleView> documents) {
        final List<String> lines = new LinkedList<>();
        lines.add("<div class='width-50'>");
        for (DocumentBundleView documentBundle : documents) {
            lines.add(renderBundle(documentBundle));
        }
        lines.add("</div>");
        return String.join("\n\n", lines);
    }

    private String renderBundle(DocumentBundleView bundle) {
        String s = bundle.documents.stream()
            .map(this::render)
            .collect(joining(""));
        return collapsible(String.format("<h3>%s (%d documents)</h3>", bundle.name, bundle.documents.size()), s);
    }

    private String render(DocumentView documentView) {
        String details = String.join("", renderItems(List.of(
            Pair.of("Name", documentView.type),
            Pair.of("Uploaded by", documentView.uploadedBy),
            Pair.of("Uploaded at", documentView.uploadedAt),
            Pair.of("Document", "<a href='" + getDocumentUrl(documentView.document) + "'>" + documentView.document.getFilename() + "</a>")
        )));
        return collapsible(String.format("%s - %s", documentView.type, documentView.document.getFilename()), details);
    }

    private String collapsible(String title, String content) {
        return "<details class=\"govuk-details\">" +
            "       <summary class=\"govuk-details__summary\">" + title + "</summary>" +
            "       <div class=\"govuk-details__text\">" + content + "</div>" +
            "</details>";
    }

    private List<String> renderItems(List<Pair<String, String>> props) {
        List<String> lines = new ArrayList<>();
        lines.add("<dl class=\"govuk-summary-list\">");
        lines.addAll(props.stream().map(tuple -> renderItem(tuple.getKey(), tuple.getValue())).collect(toList()));
        lines.add("</dl>");

        return lines;
    }

    private String renderItem(String name, String value) {
        return "<div class=\"govuk-summary-list__row\">" +
            "     <dt class=\"govuk-summary-list__key\">" + name + "</dt>" +
            "     <dd class=\"govuk-summary-list__value\">" + value + "</dd>" +
            "</div>";
    }

    private String getDocumentUrl(DocumentReference document) {
        String binaryUrl = document.getBinaryUrl();
        try {
            URI uri = new URI(binaryUrl);
            return caseUrlService.getBaseUrl() + uri.getPath();
        } catch (URISyntaxException e) {
            log.error(binaryUrl + " url incorrect.", e);
        }
        return "";
    }
}
