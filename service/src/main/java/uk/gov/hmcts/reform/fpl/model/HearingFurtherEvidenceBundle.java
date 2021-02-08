package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingFurtherEvidenceBundle implements ConfidentialBundle {
    private String hearingName;
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;

    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle() {
        return defaultIfNull(supportingEvidenceBundle, new ArrayList<>());
    }

    // TODO: 03/02/2021 update case field name
    @JsonGetter(value = "supportingEvidenceLA")
    @Override
    public List<Element<SupportingEvidenceBundle>> getLABundle() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !(doc.getValue().isUploadedByHMCTS() && doc.getValue().isConfidential()))
            .collect(Collectors.toList());
    }
    // TODO: 03/02/2021 update case field name
    @JsonGetter(value = "supportingEvidenceNC")
    @Override
    public List<Element<SupportingEvidenceBundle>> getNonConfidentialBundle() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !doc.getValue().isConfidential())
            .collect(Collectors.toList());
    }
}
