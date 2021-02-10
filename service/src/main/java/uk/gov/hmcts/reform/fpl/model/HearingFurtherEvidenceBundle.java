package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingFurtherEvidenceBundle implements ConfidentialBundle {
    private String hearingName;
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;

    /*
    If want it the way listed in the docs
    Add a json ignore to the getSupportingEvidenceBundle
    add new getter that returns hmcts view (maybe as a new field):
        all confidential
    update getLABundle so that:
        only confidential uploaded by la
    update perms for all collections

    have setters that merge everything, i.e. should just need to be the hmcts confidential docs view (all
    confidential docs) and non confidential docs
     */

    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle() {
        return defaultIfNull(this.supportingEvidenceBundle, new ArrayList<>());
    }

    @JsonGetter(value = "supportingEvidenceLA")
    @Override
    public List<Element<SupportingEvidenceBundle>> getLABundle() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !(doc.getValue().isUploadedByHMCTS() && doc.getValue().isConfidential()))
            .collect(Collectors.toList());
    }

    @JsonGetter(value = "supportingEvidenceNC")
    @Override
    public List<Element<SupportingEvidenceBundle>> getNonConfidentialBundle() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !doc.getValue().isConfidential())
            .collect(Collectors.toList());
    }
}
