package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public List<Element<C2DocumentBundle>> buildC2DocumentBundle(CaseData caseData) {
        List<Element<C2DocumentBundle>> c2DocumentBundle = defaultIfNull(
            caseData.getC2DocumentBundle(), new ArrayList<>()
        );

        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        List<SupportingEvidenceBundle> updatedSupportingEvidenceBundle =
            getSupportingEvidenceBundle(caseData.getTemporaryC2Document().getSupportingEvidenceBundle(), uploadedBy);

        C2DocumentBundle.C2DocumentBundleBuilder c2DocumentBundleBuilder = caseData.getTemporaryC2Document()
            .toBuilder()
            .author(uploadedBy)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME))
            .supportingEvidenceBundle(wrapElements(updatedSupportingEvidenceBundle))
            .type(caseData.getC2ApplicationType().get("type"));

        c2DocumentBundle.add(element(c2DocumentBundleBuilder.build()));

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

    public List<String> validate(C2DocumentBundle c2DocumentBundle) {
        return Optional.ofNullable(c2DocumentBundle)
            .map(C2DocumentBundle::getSupportingEvidenceBundle)
            .map(validateSupportingEvidenceBundleService::validate)
            .orElse(emptyList());
    }

    public OtherApplicationsBundle buildOtherApplicationsBundle(CaseData caseData) {
        OtherApplicationsBundle c2DocumentBundle = caseData.getOtherApplicationsBundle();
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();
        List<SupportingEvidenceBundle> updatedSupportingEvidenceBundle = getSupportingEvidenceBundle(
            caseData.getTemporaryOtherApplicationsBundle().getSupportingEvidenceBundle(), uploadedBy);

        C2DocumentBundle temporaryC2Document = caseData.getTemporaryC2Document();
        C2DocumentBundle.C2DocumentBundleBuilder c2DocumentBundleBuilder = temporaryC2Document
            .toBuilder()
            .author(uploadedBy)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME))
            .supportingEvidenceBundle(wrapElements(updatedSupportingEvidenceBundle))
            .type(caseData.getC2ApplicationType().get("type"));

        //c2DocumentBundle.add(element(c2DocumentBundleBuilder.build()));

        return c2DocumentBundle;
    }
}
