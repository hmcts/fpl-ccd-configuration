package uk.gov.hmcts.reform.fpl.components;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Component
public class OptionCountBuilder {

    public static String CASE_FIELD = "optionCount";

    public String generateCode(Collection<?> collection) {
        return IntStream.range(0, defaultIfNull(collection, emptyList()).size())
            .mapToObj(Integer::toString)
            .collect(joining());
    }

}
