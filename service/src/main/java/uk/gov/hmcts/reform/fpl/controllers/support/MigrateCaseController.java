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
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LABARRISTER;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private static final String FMN_ERROR_MESSAGE = "Unexpected FMN ";

    private final CaseAccessService caseAccessService;

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {

    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2774".equals(migrationId)) {
            run2774(caseDetails);
        }

        if ("FPLA-2905".equals(migrationId)) {
            run2905(caseDetails);
        }

        if ("FPLA-2872".equals(migrationId)) {
            run2872(caseDetails);
        }

        if ("FPLA-2871".equals(migrationId)) {
            run2871(caseDetails);
        }

        if ("FPLA-2885".equals(migrationId)) {
            run2885(caseDetails);
        }

        if ("FPLA-2947".equals(migrationId)) {
            run2947(caseDetails);
        }

        if ("FPLA-2913".equals(migrationId)) {
            run2913(caseDetails);
        }

        if ("FPLA-2946".equals(migrationId)) {
            run2946(caseDetails);
        }

        if ("FPLA-2960".equals(migrationId)) {
            run2960(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }


    private void run2960(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        String familyManNumber = "YO21C50001";
        String userId = "d81bba10-dba7-4a2e-8bb8-b372407c1fba";
        UUID elementId = UUID.fromString("f2b1e4ee-287e-4257-9413-8f61d61af27f");

        if (familyManNumber.equals(caseData.getFamilyManCaseNumber())) {
            List<Element<LegalRepresentative>> legalRepresentatives = caseData.getLegalRepresentatives();

            if (isEmpty(legalRepresentatives)) {
                throw new IllegalStateException("Empty list of legal representatives");
            }

            List<Element<LegalRepresentative>> toBeRemoved = legalRepresentatives.stream()
                .filter(e -> e.getId().equals(elementId))
                .collect(toList());

            log.info("Migration FPLA-2960. Number of legal representatives to be removed: " + toBeRemoved.size());

            boolean removed = legalRepresentatives.removeAll(toBeRemoved);

            if (removed) {
                caseAccessService.revokeCaseRoleFromUser(caseData.getId(), userId, LABARRISTER);
            }

            caseDetails.getData().put("legalRepresentatives", legalRepresentatives);
        } else {
            throw new IllegalStateException(FMN_ERROR_MESSAGE + caseData.getFamilyManCaseNumber());
        }
    }

    private void run2946(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        Set<String> users = Set.of(
            "be3f6712-b01f-49d6-892a-7b00bdb24f5f",
            "c596b0b5-d32c-46a8-b92b-e5a9ebef6bb6",
            "9fe14653-897e-4643-85bc-96ab3aeb453c",
            "47533fd8-293a-481a-ba61-b6d1f1a63cea",
            "fc1b1c0f-2a2d-463d-9977-ff74ce336fb0",
            "cdf6313c-7948-44c2-86d8-8444222e079f",
            "0286746f-5e5e-4509-b25b-fd2c92d015c5",
            "b6530997-ae3f-4170-8c98-70cb42acb475",
            "2e28afcc-6d81-4695-9a5d-9cfb9a560d5f",
            "d2b6f10e-1296-4b45-aa62-733d1e76ec33",
            "bb3ae7ac-0781-4531-814d-652c52a06eb5",
            "de58407e-7c60-4a22-92a2-1044a8e214c5",
            "943d9dc2-8b50-4ee2-8afa-2e95bbf1a73b",
            "44921108-0c72-4479-bb59-b82cdbd85806",
            "affe04bc-7a13-4563-9995-4200c8548800",
            "8167cf9e-8847-45a0-a2d2-a55dd8187ea6",
            "5eeca97a-fad6-478b-bd73-8fe01a10532e",
            "d981d8fc-a9ab-48ed-afe1-508ad955c7e5",
            "3e40f009-75ba-4a7d-b9a4-d06f21f067b6",
            "82f45661-6067-4a23-95e1-dd4818eeb4f5",
            "133319ee-cd34-4f8b-953e-624b59263e3b",
            "5564fd08-d823-4a55-86bf-d72a7e1664dc",
            "67b61f34-2e2a-4801-a482-eaa10a843538",
            "aeb944b6-9e66-4821-9126-d6b397a7cad2",
            "365c2f8d-cc77-4f95-9f28-691252af630f",
            "8a9b7a70-64c3-4931-9325-2a9cff917263",
            "c3612b21-c8a5-41fa-8bb6-5c996e00ec1c",
            "32115f54-0485-4c39-99aa-f51c68c08673"
        );

        if ("WR21C50052".equals(caseData.getFamilyManCaseNumber())) {
            caseAccessService.grantCaseRoleToUsers(caseData.getId(), users, CaseRole.LASOLICITOR);
        } else {
            throw new IllegalStateException(FMN_ERROR_MESSAGE + caseData.getFamilyManCaseNumber());
        }
    }

    private void run2913(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("SA20C50026".equals(caseData.getFamilyManCaseNumber())) {

            if (caseData.getSealedCMOs().size() < 3) {
                throw new IllegalArgumentException(
                    "Expected at least 3 sealed cmos, but found " + caseData.getSealedCMOs().size());
            }

            Element<HearingOrder> sealedCmo = caseData.getSealedCMOs().get(2);
            List<Element<SupportingEvidenceBundle>> supportingDocuments = sealedCmo.getValue().getSupportingDocs();

            Set<String> documentToBeRemoved = Set.of("Draft Placement Order", "Final Placement Order");

            List<Element<SupportingEvidenceBundle>> supportingDocumentsToBeRemoved = supportingDocuments.stream()
                .filter(doc -> documentToBeRemoved.contains(doc.getValue().getName()))
                .collect(toList());

            if (supportingDocumentsToBeRemoved.size() != 2) {
                throw new IllegalStateException(
                    "Expected 2 documents to be removed, found " + supportingDocumentsToBeRemoved.size());
            }

            supportingDocuments.removeAll(supportingDocumentsToBeRemoved);

            caseDetails.getData().put("sealedCMOs", caseData.getSealedCMOs());

            List<UUID> documentsToBeRemovedIds = supportingDocumentsToBeRemoved.stream()
                .map(Element::getId)
                .collect(toList());

            List<Element<HearingFurtherEvidenceBundle>> evidenceBundles = caseData.getHearingFurtherEvidenceDocuments();

            List<Element<HearingFurtherEvidenceBundle>> hearingBundles = evidenceBundles.stream()
                .filter(bundle -> bundle.getValue().getSupportingEvidenceBundle().stream()
                    .anyMatch(doc -> documentsToBeRemovedIds.contains(doc.getId())))
                .collect(Collectors.toList());

            if (hearingBundles.size() > 1) {
                throw new IllegalStateException(
                    "Expected 1 hearing bundle with documents to be removed, found " + hearingBundles.size());
            }

            if (hearingBundles.size() == 1) {
                hearingBundles.get(0).getValue().getSupportingEvidenceBundle()
                    .removeIf(doc -> documentsToBeRemovedIds.contains(doc.getId()));
                caseDetails.getData().put("hearingFurtherEvidenceDocuments", evidenceBundles);
            } else {
                log.info("No hearing bundle with supporting documents to be removed");
            }

        } else {
            throw new IllegalStateException(FMN_ERROR_MESSAGE + caseData.getFamilyManCaseNumber());
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


    private void run2905(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("CF20C50047".equals(caseData.getFamilyManCaseNumber())) {

            if (isEmpty(caseData.getC2DocumentBundle())) {
                throw new IllegalArgumentException("No C2 document bundles in the case");
            }

            caseData.getC2DocumentBundle().remove(1);
            caseDetails.getData().put("c2DocumentBundle", caseData.getC2DocumentBundle());
        } else {
            throw new IllegalStateException(FMN_ERROR_MESSAGE + caseData.getFamilyManCaseNumber());
        }
    }

    private void run2872(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("NE20C50023".equals(caseData.getFamilyManCaseNumber())) {
            if (isEmpty(caseData.getC2DocumentBundle())) {
                throw new IllegalArgumentException("No C2 document bundles in the case");
            }

            if (caseData.getC2DocumentBundle().size() < 5) {
                throw new IllegalArgumentException(String.format("Expected at least 5 C2 document bundles in the case"
                    + " but found %s", caseData.getC2DocumentBundle().size()));
            }

            caseData.getC2DocumentBundle().remove(4);
            caseData.getC2DocumentBundle().remove(3);
            caseData.getC2DocumentBundle().remove(2);

            caseDetails.getData().put("c2DocumentBundle", caseData.getC2DocumentBundle());
        } else {
            throw new IllegalStateException(FMN_ERROR_MESSAGE + caseData.getFamilyManCaseNumber());
        }
    }

    private void run2871(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("WR20C50015".equals(caseData.getFamilyManCaseNumber())) {
            log.info("Attempting to remove first C2 from WR20C50015");
            removeFirstC2(caseDetails);
            log.info("Successfully removed C2 from WR20C50015");
        }
    }

    private void removeFirstC2(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        List<Element<C2DocumentBundle>> c2DocumentBundle = caseData.getC2DocumentBundle();

        if (isEmpty(c2DocumentBundle)) {
            throw new IllegalArgumentException("No C2s on case");
        }

        c2DocumentBundle.remove(0);

        caseDetails.getData().put("c2DocumentBundle", c2DocumentBundle);

    }

    private void run2885(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isEmpty(caseData.getCancelledHearingDetails())) {
            throw new IllegalArgumentException("Case does not contain cancelled hearing bookings");
        }

        caseData.getCancelledHearingDetails().forEach(hearingBookingElement -> {
            switch (hearingBookingElement.getValue().getCancellationReason()) {
                case "OT8":
                    hearingBookingElement.getValue().setCancellationReason("IN1");
                    break;
                case "OT9":
                    hearingBookingElement.getValue().setCancellationReason("OT8");
                    break;
                case "OT10":
                    hearingBookingElement.getValue().setCancellationReason("OT9");
                    break;
            }
        });

        caseDetails.getData().put("cancelledHearingDetails", caseData.getCancelledHearingDetails());
    }

    private void run2947(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (List.of(1602246223743823L, 1611588537917646L).contains(caseData.getId())) {
            if (isEmpty(caseData.getCancelledHearingDetails())) {
                throw new IllegalArgumentException("Case does not contain cancelled hearing bookings");
            }

            caseData.getCancelledHearingDetails().forEach(hearingBookingElement -> {
                switch (hearingBookingElement.getValue().getCancellationReason()) {
                    case "OT8":
                        hearingBookingElement.getValue().setCancellationReason("IN1");
                        break;
                    case "OT9":
                        hearingBookingElement.getValue().setCancellationReason("OT8");
                        break;
                    case "OT10":
                        hearingBookingElement.getValue().setCancellationReason("OT9");
                        break;
                }
            });
        }

        caseDetails.getData().put("cancelledHearingDetails", caseData.getCancelledHearingDetails());
    }
}
