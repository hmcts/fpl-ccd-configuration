package uk.gov.hmcts.reform.fpl.controllers.support;

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
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.document.UploadDocumentsMigrationService;
import uk.gov.hmcts.reform.fpl.service.removeorder.CMORemovalAction;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {

    private final UploadDocumentsMigrationService uploadDocumentsMigrationService;
    private final StandardDirectionsService standardDirectionsService;
    private final CMORemovalAction cmoRemovalAction;
    private static final String MIGRATION_ID_KEY = "migrationId";

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2379".equals(migrationId)) {
            run2379(caseDetails);
        }
        if ("FPLA-2525".equals(migrationId)) {
            run2525(caseDetails);
        }
        if ("FPLA-2544".equals(migrationId)) {
            run2544(caseDetails);
        }
        if ("FPLA-2481".equals(migrationId)) {
            run2481(caseDetails);
        }

        if ("FPLA-2521".equals(migrationId)) {
            run2521(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2521(CaseDetails caseDetails) {
        if ("1599470847274974".equals(caseDetails.getId().toString())) {
            CaseData caseData = getCaseData(caseDetails);

            if (isEmpty(caseData.getDraftUploadedCMOs())) {
                throw new IllegalArgumentException("No draft case management orders in the case");
            }

            Element<CaseManagementOrder> firstDraftCmo = caseData.getDraftUploadedCMOs().get(0);

            cmoRemovalAction.removeDraftCaseManagementOrder(caseData, caseDetails, firstDraftCmo);
        }
    }

    private void run2481(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("LE20C50023".equals(caseData.getFamilyManCaseNumber())) {
            Others others = caseData.getOthers();

            if (others == null) {
                throw new IllegalArgumentException("No others in the case");
            }

            if (isEmpty(others.getAdditionalOthers())) {
                caseDetails.getData().remove("others");
            } else {
                caseDetails.getData().put("others", migrateAdditionalOthers(others));
            }
        }
    }

    private Others migrateAdditionalOthers(Others others) {
        Element<Other> removedAdditionalOther = others.getAdditionalOthers().remove(0);

        others = others.toBuilder().firstOther(removedAdditionalOther.getValue()).build();

        if (isEmpty(others.getAdditionalOthers())) {
            others = others.toBuilder().additionalOthers(null).build();
        }

        return others;
    }

    private void run2525(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("SN20C50028".equals(caseData.getFamilyManCaseNumber())) {
            List<Element<HearingBooking>> hearings = caseData.getHearingDetails();

            if (hearings.isEmpty()) {
                throw new IllegalArgumentException("No hearings in a case");
            }

            Element<HearingBooking> hearingToBeRemoved = hearings.get(0);

            if (hearingToBeRemoved.getValue().getStartDate() != null) {
                throw new IllegalArgumentException(
                    format("Invalid hearing date %s", hearingToBeRemoved.getValue().getStartDate().toLocalDate()));
            }
            if (hearingToBeRemoved.getValue().getEndDate() != null) {
                throw new IllegalArgumentException(
                    format("Invalid hearing end date %s", hearingToBeRemoved.getValue().getEndDate().toLocalDate()));
            }
            if (hearingToBeRemoved.getValue().getType() != null) {
                throw new IllegalArgumentException(
                    format("Invalid hearing type %s", hearingToBeRemoved.getValue().getType()));
            }

            if (hearingToBeRemoved.getValue().getVenue() != null) {
                throw new IllegalArgumentException(
                    format("Invalid hearing venue %s", hearingToBeRemoved.getValue().getVenue()));
            }

            hearings.remove(hearingToBeRemoved);

            caseDetails.getData().put("hearingDetails", hearings);
        }
    }

    private void run2379(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        log.info("Migration of Old Documents to Application Documents for case ID {}",
            caseData.getId());
        Map<String, Object> data = caseDetails.getData();
        data.putAll(uploadDocumentsMigrationService.transformFromOldCaseData(caseData));
    }

    private void run2544(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("PO20C50030".equals(caseData.getFamilyManCaseNumber())) {
            if (!SUBMITTED.equals(caseData.getState())) {
                throw new IllegalStateException(
                    format("Case is in %s state, expected %s", caseData.getState(), SUBMITTED));
            }

            caseDetails.getData().put("state", State.GATEKEEPING);
            caseDetails.getData().putAll(standardDirectionsService.populateStandardDirections(caseData));
        }
    }

}
