package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeThirdPartyEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.service.CaseAssignmentService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfChangeService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.legalcounsel.RepresentableLegalCounselUpdater;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@RestController
@RequestMapping("/callback/noc-decision")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NoticeOfChangeController extends CallbackController {

    private final CaseAssignmentService caseAssignmentService;
    private final NoticeOfChangeService noticeOfChangeService;
    private final RespondentService respondentService;
    private final RepresentableLegalCounselUpdater legalCounselUpdater;
    private final LocalAuthorityService localAuthorityService;
    private final UserService userService;

    @SuppressWarnings("unchecked")
    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        // deep copy of the original case data to ensure that we preserve the original
        // in about-to-start caseDetailsBefore is null, this makes sense as this is the first callback that can be
        // hit so there wouldn't be any difference in caseDetails and caseDetailsBefore
        CaseData originalCaseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(noticeOfChangeService.updateRepresentation(caseData));

        caseData = getCaseData(caseDetails);

        ChangeOrganisationRequest nocRequest = caseData.getChangeOrganisationRequestField();

        Map<String, Object> changeOrgRequestField = (Map<String, Object>) caseDetails.getData().get(
            "changeOrganisationRequestField");

        if (noticeOfChangeService.isThirdPartyOutsourcing(caseData.getChangeOrganisationRequestField())) {
            caseDetails.getData().putAll(localAuthorityService.updateLocalAuthorityFromNoC(caseData, nocRequest,
                (String) changeOrgRequestField.get("CreatedBy")));
        } else {
            caseDetails.getData().putAll(legalCounselUpdater.updateLegalCounselFromNoC(caseData, originalCaseData));
        }

        return caseAssignmentService.applyDecision(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData oldCaseData = getCaseDataBefore(callbackRequest);
        CaseData newCaseData = getCaseData(callbackRequest);

        ChangeOrganisationRequest changeOrganisationRequest =  oldCaseData.getChangeOrganisationRequestField();

        if (noticeOfChangeService.isThirdPartyOutsourcing(changeOrganisationRequest)) {
            publishEventsForThirdPartyOutsourcingNoC(oldCaseData, newCaseData);
        } else {
            publishNoCEventsForRespondentOrChildSolicitor(newCaseData, oldCaseData);
        }
    }

    private void publishEventsForThirdPartyOutsourcingNoC(CaseData oldData, CaseData newData) {
        String newOrgId = newData.getOutsourcingPolicy().getOrganisation().getOrganisationID();
        String previousOrgId = oldData.getOutsourcingPolicy().getOrganisation().getOrganisationID();

        LocalAuthority oldThirdPartyOrg = oldData.getLocalAuthorities().stream().filter(la ->
            la.getValue().getId().equals(previousOrgId)).findFirst().orElseThrow().getValue();
        LocalAuthority newThirdPartyOrg = newData.getLocalAuthorities().stream().filter(la ->
            la.getValue().getId().equals(newOrgId)).findFirst().orElseThrow().getValue();

        publishEvent(new NoticeOfChangeThirdPartyEvent(oldThirdPartyOrg, newThirdPartyOrg, newData));
    }

    private void publishNoCEventsForRespondentOrChildSolicitor(CaseData newCaseData, CaseData oldCaseData) {
        Stream.of(SolicitorRole.Representing.values())
            .flatMap(role -> legalCounselUpdater.buildEventsForAccessRemoval(newCaseData, oldCaseData, role).stream())
            .forEach(this::publishEvent);

        Stream.of(SolicitorRole.Representing.values())
            .flatMap(solicitorRole ->
                respondentService.getRepresentationChanges(
                    solicitorRole.getTarget().apply(newCaseData),
                    solicitorRole.getTarget().apply(oldCaseData),
                    solicitorRole
                ).stream())
            .forEach(
                changeRequest -> {
                    SolicitorRole caseRole = changeRequest.getCaseRole();
                    Function<CaseData, List<Element<WithSolicitor>>> target = caseRole.getRepresenting().getTarget();
                    int solicitorIndex = caseRole.getIndex();
                    publishEvent(new NoticeOfChangeEvent(
                        newCaseData,
                        target.apply(oldCaseData).get(solicitorIndex).getValue(),
                        target.apply(newCaseData).get(solicitorIndex).getValue())
                    );
                }
            );
    }

    @PostMapping("/update-respondents/about-to-start")
    public CallbackResponse handleRespondentUpdate(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        AboutToStartOrSubmitCallbackResponse aacResponse = caseAssignmentService.applyDecision(caseDetails);
        if (!isEmpty(aacResponse.getErrors())) {
            log.error(aacResponse.getErrors().stream().collect(Collectors.joining(", ")));
        }
        return aacResponse;
    }

    @PostMapping("/update-respondents/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        // clean up after the NoC decision
        caseDetails.getData().remove("changeOrganisationRequestField");

        return respond(caseDetails);
    }
}
