package uk.gov.hmcts.reform.fpl.service.document;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.DocumentView;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.util.Strings.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SWET;

@Slf4j
@Service
class DocumentsListRenderer {

    private final String imagesBaseUrl;
    private final CaseUrlService caseUrlService;

    public DocumentsListRenderer(@Value("${resources.images.baseUrl}") String imagesBaseUrl,
                                 CaseUrlService caseUrlService) {
        this.imagesBaseUrl = imagesBaseUrl;
        this.caseUrlService = caseUrlService;
    }

    //TODO consider templating solution like mustache
    public String render(List<DocumentBundleView> documents) {
        final List<String> lines = new LinkedList<>();
        lines.add("<p><div class='width-50'>");
        for (DocumentBundleView documentBundle : documents) {
            lines.add(renderBundle(documentBundle));
        }
        lines.add("</div></p>");
        return String.join("\n\n", lines);
    }

    private String renderBundle(DocumentBundleView bundle) {
        String s = bundle.getDocuments().stream()
            .map(this::render)
            .collect(joining(""));
        return collapsible(bundle.getName(), s);
    }

    private String render(DocumentView documentView) {

        List<Pair<String, String>> documentFields = getFieldsBasedOnDocumentType(documentView);

        String details = String.join("", renderItems(documentFields));
        return collapsible(documentView.getType(), details);
    }

    private List<Pair<String, String>> getFieldsBasedOnDocumentType(DocumentView documentView) {
        List<Pair<String, String>> documentFields = new ArrayList<>();
        documentFields.add(Pair.of("Uploaded by", documentView.getUploadedBy()));
        documentFields.add(Pair.of("Date and time uploaded", documentView.getUploadedAt()));

        if (documentView.getType().equals(SWET.getLabel()) && !isEmpty(documentView.getIncludedInSWET())) {
                documentFields.add(Pair.of("Included in SWET", documentView.getIncludedInSWET()));
        }

        if (documentView.getType().equals(OTHER.getLabel())) {
            documentFields.add(Pair.of("Document name", documentView.getDocumentName()));
        }

        documentFields.add(Pair.of("Document", "<a href='" + getDocumentUrl(documentView.getDocument()) + "'>" + documentView.getDocument().getFilename() + "</a>"));

        boolean isConfidential = true;
        if(isConfidential) {
            documentFields.add(Pair.of(renderImage("confidential.png", "Confidential"), ""));
        }

        return documentFields;
    }

    public String renderImage(String imageName, String title) {
        return format("<img height='25px' src='%s%s' title='%s'/>", imagesBaseUrl, imageName, title);
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
