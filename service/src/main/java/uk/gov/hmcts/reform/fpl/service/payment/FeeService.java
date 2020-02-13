package uk.gov.hmcts.reform.fpl.service.payment;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig;
import uk.gov.hmcts.reform.fpl.enums.payment.FeeType;
import uk.gov.hmcts.reform.fpl.model.payment.fee.FeeParameters;
import uk.gov.hmcts.reform.fpl.model.payment.fee.FeeResponse;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeeService {

    private final RestTemplate restTemplate;
    private final FeesConfig feesConfig;

    public Optional<FeeResponse> extractFeeToUse(List<FeeResponse> feeResponses) {
        if (feeResponses == null) {
            return Optional.empty();
        }

        return feeResponses.stream()
            .filter(Objects::nonNull)
            .max(Comparator.comparing(FeeResponse::getAmount));
    }

    public List<FeeResponse> getFees(List<FeeType> feeTypes) {
        if (feeTypes == null) {
            return ImmutableList.of();
        }

        return feeTypes.stream()
            .map(feeType -> makeRequest(buildUri(feeType)))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private FeeResponse makeRequest(URI uri) {
        try {
            log.info("Making request to Fee Register with uri : {} ", uri);
            ResponseEntity<FeeResponse> response = restTemplate.getForEntity(uri, FeeResponse.class);
            log.info("Fee response : {} ", response);
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            log.error("Fee response error: {} => body: \"{}\"",
                ex.getRawStatusCode(), ex.getResponseBodyAsString());
            return null;
        }
    }

    private URI buildUri(FeeType feeType) {
        FeeParameters parameters = feesConfig.getFeeParameters(feeType);
        UriComponentsBuilder uriComponentsBuilder = fromHttpUrl(feesConfig.getUrl() + feesConfig.getApi());

        addParameterIfPresent(uriComponentsBuilder, "service", parameters.getService());
        addParameterIfPresent(uriComponentsBuilder, "jurisdiction1", parameters.getJurisdiction1());
        addParameterIfPresent(uriComponentsBuilder, "jurisdiction2", parameters.getJurisdiction2());
        addParameterIfPresent(uriComponentsBuilder, "channel", parameters.getChannel());
        addParameterIfPresent(uriComponentsBuilder, "event", parameters.getEvent());
        addParameterIfPresent(uriComponentsBuilder, "keyword", parameters.getKeyword());

        return uriComponentsBuilder.build().encode().toUri();
    }

    private void addParameterIfPresent(UriComponentsBuilder builder, String name, String parameter) {
        if (isNotEmpty(parameter)) {
            builder.queryParam(name, parameter);
        }
    }
}
