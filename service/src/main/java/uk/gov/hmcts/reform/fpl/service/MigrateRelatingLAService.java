package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class MigrateRelatingLAService {

    private final ObjectMapper objectMapper;
    private Map<String, String> casesToMap;

    @Autowired
    public MigrateRelatingLAService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        try {
            final String jsonContent = ResourceReader.readString("static_data/casesToMapRelatingLA.json");
            casesToMap = objectMapper.readValue(jsonContent, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Unable to parse casesToMapRelatingLA.json file.", e);
        }

    }

    public Optional<String> getRelatingLAString(String caseId) {
        return Optional.ofNullable(casesToMap.getOrDefault(caseId, null));
    }


}
