package uk.gov.hmcts.reform.fpl.service.representative.diff;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
public class ChildRepresentativeDiffCalculator extends SimplePartyRepresentativeDiffCalculator<Child> {
    @Override
    protected List<Child> getRegistered(List<Element<Child>> children) {
        return unwrapElements(children).stream()
            .filter(WithSolicitor::hasRegisteredOrganisation)
            .collect(Collectors.toList());
    }

    @Override
    protected List<Child> getUnregistered(List<Element<Child>> children) {
        return unwrapElements(children).stream()
            .filter(WithSolicitor::hasUnregisteredOrganisation)
            .collect(Collectors.toList());
    }
}
