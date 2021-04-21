package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadAdditionalApplicationsService {

    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;

    public AdditionalApplicationsBundle buildAdditionalApplicationsBundle(CaseData caseData) {
        final String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();
        final LocalDateTime currentDateTime = time.now();

        C2DocumentBundle c2DocumentBundle = null;
        OtherApplicationsBundle otherApplicationsBundle = null;

        if (caseData.getAdditionalApplicationType().contains(AdditionalApplicationType.C2_ORDER)) {
            c2DocumentBundle = buildC2DocumentBundle(caseData, uploadedBy, currentDateTime);
        }

        if (caseData.getAdditionalApplicationType().contains(AdditionalApplicationType.OTHER_ORDER)) {
            otherApplicationsBundle = buildOtherApplicationsBundle(caseData, uploadedBy, currentDateTime);
        }

        return AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(c2DocumentBundle)
            .otherApplicationsBundle(otherApplicationsBundle)
            .pbaPayment(caseData.getTemporaryPbaPayment())
            .author(uploadedBy)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(currentDateTime, DATE_TIME))
            .build();
    }

    private C2DocumentBundle buildC2DocumentBundle(CaseData caseData, String uploadedBy, LocalDateTime uploadedTime) {
        List<Element<SupportingEvidenceBundle>> updatedSupportingEvidenceBundle = getSupportingEvidenceBundle(
            caseData.getTemporaryC2Document().getSupportingEvidenceBundle(), uploadedBy, uploadedTime);

        List<Element<Supplement>> updatedSupplementsBundle =
            getSupplementsBundle(defaultIfNull(caseData.getTemporaryC2Document().getSupplementsBundle(), emptyList()),
                uploadedBy, uploadedTime);

        return caseData.getTemporaryC2Document()
            .toBuilder()
            .id(UUID.randomUUID())
            .author(uploadedBy)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(uploadedTime, DATE_TIME))
            .supplementsBundle(updatedSupplementsBundle)
            .supportingEvidenceBundle(updatedSupportingEvidenceBundle)
            .type(caseData.getC2Type()).build();
    }

    private OtherApplicationsBundle buildOtherApplicationsBundle(
        CaseData caseData, String uploadedBy, LocalDateTime uploadedTime) {

        OtherApplicationsBundle temporaryOtherApplicationsBundle = caseData.getTemporaryOtherApplicationsBundle();

        List<Element<SupportingEvidenceBundle>> updatedSupportingEvidenceBundle = getSupportingEvidenceBundle(
            caseData.getTemporaryOtherApplicationsBundle().getSupportingEvidenceBundle(), uploadedBy, uploadedTime);

        List<Element<Supplement>> updatedSupplementsBundle = getSupplementsBundle(
            temporaryOtherApplicationsBundle.getSupplementsBundle(), uploadedBy, uploadedTime);

        return temporaryOtherApplicationsBundle.toBuilder()
            .author(uploadedBy)
            .id(UUID.randomUUID())
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(uploadedTime, DATE_TIME))
            .applicationType(temporaryOtherApplicationsBundle.getApplicationType())
            .document(temporaryOtherApplicationsBundle.getDocument())
            .supportingEvidenceBundle(updatedSupportingEvidenceBundle)
            .supplementsBundle(updatedSupplementsBundle)
            .build();
    }

    public List<Element<C2DocumentBundle>> sortOldC2DocumentCollection(
        List<Element<C2DocumentBundle>> c2DocumentBundle) {
        c2DocumentBundle.sort(comparing(e -> e.getValue().getUploadedDateTime(), reverseOrder()));
        return c2DocumentBundle;
    }

    private List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle,
        String uploadedBy, LocalDateTime uploadedDateTime) {

        supportingEvidenceBundle.forEach(supportingEvidence -> {
            supportingEvidence.getValue().setDateTimeUploaded(uploadedDateTime);
            supportingEvidence.getValue().setUploadedBy(uploadedBy);
        });

        return supportingEvidenceBundle;
    }

    private List<Element<Supplement>> getSupplementsBundle(
        List<Element<Supplement>> supplementsBundle, String uploadedBy, LocalDateTime dateTime) {
        supplementsBundle.forEach(supplementElement -> {
            supplementElement.getValue().setUploadedBy(uploadedBy);
            supplementElement.getValue().setDateTimeUploaded(dateTime);
        });

        return supplementsBundle;
    }
}
