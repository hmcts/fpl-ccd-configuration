package uk.gov.hmcts.reform.fpl.service.payment;

import com.google.common.collect.ImmutableList;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fnp.client.FeesRegisterApi;
import uk.gov.hmcts.reform.fnp.model.fee.FeeResponse;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeeService {

    private final FeesConfig feesConfig;
    private final FeesRegisterApi feesRegisterApi;

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
            .map(this::makeRequest)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toImmutableList());
    }

    private Optional<FeeResponse> makeRequest(FeeType feeType) {
        try {
            FeesConfig.FeeParameters parameters = feesConfig.getFeeParametersByFeeType(feeType);
            log.debug("Making request to Fee Register with parameters : {} ", parameters);

            FeeResponse fee = feesRegisterApi.findFee(parameters.getChannel(),
                parameters.getEvent(),
                parameters.getJurisdiction1(),
                parameters.getJurisdiction2(),
                parameters.getKeyword(),
                parameters.getService());

            log.debug("Fee response: {} ", fee);

            return Optional.of(fee);
        } catch (FeignException ex) {
            log.error("Fee response error: {} => body: \"{}\"",
                ex.status(), ex.getMessage());
        }
        return Optional.empty();
    }
}
