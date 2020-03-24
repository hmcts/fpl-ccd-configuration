package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.formatRepresentativesForPostNotification;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

@Slf4j
@Service
public class OrderIssuedEmailContentProvider extends AbstractEmailContentProvider {
    private final HmctsCourtLookupConfiguration config;
    private final RepresentativeService service;

    @Autowired
    protected OrderIssuedEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                           HmctsCourtLookupConfiguration config,
                                           RepresentativeService service) {
        super(uiBaseUrl);
        this.service = service;
        this.config = config;
    }

    public Map<String, Object> buildNotificationParametersForHmctsAdmin(final CaseDetails caseDetails,
                                                                        final String localAuthorityCode,
                                                                        final byte[] documentContents,
                                                                        final IssuedOrderType issuedOrderType) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        List<Representative> representativesServedByPost = service.getRepresentativesByServedPreference(
            caseData.getRepresentatives(), POST);
        List<String> formattedRepresentatives = formatRepresentativesForPostNotification(representativesServedByPost);

        return ImmutableMap.<String, Object>builder()
            .put("callout", (issuedOrderType != NOTICE_OF_PLACEMENT_ORDER) ? buildCallout(caseData) : "")
            .put("needsPosting", isNotEmpty(representativesServedByPost) ? "Yes" : "No")
            .put("doesNotNeedPosting", representativesServedByPost.isEmpty() ? "Yes" : "No")
            .put("courtName", config.getCourt(localAuthorityCode).getName())
            .putAll(caseUrlOrDocumentLink(isNotEmpty(representativesServedByPost), documentContents,
                caseDetails.getId()))
            .put("respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()))
            .put("representatives", formattedRepresentatives.isEmpty() ? "" : formattedRepresentatives)
            .build();
    }

    public Map<String, Object> buildNotificationParametersForRepresentatives(final CaseDetails caseDetails,
                                                                             final String localAuthorityCode,
                                                                             final byte[] documentContents,
                                                                             final IssuedOrderType issuedOrderType) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return ImmutableMap.<String, Object>builder()
            .put("orderType", getTypeOfOrder(caseData, issuedOrderType))
            .put("callout", (issuedOrderType != NOTICE_OF_PLACEMENT_ORDER) ? buildCallout(caseData) : "")
            .put("courtName", config.getCourt(localAuthorityCode).getName())
            .putAll(linkToAttachedDocument(documentContents))
            .put("respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()))
            .build();
    }

    private String buildCallout(CaseData caseData) {
        return "^" + buildSubjectLineWithHearingBookingDateSuffix(buildSubjectLine(caseData),
            caseData.getHearingDetails());
    }

    private String getTypeOfOrder(CaseData caseData, IssuedOrderType issuedOrderType) {
        String orderType;
        if (issuedOrderType == GENERATED_ORDER) {
            orderType = Iterables.getLast(caseData.getOrderCollection()).getValue().getType();
        } else {
            orderType = issuedOrderType.getLabel();
        }

        return orderType.toLowerCase();
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

    private Map<String, Object> linkToAttachedDocument(final byte[] documentContents) {
        ImmutableMap.Builder<String, Object> url = ImmutableMap.builder();

        try {
            url.put("documentLink", prepareUpload(documentContents));
        } catch (NotificationClientException e) {
            log.error("Unable to send notification due to ", e);
        }

        return url.build();
    }
}
