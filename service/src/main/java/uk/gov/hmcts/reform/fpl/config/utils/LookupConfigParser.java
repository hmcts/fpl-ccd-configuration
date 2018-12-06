package uk.gov.hmcts.reform.fpl.config.utils;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class LookupConfigParser {

    private static final String MAPPING_SEPARATOR = ";";
    private static final String ENTRY_SEPARATOR = "=>";

    /**
     * Parses mapping containing single string as a value.
     * <p>
     * Example: SA=>Swansea;HN=>Hillingdon
     *
     * @param config raw mapping
     * @return lookup table
     */
    public static Map<String, String> parseStringValue(String config) {
        return parse(config, value -> value);
    }

    /**
     * Parses mapping containing list of string as a value.
     * <p>
     * Example: SA=>1,2,3;HN=>4,5,6
     *
     * @param config raw mapping
     * @return lookup table
     */
    public static Map<String, List<String>> parseStringListValue(String config) {
        return parse(config, value -> ImmutableList.<String>builder()
            .add(value.split(","))
            .build()
        );
    }

    private static <T> Map<String, T> parse(String config, ValueParser<T> valueParser) {
        checkArgument(!isNullOrEmpty(config), "Mapping configuration cannot be empty");

        return Arrays.stream(config.split(MAPPING_SEPARATOR))
            .map(entry -> entry.split(ENTRY_SEPARATOR))
            .collect(toImmutableMap(
                entry -> entry[0],
                entry -> valueParser.parse(entry[1])
            ));
    }

    interface ValueParser<T> {
        T parse(String value);
    }

}
