package uk.gov.hmcts.reform.fpl.config.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

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

        ImmutableMap.Builder<String, T> lookupTable = ImmutableMap.builder();

        Arrays.stream(config.split(MAPPING_SEPARATOR)).forEach(entry -> {
            String[] entrySplit = entry.split(ENTRY_SEPARATOR);
            lookupTable.put(entrySplit[0], valueParser.parse(entrySplit[1]));
        });

        return lookupTable.build();
    }

    interface ValueParser<T> {
        T parse(String value);
    }

}
