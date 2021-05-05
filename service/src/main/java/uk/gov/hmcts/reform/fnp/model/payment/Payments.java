package uk.gov.hmcts.reform.fnp.model.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
@Builder
public class Payments {
    private List<Payment> payments;
}
