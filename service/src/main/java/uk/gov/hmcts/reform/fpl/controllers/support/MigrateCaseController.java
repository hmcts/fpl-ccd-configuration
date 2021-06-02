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
import uk.gov.hmcts.reform.fpl.service.casesubmission.CaseSubmissionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final CaseSubmissionService caseSubmissionService;

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
        String ID_FIRST_BUNDLE = "fbf05208-f5dd-4942-b735-9aa226d73a2e";
        if (firstBundle.getId().equals(UUID.fromString(ID_FIRST_BUNDLE))) {

            firstBundle.getValue().setC2DocumentBundle(firstBundle.getValue()
                .getC2DocumentBundle()
                .toBuilder()
                .supportingEvidenceBundle(buildSupportingEvidenceBundle())
                .document(DocumentReference.builder()
                    .filename("S45C-921052410100.pdf")
                    .url("http://dm-store:8080/documents/5ddafb9c-1396-44c1-a4dc-25204b0989f0")
                    .binaryUrl("http://dm-store:8080/documents/5ddafb9c-1396-44c1-a4dc-25204b0989f0/binary")
                    .build()).build());

            //DO I NEED TO ADD LA
            additionalApplicationsBundle.set(0, firstBundle);

        } else {
            throw new IllegalStateException(String
                .format("Migration failed on case %s: Expected" + ID_FIRST_BUNDLE + "but got %s",
                    caseData.getFamilyManCaseNumber(), firstBundle.getId()));
        }

        Element<AdditionalApplicationsBundle> secondBundle = additionalApplicationsBundle.get(1);
        String ID_SECOND_BUNDLE = "4e4def36-2323-4e95-b93a-2f46fc4d6fc0";

        if (secondBundle.getId().equals(UUID.fromString(ID_SECOND_BUNDLE))) {
            swapC2WithSupportingDocument(secondBundle.getValue(), additionalApplicationsBundle);
        } else {
            throw new IllegalStateException(String
                .format("Migration failed on case %s: Expected" + ID_SECOND_BUNDLE + "but got %s",
                    caseData.getFamilyManCaseNumber(), secondBundle.getId()));
        }

        Map<String, Object> data = caseDetails.getData();
        data.put("additionalApplicationsBundle", additionalApplicationsBundle);
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
        //CHANGE TO PROD LINKS
        SupportingEvidenceBundle firstBundle = SupportingEvidenceBundle.builder()
            .name("Position Statement for C2")
            .document(DocumentReference.builder()
                .url("http://dm-store:8080/documents/d448738d-51fe-439e-9a0b-5ecc0bb378b6")
                .binaryUrl("http://dm-store:8080/documents/d448738d-51fe-439e-9a0b-5ecc0bb378b6/binary")
                .filename("Position Statement for C2.docx")
                .build())
            .uploadedBy("HMCTS")
            .dateTimeUploaded(LocalDateTime.of(2021, 05, 24, 10, 23, 32))
            .build();

        SupportingEvidenceBundle secondBundle = SupportingEvidenceBundle.builder()
            .name("Draft LOI")
            .document(DocumentReference.builder()
                .url("http://dm-store:8080/documents/d8357dbc-e3bd-464f-8edb-aac0fe1c58c2")
                .binaryUrl("http://dm-store:8080/documents/d8357dbc-e3bd-464f-8edb-aac0fe1c58c2/binary")
                .filename("AMO0030002 Draft LOI.docx")
                .build())
            .uploadedBy("HMCTS")
            .dateTimeUploaded(LocalDateTime.of(2021, 05, 24, 10, 23, 32))
            .build();

        SupportingEvidenceBundle thirdBundle = SupportingEvidenceBundle.builder()
            .name("CV")
            .document(DocumentReference.builder()
                .url("http://dm-store:8080/documents/6b6b5071-e097-4074-b317-b00dc3cfa89c")
                .binaryUrl("http://dm-store:8080/documents/6b6b5071-e097-4074-b317-b00dc3cfa89c/binary")
                .filename("AMO0030002 Medico-legal_CV-May2021.doc")
                .build())
            .uploadedBy("HMCTS")
            .dateTimeUploaded(LocalDateTime.of(2021, 05, 24, 10, 23, 32))
            .build();

        return wrapElements(firstBundle, secondBundle, thirdBundle);

    }

    private List<Element<AdditionalApplicationsBundle>> swapC2WithSupportingDocument(AdditionalApplicationsBundle application,
                                                                                     List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle) {
        C2DocumentBundle c2DocumentBundle = application.getC2DocumentBundle();

        Optional<Element<SupportingEvidenceBundle>> bundle = c2DocumentBundle.getSupportingEvidenceBundle().stream().filter(
            supportingEvidenceBundle -> supportingEvidenceBundle.getId().equals(UUID.fromString("3f3a183e-44ab-4e63-ac27-0ca40f3058ff"))
        ).findFirst();


        if(bundle.isPresent()) {

            application.setC2DocumentBundle(application.getC2DocumentBundle().toBuilder().document(bundle.get().getValue().getDocument()).build());

        }

        c2DocumentBundle.getSupportingEvidenceBundle()
            .removeIf(supportingEvidenceBundle -> supportingEvidenceBundle.getId()
                .toString()
                .equals("3f3a183e-44ab-4e63-ac27-0ca40f3058ff")
            );

        additionalApplicationsBundle.set(1, element(application));

        return additionalApplicationsBundle;
    }
}
