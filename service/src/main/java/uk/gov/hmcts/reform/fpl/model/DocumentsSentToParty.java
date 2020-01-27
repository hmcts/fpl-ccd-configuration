package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentsSentToParty {
    private String partyName;
    private List<Element<DocumentSent>> documentsSentToParty;

    public void addDocument(DocumentSent documentSent) {
        this.documentsSentToParty.add(ElementUtils.element(documentSent));
    }
}
