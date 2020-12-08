package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisAnnexDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisAnnexDocuments;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Component
public class CaseSubmissionDocumentAnnexGenerator {

    public DocmosisAnnexDocuments generate(CaseData caseData) {

        List<Element<ApplicationDocument>> documents = caseData.getApplicationDocuments();

        return DocmosisAnnexDocuments.builder()
            .featureToggleOn(true)
            .documents(transformDocuments(documents))
            .toFollowReason(caseData.getApplicationDocumentsToFollowReason())
            .build();
    }

    private List<DocmosisAnnexDocument> transformDocuments(List<Element<ApplicationDocument>> documents) {

        Map<String, Long> documentTitlesAndCounts = nullSafeList(documents).stream().map(this::generateTitle)
            .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));

        return documentTitlesAndCounts.entrySet().stream().map(
            entry -> DocmosisAnnexDocument.builder()
                .title(entry.getKey())
                .description(generateDescription(entry.getValue()))
                .build()
        ).collect(Collectors.toList());
    }

    private String generateTitle(Element<ApplicationDocument> document) {
        ApplicationDocumentType documentType = document.getValue().getDocumentType();

        return ApplicationDocumentType.OTHER.equals(documentType)
            ? document.getValue().getDocumentName() : documentType.getLabel();
    }

    private String generateDescription(long size) {
        return size > 1 ? String.format("%d attached", size) : "Attached";
    }
}
