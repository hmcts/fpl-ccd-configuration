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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentLocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.OtherToRespondentEventData;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfChangeService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.RespondentAfterSubmissionRepresentationService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.legalcounsel.RepresentableLegalCounselUpdater;
import uk.gov.hmcts.reform.fpl.service.others.OthersListGenerator;
import uk.gov.hmcts.reform.fpl.service.respondent.RespondentValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.model.Respondent.expandCollection;
import static uk.gov.hmcts.reform.fpl.model.RespondentLocalAuthority.DUMMY_UUID;
import static uk.gov.hmcts.reform.fpl.model.common.Element.newElement;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Slf4j
@RestController
@RequestMapping("/callback/enter-respondents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentController extends CallbackController {

    private static final String OTHERS_LIST_KEY = "othersList";
    private static final String REPRESENTATIVES_KEY = "representatives";
    private static final String RESPONDENTS_KEY = "respondents1";
    private static final String TRANSFORMED_RESPONDENT = "transformedRespondent";
    private final OthersListGenerator othersListGenerator;
    private final OthersService othersService;
    private final RepresentativeService representativeService;
    private final ConfidentialDetailsService confidentialDetailsService;
    private final RespondentService respondentService;
    private final RespondentAfterSubmissionRepresentationService respondentAfterSubmissionRepresentationService;
    private final RespondentValidator respondentValidator;
    private final NoticeOfChangeService noticeOfChangeService;
    private final RepresentableLegalCounselUpdater representableCounselUpdater;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<Element<Respondent>> respondents = confidentialDetailsService.prepareCollection(
            caseData.getAllRespondents(), caseData.getConfidentialRespondents(), expandCollection());

        // if we have a respondent LA, remove from collection and migrate to the other field
        if (!RepresentativeType.LOCAL_AUTHORITY.equals(caseData.getRepresentativeType())
            && isNotEmpty(respondents) && respondents.get(0).getId().equals(DUMMY_UUID)) {
            Respondent fakeRespondentLA = respondents.get(0).getValue();
            RespondentLocalAuthority respondentLA = RespondentLocalAuthority.fromRespondent(fakeRespondentLA);
            caseDetails.getData().put("respondentLocalAuthority", respondentLA);

            respondents.remove(0);
        }

        caseDetails.getData().put(RESPONDENTS_KEY, respondents);

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        caseDetails.getData().put(RESPONDENTS_KEY,
            respondentService.consolidateAndRemoveHiddenFields(caseData.getRespondents1()));

        List<String> errors = respondentValidator.validate(caseData, caseDataBefore);
        return respond(caseDetails, errors);
    }

    private void prepareNewRespondents(CaseDetails caseDetails, CaseData caseData, CaseData caseDataBefore) {
        confidentialDetailsService.addConfidentialDetailsToCase(caseDetails, caseData.getAllRespondents(), RESPONDENT);
        caseData = getCaseData(caseDetails);

        // can either do before or after but have to update case details manually either way as if there is no
        // confidential info then caseDetails won't be updated in the confidential details method and as such just
        // passing the updated list to the method won't work
        List<Element<Respondent>> oldRespondents = caseDataBefore.getAllRespondents();
        List<Element<Respondent>> newRespondents = respondentService.persistRepresentativesRelationship(
            caseData.getAllRespondents(), oldRespondents
        );

        newRespondents = respondentService.consolidateAndRemoveHiddenFields(newRespondents);

        newRespondents = representableCounselUpdater.updateLegalCounsel(
            oldRespondents, newRespondents, caseData.getAllChildren()
        );

        caseDetails.getData().put(RESPONDENTS_KEY, newRespondents);

        caseData = getCaseData(caseDetails);
        if (!OPEN.equals(caseData.getState())) {
            caseDetails.getData().putAll(respondentAfterSubmissionRepresentationService.updateRepresentation(
                caseData, caseDataBefore, Representing.RESPONDENT, true
            ));
        }
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        if (!RepresentativeType.LOCAL_AUTHORITY.equals(caseData.getRepresentativeType())
            && isNotEmpty(caseData.getRespondentLocalAuthority())) {

            try {
                respondentService.transformRespondentLocalAuthority(caseDetails, caseData, caseDataBefore);
            } catch (IllegalArgumentException ex) {
                log.error("Failed to transform respondent local authority on case {}", caseDetails.getId(), ex);
            }
        }

        prepareNewRespondents(caseDetails, caseData, caseDataBefore);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        if (!OPEN.equals(caseData.getState())) {
            noticeOfChangeService.updateRepresentativesAccess(caseData, caseDataBefore, Representing.RESPONDENT);
            representableCounselUpdater.buildEventsForAccessRemoval(caseData, caseDataBefore, Representing.RESPONDENT)
                .forEach(this::publishEvent);
            publishEvent(new RespondentsUpdated(caseData, caseDataBefore));
            publishEvent(new AfterSubmissionCaseDataUpdated(caseData, caseDataBefore));
        }
    }

    @PostMapping("/change-from-other/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleChangeFromOtherAboutToStart(
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<Element<Other>> others = caseData.getOthersV2();
        DynamicList otherList = othersListGenerator.buildOthersList(others);

        List<String> errors = new ArrayList<>();
        if (otherList.getListItems().isEmpty()) {
            errors.add("There is no other person in this case.");
        } else {
            caseDetails.getData().put(OTHERS_LIST_KEY, otherList);
        }
        return respond(caseDetails, errors);
    }

    @PostMapping("/change-from-other/enter-respondent/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleChangeFromOtherEnterRespondentMidEvent(
        @RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        OtherToRespondentEventData eventData = caseData.getOtherToRespondentEventData();
        Other selectedPreparedOther = othersService.getSelectedPreparedOther(caseData, eventData.getOthersList())
            .getValue();
        caseDetails.getData().put(TRANSFORMED_RESPONDENT,
            respondentService.transformOtherToRespondent(selectedPreparedOther));

        return respond(caseDetails);
    }

    @PostMapping("/change-from-other/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleChangeFromOtherMidEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        OtherToRespondentEventData eventData = caseData.getOtherToRespondentEventData();

        List<Element<Respondent>> newRespondents = confidentialDetailsService.prepareCollection(
            caseData.getAllRespondents(), caseData.getConfidentialRespondents(), expandCollection());
        newRespondents.add(newElement(eventData.getTransformedRespondent()));

        CaseData dummyCaseData = caseData.toBuilder()
            .respondents1(newRespondents)
            .build();

        List<String> errors = respondentValidator.validate(dummyCaseData, dummyCaseData, true);
        return respond(caseDetails, errors);
    }

    private Element<Other> getSelectedOther(CaseData caseData) {
        return othersService.getSelectedOther(caseData, caseData.getOtherToRespondentEventData().getOthersList());
    }

    private List<Element<Other>> prepareNewOthers(CaseData caseData, CaseDetails caseDetails,
                                                  Element<Other> selectedOther) {
        List<Element<Other>> newAllOthers = caseData.getOthersV2().stream()
            .filter(ele -> !Objects.equals(ele.getId(), selectedOther.getId()))
            .toList();

        // remove the other person from confidentialOthers if any
        List<Element<Other>> newConfidentialOthers = caseData.getConfidentialOthers().stream()
            .filter(ele -> !Objects.equals(ele.getId(), selectedOther.getId()))
            .toList();

        caseDetails.getData().put(OTHER.getCaseDataKey(), newAllOthers);
        caseDetails.getData().put(OTHER.getConfidentialKey(), newConfidentialOthers);
        return newAllOthers;
    }

    private void addTransformedRespondentToRespondents(CaseDetails caseDetails, CaseData caseData,
                                                       UUID newRespondentId) {
        OtherToRespondentEventData eventData = caseData.getOtherToRespondentEventData();
        Respondent transformedRespondent = eventData.getTransformedRespondent();
        if (IsAddressKnowType.LIVE_IN_REFUGE.equals(transformedRespondent.getParty().getAddressKnow())) {
            transformedRespondent = transformedRespondent.toBuilder()
                .party(transformedRespondent.getParty().toBuilder()
                    .contactDetailsHidden(YesNo.YES.getValue())
                    .hideAddress(YesNo.YES.getValue())
                    .hideTelephone(YesNo.YES.getValue())
                    .contactDetailsHiddenReason(null)
                    .build())
                .build();
        }

        List<Element<Respondent>> newRespondents = confidentialDetailsService.prepareCollection(
            caseData.getAllRespondents(), caseData.getConfidentialRespondents(), expandCollection());
        newRespondents.add(element(newRespondentId, transformedRespondent));
        caseDetails.getData().put(RESPONDENTS_KEY, newRespondents);
    }

    private void prepareUpdatedRepresentative(CaseDetails caseDetails, CaseData caseData,
                                              List<Element<Other>> newOthers, Element<Other> selectedOther) {
        representativeService.updateRepresentativeRole(caseData, selectedOther.getValue()
            .getRepresentedBy(), Type.RESPONDENT, caseData.getRespondents1().size());
        if (isNotEmpty(newOthers)) {
            representativeService.updateRepresentativeRoleForOthers(caseData, newOthers);
        }
        caseDetails.getData().put(REPRESENTATIVES_KEY, caseData.getRepresentatives());
    }

    @PostMapping("/change-from-other/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleChangeFromOtherAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        final CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        Element<Other> selectedOther = getSelectedOther(caseData);
        final List<Element<Other>> newOthers = prepareNewOthers(caseData, caseDetails, selectedOther);
        // add the transformedRespondent to respondents1 with the same other id
        // therefore, relations in representedBy can be kept unchanged
        UUID selectedOtherId = selectedOther.getId();
        addTransformedRespondentToRespondents(caseDetails, caseData, selectedOtherId);
        caseData = getCaseData(caseDetails); // update case data object
        prepareNewRespondents(caseDetails, caseData, caseDataBefore);
        // Setting "representatives" to caseDetails
        prepareUpdatedRepresentative(caseDetails, caseData, newOthers, selectedOther);

        return respond(removeTemporaryFields(caseDetails, OtherToRespondentEventData.class));
    }

}
