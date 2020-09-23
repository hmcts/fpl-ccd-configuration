package uk.gov.hmcts.reform.fpl.json.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

public class BasicChildConverter extends StdConverter<Element<Child>, Element<Child>> {

    @Override
    public Element<Child> convert(Element<Child> childElement) {
        if (childElement == null) {
            return null;
        }
        if (childElement.getValue() == null) {
            return childElement;
        }
        ChildParty childParty = childElement.getValue().getParty();
        Child newChild = Child.builder()
            .party(ChildParty.builder()
                .firstName(childParty.getFirstName())
                .lastName(childParty.getLastName())
                .dateOfBirth(childParty.getDateOfBirth())
                .gender(childParty.getGender())
                .build())
            .finalOrderIssued(childElement.getValue().getFinalOrderIssued())
            .build();
        return Element.<Child>builder().id(childElement.getId()).value(newChild).build();
    }
}

