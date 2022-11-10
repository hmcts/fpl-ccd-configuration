package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class ResendCafcassEmailService {

    private final ObjectMapper objectMapper;
    private Map<Long, ResendData> casesToResend;

    @Autowired
    public ResendCafcassEmailService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadResendConfig();

        log.info("We will be resending emails on {} cases", casesToResend.keySet().size());
    }

    private void loadResendConfig() {
        try {
            final String jsonContent = ResourceReader.readString("static_data/casesToResend.json");
            casesToResend = objectMapper.readValue(jsonContent, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Unable to parse casesToResend.json file.", e);
        }
    }

    public List<LocalDate> getOrderDates(Long caseId) {
        return this.casesToResend.get(caseId).getOrders();
    }

    public List<LocalDateTime> getNoticeOfHearingDateTimes(Long caseId) {
        return this.casesToResend.get(caseId).getHearings();
    }

    public Set<Long> getAllCaseIds() {
        return this.casesToResend.keySet();
    }

}

@Data
class ResendData {
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yyyy", timezone="GMT")
    private List<LocalDate> orders;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yyyy HH:mm", timezone="GMT")
    private List<LocalDateTime> hearings;
}
