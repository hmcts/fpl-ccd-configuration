package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
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
            .putAll(buildCMODocumentLinkNotificationParameters(documentContents))
            .put("cafcassOrRespondentName", recipientName)
            .build();
    }

    private Map<String, Object> buildCommonCMONotificationParameters(final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        final String subjectLine = buildSubjectLine(caseData);

        return ImmutableMap.of(
            "subjectLineWithHearingDate", buildSubjectLineWithHearingBookingDateSuffix(subjectLine,
                caseData.getHearingDetails()),
            "reference", String.valueOf(caseDetails.getId()),
            "caseUrl", String.format("%1$s/case/%2$s/%3$s/%4$s",
                uiBaseUrl, JURISDICTION, CASE_TYPE, caseDetails.getId())
        );
    }

    private Map<String, Object> buildCMODocumentLinkNotificationParameters(final byte[] documentContents) {

        ImmutableMap.Builder<String, Object> cmoNotificationParameters = ImmutableMap.builder();

        try {
            cmoNotificationParameters.put("link_to_document", prepareUpload(documentContents));
        } catch (NotificationClientException e) {
            log.error("Unable to send notification for cafcass due to ", e);
        }

        return cmoNotificationParameters.build();
    }
}
