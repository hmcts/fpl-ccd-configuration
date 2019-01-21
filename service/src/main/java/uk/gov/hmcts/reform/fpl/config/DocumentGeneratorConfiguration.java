package uk.gov.hmcts.reform.fpl.config;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.loader.StringLoader;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.pebble.EmergencyProtectionOrderDirectionMappingFilter;
import uk.gov.hmcts.reform.fpl.pebble.EmergencyProtectionOrderMappingFilter;
import uk.gov.hmcts.reform.fpl.pebble.EmergencyProtectionOrderReasonMappingFilter;
import uk.gov.hmcts.reform.fpl.pebble.OrderMappingFilter;
import uk.gov.hmcts.reform.pdf.generator.HTMLTemplateProcessor;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;
import uk.gov.hmcts.reform.pdf.generator.PDFGenerator;
import uk.gov.hmcts.reform.pdf.generator.XMLContentSanitizer;
import uk.gov.hmcts.reform.pebble.AgeFilter;
import uk.gov.hmcts.reform.pebble.TodayFilter;

import java.time.Clock;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

@Configuration
public class DocumentGeneratorConfiguration {

    @Bean
    public HTMLToPDFConverter getConverter(Clock clock) {
        return new HTMLToPDFConverter(
            new HTMLTemplateProcessor(buildEngine(new TodayFilter(clock), new AgeFilter(clock),
                new OrderMappingFilter(), new EmergencyProtectionOrderMappingFilter(),
                new EmergencyProtectionOrderReasonMappingFilter(),
                new EmergencyProtectionOrderDirectionMappingFilter())),
            new PDFGenerator(),
            new XMLContentSanitizer()
        );
    }

    private PebbleEngine buildEngine(Filter... filters) {
        return new PebbleEngine.Builder()
            .loader(new StringLoader())
            .strictVariables(true)
            .cacheActive(false)
            .extension(new AbstractExtension() {
                @Override
                public Map<String, Filter> getFilters() {
                    return Stream.of(filters)
                        .collect(toImmutableMap(
                            filter -> StringUtils.uncapitalize(filter.getClass().getSimpleName()
                                .replace("Filter", "")),
                            filter -> filter)
                        );
                }
            })
            .build();
    }
}
