package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialBundle;

import java.util.List;
import java.util.stream.Collectors;

public class ConfidentialBundleHelper {

    public static List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle(
            List<? extends ConfidentialBundle> confidentialBundle) {
        return confidentialBundle.stream()
            .map(ConfidentialBundle::getSupportingEvidenceBundle)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
}
