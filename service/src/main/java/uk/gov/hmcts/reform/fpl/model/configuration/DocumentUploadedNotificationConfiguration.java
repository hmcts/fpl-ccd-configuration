package uk.gov.hmcts.reform.fpl.model.configuration;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;

/**
 * Confidential level configuration for document uploaded notification
 * e.g. sendToCafcassEngland
 * If set to LA, only send notification to cafcass england when a LA level or non-confidential level document is uploaded
 * If null, no notification will be sent cafcass england.
 */
@Getter
@Builder
public class DocumentUploadedNotificationConfiguration {
    private ConfidentialLevel sendToCafcassEngland;
    private ConfidentialLevel sendToCafcassWelsh;

    private ConfidentialLevel sendToRespondentSolicitor;
    private ConfidentialLevel sendToChildSolicitor;

    private ConfidentialLevel sendToDesignatedLA;
    private ConfidentialLevel sendToSecondaryLA;
    private ConfidentialLevel sendToLegalRepresentative;

    private ConfidentialLevel sendToJudge;
    private ConfidentialLevel sendToCTSC;

    private ConfidentialLevel sendToCafcassRepresentative;

    private ConfidentialLevel sendToOthersSelected;

    private ConfidentialLevel sendToTranslationTeam;

    public static final DocumentUploadedNotificationConfiguration DEFAULT_MANAGED_DOCUMENTS_NOTIFICATION_CONFIG =
        DocumentUploadedNotificationConfiguration.builder()
            .sendToCafcassEngland(ConfidentialLevel.LA)
            .sendToCafcassWelsh(ConfidentialLevel.LA)
            .sendToRespondentSolicitor(ConfidentialLevel.NON_CONFIDENTIAL)
            .sendToChildSolicitor(ConfidentialLevel.NON_CONFIDENTIAL)
            .sendToDesignatedLA(ConfidentialLevel.LA)
            .sendToSecondaryLA(ConfidentialLevel.LA)
            .sendToLegalRepresentative(ConfidentialLevel.LA)
            .sendToJudge(ConfidentialLevel.CTSC)
            .sendToCTSC(ConfidentialLevel.CTSC)
            .sendToCafcassRepresentative(ConfidentialLevel.LA)
            .sendToTranslationTeam(ConfidentialLevel.CTSC)
            .build();
}
