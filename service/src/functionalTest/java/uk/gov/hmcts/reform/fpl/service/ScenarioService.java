package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Scenario;
import uk.gov.hmcts.reform.fpl.util.TestConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.springframework.util.StreamUtils.copyToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public final class ScenarioService {

    private static final Resource BASE_DIR = new DefaultResourceLoader().getResource("/stateless-scenarios");

    private final TestConfiguration testConfiguration;

    private final ObjectMapper objectMapper;

    public List<Scenario> getScenarios() {
        try {
            return Stream.of(new PathMatchingResourcePatternResolver()
                .getResources(format("%s/**/*.json", BASE_DIR.getFilename())))
                .map(this::loadScenario)
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Scenario loadScenario(Resource resource) {
        try {
            String fileContent = copyToString(resource.getInputStream(), defaultCharset());
            String scenarioString = StringSubstitutor.replace(fileContent, testConfiguration.getPlaceholders());
            Scenario scenario = objectMapper.readValue(scenarioString, Scenario.class);

            if (isEmpty(scenario.getName())) {
                scenario.setName(getName(resource));
            }
            return scenario;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getName(Resource resource) throws IOException {
        Path resourcePath = Paths.get(resource.getFile().getAbsolutePath());
        Path baseResourcePath = Paths.get(BASE_DIR.getFile().getAbsolutePath());

        return removeExtension(baseResourcePath.relativize(resourcePath).toString())
            .replace("-", " ")
            .replace("_", " ")
            .replace(File.separator, " - ");
    }
}
