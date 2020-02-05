package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.formatRepresentativesForPostNotification;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

@Slf4j
@Service
public class OrderEmailContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final RepresentativeService representativeService;
    private final ObjectMapper objectMapper;

    public OrderEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                     ObjectMapper objectMapper,
                                     HearingBookingService hearingBookingService,
                                     LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                     DateFormatterService dateFormatterService,
                                     RepresentativeService representativeService) {
        super(uiBaseUrl, dateFormatterService, hearingBookingService);
        this.objectMapper = objectMapper;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.representativeService = representativeService;
    }

    public Map<String, Object> buildOrderNotificationParametersForLocalAuthority(
        final CaseDetails caseDetails, final String localAuthorityCode, final String mostRecentUploadedDocumentUrl) {
        return ImmutableMap.<String, Object>builder()
            .putAll(commonOrderNotificationParameters(caseDetails, mostRecentUploadedDocumentUrl))
            .put("localAuthorityOrCafcass",
                localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    public Map<String, Object> buildOrderNotificationParametersForHmctsAdmin(final CaseDetails caseDetails,
                                                                             final String localAuthorityCode,
                                                                             final byte[] documentContents) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        List<Representative> representativesServedByPost = representativeService.getRepresentativesByServedPreference(
            caseData.getRepresentatives(), POST);
        List<String> formattedRepresentatives = formatRepresentativesForPostNotification(representativesServedByPost);

        return ImmutableMap.<String, Object>builder()
            .put("needsPosting", !representativesServedByPost.isEmpty() ? "Yes" : "No")
            .put("doesNotNeedPosting", representativesServedByPost.isEmpty() ? "Yes" : "No")
            .put("courtName", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .putAll(caseUrlOrDocumentLink(!representativesServedByPost.isEmpty(), documentContents,
                caseDetails.getId()))
            .put("respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()))
            .put("representatives", formattedRepresentatives.isEmpty() ? "" : formattedRepresentatives)
            .build();
    }

    private Map<String, Object> commonOrderNotificationParameters(final CaseDetails caseDetails,
                                                                  final String linkToDocument) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        final String subjectLine = buildSubjectLine(caseData);
        return ImmutableMap.of(
            "subjectLine", subjectLine,
            "linkToDocument", linkToDocument,
            "hearingDetailsCallout", buildSubjectLineWithHearingBookingDateSuffix(subjectLine,
                caseData.getHearingDetails()),
            "reference", String.valueOf(caseDetails.getId()),
            "caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId()
        );
    }

    private Map<String, Object> caseUrlOrDocumentLink(boolean needsServing,
                                                      final byte[] documentContents,
                                                      Long caseId) {
        ImmutableMap.Builder<String, Object> url = ImmutableMap.builder();

        try {
            url.put("caseUrlOrDocumentLink", needsServing ? prepareUpload(documentContents)
                : formatCaseUrl(uiBaseUrl, caseId));
        } catch (NotificationClientException e) {
            log.error("Unable to send notification due to ", e);
        }

        return url.build();
    }
}
