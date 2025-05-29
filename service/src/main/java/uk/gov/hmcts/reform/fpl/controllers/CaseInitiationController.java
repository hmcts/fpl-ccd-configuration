package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CaseInitiationService;
import uk.gov.hmcts.reform.fpl.service.CourtLookUpService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Slf4j
@RestController
@RequestMapping("/callback/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationController extends CallbackController {

    private final CaseInitiationService caseInitiationService;

    private final CoreCaseDataApiV2 coreCaseDataApi;

    private final RequestData requestData;

    private final AuthTokenGenerator authToken;

    private final CourtLookUpService courtLookUpService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        final CaseDetailsMap caseData = caseDetailsMap(callbackrequest.getCaseDetails());

        if (caseInitiationService.isUserLocalAuthority()) {
            caseData.put("isLocalAuthority", YesNo.YES);
        } else {
            caseData.put("isLocalAuthority", YesNo.NO);
        }

        caseInitiationService.getUserOrganisationId().ifPresent(organisationId ->
            caseInitiationService.getOutsourcingType(organisationId).ifPresent(outsourcingType -> {
                caseData.putIfNotEmpty("outsourcingType", outsourcingType);
                caseData.putIfNotEmpty("outsourcingLAs", caseInitiationService
                    .getOutsourcingLocalAuthorities(organisationId, outsourcingType));
            }));

        return respond(caseData);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        final CaseDetails caseDetails = callbackrequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        boolean isOutsourcedCase = caseInitiationService.isCaseOutsourced(caseData);

        if (isOutsourcedCase) {
            caseDetails.getData().put("isOutsourcedCase", YesNo.from(isOutsourcedCase).getValue());
            caseDetails.getData().put("sharingWithUsers", caseInitiationService.getOrganisationUsers());
        }

        return respond(caseDetails, caseInitiationService.checkUserAllowedToCreateCase(caseData));
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        final CaseData caseData = getCaseData(callbackrequest);
        final CaseDetailsMap caseDetails = caseDetailsMap(callbackrequest.getCaseDetails());

        final CaseData updatedCaseData = caseInitiationService.updateOrganisationsDetails(caseData);

        caseDetails.putIfNotEmpty("caseLocalAuthority", updatedCaseData.getCaseLocalAuthority());
        caseDetails.putIfNotEmpty("caseLocalAuthorityName", updatedCaseData.getCaseLocalAuthorityName());
        caseDetails.putIfNotEmpty("localAuthorityPolicy", updatedCaseData.getLocalAuthorityPolicy());
        caseDetails.putIfNotEmpty("outsourcingPolicy", updatedCaseData.getOutsourcingPolicy());
        caseDetails.putIfNotEmpty("court", updatedCaseData.getCourt());
        caseDetails.putIfNotEmpty("multiCourts", updatedCaseData.getMultiCourts());
        caseDetails.putIfNotEmpty("caseNameHmctsInternal", updatedCaseData.getCaseName());

        if (updatedCaseData.getCourt() != null) {
            String courtCode = updatedCaseData.getCourt().getCode();
            Optional<Court> lookedUpCourt = courtLookUpService.getCourtByCode(courtCode);
            if (lookedUpCourt.isPresent()) {
                caseDetails.putIfNotEmpty("caseManagementLocation", CaseLocation.builder()
                    .baseLocation(lookedUpCourt.get().getEpimmsId())
                    .region(lookedUpCourt.get().getRegionId())
                    .build());
            } else {
                log.error("Fail to lookup ePIMMS ID for code: " + courtCode);
            }
        } else {
            log.debug("leave `caseManagementLocation` blank since it may be the multiCourts case.");
        }
        caseDetails.putIfNotEmpty("caseManagementCategory", DynamicList.builder()
            .value(DynamicListElement.builder().code("FPL").label("Family Public Law").build())
            .listItems(List.of(
                DynamicListElement.builder().code("FPL").label("Family Public Law").build()
            ))
            .build());
        caseDetails.putIfNotEmpty("representativeType", updatedCaseData.getRepresentativeType());
        if (Objects.nonNull(updatedCaseData.getDfjArea())) {
            caseDetails.putIfNotEmpty("dfjArea", updatedCaseData.getDfjArea());
            caseDetails.putIfNotEmpty(updatedCaseData.getCourtField(), updatedCaseData.getCourt().getCode());
        }

        if (!RepresentativeType.LOCAL_AUTHORITY.equals(updatedCaseData.getRepresentativeType())) {
            // if we're a 3rd party app, prepopulate the respondentLocalAuthority field
            caseDetails.putIfNotEmpty("respondentLocalAuthority",
                caseInitiationService.getRespondentLocalAuthorityDetails(caseData));
            caseDetails.putIfNotEmpty("hasRespondentLA", YesNo.YES);
        } else {
            caseDetails.putIfNotEmpty("hasRespondentLA", YesNo.NO);
        }

        caseDetails.removeAll("outsourcingType", "outsourcingLAs", "sharingWithUsers", "isOutsourcedCase");

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);

        caseInitiationService.grantCaseAccess(caseData);

        publishEvent(new CaseDataChanged(caseData));

        // update supplementary data
        Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
        supplementaryData.put("supplementary_data_updates",
            Map.of("$set", Map.of("HMCTSServiceId", "ABA3")));
        coreCaseDataApi.submitSupplementaryData(requestData.authorisation(), authToken.generate(),
            caseData.getId().toString(), supplementaryData);
    }
}
