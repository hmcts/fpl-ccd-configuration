package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

public interface ConfidentialBundle {
    List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle();
    List<Element<SupportingEvidenceBundle>> getLABundle();
    List<Element<SupportingEvidenceBundle>> getNonConfidentialBundle();
}
