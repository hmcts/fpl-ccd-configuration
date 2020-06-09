package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@Service
public class JsonOrdersLookupService implements OrdersLookupService {
    @Value("${orders.config:ordersConfig.json}")
    private String ordersConfigFilename;
    private final ObjectMapper objectMapper;

    @Autowired
    public JsonOrdersLookupService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OrderDefinition getStandardDirectionOrder() {
        System.out.println(ordersConfigFilename);
        String content = readString(ordersConfigFilename);

        try {
            return this.objectMapper.readValue(content, OrderDefinition.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not read file " + ordersConfigFilename);
        }
    }
}
