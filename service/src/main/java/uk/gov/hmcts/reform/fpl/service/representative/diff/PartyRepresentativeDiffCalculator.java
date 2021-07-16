package uk.gov.hmcts.reform.fpl.service.representative.diff;

import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.List;

public interface PartyRepresentativeDiffCalculator<P extends WithSolicitor> {
    List<P> getRegisteredDiff(List<Element<P>> current, List<Element<P>> old);

    List<P> getUnregisteredDiff(List<Element<P>> current, List<Element<P>> old);
}
