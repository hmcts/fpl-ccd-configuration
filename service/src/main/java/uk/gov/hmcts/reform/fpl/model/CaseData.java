package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude
public class CaseData {
    private final OldChildren children;
    private final List<Element<Child>> children1;
    private final String childrenMigrated;

    public CaseData(OldChildren children, List<Element<Child>> children1, String childrenMigrated) {
        this.children = children;
        this.children1 = children1;
        this.childrenMigrated = childrenMigrated;
    }
}
