package uk.gov.hmcts.reform.fpl.model.configuration;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider;

/**
 * Confidential level configuration for document uploaded notification
 * e.g. sendToCafcassEngland
 * If set to LA, only send notification to cafcass england when a LA level or non-confidential level document is
 * uploaded. If null, no notification will be sent to cafcass england.
 */
@Getter
@Builder(toBuilder = true)
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

    private CafcassRequestEmailContentProvider cafcassRequestEmailContentProvider;

    public static final DocumentUploadedNotificationConfiguration DEFAULT_NOTIFICATION_CONFIG =
        DocumentUploadedNotificationConfiguration.builder()
            .sendToCafcassEngland(ConfidentialLevel.LA)
            .sendToRespondentSolicitor(ConfidentialLevel.NON_CONFIDENTIAL)
            .sendToChildSolicitor(ConfidentialLevel.NON_CONFIDENTIAL)
            .sendToDesignatedLA(ConfidentialLevel.LA)
            .sendToSecondaryLA(ConfidentialLevel.LA)
            .sendToCafcassRepresentative(ConfidentialLevel.LA)
            .sendToLegalRepresentative(ConfidentialLevel.LA)
            .sendToTranslationTeam(ConfidentialLevel.CTSC)
            .cafcassRequestEmailContentProvider(CafcassRequestEmailContentProvider.NEW_DOCUMENT)
            .build();

    public static final DocumentUploadedNotificationConfiguration NO_TRANSLATION_NOTIFICATION_CONFIG =
        DEFAULT_NOTIFICATION_CONFIG.toBuilder()
            .sendToTranslationTeam(null)
            .build();

    public static final DocumentUploadedNotificationConfiguration NO_CAFCASS_NOTIFICATION_CONFIG =
        DEFAULT_NOTIFICATION_CONFIG.toBuilder()
            .sendToCafcassEngland(null)
            .sendToCafcassWelsh(null)
            .build();

    public static final DocumentUploadedNotificationConfiguration COURT_BUNDLE_NOTIFICATION_CONFIG =
        DEFAULT_NOTIFICATION_CONFIG.toBuilder()
            .cafcassRequestEmailContentProvider(CafcassRequestEmailContentProvider.COURT_BUNDLE)
            .build();

    public static final DocumentUploadedNotificationConfiguration CASE_SUMMARY_NOTIFICATION_CONFIG =
        DEFAULT_NOTIFICATION_CONFIG.toBuilder()
            .cafcassRequestEmailContentProvider(CafcassRequestEmailContentProvider.CASE_SUMMARY)
            .build();

    public static final DocumentUploadedNotificationConfiguration POSITION_STATEMENT_NOTIFICATION_CONFIG =
        DEFAULT_NOTIFICATION_CONFIG.toBuilder()
            .cafcassRequestEmailContentProvider(CafcassRequestEmailContentProvider.POSITION_STATEMENT)
            .build();

    public static final DocumentUploadedNotificationConfiguration SKELETON_ARGUMENT_NOTIFICATION_CONFIG =
        DEFAULT_NOTIFICATION_CONFIG.toBuilder()
            .cafcassRequestEmailContentProvider(CafcassRequestEmailContentProvider.SKELETON_ARGUMENT)
            .build();

    public static final DocumentUploadedNotificationConfiguration COURT_CORRESPONDENCE_NOTIFICATION_CONFIG =
        DocumentUploadedNotificationConfiguration.builder()
            .sendToCafcassEngland(ConfidentialLevel.LA)
            .cafcassRequestEmailContentProvider(CafcassRequestEmailContentProvider.NEW_DOCUMENT)
            .build();

    public static final DocumentUploadedNotificationConfiguration ADDITIONAL_APPLCIATION_NOTIFICAITON_CONFIG =
        DocumentUploadedNotificationConfiguration.builder()
            .sendToCafcassEngland(ConfidentialLevel.LA)
            .cafcassRequestEmailContentProvider(CafcassRequestEmailContentProvider.NEW_DOCUMENT)
            .build();

    public static final DocumentUploadedNotificationConfiguration NOTICE_OF_ACTING_OR_ISSUE_NOTIFICATION_CONFIG =
        NO_CAFCASS_NOTIFICATION_CONFIG.toBuilder()
            .sendToCafcassRepresentative(null)
            .build();

    public static final DocumentUploadedNotificationConfiguration CAFCASS_API_NOTIFICATION_CONFIG =
        DEFAULT_NOTIFICATION_CONFIG.builder()
            .sendToCafcassEngland(null)
            .sendToRespondentSolicitor(ConfidentialLevel.NON_CONFIDENTIAL)
            .sendToChildSolicitor(ConfidentialLevel.NON_CONFIDENTIAL)
            .sendToDesignatedLA(ConfidentialLevel.LA)
            .sendToSecondaryLA(ConfidentialLevel.LA)
            .sendToCafcassRepresentative(null)
            .sendToLegalRepresentative(ConfidentialLevel.LA)
            .sendToTranslationTeam(ConfidentialLevel.CTSC)
            .cafcassRequestEmailContentProvider(CafcassRequestEmailContentProvider.NEW_DOCUMENT)
            .build();
}
