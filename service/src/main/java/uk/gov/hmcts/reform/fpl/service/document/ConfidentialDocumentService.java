package uk.gov.hmcts.reform.fpl.service.document;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ConfidentialDocumentService {

    private final UserService user;

    public List<Element<SupportingEvidenceBundle>> getDocumentsForUser(List<Element<SupportingEvidenceBundle>> docs,
                                                                       Long caseId) {
        List<Element<SupportingEvidenceBundle>> appropriateDocs;
        if (user.isHmctsUser()) {
            // wants their own docs
            appropriateDocs = docs.stream()
                .filter(doc -> doc.getValue().isUploadedByHMCTS())
                .collect(Collectors.toList());
        } else {
            // wants docs that aren't uploaded by hmcts
            if (user.hasCaseRole(CaseRole.LASOLICITOR, caseId.toString())) {
                appropriateDocs = docs.stream()
                    .filter(doc -> !doc.getValue().isUploadedByHMCTS())
                    .collect(Collectors.toList());
            } else {
                // wants docs that aren't uploaded by hmcts and not confidential
                appropriateDocs = docs.stream()
                    .filter(doc -> !doc.getValue().isUploadedByHMCTS() && !doc.getValue().isConfidential())
                    .collect(Collectors.toList());
            }
        }
        return appropriateDocs;
    }

    // TODO: 03/02/2021 create appropriate case fields
    // to be called for furtherEvidenceDocuments + furtherEvidenceDocumentsLA,
    // and correspondenceDocuments + correspondenceDocumentsLA
    public Map<String, List<Element<SupportingEvidenceBundle>>> splitIntoAllAndNonConfidential(
        List<Element<SupportingEvidenceBundle>> documents, String keyPrefix) {
        return Map.of(
            keyPrefix, documents,
            keyPrefix + "NonConf", buildNonConfidentialCopy(documents)
        );
    }

    private List<Element<SupportingEvidenceBundle>> buildNonConfidentialCopy(
        List<Element<SupportingEvidenceBundle>> documents) {

        return documents.stream()
            .filter(doc -> !doc.getValue().isConfidential())
            .collect(Collectors.toList());
    }
}
