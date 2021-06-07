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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private static final UUID FIRST_BUNDLE_ID = UUID.fromString("fbf05208-f5dd-4942-b735-9aa226d73a2e");
    private static final UUID SECOND_BUNDLE_ID = UUID.fromString("4e4def36-2323-4e95-b93a-2f46fc4d6fc0");
    private static final UUID SUPPORTING_EVIDENCE_ID = UUID.fromString("3f3a183e-44ab-4e63-ac27-0ca40f3058ff");

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-3037".equals(migrationId)) {
            run3037(caseDetails);
        }

        if ("FPLA-3080".equals(migrationId)) {
            run3080(caseDetails);
        }

        if ("FPLA-2991".equals(migrationId)) {
            run2991(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3037(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundles =
            caseData.getAdditionalApplicationsBundle();

        additionalApplicationsBundles.forEach(
            additionalApplicationsBundle -> {
                C2DocumentBundle c2DocumentBundle = additionalApplicationsBundle.getValue().getC2DocumentBundle();

                c2DocumentBundle.getSupportingEvidenceBundle()
                    .removeIf(supportingEvidenceBundle -> supportingEvidenceBundle.getId()
                        .toString()
                        .equals("4885a0e2-fd88-4614-9c35-6c61d6b5e422")
                    );
            }
        );

        data.put("additionalApplicationsBundle", additionalApplicationsBundles);
        caseDetails.setData(data);
    }

    private void run3080(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = caseData
            .getAdditionalApplicationsBundle();

        Element<AdditionalApplicationsBundle> firstBundle = additionalApplicationsBundle.get(0);
        if (firstBundle.getId().equals(FIRST_BUNDLE_ID)) {
            firstBundle.getValue().setC2DocumentBundle(firstBundle.getValue()
                .getC2DocumentBundle()
                .toBuilder()
                .supportingEvidenceBundle(buildSupportingEvidenceBundle())
                .document(DocumentReference.builder()
                    .filename("S45C-921052410100.pdf")
                    .url("http://dm-store-prod.service.core-compute-prod.internal/documents/5ddafb9c-1396-44c1-a4dc-25204b0989f0")
                    .binaryUrl("http://dm-store-prod.service.core-compute-prod.internal/documents/5ddafb9c-1396-44c1-a4dc-25204b0989f0/binary")
                    .build())
                .build());

            additionalApplicationsBundle.set(0, firstBundle);

        } else {
            throw new IllegalStateException(String
                .format("Migration failed on case %s: Expected %s but got %s",
                    caseData.getFamilyManCaseNumber(), FIRST_BUNDLE_ID, firstBundle.getId()));
        }

        Element<AdditionalApplicationsBundle> secondBundle = additionalApplicationsBundle.get(1);

        if (secondBundle.getId().equals(SECOND_BUNDLE_ID)) {
            AdditionalApplicationsBundle bundle = swapC2WithSupportingDocument(secondBundle.getValue());
            additionalApplicationsBundle.set(1, element(bundle));
        } else {
            throw new IllegalStateException(String
                .format("Migration failed on case %s: Expected %s but got %s",
                    caseData.getFamilyManCaseNumber(), SECOND_BUNDLE_ID, secondBundle.getId()));
        }

        Map<String, Object> data = caseDetails.getData();
        data.put("additionalApplicationsBundle", additionalApplicationsBundle);
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
        SupportingEvidenceBundle firstBundle = SupportingEvidenceBundle.builder()
            .name("Position Statement for C2")
            .document(DocumentReference.builder()
                .url("http://dm-store-prod.service.core-compute-prod.internal/documents/d448738d-51fe-439e-9a0b-5ecc0bb378b6")
                .binaryUrl("http://dm-store-prod.service.core-compute-prod.internal/documents/d448738d-51fe-439e-9a0b-5ecc0bb378b6/binary")
                .filename("Position Statement for C2.docx")
                .build())
            .uploadedBy("HMCTS")
            .dateTimeUploaded(LocalDateTime.of(2021, 05, 24, 10, 23, 32))
            .build();

        SupportingEvidenceBundle secondBundle = SupportingEvidenceBundle.builder()
            .name("Draft LOI")
            .document(DocumentReference.builder()
                .url("http://dm-store-prod.service.core-compute-prod.internal/documents/d8357dbc-e3bd-464f-8edb-aac0fe1c58c2")
                .binaryUrl("http://dm-store-prod.service.core-compute-prod.internal/documents/d8357dbc-e3bd-464f-8edb-aac0fe1c58c2/binary")
                .filename("AMO0030002 Draft LOI.docx")
                .build())
            .uploadedBy("HMCTS")
            .dateTimeUploaded(LocalDateTime.of(2021, 05, 24, 10, 23, 32))
            .build();

        SupportingEvidenceBundle thirdBundle = SupportingEvidenceBundle.builder()
            .name("CV")
            .document(DocumentReference.builder()
                .url("http://dm-store-prod.service.core-compute-prod.internal/documents/6b6b5071-e097-4074-b317-b00dc3cfa89c")
                .binaryUrl("http://dm-store-prod.service.core-compute-prod.internal/documents/6b6b5071-e097-4074-b317-b00dc3cfa89c/binary")
                .filename("AMO0030002 Medico-legal_CV-May2021.doc")
                .build())
            .uploadedBy("HMCTS")
            .dateTimeUploaded(LocalDateTime.of(2021, 05, 24, 10, 23, 32))
            .build();

        return wrapElements(firstBundle, secondBundle, thirdBundle);
    }

    private AdditionalApplicationsBundle swapC2WithSupportingDocument(AdditionalApplicationsBundle application) {
        C2DocumentBundle c2DocumentBundle = application.getC2DocumentBundle();
        Element<SupportingEvidenceBundle> supportingEvidenceBundle = c2DocumentBundle
            .getSupportingEvidenceBundle()
            .stream()
            .filter(bundle -> bundle.getId().equals(SUPPORTING_EVIDENCE_ID))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("Supporting evidence "
                + "document with %s does not exist", SUPPORTING_EVIDENCE_ID)));

        application.setC2DocumentBundle(application.getC2DocumentBundle().toBuilder()
            .document(supportingEvidenceBundle.getValue().getDocument())
            .build());


        c2DocumentBundle.getSupportingEvidenceBundle()
            .removeIf(bundle -> bundle.getId()
                .equals(SUPPORTING_EVIDENCE_ID)
            );

        return application;
    }

    private void run2991(CaseDetails caseDetails) {
        final UUID secondBundleID = UUID.fromString("1bae342e-f73c-4ef3-b7e2-044d6c618825");
        final UUID supportingEvidenceID = UUID.fromString("045c1fd6-3fed-42d3-be0b-e47257f6c01c");
        CaseData caseData = getCaseData(caseDetails);

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = caseData
            .getAdditionalApplicationsBundle();

        Element<AdditionalApplicationsBundle> secondBundle = additionalApplicationsBundle.get(1);
        if (secondBundle.getId().equals(secondBundleID)) {
            C2DocumentBundle c2DocumentBundle = secondBundle.getValue().getC2DocumentBundle();

            c2DocumentBundle.getSupportingEvidenceBundle()
                .removeIf(bundle -> bundle.getId()
                    .equals(supportingEvidenceID)
                );

        } else {
            throw new IllegalStateException(String
                .format("Migration failed on case %s: Expected %s but got %s",
                    caseData.getFamilyManCaseNumber(), secondBundleID, secondBundle.getId()));
        }

        Map<String, Object> data = caseDetails.getData();
        data.put("additionalApplicationsBundle", additionalApplicationsBundle);
    }
}
