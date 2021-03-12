package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.SupplementsBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadC2DocumentsService {

    private final Time time;
    private final SupportingEvidenceValidatorService validateSupportingEvidenceBundleService;
    private final DocumentUploadHelper documentUploadHelper;
    private final FeatureToggleService featureToggleService;

    public List<Element<C2DocumentBundle>> buildC2DocumentBundle(CaseData caseData) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        List<SupportingEvidenceBundle> updatedSupportingEvidenceBundle =
            getSupportingEvidenceBundle(caseData.getTemporaryC2Document().getSupportingEvidenceBundle(), uploadedBy);

        C2DocumentBundle.C2DocumentBundleBuilder c2DocumentBundleBuilder = caseData.getTemporaryC2Document()
            .toBuilder()
            .author(uploadedBy)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME))
            .supportingEvidenceBundle(wrapElements(updatedSupportingEvidenceBundle))
            .type(caseData.getC2ApplicationType().get("type"));

        if (featureToggleService.isUploadAdditionalApplicationsEnabled()) {
            return List.of(element(c2DocumentBundleBuilder.supplementsBundle(wrapElements(
                getSupplementsBundle(caseData.getTemporaryC2Document().getSupplementsBundle(), uploadedBy)))
                .build()));
        } else {
            List<Element<C2DocumentBundle>> c2DocumentBundle = defaultIfNull(
                caseData.getC2DocumentBundle(), new ArrayList<>()
            );

            if (featureToggleService.isUploadAdditionalApplicationsEnabled() && !c2DocumentBundle.isEmpty()) {
                //sort existing c2's from old event
                c2DocumentBundle.sort(comparing(e -> e.getValue().getUploadedDateTime(), reverseOrder()));
            }
            c2DocumentBundle.add(element(c2DocumentBundleBuilder.build()));
            return c2DocumentBundle;
        }
    }

    public List<String> validate(C2DocumentBundle c2DocumentBundle) {
        return Optional.ofNullable(c2DocumentBundle)
            .map(C2DocumentBundle::getSupportingEvidenceBundle)
            .map(validateSupportingEvidenceBundleService::validate)
            .orElse(emptyList());
    }

    public OtherApplicationsBundle buildOtherApplicationsBundle(CaseData caseData) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        OtherApplicationsBundle temporaryOtherApplicationsBundle = caseData.getTemporaryOtherApplicationsBundle();

        List<SupportingEvidenceBundle> updatedSupportingEvidenceBundle = getSupportingEvidenceBundle(
            temporaryOtherApplicationsBundle.getSupportingEvidenceBundle(), uploadedBy);

        List<SupplementsBundle> updatedSupplementsBundle = getSupplementsBundle(
            temporaryOtherApplicationsBundle.getSupplementsBundle(), uploadedBy);

        return temporaryOtherApplicationsBundle.toBuilder()
            .author(uploadedBy)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME))
            .applicationType(temporaryOtherApplicationsBundle.getApplicationType())
            .document(temporaryOtherApplicationsBundle.getDocument())
            .supportingEvidenceBundle(wrapElements(updatedSupportingEvidenceBundle))
            .supplementsBundle(wrapElements(updatedSupplementsBundle))
            .build();
    }

    public AdditionalApplicationsBundle buildAdditionalApplicationsBundle(
        CaseData caseData,
        C2DocumentBundle c2DocumentBundle,
        OtherApplicationsBundle otherApplicationsBundle
    ) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();
        PBAPayment temporaryPbaPayment = caseData.getTemporaryPbaPayment();

        return AdditionalApplicationsBundle.builder()
            .c2Document(c2DocumentBundle)
            .otherApplications(otherApplicationsBundle)
            .pbaPayment(PBAPayment.builder()
                .usePbaPayment(temporaryPbaPayment.getUsePbaPayment())
                .pbaNumber(temporaryPbaPayment.getPbaNumber())
                .clientCode(temporaryPbaPayment.getClientCode())
                .fileReference(temporaryPbaPayment.getFileReference())
                .build())
            .author(uploadedBy)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME))
            .build();
    }

    private List<SupportingEvidenceBundle> getSupportingEvidenceBundle(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle, String uploadedBy) {
        return unwrapElements(supportingEvidenceBundle)
            .stream()
            .map(supportingEvidence -> supportingEvidence.toBuilder()
                .dateTimeUploaded(time.now())
                .uploadedBy(uploadedBy)
                .build())
            .collect(Collectors.toList());
    }

    private List<SupplementsBundle> getSupplementsBundle(
        List<Element<SupplementsBundle>> supplementsBundle, String uploadedBy) {
        return unwrapElements(supplementsBundle)
            .stream()
            .map(supplement -> supplement.toBuilder()
                .dateTimeUploaded(time.now())
                .uploadedBy(uploadedBy)
                .build())
            .collect(Collectors.toList());
    }
}
