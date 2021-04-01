package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public interface Recipient {
    String getFullName();

    Address getAddress();

    @JsonIgnore
    default boolean isDeliverable() {
        return isNotBlank(getFullName())
            && isNotEmpty(getAddress())
            && isNotBlank(getAddress().getAddressLine1())
            && isNotBlank(getAddress().getPostcode());
    }
}
