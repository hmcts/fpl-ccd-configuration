package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;
import uk.gov.hmcts.reform.fpl.service.ApplicantLocalAuthorityService;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@RestController
@RequestMapping("/callback/enter-local-authority")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicantLocalAuthorityController extends CallbackController {

    private static final String LOCAL_AUTHORITY = "localAuthority";
    private static final String LOCAL_AUTHORITIES = "localAuthorities";
    private static final String COLLEAGUES = "localAuthorityColleagues";
    private static final String COLLEAGUES_LIST = "localAuthorityColleaguesList";
    private static final String SHOW_MAIN_CONTACT = "localAuthorityMainContactShown";

    private final ApplicantLocalAuthorityService applicantLocalAuthorityService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final LocalAuthority localAuthority = applicantLocalAuthorityService.getUserLocalAuthority(caseData);

        // Only do these checks when not in open/returned states
        if (!List.of(OPEN, RETURNED).contains(caseData.getState())
            && !applicantLocalAuthorityService.isApplicantOrOnBehalfOfOrgId(localAuthority.getId(), caseData)) {
            // user is not operating on behalf of the applicant - it's likely a respondent solicitor on a 3rd party
            // case (both actual applicant + respondent get [SOLICITORA] roles so can't do this via event permissions
            return respond(caseDetails,
                List.of("You must be the applicant or acting on behalf of the applicant to modify these details."));
        }

        caseDetails.getData().put(LOCAL_AUTHORITY, localAuthority);
        caseDetails.getData().put(COLLEAGUES, localAuthority.getColleagues());

        return respond(caseDetails);
    }

    @PostMapping("/organisation/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleOrganisationMidEvent(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final LocalAuthority localAuthority = caseData.getLocalAuthorityEventData().getLocalAuthority();

        applicantLocalAuthorityService.normalisePba(localAuthority);

        final List<String> errors = applicantLocalAuthorityService.validateLocalAuthority(localAuthority);

        if (isNotEmpty(errors)) {
            return respond(caseDetails, errors);
        }

        caseDetails.getData().put(LOCAL_AUTHORITY, localAuthority);

        return respond(caseDetails);
    }

    @PostMapping("/colleagues/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleColleaguesMidEvent(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final LocalAuthorityEventData eventData = caseData.getLocalAuthorityEventData();
        final List<Element<Colleague>> colleagues = eventData.getLocalAuthorityColleagues();

        List<String> errors = applicantLocalAuthorityService.validateColleagues(colleagues);

        if (isNotEmpty(errors)) {
            return respond(caseDetails, errors);
        }

        caseDetails.getData().put(SHOW_MAIN_CONTACT, YesNo.from(colleagues.size() > 1).getValue());
        caseDetails.getData().put(COLLEAGUES_LIST, applicantLocalAuthorityService.buildContactsList(colleagues));
        caseDetails.getData().put(COLLEAGUES, colleagues);

        return respond(caseDetails);
    }

    @PostMapping("/main-contact/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMainContactMidEvent(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final LocalAuthorityEventData eventData = caseData.getLocalAuthorityEventData();

        caseDetails.getData().put(COLLEAGUES, applicantLocalAuthorityService.updateMainContact(eventData));

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final LocalAuthorityEventData eventData = caseData.getLocalAuthorityEventData();

        caseDetails.getData().put(LOCAL_AUTHORITIES, applicantLocalAuthorityService.save(caseData, eventData));

        removeTemporaryFields(caseDetails, LocalAuthorityEventData.class);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {

        final CaseData caseData = getCaseData(request);
        final CaseData caseDataBefore = getCaseDataBefore(request);

        if (caseData.getState() == OPEN) {
            publishEvent(new CaseDataChanged(caseData));
        } else {
            publishEvent(new AfterSubmissionCaseDataUpdated(caseData, caseDataBefore));
        }
    }
}
