package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupplementsBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
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
        List<Element<C2DocumentBundle>> c2DocumentBundle = defaultIfNull(
            caseData.getC2DocumentBundle(), new ArrayList<>()
        );

        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        List<SupportingEvidenceBundle> updatedSupportingEvidenceBundle =
            unwrapElements(caseData.getTemporaryC2Document().getSupportingEvidenceBundle())
                .stream()
                .map(supportingEvidence -> supportingEvidence.toBuilder()
                    .dateTimeUploaded(time.now())
                    .uploadedBy(uploadedBy)
                    .build())
                .collect(Collectors.toList());

        C2DocumentBundle.C2DocumentBundleBuilder c2DocumentBundleBuilder = caseData.getTemporaryC2Document().toBuilder()
            .author(uploadedBy)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME))
            .supportingEvidenceBundle(wrapElements(updatedSupportingEvidenceBundle))
            .type(caseData.getC2ApplicationType().get("type"));

        if (featureToggleService.isUploadAdditionalApplicationsEnabled()) {
            List<SupplementsBundle> updatedSupplementsBundle =
                unwrapElements(caseData.getTemporaryC2Document().getSupplementsBundle())
                    .stream()
                    .map(supplementsBundle -> supplementsBundle.toBuilder()
                        .dateTimeUploaded(time.now())
                        .uploadedBy(uploadedBy)
                        .build())
                    .collect(Collectors.toList());

            c2DocumentBundleBuilder.usePbaPayment(caseData.getUsePbaPayment())
                .pbaNumber(caseData.getPbaNumber())
                .clientCode(caseData.getClientCode())
                .fileReference(caseData.getFileReference())
                .supplementsBundle(wrapElements(updatedSupplementsBundle)
                );
        }

        c2DocumentBundle.add(element(c2DocumentBundleBuilder.build()));

        return c2DocumentBundle;
    }

    public List<String> validate(C2DocumentBundle c2DocumentBundle) {
        return Optional.ofNullable(c2DocumentBundle)
            .map(C2DocumentBundle::getSupportingEvidenceBundle)
            .map(validateSupportingEvidenceBundleService::validate)
            .orElse(emptyList());
    }
}
