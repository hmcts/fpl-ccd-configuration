package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
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

    public DocmosisAnnexDocuments generate(CaseData caseData,
                                           Language applicationLanguage) {

        List<Element<ApplicationDocument>> documents = caseData.getTemporaryApplicationDocuments();

        return DocmosisAnnexDocuments.builder()
            .documents(transformDocuments(documents, applicationLanguage))
            .toFollowReason(caseData.getApplicationDocumentsToFollowReason())
            .build();
    }

    private List<DocmosisAnnexDocument> transformDocuments(List<Element<ApplicationDocument>> documents,
                                                           Language applicationLanguage) {

        Map<String, Long> documentTitlesAndCounts = nullSafeList(documents).stream()
            .map(document -> generateTitle(document, applicationLanguage))
            .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));

        return documentTitlesAndCounts.entrySet().stream().map(
            entry -> DocmosisAnnexDocument.builder()
                .title(entry.getKey())
                .description(generateDescription(entry.getValue(), applicationLanguage))
                .build()
        ).collect(Collectors.toList());
    }

    private String generateTitle(Element<ApplicationDocument> document,
                                 Language applicationLanguage) {
        ApplicationDocumentType documentType = document.getValue().getDocumentType();

        return ApplicationDocumentType.OTHER.equals(documentType)
            ? document.getValue().getDocumentName() : documentType.getLabel(applicationLanguage);
    }

    private String generateDescription(long size,
                                       Language applicationLanguage) {

        if (applicationLanguage == Language.WELSH) {
            return size > 1 ? String.format("%d ynghlwm", size) : "Ynghlwm";
        }

        return size > 1 ? String.format("%d attached", size) : "Attached";
    }
}
