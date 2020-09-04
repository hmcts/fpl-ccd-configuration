package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForCMO;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CmoNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;

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

    public CmoNotifyData buildCMOIssuedCaseLinkNotificationParameters(CaseData caseData, String recipientName) {

        return CmoNotifyData.builder()
            .subjectLineWithHearingDate(buildCallout(caseData))
            .reference(String.valueOf(caseData.getId()))
            .caseUrl(getCaseUrl(caseData.getId()))
            .localAuthorityNameOrRepresentativeFullName(recipientName)
            .build();
    }

    public IssuedCMOTemplate getCMOIssuedNotifyData(CaseData caseData, CaseManagementOrder cmo,
                                                    RepresentativeServingPreferences servingPreference) {

        IssuedCMOTemplate template = new IssuedCMOTemplate();

        template.setRespondentLastName(getFirstRespondentLastName(caseData));
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

        template.setRespondentLastName(getFirstRespondentLastName(caseData));
        template.setFamilyManCaseNumber(caseData.getFamilyManCaseNumber());
        template.setHearing(uncapitalize(cmo.getHearing()));
        template.setCaseUrl(getCaseUrl(caseData.getId()));
        template.setRequestedChanges(cmo.getRequestedChanges());

        return template;
    }

    public CmoNotifyData buildCMOPartyReviewParameters(final CaseData caseData,
                                                       byte[] documentContents,
                                                       RepresentativeServingPreferences servingPreference) {

        return CmoNotifyData.builder()
            .subjectLineWithHearingDate(buildCallout(caseData))
            .digitalPreference(servingPreference == DIGITAL_SERVICE ? "Yes" : "No")
            .caseUrl(servingPreference == DIGITAL_SERVICE ? getCaseUrl(caseData.getId()) : "")
            .respondentLastName(getFirstRespondentLastName(caseData))
            .documentLink(generateAttachedDocumentLink(documentContents)
                .map(JSONObject::toMap)
                .orElse(null))
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

    public AllocatedJudgeTemplateForCMO buildCMOReadyForJudgeReviewNotificationParameters(CaseData caseData) {

        return AllocatedJudgeTemplateForCMO.builder()
            .subjectLineWithHearingDate(buildCallout(caseData))
            .caseUrl(getCaseUrl(caseData.getId()))
            .reference(caseData.getId().toString())
            .respondentLastName(getFirstRespondentLastName(caseData))
            .judgeTitle(caseData.getAllocatedJudge().getJudgeOrMagistrateTitle())
            .judgeName(caseData.getAllocatedJudge().getJudgeName())
            .build();
    }

    private boolean hasDigitalServingPreference(RepresentativeServingPreferences servingPreference) {
        return servingPreference == DIGITAL_SERVICE;
    }
}
