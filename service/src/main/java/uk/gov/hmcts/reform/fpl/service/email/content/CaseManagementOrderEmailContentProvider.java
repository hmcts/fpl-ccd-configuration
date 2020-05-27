package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForCMO;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderEmailContentProvider extends AbstractEmailContentProvider {

    private final EmailNotificationHelper emailNotificationHelper;
    private final ObjectMapper mapper;

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
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return ImmutableMap.<String, Object>builder()
            .putAll(buildCommonCMONotificationParameters(caseDetails))
            .put("requestedChanges", caseData.getCaseManagementOrder().getAction().getChangeRequestedByJudge())
            .build();
    }

    public Map<String, Object> buildCMOPartyReviewParameters(final CaseDetails caseDetails,
                                                             byte[] documentContents,
                                                             RepresentativeServingPreferences servingPreference) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return ImmutableMap.<String, Object>builder()
            .put("subjectLineWithHearingDate", emailNotificationHelper
                .buildSubjectLineWithHearingBookingDateSuffix(caseData,
                    caseData.getHearingDetails()))
            .put("respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()))
            .put("digitalPreference", servingPreference == DIGITAL_SERVICE ? "Yes" : "No")
            .put("caseUrl", servingPreference == DIGITAL_SERVICE ? getCaseUrl(caseDetails.getId()) : "")
            .putAll(linkToAttachedDocument(documentContents))
            .build();
    }

    public AllocatedJudgeTemplateForCMO buildCMOReadyForJudgeReviewNotificationParameters(
        final CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        Map<String, Object> commonCMONotificationParameters = buildCommonCMONotificationParameters(caseDetails);

        AllocatedJudgeTemplateForCMO allocatedJudgeTemplate
            = new AllocatedJudgeTemplateForCMO();
        allocatedJudgeTemplate.setSubjectLineWithHearingDate(commonCMONotificationParameters
            .get("subjectLineWithHearingDate")
            .toString());
        allocatedJudgeTemplate.setCaseUrl(commonCMONotificationParameters.get("caseUrl").toString());
        allocatedJudgeTemplate.setReference(commonCMONotificationParameters.get("reference").toString());
        allocatedJudgeTemplate.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        allocatedJudgeTemplate.setJudgeTitle(caseData.getAllocatedJudge().getJudgeOrMagistrateTitle());
        allocatedJudgeTemplate.setJudgeName(caseData.getAllocatedJudge().getJudgeName());

        return allocatedJudgeTemplate;
    }

    private Map<String, Object> buildCommonCMONotificationParameters(final CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return ImmutableMap.of(
            "subjectLineWithHearingDate", emailNotificationHelper
                .buildSubjectLineWithHearingBookingDateSuffix(caseData,
                    caseData.getHearingDetails()),
            "reference", String.valueOf(caseDetails.getId()),
            "caseUrl", getCaseUrl(caseDetails.getId())
        );
    }

    private Map<String, Object> linkToAttachedDocument(final byte[] documentContents) {
        ImmutableMap.Builder<String, Object> url = ImmutableMap.builder();

        generateAttachedDocumentLink(documentContents).ifPresent(
            attachedDocumentLink -> url.put("link_to_document", attachedDocumentLink));

        return url.build();
    }
}
