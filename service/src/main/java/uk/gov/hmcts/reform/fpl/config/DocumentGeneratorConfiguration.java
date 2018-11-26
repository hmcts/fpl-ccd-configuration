package uk.gov.hmcts.reform.fpl.config;

import com.google.common.collect.ImmutableMap;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.loader.StringLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.pdf.generator.HTMLTemplateProcessor;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;
import uk.gov.hmcts.reform.pdf.generator.PDFGenerator;
import uk.gov.hmcts.reform.pdf.generator.XMLContentSanitizer;
import uk.gov.hmcts.reform.pebble.AgeFilter;
import uk.gov.hmcts.reform.pebble.TodayFilter;

import java.util.Map;

@Configuration
public class DocumentGeneratorConfiguration {

    @Bean
    public HTMLToPDFConverter getConverter() {
        return new HTMLToPDFConverter(
            new HTMLTemplateProcessor(buildEngine()),
            new PDFGenerator(),
            new XMLContentSanitizer()
        );
    }

    private PebbleEngine buildEngine() {
        return new PebbleEngine.Builder()
            .loader(new StringLoader())
            .strictVariables(true)
            .cacheActive(false)
            .extension(new AbstractExtension() {
                @Override
                public Map<String, Filter> getFilters() {
                    return ImmutableMap.<String, Filter>builder()
                        .put("today", new TodayFilter())
                        .put("age", new AgeFilter())
                        .build();
                }
            })
            .build();
    }
}
