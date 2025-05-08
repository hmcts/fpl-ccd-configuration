package uk.gov.hmcts.reform.am.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class QueryRequest {

    private List<String> actorId;
    private Map<String, List<String>> attributes;
    private List<String> authorisations;
    private List<String> classification;
    private List<String> grantType;
    private List<String> hasAttributes;
    private boolean readOnly;
    private List<String> roleCategory;
    private List<String> roleName;
    private List<String> roleType;
    private LocalDateTime validAt;
}
