package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Slf4j
@Service
public class OrderIssuedEmailContentProvider extends AbstractEmailContentProvider {
    private final HmctsCourtLookupConfiguration config;

    @Autowired
    protected OrderIssuedEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                              ObjectMapper mapper,
                                              HmctsCourtLookupConfiguration config) {
        super(uiBaseUrl, mapper);
        this.config = config;
    }

    public Map<String, Object> buildParametersForEmailServedRepresentatives(final CaseDetails caseDetails,
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

    //For admin, LA and digital served representatives
    public Map<String, Object> buildParametersForCaseRoleUsers(final CaseDetails caseDetails,
                                                               final String localAuthorityCode,
                                                               final byte[] documentContents,
                                                               final IssuedOrderType issuedOrderType) {
        return ImmutableMap.<String, Object>builder()
            .putAll(buildParametersForEmailServedRepresentatives(caseDetails, localAuthorityCode, documentContents,
                issuedOrderType))
            .put("caseUrl", formatCaseUrl(uiBaseUrl, caseDetails.getId()))
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

    private Map<String, Object> linkToAttachedDocument(final byte[] documentContents) {
        ImmutableMap.Builder<String, Object> url = ImmutableMap.builder();

        generateAttachedDocumentLink(documentContents).ifPresent(
            attachedDocumentLink -> url.put("documentLink", attachedDocumentLink));

        return url.build();
    }
}
