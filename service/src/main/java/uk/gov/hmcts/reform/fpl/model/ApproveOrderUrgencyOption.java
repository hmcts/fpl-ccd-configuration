package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class ApproveOrderUrgencyOption {

    @Builder.Default
    private final List<YesNo> urgency = new ArrayList<>();
}
