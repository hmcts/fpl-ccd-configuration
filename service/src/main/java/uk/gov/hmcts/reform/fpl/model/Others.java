package uk.gov.hmcts.reform.fpl.model;

import ccd.sdk.types.ComplexType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@ComplexType(name = "Person")
public class Others {
    private final Other firstOther;
    private final List<Element<Other>> additionalOthers;
}
