package uk.gov.hmcts.reform.fpl.service.orders.history;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.OthersService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SealedOrderHistoryExtraOthersNotifiedGenerator {

    private final OthersService othersService;

    public String getOthersNotified(List<Element<Other>> selectedOthers) {
        return Optional.ofNullable(selectedOthers).map(
            others -> others.stream()
                .filter(other -> othersService.isRepresented(other.getValue()) || othersService
                    .hasAddressAdded(other.getValue()))
                .map(other -> other.getValue().getName()).collect(Collectors.joining(", "))
        ).orElse(null);
    }
}
