package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

public class ConfidentialDetailsHelper {

    private ConfidentialDetailsHelper() {
        // NO OP
    }

    public static <T> T getConfidentialItemToAdd(List<Element<T>> confidential, Element<T> element) {
        return confidential.stream()
            .filter(item -> item.getId().equals(element.getId()))
            .map(Element::getValue)
            .findFirst()
            .orElse(element.getValue());
    }
}
