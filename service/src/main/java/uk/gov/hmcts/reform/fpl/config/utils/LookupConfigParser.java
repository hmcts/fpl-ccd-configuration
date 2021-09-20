package uk.gov.hmcts.reform.fpl.config.utils;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class LookupConfigParser {

    private static final String MAPPING_SEPARATOR = ";";
    private static final String ENTRY_SEPARATOR = "=>";
    private static final String LIST_ELEMENT_SEPARATOR = "\\|";

    private LookupConfigParser() {
        //NO-OP
    }

    /**
     * Parses mapping containing single string as a value.
     *
     * <p>Example: SA=>Swansea;HN=>Hillingdon
     *
     * @param config raw mapping
     * @return lookup table
     */
    public static Map<String, String> parseStringValue(String config) {
        return parse(config, StringUtils::trim);
    }

    /**
     * Parses mapping containing list of string as a value.
     *
     * <p>Example: SA=>1,2,3;HN=>4,5,6
     *
     * @param config raw mapping
     * @return lookup table
     */
    public static Map<String, List<String>> parseStringListValue(String config) {
        return parse(config, value -> ImmutableList.<String>builder()
            .add(value.split(LIST_ELEMENT_SEPARATOR))
            .build()
        );
    }

    /**
     * Parses mapping using provided value parser.
     *
     * @param config      raw mapping
     * @param valueParser value parser
     * @return lookup table
     */
    public static <T> Map<String, T> parse(String config, ValueParser<T> valueParser) {
        checkArgument(!isNullOrEmpty(config), "Mapping configuration cannot be empty");

        return Arrays.stream(config.split(MAPPING_SEPARATOR))
            .map(entry -> entry.split(ENTRY_SEPARATOR, 2))
            .collect(toImmutableMap(
                entry -> checkNotNull(emptyToNull(StringUtils.trim(entry[0])), "Mapping key cannot be empty"),
                entry -> valueParser.parse(checkNotNull(emptyToNull(entry[1]), "Mapping value cannot be empty"))
            ));
    }

    public interface ValueParser<T> {
        T parse(String value);
    }
}
