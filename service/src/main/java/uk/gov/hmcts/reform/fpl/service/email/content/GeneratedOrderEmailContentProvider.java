package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;

@Service
public class GeneratedOrderEmailContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper objectMapper;
    private final HmctsCourtLookupConfiguration courtLookupConfiguration;

    public GeneratedOrderEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                              ObjectMapper objectMapper,
                                              HearingBookingService hearingBookingService,
                                              HmctsCourtLookupConfiguration courtLookupConfiguration) {
        super(uiBaseUrl, hearingBookingService);
        this.objectMapper = objectMapper;
        this.courtLookupConfiguration = courtLookupConfiguration;
    }

    public Map<String, Object> buildOrderNotificationParameters(final CaseDetails caseDetails,
                                                                final String localAuthorityCode,
                                                                final byte[] documentContents) {
        ImmutableMap.Builder<String, Object> notificationParameters = ImmutableMap.builder();
        notificationParameters.putAll(commonOrderNotificationParameters(caseDetails, localAuthorityCode));

        generateAttachedDocumentLink(documentContents).ifPresent(
            attachedDocumentLink -> notificationParameters.put("attachedDocumentLink", attachedDocumentLink));

        return notificationParameters.build();
    }

    private Map<String, Object> commonOrderNotificationParameters(final CaseDetails caseDetails,
                                                                  final String localAuthorityCode) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        final String subjectLine = buildSubjectLine(caseData);
        return ImmutableMap.of(
            "callOut", buildSubjectLineWithHearingBookingDateSuffix(subjectLine,
                caseData.getHearingDetails()),
            "courtName", courtLookupConfiguration.getCourt(localAuthorityCode).getName(),
            "orderType", Iterables.getLast(caseData.getOrderCollection()).getValue().getType(),
            "reference", String.valueOf(caseDetails.getId()),
            "caseUrl", formatCaseUrl(uiBaseUrl, caseDetails.getId())
        );
    }
}
