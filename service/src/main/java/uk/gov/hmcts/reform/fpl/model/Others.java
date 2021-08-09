package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Others {
    private final Other firstOther;
    private final List<Element<Other>> additionalOthers;

    public static Others from(List<Element<Other>> allOthers) {

        final LinkedList<Element<Other>> others = new LinkedList<>(Optional.ofNullable(allOthers).orElse(emptyList()));
        return Others.builder()
            .firstOther(ofNullable(others.pollFirst()).map(Element::getValue).filter(not(Other::isEmpty)).orElse(null))
            .additionalOthers(others)
            .build();
    }

    @JsonIgnore
    public boolean hasOthers() {
        return !(firstOther == null || firstOther.isEmpty()) || isNotEmpty(additionalOthers);
    }
}
