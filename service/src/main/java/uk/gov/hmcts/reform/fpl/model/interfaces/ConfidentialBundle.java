package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

public interface ConfidentialBundle {
    List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle();

    List<Element<SupportingEvidenceBundle>> getSupportingEvidenceLA();

    List<Element<SupportingEvidenceBundle>> getSupportingEvidenceNC();
}
