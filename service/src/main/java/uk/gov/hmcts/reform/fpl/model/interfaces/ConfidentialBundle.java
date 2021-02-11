package uk.gov.hmcts.reform.fpl.model.interfaces;

import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.stream.Collectors;

@SuperBuilder
public abstract class ConfidentialBundle {
    public abstract List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle();

    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceLA() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !(doc.getValue().isUploadedByHMCTS() && doc.getValue().isConfidentialDocument()))
            .collect(Collectors.toList());
    }

    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceNC() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !doc.getValue().isConfidentialDocument())
            .collect(Collectors.toList());

    }
}
