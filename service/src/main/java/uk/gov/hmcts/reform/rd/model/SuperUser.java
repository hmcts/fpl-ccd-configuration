package uk.gov.hmcts.reform.rd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperUser {
    private String firstName;
    private String lastName;
    private String email;
}
