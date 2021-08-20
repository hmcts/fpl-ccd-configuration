package uk.gov.hmcts.reform.fpl.service.document;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentContainerView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentFolderView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

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

    public String render(List<DocumentContainerView> documents) {
        final List<String> lines = new LinkedList<>();
        lines.add("<p><div class='width-50'>");
        for (DocumentContainerView containerView : documents) {
            if (containerView instanceof DocumentBundleView) {
                lines.add(renderBundle((DocumentBundleView) containerView));
            } else if (containerView instanceof DocumentFolderView) {
                lines.add(renderFolder((DocumentFolderView) containerView));
            }
        }
        lines.add("</div></p>");
        return String.join("\n\n", lines);
    }

    private String renderFolder(DocumentFolderView folderView) {
        String s = folderView.getDocumentBundleViews().stream()
            .map(this::renderBundle)
            .collect(joining(""));
        return collapsible(folderView.getName(), s);
    }

    private String renderBundle(DocumentBundleView bundle) {
        String s = bundle.getDocuments().stream()
            .map(this::renderDocument)
            .collect(joining(""));
        return collapsible(bundle.getName(), s);
    }

    private String renderDocument(DocumentView documentView) {

        List<Pair<String, String>> documentFields = getFieldsBasedOnDocumentType(documentView);

        var details = String.join("", renderItems(documentFields));

        return collapsible(documentView.getTitle(), details);
    }

    private List<Pair<String, String>> getFieldsBasedOnDocumentType(DocumentView documentView) {
        List<Pair<String, String>> documentFields = new ArrayList<>();

        if (isNotEmpty(documentView.getUploadedBy())) {
            documentFields.add(Pair.of("Uploaded by", documentView.getUploadedBy()));
        }

        if (isNotEmpty(documentView.getUploadedAt())) {
            documentFields.add(Pair.of("Date and time uploaded", documentView.getUploadedAt()));
        }

        if (documentView.isIncludeSWETField() && isNotEmpty(documentView.getIncludedInSWET())) {
            documentFields.add(Pair.of("Included in SWET", documentView.getIncludedInSWET()));
        }

        if (documentView.isIncludeDocumentName() && isNotEmpty(documentView.getDocumentName())) {
            documentFields.add(Pair.of("Document name", documentView.getDocumentName()));
        }

        if (documentView.isConfidential()) {
            documentFields.add(Pair.of(renderImage("confidential.png", "Confidential"), ""));
        }

        if (isNotEmpty(documentView.getDocument())) {
            documentFields.add(Pair.of("Document",
                "<a href='"
                    + getDocumentUrl(documentView.getDocument())
                    + "'>"
                    + documentView.getDocument().getFilename() + "</a>"));

        }

        if (documentView.isSentForTranslation()) {
            documentFields.add(Pair.of("Sent for translation", ""));
        }

        if (isNotEmpty(documentView.getTranslatedDocument())) {
            documentFields.add(Pair.of("Translated document",
                "<a href='"
                    + getDocumentUrl(documentView.getTranslatedDocument())
                    + "'>"
                    + documentView.getTranslatedDocument().getFilename() + "</a>"));
        }

        return documentFields;
    }

    public String renderImage(String imageName, String title) {
        return format("<img height='25px' src='%s%s' title='%s'/>", imagesBaseUrl, imageName, title);
    }

    private String collapsible(String title, String content) {
        return "<details class=\"govuk-details\">"
            + "       <summary class=\"govuk-details__summary\">" + title + "</summary>"
            + "       <div class=\"govuk-details__text\">" + content + "</div>"
            + "</details>";
    }

    private List<String> renderItems(List<Pair<String, String>> props) {
        List<String> lines = new ArrayList<>();
        lines.add("<dl class=\"govuk-summary-list\">");
        lines.addAll(props.stream().map(tuple -> renderItem(tuple.getKey(), tuple.getValue())).collect(toList()));
        lines.add("</dl>");

        return lines;
    }

    private String renderItem(String name, String value) {
        return "<div class=\"govuk-summary-list__row\">"
            + "     <dt class=\"govuk-summary-list__key\">" + name + "</dt>"
            + "     <dd class=\"govuk-summary-list__value\">" + value + "</dd>"
            + "</div>";
    }

    private String getDocumentUrl(DocumentReference document) {
        String binaryUrl = document.getBinaryUrl();
        try {
            var uri = new URI(binaryUrl);
            return caseUrlService.getBaseUrl() + uri.getPath();
        } catch (URISyntaxException e) {
            log.error(binaryUrl + " url incorrect.", e);
        }
        return "";
    }
}
