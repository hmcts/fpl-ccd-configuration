package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialBundle;

import java.util.List;

public class ConfidentialBundleHelper {

    private ConfidentialBundleHelper() {
        // NO-OP
    }

    public static List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle(
            List<? extends ConfidentialBundle> confidentialBundle) {
        return confidentialBundle.stream()
            .map(ConfidentialBundle::getSupportingEvidenceBundle)
            .flatMap(List::stream)
            .toList();
    }
}
