package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;


public interface Representable {
    List<Element<UUID>> getRepresentedBy();

    void addRepresentative(UUID representativeId);
}
