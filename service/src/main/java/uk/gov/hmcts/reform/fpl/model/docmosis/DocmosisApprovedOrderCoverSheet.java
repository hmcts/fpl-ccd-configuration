package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode()
@Builder
public class DocmosisApprovedOrderCoverSheet implements DocmosisData {
    private final String familyManCaseNumber;
    private final String courtName;
    private final List<DocmosisChild> children;
    private final String judgeTitleAndName;
    private final String dateOfApproval;
    private final String orderByConsent;
    private final String crest;
}
