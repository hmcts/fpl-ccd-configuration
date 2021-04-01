package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespondentStatement implements ConfidentialBundle {
    private String respondentName;
    private UUID respondentId;
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;

    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle() {
        return defaultIfNull(this.supportingEvidenceBundle, new ArrayList<>());
    }

    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceLA() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !(doc.getValue().isUploadedByHMCTS() && doc.getValue().isConfidentialDocument()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceNC() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !doc.getValue().isConfidentialDocument())
            .collect(Collectors.toList());
    }
}
