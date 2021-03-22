package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.document.ConfidentialDocumentsSplitter;
import uk.gov.hmcts.reform.fpl.service.removeorder.DraftCMORemovalAction;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private final ConfidentialDocumentsSplitter splitter;
    private final DraftCMORemovalAction draftCMORemovalAction;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2724".equals(migrationId)) {
            run2724(caseDetails);
        }

        if ("FPLA-2705".equals(migrationId)) {
            run2705(caseDetails);
        }

        if ("FPLA-2706".equals(migrationId)) {
            run2706(caseDetails);
        }

        if ("FPLA-2715".equals(migrationId)) {
            run2715(caseDetails);
        }

        if ("FPLA-2740".equals(migrationId)) {
            run2740(caseDetails);
        }

        if ("FPLA-2774".equals(migrationId)) {
            run2774(caseDetails);
        }

        if ("FPLA-2898".equals(migrationId)) {
            run2898(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }


    private void run2898(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("PO20C50010".equals(caseData.getFamilyManCaseNumber())) {

            final String hearingName = "Issues Resolution/Early Final Hearing hearing, 5 March 2021";
            final Set<String> documentNames = Set.of("Placement application", "Statement of facts", "CPR");

            List<Element<HearingFurtherEvidenceBundle>> bundles =
                defaultIfNull(caseData.getHearingFurtherEvidenceDocuments(), new ArrayList<>());

            Element<HearingFurtherEvidenceBundle> hearingBundle = bundles.stream()
                .peek(hearing -> log.info("Migration 2898 - hearing name" + hearing.getValue().getHearingName()))
                .filter(hearing -> hearing.getValue().getHearingName().equals(hearingName))
                .findFirst()
                .orElseThrow(() -> new HearingNotFoundException(hearingName));

            List<Element<SupportingEvidenceBundle>> all = hearingBundle.getValue()
                .getSupportingEvidenceBundle().stream()
                .filter(doc -> documentNames.contains(doc.getValue().getName()))
                .collect(Collectors.toList());

            if (all.size() != 3) {
                throw new IllegalStateException("Unexpected number of found documents: " + all.size());
            }

            hearingBundle.getValue().getSupportingEvidenceBundle().removeAll(all);

            if (isEmpty(hearingBundle.getValue().getSupportingEvidenceBundle())) {
                bundles.remove(hearingBundle);
            }

            caseDetails.getData().put("hearingFurtherEvidenceDocuments", bundles);
        } else {
            throw new IllegalStateException("Unexpected FMN " + caseData.getFamilyManCaseNumber());
        }
    }

    private void run2774(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("NE21C50007".equals(caseData.getFamilyManCaseNumber())) {
            List<Element<HearingBooking>> hearings = caseData.getHearingDetails();

            if (ObjectUtils.isEmpty(hearings)) {
                throw new IllegalArgumentException("No hearings in the case");
            }

            if (hearings.size() < 2) {
                throw new IllegalArgumentException(String.format("Expected 2 hearings in the case but found %s",
                    hearings.size()));
            }

            Element<HearingBooking> hearingToBeRemoved = hearings.get(1);

            hearings.remove(hearingToBeRemoved);

            caseDetails.getData().put("hearingDetails", hearings);
        }
    }

    private void run2715(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("CF20C50079".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void run2740(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("ZW21C50002".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstCaseNotes(caseDetails);
        }
    }

    private void run2706(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("CF20C50049".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void run2724(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("WR20C50007".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void run2705(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("SN20C50023".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void removeFirstDraftCaseManagementOrder(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

        if (isEmpty(caseData.getDraftUploadedCMOs())) {
            throw new IllegalArgumentException("No draft case management orders in the case");
        }

        Element<HearingOrder> firstDraftCmo = caseData.getDraftUploadedCMOs().get(0);

        draftCMORemovalAction.removeDraftCaseManagementOrder(caseData, caseDetailsMap, firstDraftCmo);
        caseDetails.setData(caseDetailsMap);
    }

    private void removeFirstCaseNotes(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isEmpty(caseData.getCaseNotes()) || caseData.getCaseNotes().size() != 4) {
            throw new IllegalArgumentException(String.format("Expected at least 4 case notes but found %s",
                isEmpty(caseData.getCaseNotes()) ? "empty" : caseData.getCaseNotes().size()));
        }

        caseData.getCaseNotes().remove(0);
        caseDetails.getData().put("caseNotes", caseData.getCaseNotes());
    }
}
