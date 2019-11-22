package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
public class Others {
    private final Other firstOther;
    private final List<Element<Other>> additionalOthers;

    @JsonIgnore
    public List<Other> getAllOthers() {
        ImmutableList.Builder<Other> builder = ImmutableList.builder();

        if (firstOther != null) {
            builder.add(firstOther);
        }

        if (additionalOthers != null) {
            builder.addAll(additionalOthers.stream().map(Element::getValue).collect(Collectors.toList()));
        }

        return builder.build();
    }
}
