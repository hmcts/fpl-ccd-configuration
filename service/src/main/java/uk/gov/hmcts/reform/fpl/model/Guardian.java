package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Guardian {
    private String guardianName;
    private String telephoneNumber;
    private String email;
    private List<String> children;
}
