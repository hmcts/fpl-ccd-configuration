package uk.gov.hmcts.reform.fpl.service.document;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConfidentialDocumentsSplitter {

    public void updateConfidentialDocsInCaseDetails(CaseDetails caseDetails,
                                                    List<Element<SupportingEvidenceBundle>> documents,
                                                    String keyPrefix) {
        String nonConfidentialPrefix = keyPrefix + "NC";

        List<Element<SupportingEvidenceBundle>> nonConfidentialCopy = documents.stream()
            .filter(doc -> !doc.getValue().isConfidentialDocument())
            .collect(Collectors.toList());

        if (!nonConfidentialCopy.isEmpty()) {
            caseDetails.getData().put(nonConfidentialPrefix, nonConfidentialCopy);
        } else {
            caseDetails.getData().remove(nonConfidentialPrefix);
        }
    }
}
