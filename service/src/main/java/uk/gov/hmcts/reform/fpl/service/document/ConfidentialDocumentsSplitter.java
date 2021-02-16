package uk.gov.hmcts.reform.fpl.service.document;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ConfidentialDocumentsSplitter {

    public Map<String, Object> splitIntoAllAndNonConfidential(List<Element<SupportingEvidenceBundle>> documents,
                                                              String keyPrefix) {
        return Map.of(
            keyPrefix, documents,
            keyPrefix + "NC", buildNonConfidentialCopy(documents)
        );
    }

    private List<Element<SupportingEvidenceBundle>> buildNonConfidentialCopy(
        List<Element<SupportingEvidenceBundle>> documents) {

        return documents.stream()
            .filter(doc -> !doc.getValue().isConfidentialDocument())
            .collect(Collectors.toList());
    }
}
