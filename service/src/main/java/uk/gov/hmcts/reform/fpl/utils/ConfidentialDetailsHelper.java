package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.Other;
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

    // TODO - Move this back into the above function, once Others has been refactored into a collection like respondents
    public static Other getConfidentialOtherToAdd(List<Element<Other>> confidential, Element<Other> element) {
        return confidential.stream()
            .filter(item -> item.getId().equals(element.getId())
                || item.getValue().getName().equals(element.getValue().getName()))
            .map(Element::getValue)
            .findFirst()
            .orElse(element.getValue());
    }
}
