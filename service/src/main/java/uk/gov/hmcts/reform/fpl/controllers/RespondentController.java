package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
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
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.model.Respondent.expandCollection;
import static uk.gov.hmcts.reform.fpl.model.common.Element.newElement;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.ConfidentialDetailsHelper.getConfidentialItemToAdd;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Slf4j
@Api
@RestController
@RequestMapping("/callback/enter-respondents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentController extends CallbackController {

    private static final String OTHERS_KEY = "others";
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

        caseDetails.getData().put(RESPONDENTS_KEY, confidentialDetailsService.prepareCollection(
            caseData.getAllRespondents(), caseData.getConfidentialRespondents(), expandCollection()));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        caseDetails.getData().put(RESPONDENTS_KEY, respondentService.removeHiddenFields(caseData.getRespondents1()));

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

        newRespondents = respondentService.removeHiddenFields(newRespondents);

        newRespondents = representableCounselUpdater.updateLegalCounsel(
            oldRespondents, newRespondents, caseData.getAllChildren()
        );

        caseDetails.getData().put(RESPONDENTS_KEY, newRespondents);
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

        List<Element<Other>> others = caseData.getAllOthers();
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

        CaseData dummyCaseData = caseData.builder()
            .respondents1(newRespondents)
            .build();

        List<String> errors = respondentValidator.validate(dummyCaseData, dummyCaseData, true);
        return respond(caseDetails, errors);
    }

    private UUID getFirstOtherId(CaseData caseData) {
        Set<UUID> additionalOtherIds = nullSafeList(caseData.getOthers().getAdditionalOthers())
            .stream().map(Element::getId).collect(Collectors.toSet());
        UUID firstOtherUUID = caseData.getConfidentialOthers().stream().map(Element::getId)
            .filter(co -> !additionalOtherIds.contains(co)).findFirst()
            .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        return firstOtherUUID;
    }

    private Element<Other> getSelectedOther(CaseData caseData) {
        UUID firstOtherUUID = getFirstOtherId(caseData);
        return othersService.getSelectedOther(caseData,
            caseData.getOtherToRespondentEventData().getOthersList(), firstOtherUUID);
    }

    private List<Element<Other>> buildNewAllOthers(CaseData caseData, Element<Other> selectedOther) {
        List<Element<Other>> listOfNewOtherElement = new ArrayList<>(caseData.getAllOthers());
        listOfNewOtherElement.removeIf(ele -> Objects.equals(ele.getValue(), selectedOther.getValue()));
        List<Element<Other>> ret = new ArrayList<>();
        final List<Element<Other>> confidentialOthers = caseData.getConfidentialOthers();
        listOfNewOtherElement.forEach(element -> {
            if (element.getValue().containsConfidentialDetails()) {
                Element<Other> otherElementWithConfidentialDetails = element(element.getId(), othersService
                    .addConfidentialDetails(getConfidentialItemToAdd(confidentialOthers, element), element));
                // copying the representedBy to confidential others
                element.getValue().getRepresentedBy()
                    .forEach(uuid -> otherElementWithConfidentialDetails.getValue()
                        .addRepresentative(uuid.getId(), uuid.getValue()));
                ret.add(otherElementWithConfidentialDetails);
            } else {
                ret.add(element);
            }
        });
        return ret;
    }

    private Others prepareNewOthers(CaseDetails caseDetails, List<Element<Other>> newAllOthers) {
        // Setting "confidentialOthers" to caseDetails
        confidentialDetailsService.addConfidentialDetailsToCase(caseDetails, newAllOthers, OTHER);
        // Setting "others" to caseDetails from previous built new allOthers collection
        List<Element<Other>> preparingNewOthers = confidentialDetailsService
            .removeConfidentialDetails(newAllOthers);
        preparingNewOthers.forEach(
            preparingNewOther -> newAllOthers.stream()
                .filter(newOther -> newOther.getId().equals(preparingNewOther.getId()))
                .findFirst().ifPresent(
                    newOther -> {
                        for (Element<UUID> uuid : newOther.getValue().getRepresentedBy()) {
                            if (!preparingNewOther.getValue().getRepresentedBy().contains(uuid)) {
                                preparingNewOther.getValue().getRepresentedBy().add(uuid);
                            }
                        }
                    }
                )
        );
        Others newOthers = Others.from(preparingNewOthers);
        if (isNull(newOthers)) {
            caseDetails.getData().remove(OTHERS_KEY);
        } else {
            caseDetails.getData().put(OTHERS_KEY, newOthers);
        }
        return newOthers;
    }

    private void addTransformedRespondentToRespondents(CaseDetails caseDetails, CaseData caseData,
                                                       Element<Other> selectedOther) {
        OtherToRespondentEventData eventData = caseData.getOtherToRespondentEventData();
        Respondent transformedRespondent = eventData.getTransformedRespondent();
        List<Element<Respondent>> newRespondents = confidentialDetailsService.prepareCollection(
            caseData.getAllRespondents(), caseData.getConfidentialRespondents(), expandCollection());
        newRespondents.add(element(selectedOther.getId(), transformedRespondent));
        caseDetails.getData().put(RESPONDENTS_KEY, newRespondents);
    }

    private void prepareUpdatedRepresentative(CaseDetails caseDetails, CaseData caseData, Others newOthers,
                                              Element<Other> selectedOther) {
        representativeService.updateRepresentativeRole(caseData, selectedOther.getValue()
            .getRepresentedBy(), Type.RESPONDENT, caseData.getRespondents1().size());
        if (nonNull(newOthers) && nonNull(newOthers.getFirstOther())) {
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
        List<Element<Other>> newAllOthers = buildNewAllOthers(caseData, selectedOther);
        final Others newOthers = prepareNewOthers(caseDetails, newAllOthers);
        // build new respondents1
        addTransformedRespondentToRespondents(caseDetails, caseData, selectedOther);
        caseData = getCaseData(caseDetails); // update case data object
        prepareNewRespondents(caseDetails, caseData, caseDataBefore);
        // Setting "representatives" to caseDetails
        prepareUpdatedRepresentative(caseDetails, caseData, newOthers, selectedOther);

        return respond(removeTemporaryFields(caseDetails, OtherToRespondentEventData.class));
    }

}
