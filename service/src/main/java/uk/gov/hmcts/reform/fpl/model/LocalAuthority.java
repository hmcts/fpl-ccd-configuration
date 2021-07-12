package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class LocalAuthority {
    private final String name;
    private final String email;
    private final String phone;
    private final Address address;
    private final String legalTeamManager;
    private final String pbaNumber;
    private final String clientCode;
    private final String customerReference;
    private final String dx;
    private final List<Element<Colleague>> colleagues;
}
