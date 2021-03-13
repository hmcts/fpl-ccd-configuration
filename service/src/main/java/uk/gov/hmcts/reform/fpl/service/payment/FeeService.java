package uk.gov.hmcts.reform.fpl.service.payment;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fnp.client.FeesRegisterApi;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.model.fee.FeeResponse;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;
import uk.gov.hmcts.reform.fnp.model.payment.FeeDto;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeParameters;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.C2OrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType;
import uk.gov.hmcts.reform.fpl.enums.Supplements;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromApplicationType;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromC2ApplicationType;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromC2OrdersRequestedType;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromOrderType;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromParentalResponsibilityTypes;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromSecureAccommodationTypes;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromSupplementTypes;
import static uk.gov.hmcts.reform.fpl.enums.C2OrdersRequested.PARENTAL_RESPONSIBILITY;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeeService {

    private final FeesConfig feesConfig;
    private final FeesRegisterApi feesRegisterApi;

    public FeesData getFeesDataForOrders(Orders orders) {
        return Optional.ofNullable(orders)
            .map(Orders::getOrderType)
            .map(orderTypeList -> getFees(fromOrderType(orderTypeList)))
            .map(this::buildFeesDataFromFeeResponses)
            .orElse(FeesData.builder().totalAmount(BigDecimal.ZERO).build());
    }

    public Optional<FeeResponse> extractFeeToUse(List<FeeResponse> feeResponses) {
        return ofNullable(feeResponses).stream()
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .max(Comparator.comparing(FeeResponse::getAmount));
    }

    public List<FeeResponse> getFees(List<FeeType> feeTypes) {
        return ofNullable(feeTypes).stream()
            .flatMap(Collection::stream)
            .map(this::makeRequest)
            .filter(Objects::nonNull)
            .collect(toImmutableList());
    }

    public FeesData getFeesDataForC2(C2ApplicationType c2ApplicationType) {
        FeeResponse feeResponse = makeRequest(FeeType.fromC2ApplicationType(c2ApplicationType));

        return FeesData.builder()
            .totalAmount(feeResponse.getAmount())
            .fees(List.of(FeeDto.fromFeeResponse(feeResponse)))
            .build();
    }

    public FeesData getFeesDataForAdditionalApplications(
        C2DocumentBundle c2DocumentBundle,
        OtherApplicationsBundle otherApplicationsBundle,
        List<Supplements> supplementTypes,
        List<SecureAccommodationType> secureAccommodationTypes) {

        List<FeeType> feeTypes = getFeeTypes(
            c2DocumentBundle, otherApplicationsBundle, supplementTypes, secureAccommodationTypes);

        return Optional.of(feeTypes)
            .map(this::getFees)
            .map(this::buildFeesDataFromFeeResponses)
            .orElse(FeesData.builder().totalAmount(BigDecimal.ZERO).build());
    }

    private List<FeeType> getFeeTypes(
        C2DocumentBundle c2DocumentBundle,
        OtherApplicationsBundle otherApplicationsBundle,
        List<Supplements> supplementTypes,
        List<SecureAccommodationType> secureAccommodationTypes
    ) {
        List<FeeType> feeTypes = new ArrayList<>();

        if (!isNull(c2DocumentBundle)) {
            feeTypes.addAll(getC2ApplicationsFeeTypes(c2DocumentBundle));
        }

        if (!isNull(otherApplicationsBundle)) {
            feeTypes.addAll(getOtherApplicationsFeeTypes(otherApplicationsBundle));
        }

        feeTypes.addAll(fromSupplementTypes(supplementTypes));
        feeTypes.addAll(fromSecureAccommodationTypes(secureAccommodationTypes));

        return feeTypes;
    }

    private List<FeeType> getOtherApplicationsFeeTypes(OtherApplicationsBundle applicationsBundle) {
        List<FeeType> feeTypes = new ArrayList<>();

        if (OtherApplicationType.C1_PARENTAL_RESPONSIBILITY == applicationsBundle.getApplicationType()
            && !isNull(applicationsBundle.getParentalResponsibilityType())) {
            feeTypes.add(fromParentalResponsibilityTypes(applicationsBundle.getParentalResponsibilityType()));
        } else {
            fromApplicationType(applicationsBundle.getApplicationType()).ifPresent(feeTypes::add);
        }

        return feeTypes;
    }

    private List<FeeType> getC2ApplicationsFeeTypes(C2DocumentBundle c2DocumentBundle) {
        List<FeeType> feeTypes = new ArrayList<>();

        feeTypes.add(fromC2ApplicationType(c2DocumentBundle.getType()));

        List<C2OrdersRequested> c2OrdersRequested = c2DocumentBundle.getC2OrdersRequested();

        if (isNotEmpty(c2OrdersRequested)) {
            if (c2OrdersRequested.contains(PARENTAL_RESPONSIBILITY)) {
                c2OrdersRequested.remove(PARENTAL_RESPONSIBILITY);
                feeTypes.add(fromParentalResponsibilityTypes(c2DocumentBundle.getParentalResponsibilityType()));
            }

            feeTypes.addAll(fromC2OrdersRequestedType(c2OrdersRequested));
        }
        return feeTypes;
    }

    private FeeResponse makeRequest(FeeType feeType) {
        FeeParameters parameters = feesConfig.getFeeParametersByFeeType(feeType);
        try {
            log.debug("Making request to Fee Register with parameters : {} ", parameters);

            FeeResponse fee = feesRegisterApi.findFee(
                parameters.getChannel(),
                parameters.getEvent(),
                parameters.getJurisdiction1(),
                parameters.getJurisdiction2(),
                parameters.getKeyword(),
                parameters.getService()
            );

            log.debug("Fee response: {} ", fee);

            return fee;
        } catch (FeignException ex) {
            log.error("Fee response error for {}\n\tstatus: {} => message: \"{}\"",
                parameters, ex.status(), ex.contentUTF8(), ex);

            throw new FeeRegisterException(ex.status(), ex.contentUTF8(), ex);
        }
    }

    private FeesData buildFeesDataFromFeeResponses(List<FeeResponse> feeResponses) {
        var feeDto = extractFeeToUse(feeResponses).map(FeeDto::fromFeeResponse)
            .orElse(FeeDto.builder().calculatedAmount(BigDecimal.ZERO).build());

        return FeesData.builder()
            .totalAmount(feeDto.getCalculatedAmount())
            .fees(List.of(feeDto))
            .build();
    }
}
