package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Guardian {
    private String guardianName;
    private String telephoneNumber;
    private String email;
    private List<String> children;

    // getter/setter work around for CCD persisting only
    public List<Element<String>> getChildrenRepresenting() {
        return wrapElements(children);
    }

    // getter/setter work around for CCD persisting only
    public void setChildrenRepresenting(List<Element<String>> childrenRepresenting) {
        children = unwrapElements(childrenRepresenting);
    }
}
