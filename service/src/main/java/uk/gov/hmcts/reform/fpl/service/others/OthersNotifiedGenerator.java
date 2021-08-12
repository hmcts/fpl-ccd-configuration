package uk.gov.hmcts.reform.fpl.service.others;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OthersNotifiedGenerator {

    public String getOthersNotified(List<Element<Other>> selectedOthers) {
        return Optional.ofNullable(selectedOthers).map(
            others -> others.stream()
                .filter(other -> other.getValue().isRepresented() || other.getValue()
                    .hasAddressAdded())
                .map(other -> other.getValue().getName()).collect(Collectors.joining(", "))
        ).orElse(null);
    }
}
