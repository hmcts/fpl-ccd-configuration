package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForCMO;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderEmailContentProvider extends AbstractEmailContentProvider {

    private final Time time;

    private static final String CASE_URL = "caseUrl";
    private static final String SUBJECT_LINE = "subjectLineWithHearingDate";
    private static final String REFERENCE = "reference";
    private static final String RESPONDENT_LAST_NAME = "respondentLastName";
    private static final String DIGITAL_PREFERENCE = "digitalPreference";

    public Map<String, Object> buildCMOIssuedCaseLinkNotificationParameters(CaseData caseData, String recipientName) {
        return ImmutableMap.<String, Object>builder()
            .putAll(buildCommonCMONotificationParameters(caseData))
            .put("localAuthorityNameOrRepresentativeFullName", recipientName)
            .build();
    }

    public IssuedCMOTemplate buildCMOIssuedNotificationParameters(CaseData caseData, CaseManagementOrder cmo,
                                                                  RepresentativeServingPreferences servingPreference) {

        IssuedCMOTemplate template = new IssuedCMOTemplate();

        template.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        template.setFamilyManCaseNumber(caseData.getFamilyManCaseNumber());
        template.setHearing(uncapitalize(cmo.getHearing()));
        template.setDigitalPreference(hasDigitalServingPreference(servingPreference) ? "Yes" : "No");
        template.setDocumentLink(linkToAttachedDocument(cmo.getOrder()));
        template.setCaseUrl((hasDigitalServingPreference(servingPreference) ? getCaseUrl(caseData.getId()) : ""));

        return template;
    }

    public RejectedCMOTemplate buildCMORejectedByJudgeNotificationParameters(final CaseData caseData,
                                                                             CaseManagementOrder cmo) {
        RejectedCMOTemplate template = new RejectedCMOTemplate();

        template.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        template.setFamilyManCaseNumber(caseData.getFamilyManCaseNumber());
        template.setHearing(uncapitalize(cmo.getHearing()));
        template.setCaseUrl(getCaseUrl(caseData.getId()));
        template.setRequestedChanges(cmo.getRequestedChanges());

        return template;
    }

    public Map<String, Object> buildCMOPartyReviewParameters(final CaseData caseData,
                                                             byte[] documentContents,
                                                             RepresentativeServingPreferences servingPreference) {

        return ImmutableMap.<String, Object>builder()
            .put(SUBJECT_LINE, buildCallout(caseData))
            .put(RESPONDENT_LAST_NAME, getFirstRespondentLastName(caseData.getRespondents1()))
            .put(DIGITAL_PREFERENCE, servingPreference == DIGITAL_SERVICE ? "Yes" : "No")
            .put(CASE_URL, servingPreference == DIGITAL_SERVICE ? getCaseUrl(caseData.getId()) : "")
            .putAll(linkToAttachedDocument(documentContents))
            .build();
    }

    private String buildCallout(final CaseData caseData) {
        HearingBooking hearing = null;
        if (caseData.hasFutureHearing(caseData.getHearingDetails())) {
            hearing = caseData.getMostUrgentHearingBookingAfter(time.now());
        }
        return buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(),
            hearing);
    }

    public AllocatedJudgeTemplateForCMO buildCMOReadyForJudgeReviewNotificationParameters(
        final CaseData caseData) {
        Map<String, Object> commonCMONotificationParameters = buildCommonCMONotificationParameters(caseData);

        AllocatedJudgeTemplateForCMO allocatedJudgeTemplate
            = new AllocatedJudgeTemplateForCMO();
        allocatedJudgeTemplate.setSubjectLineWithHearingDate(commonCMONotificationParameters
            .get(SUBJECT_LINE)
            .toString());
        allocatedJudgeTemplate.setCaseUrl(commonCMONotificationParameters.get(CASE_URL).toString());
        allocatedJudgeTemplate.setReference(commonCMONotificationParameters.get(REFERENCE).toString());
        allocatedJudgeTemplate.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        allocatedJudgeTemplate.setJudgeTitle(caseData.getAllocatedJudge().getJudgeOrMagistrateTitle());
        allocatedJudgeTemplate.setJudgeName(caseData.getAllocatedJudge().getJudgeName());

        return allocatedJudgeTemplate;
    }

    private Map<String, Object> buildCommonCMONotificationParameters(final CaseData caseData) {

        return ImmutableMap.of(
            SUBJECT_LINE, buildCallout(caseData),
            REFERENCE, String.valueOf(caseData.getId()),
            CASE_URL, getCaseUrl(caseData.getId())
        );
    }

    private Map<String, Object> linkToAttachedDocument(final byte[] documentContents) {
        ImmutableMap.Builder<String, Object> url = ImmutableMap.builder();

        generateAttachedDocumentLink(documentContents).ifPresent(
            attachedDocumentLink -> url.put("link_to_document", attachedDocumentLink));

        return url.build();
    }

    private boolean hasDigitalServingPreference(RepresentativeServingPreferences servingPreference) {
        return servingPreference == DIGITAL_SERVICE;
    }
}
