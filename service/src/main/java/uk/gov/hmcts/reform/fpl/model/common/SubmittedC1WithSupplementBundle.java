package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SubmittedC1WithSupplementBundle  {
    private final DocumentReference document;
    private final UrgencyTimeFrameType urgencyTimeFrameType;
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
    private final List<Element<Supplement>> supplementsBundle;
    private final String clearSubmittedC1WithSupplement;
    private final String isDocumentUploaded;

    public String getClearSubmittedC1WithSupplement() {
        if (clearSubmittedC1WithSupplement != null) {
            return this.clearSubmittedC1WithSupplement;
        }
        return document != null ? YesNo.NO.getValue().toUpperCase() : null;
    }

    public String getIsDocumentUploaded() {
        return YesNo.from(document != null).getValue().toUpperCase();
    }
}
