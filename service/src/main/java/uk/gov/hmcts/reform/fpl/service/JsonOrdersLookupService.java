package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import java.io.IOException;

import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@Service
public class JsonOrdersLookupService implements OrdersLookupService {
    private final ObjectMapper objectMapper;

    @Autowired
    public JsonOrdersLookupService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OrderDefinition getStandardDirectionOrder() throws IOException {
        String content = readString("ordersConfig.json");

        return this.objectMapper.readValue(content, OrderDefinition.class);
    }

    public OrderDefinition getStandardDirectionOrderNoSingleList() throws IOException {
        String content = readString("ordersConfig.json");

        return this.objectMapper.readValue(content, OrderDefinition.class);
    }

    public OrderDefinition getStandardDirectionOrderNoCollections() throws IOException {
        String content = readString("ordersConfig.json");

        return this.objectMapper.readValue(content, OrderDefinition.class);
    }
}
