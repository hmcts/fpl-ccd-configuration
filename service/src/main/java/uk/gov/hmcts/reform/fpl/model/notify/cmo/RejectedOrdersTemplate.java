package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class RejectedOrdersTemplate extends RejectedCMOTemplate {
    private final List<String> ordersAndRequestedChanges;
}
