package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

@Slf4j
@Service
public class CaseManagementOrderEmailContentProvider extends AbstractEmailContentProvider {
    private final ObjectMapper objectMapper;

    protected CaseManagementOrderEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                                      DateFormatterService dateFormatterService,
                                                      HearingBookingService hearingBookingService,
                                                      ObjectMapper objectMapper) {
        super(uiBaseUrl, dateFormatterService, hearingBookingService);
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> buildCMOIssuedCaseLinkNotificationParameters(final CaseDetails caseDetails,
                                                                            final String recipientName) {
        return ImmutableMap.<String, Object>builder()
            .putAll(buildCommonCMONotificationParameters(caseDetails))
            .put("localAuthorityNameOrRepresentativeFullName", recipientName)
            .build();
    }

    public Map<String, Object> buildCMOIssuedDocumentLinkNotificationParameters(final CaseDetails caseDetails,
                                                                                final String recipientName,
                                                                                final byte[] documentContents) {

        return ImmutableMap.<String, Object>builder()
            .putAll(buildCommonCMONotificationParameters(caseDetails))
            .putAll(linkToAttachedDocument(documentContents))
            .put("cafcassOrRespondentName", recipientName)
            .build();
    }

    public Map<String, Object> buildCMORejectedByJudgeNotificationParameters(final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        return ImmutableMap.<String, Object>builder()
            .putAll(buildCommonCMONotificationParameters(caseDetails))
            .put("requestedChanges", caseData.getCaseManagementOrder().getAction().getChangeRequestedByJudge())
            .build();
    }

    public Map<String, Object> buildCMOPartyReviewParameters(final CaseDetails caseDetails,
                                                             byte[] documentContents,
                                                             RepresentativeServingPreferences servingPreference) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        final String subjectLine = buildSubjectLine(caseData);

        return ImmutableMap.<String, Object>builder()
            .put("subjectLineWithHearingDate", buildSubjectLineWithHearingBookingDateSuffix(subjectLine,
                caseData.getHearingDetails()))
            .put("respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()))
            .put("digitalPreference", servingPreference == DIGITAL_SERVICE ? "Yes" : "No")
            .put("caseUrl", servingPreference == DIGITAL_SERVICE ? formatCaseUrl(uiBaseUrl, caseDetails.getId()) : "")
            .putAll(linkToAttachedDocument(documentContents))
            .build();
    }

    public Map<String, Object> buildCMOReadyForJudgeReviewNotificationParameters(final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        return ImmutableMap.<String, Object>builder()
            .putAll(buildCommonCMONotificationParameters(caseDetails))
            .put("respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()))
            .put("judgeTitle", caseData.getAllocatedJudge().getJudgeOrMagistrateTitle())
            .put("judgeName", caseData.getAllocatedJudge().getJudgeName())
            .build();
    }

    private Map<String, Object> buildCommonCMONotificationParameters(final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        final String subjectLine = buildSubjectLine(caseData);

        return ImmutableMap.of(
            "subjectLineWithHearingDate", buildSubjectLineWithHearingBookingDateSuffix(subjectLine,
                caseData.getHearingDetails()),
            "reference", String.valueOf(caseDetails.getId()),
            "caseUrl", formatCaseUrl(uiBaseUrl, caseDetails.getId())
        );
    }

    private Map<String, Object> linkToAttachedDocument(final byte[] documentContents) {

        ImmutableMap.Builder<String, Object> url = ImmutableMap.builder();

        try {
            url.put("link_to_document", prepareUpload(documentContents));
        } catch (NotificationClientException e) {
            log.error("Unable to send notification for cafcass due to ", e);
        }

        return url.build();
    }
}
