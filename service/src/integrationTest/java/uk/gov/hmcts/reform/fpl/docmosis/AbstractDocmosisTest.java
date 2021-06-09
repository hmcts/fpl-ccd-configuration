package uk.gov.hmcts.reform.fpl.docmosis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;

@ActiveProfiles({"integration-test", "docmosis-template-test"})
@OverrideAutoConfiguration(enabled = true)
@Import(AbstractDocmosisTest.TestConfiguration.class)
public class AbstractDocmosisTest {

    public static class TestConfiguration {

        @Bean
        public DocmosisConfiguration docmosisConfiguration(
            @Value("${integration-test.docmosis.tornado.key}") String key,
            @Value("${integration-test.docmosis.tornado.url}") String url) {
            return new DocmosisConfiguration(url, key);
        }
    }


}
