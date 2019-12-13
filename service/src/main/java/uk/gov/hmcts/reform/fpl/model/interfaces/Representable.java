package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrap;

public abstract class Representable {

    private List<Element<UUID>> representedBy = new ArrayList<>();

    public List<Element<UUID>> getRepresentedBy() {
        return representedBy;
    }

    public void addRepresentative(UUID representativeId) {
        if (!unwrap(representedBy).contains(representativeId)) {
            this.representedBy.add(element(representativeId));
        }
    }
}
