package uk.gov.hmcts.reform.fpl.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.payment.FeeConfig;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.payment.fees.FeeParameters;
import uk.gov.hmcts.reform.fpl.model.payment.fees.FeeResponse;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeeService {

    private final RestTemplate restTemplate;
    private final FeeConfig feeConfig;

    public List<FeeResponse> getFees(List<OrderType> orderTypes) {
        return orderTypes.stream()
            .map(orderType -> makeRequest(buildUri(orderType)))
            .collect(Collectors.toList());
    }

    private FeeResponse makeRequest(URI uri) {
        log.info("Making request to Fee Register, FeeResponse API uri : {} ", uri);
        ResponseEntity<FeeResponse> response = restTemplate.getForEntity(uri, FeeResponse.class);
        log.info("Fee response : {} ", response);
        return response.getBody();
    }

    private URI buildUri(OrderType orderType) {
        FeeParameters parameters = feeConfig.getFeeParameters(orderType);
        return fromHttpUrl(feeConfig.getUrl() + feeConfig.getApi())
            .queryParam("service", parameters.getService())
            .queryParam("jurisdiction1", parameters.getJurisdiction1())
            .queryParam("jurisdiction2", parameters.getJurisdiction2())
            .queryParam("channel", parameters.getChannel())
            .queryParam("event", parameters.getEvent())
            .queryParam("keyword", parameters.getKeyword())
            .build()
            .encode()
            .toUri();
    }


}
