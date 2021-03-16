package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadAdditionalApplicationsService {

    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;

    public C2DocumentBundle buildC2DocumentBundle(CaseData caseData) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        List<SupportingEvidenceBundle> updatedSupportingEvidenceBundle =
            getSupportingEvidenceBundle(caseData.getTemporaryC2Document().getSupportingEvidenceBundle(), uploadedBy);

        List<Supplement> updatedSupplementsBundle =
            getSupplementsBundle(caseData.getTemporaryC2Document().getSupplementsBundle(), uploadedBy);

        return caseData.getTemporaryC2Document()
            .toBuilder()
            .author(uploadedBy)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME))
            .supplementsBundle(wrapElements(updatedSupplementsBundle))
            .supportingEvidenceBundle(wrapElements(updatedSupportingEvidenceBundle))
            .type(caseData.getC2Type()).build();
    }

    public OtherApplicationsBundle buildOtherApplicationsBundle(CaseData caseData) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        OtherApplicationsBundle temporaryOtherApplicationsBundle = caseData.getTemporaryOtherApplicationsBundle();

        List<SupportingEvidenceBundle> updatedSupportingEvidenceBundle = getSupportingEvidenceBundle(
            temporaryOtherApplicationsBundle.getSupportingEvidenceBundle(), uploadedBy);

        List<Supplement> updatedSupplementsBundle = getSupplementsBundle(
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
            .c2DocumentBundle(c2DocumentBundle)
            .otherApplicationsBundle(otherApplicationsBundle)
            .pbaPayment(temporaryPbaPayment)
            .author(uploadedBy)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME))
            .build();
    }

    public List<Element<C2DocumentBundle>> sortOldC2DocumentCollection(List<Element<C2DocumentBundle>>
                                                                           c2DocumentBundle) {
        c2DocumentBundle.sort(comparing(e -> e.getValue().getUploadedDateTime(), reverseOrder()));
        return c2DocumentBundle;
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

    private List<Supplement> getSupplementsBundle(
        List<Element<Supplement>> supplementsBundle, String uploadedBy) {
        return unwrapElements(supplementsBundle)
            .stream()
            .map(supplement -> supplement.toBuilder()
                .dateTimeUploaded(time.now())
                .uploadedBy(uploadedBy)
                .build())
            .collect(Collectors.toList());
    }
}
