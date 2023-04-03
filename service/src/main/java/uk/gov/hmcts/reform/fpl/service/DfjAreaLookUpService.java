package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.DfjAreaCourtMapping;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
public class DfjAreaLookUpService {

    private final ObjectMapper objectMapper;
    private List<DfjAreaCourtMapping> dfjCourtMapping;
    private Set<String> courtFields;

    public DfjAreaLookUpService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadDfjMappings();
        loadCourtFields();
    }

    private void loadDfjMappings() {
        try {
            final String jsonContent = ResourceReader.readString("static_data/dfjAreaCourtMapping.json");
            dfjCourtMapping = objectMapper.readValue(jsonContent, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Unable to parse dfjAreaCourtMapping.json file.", e);
        }
    }

    private void loadCourtFields() {
        courtFields = dfjCourtMapping.stream()
            .map(DfjAreaCourtMapping::getCourtField)
            .collect(toSet());
    }

    public DfjAreaCourtMapping getDfjArea(String courtCode) {
        return dfjCourtMapping.stream()
            .filter(dfjCourtMap -> dfjCourtMap.getCourtCode().equals(courtCode))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("No dfjArea found for court code: " + courtCode));
    }

    public Set<String> getAllCourtFields() {
        return courtFields;
    }
}
