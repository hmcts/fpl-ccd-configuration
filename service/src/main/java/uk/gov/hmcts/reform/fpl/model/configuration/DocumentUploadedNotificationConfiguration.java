package uk.gov.hmcts.reform.fpl.model.configuration;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;

@Getter
@Builder
public class DocumentUploadedNotificationConfiguration {
    private ConfidentialLevel sendToCafcassEngland = ConfidentialLevel.LA;
    private ConfidentialLevel sendToCafcassWelsh = ConfidentialLevel.LA;

    private ConfidentialLevel sendToRespondentSolicitor = ConfidentialLevel.NON_CONFIDENTIAL;
    private ConfidentialLevel sendToChildSolicitor = ConfidentialLevel.NON_CONFIDENTIAL;

    private ConfidentialLevel sendToDesignatedLA = ConfidentialLevel.LA;
    private ConfidentialLevel sendToSecondaryLA = ConfidentialLevel.LA;

    private ConfidentialLevel sendToJudge = ConfidentialLevel.CTSC;
    private ConfidentialLevel sendToCTSC = ConfidentialLevel.CTSC;

    private ConfidentialLevel sendToCafcassRepresentative = ConfidentialLevel.LA;
    private ConfidentialLevel sendToLegalRepresentative = ConfidentialLevel.NON_CONFIDENTIAL;

    private ConfidentialLevel sendToOthersSelected;

    private ConfidentialLevel sendToTranslationTeam = ConfidentialLevel.CTSC;


}
