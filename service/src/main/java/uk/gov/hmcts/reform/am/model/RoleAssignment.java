package uk.gov.hmcts.reform.am.model;

import lombok.Builder;
import lombok.Data;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class RoleAssignment {

    @Builder.Default
    private String actorIdType = "IDAM";

    @Builder.Default
    private Map<String, Object> attributes = Map.of();

    @Builder.Default
    private List<String> authorisations = List.of();

    @Builder.Default
    private List<String> notes = List.of();

    @Builder.Default
    private DateTime beginTime = DateTime.now();

    @Builder.Default
    private DateTime endTime = DateTime.now().plusYears(10);

    @Builder.Default
    private DateTime created = DateTime.now();

    @Builder.Default
    private String status = "CREATE_REQUESTED";

    @Builder.Default
    private String classification = "PUBLIC";

    private String actorId;
    private GrantType grantType;
    private RoleCategory roleCategory;
    private String roleName;
    private RoleType roleType;
    private boolean readOnly;



}
