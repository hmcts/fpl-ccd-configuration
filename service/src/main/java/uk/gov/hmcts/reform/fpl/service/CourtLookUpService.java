package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class CourtLookUpService {

    public static final String RCJ_HIGH_COURT_CODE = "100";

    public static final String RCJ_HIGH_COURT_REGION = "London";

    public static final String RCJ_HIGH_COURT_NAME = "High Court Family Division - (100)";

    private final ObjectMapper objectMapper;

    private List<Court> courts;

    @Autowired
    public CourtLookUpService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadCourtsMappings();
    }

    private void loadCourtsMappings() {
        try {
            final String jsonContent = ResourceReader.readString("static_data/courts.json");
            courts = objectMapper.readValue(jsonContent, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Unable to parse courts.json file.", e);
        }
    }

    public Court buildRcjHighCourt() {
        return Court.builder().code(RCJ_HIGH_COURT_CODE)
            .name(RCJ_HIGH_COURT_NAME)
            .region(RCJ_HIGH_COURT_REGION)
            .build();
    }

    public List<Court> getCourtFullListWithRcjHighCourt() {
        List<Court> ret = new ArrayList<>(List.of(buildRcjHighCourt()));
        ret.addAll(courts);
        return ret;
    }

    public Optional<Court> getCourtByCode(String courtCode) {
        return getCourtFullListWithRcjHighCourt().stream()
            .filter(court -> Objects.equals(court.getCode(), courtCode))
            .findFirst();
    }

}
