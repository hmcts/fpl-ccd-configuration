package uk.gov.hmcts.reform.fpl.service.representative.diff;

import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.ArrayList;
import java.util.List;

public abstract class SimplePartyRepresentativeDiffCalculator<P extends WithSolicitor>
    implements PartyRepresentativeDiffCalculator<P> {

    @Override
    public List<P> getRegisteredDiff(List<Element<P>> current, List<Element<P>> old) {
        List<P> currentRegistered = getRegistered(new ArrayList<>(current));
        List<P> oldRegistered = getRegistered(new ArrayList<>(old));

        currentRegistered.removeAll(oldRegistered);

        return currentRegistered;
    }

    @Override
    public List<P> getUnregisteredDiff(List<Element<P>> current, List<Element<P>> old) {
        List<P> currentUnregistered = getUnregistered(current);
        List<P> oldUnregistered = getUnregistered(old);

        currentUnregistered.removeAll(oldUnregistered);

        return currentUnregistered;
    }

    protected abstract List<P> getRegistered(List<Element<P>> children);

    protected abstract List<P> getUnregistered(List<Element<P>> children);
}
