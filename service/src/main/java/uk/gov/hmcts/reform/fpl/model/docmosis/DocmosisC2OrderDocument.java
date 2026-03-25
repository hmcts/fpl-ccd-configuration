package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode()
@Builder
public class DocmosisC2OrderDocument implements DocmosisData {
    private final String crest;
    private final String courtName;
    private final String caseNumber;
    private final String dateIssued;
    private final String feeCharged;
    private final String applicantName;
    private final String respondents;
    private final String consent;
    private final String isConfidential;
    private final String permission;
    private final String applicationRelatesToAllChildren;
    private final String childrenOnApplication;
    private final String applicationSummary;
    private final String safeguarding;
    private final String safeguardingReason;
    private final String requestAdjournment;
    private final String whichHearing;
    private final String considerAtNextHearing;
}
