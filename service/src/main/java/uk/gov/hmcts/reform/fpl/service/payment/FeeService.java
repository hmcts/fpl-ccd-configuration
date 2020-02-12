package uk.gov.hmcts.reform.fpl.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig;
import uk.gov.hmcts.reform.fpl.model.payment.fee.FeeParameters;
import uk.gov.hmcts.reform.fpl.model.payment.fee.FeeResponse;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeType;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeeService {

    private final RestTemplate restTemplate;
    private final FeesConfig feesConfig;

    public FeeResponse calculateFeeToUse(List<FeeResponse> feeResponses) {
        return feeResponses.stream().max(Comparator.comparing(FeeResponse::getAmount)).orElseThrow();
    }

    public List<FeeResponse> getFees(List<FeeType> feeTypes) {
        return feeTypes.stream()
            .map(feeType -> makeRequest(buildUri(feeType)))
            .collect(Collectors.toList());
    }

    private FeeResponse makeRequest(URI uri) {
        log.info("Making request to Fee Register, FeeResponse API uri : {} ", uri);
        ResponseEntity<FeeResponse> response = restTemplate.getForEntity(uri, FeeResponse.class);
        log.info("Fee response : {} ", response);
        return response.getBody();
    }

    public URI buildUri(FeeType feeType) {
        FeeParameters parameters = feesConfig.getFeeParameters(feeType);
        return fromHttpUrl(feesConfig.getUrl() + feesConfig.getApi())
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
