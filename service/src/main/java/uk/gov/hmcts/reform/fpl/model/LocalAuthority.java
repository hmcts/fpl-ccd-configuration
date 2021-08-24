package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Data
@Jacksonized
@Builder(toBuilder = true)
public class LocalAuthority {

    private final String id;
    private String name;
    private String email;
    private String phone;
    private Address address;
    private String legalTeamManager;
    private String pbaNumber;
    private String clientCode;
    private String customerReference;
    @Builder.Default
    private List<Element<Colleague>> colleagues = new ArrayList<>();
    private String designated;

    @JsonIgnore
    public Optional<Colleague> getFirstSolicitor() {
        return unwrapElements(colleagues).stream()
            .filter(colleague -> SOLICITOR.equals(colleague.getRole()))
            .findFirst();
    }

    @JsonIgnore
    public Optional<Colleague> getMainContact() {
        return unwrapElements(colleagues).stream()
            .filter(colleague -> YesNo.YES.getValue().equals(colleague.getMainContact()))
            .findFirst();
    }
}
