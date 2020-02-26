package uk.gov.hmcts.reform.rd.model;

    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organisation {
    private String organisationIdentifier;
    private String name;
    private String status;
    private boolean sraRegulated;
    private SuperUser superUser;
}
