package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;

import java.util.List;

import static java.lang.String.format;

@Data
@Builder(toBuilder = true)
public class C2DocumentBundle {
    private final C2ApplicationType type;
    private final String nameOfRepresentative;
    private final String usePbaPayment;
    private final String pbaNumber;
    private final String clientCode;
    private final String fileReference;
    private final DocumentReference document;
    private final String description;
    private final String uploadedDateTime;
    private final String author;
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;

    public String toLabel(String applicationIndex) {
        return format("Application %s: %s", applicationIndex, uploadedDateTime);
    }
}
