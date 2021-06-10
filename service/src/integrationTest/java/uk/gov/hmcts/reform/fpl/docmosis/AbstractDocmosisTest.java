package uk.gov.hmcts.reform.fpl.docmosis;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.utils.captor.ResultsCaptor;

import java.io.File;
import java.io.IOException;

@ActiveProfiles({"integration-test", "docmosis-template-test"})
@OverrideAutoConfiguration(enabled = true)
@Import(AbstractDocmosisTest.TestConfiguration.class)
public class AbstractDocmosisTest {

    protected ResultsCaptor<DocmosisDocument> resultsCaptor = new ResultsCaptor<>();

    @Value("${integration-test.docmosis.tornado.output.folder}")
    private String outputFolder;

    public static class TestConfiguration {

        @Bean
        public DocmosisConfiguration docmosisConfiguration(
            @Value("${integration-test.docmosis.tornado.key}") String key,
            @Value("${integration-test.docmosis.tornado.url}") String url) {
            return new DocmosisConfiguration(url, key);
        }
    }

    public void storeToOuputFolder(String fileName, byte[] bytes) throws IOException {
        FileUtils.writeByteArrayToFile(new File(outputFolder + "/" + fileName), bytes);
    }
}
