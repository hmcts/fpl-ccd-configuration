package uk.gov.hmcts.reform.fpl.service.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.OTHER_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDocumentService {
    private final ObjectMapper mapper;
    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;
    private final UserService user;

    public static final String CORRESPONDING_DOCUMENTS_COLLECTION_KEY = "correspondenceDocuments";
    public static final String C2_DOCUMENTS_COLLECTION_KEY = "c2DocumentBundle";
    public static final String TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY = "supportingEvidenceDocumentsTemp";
    public static final String FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY = "furtherEvidenceDocuments";
    public static final String HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY = "hearingFurtherEvidenceDocuments";
    public static final String C2_SUPPORTING_DOCUMENTS_COLLECTION = "c2SupportingDocuments";
    public static final String MANAGE_DOCUMENTS_HEARING_LIST_KEY = "manageDocumentsHearingList";
    public static final String SUPPORTING_C2_LIST_KEY = "manageDocumentsSupportingC2List";
    public static final String MANAGE_DOCUMENTS_HEARING_LABEL_KEY = "manageDocumentsHearingLabel";
    public static final String SUPPORTING_C2_LABEL = "manageDocumentsSupportingC2Label";
    public static final String MANAGE_DOCUMENT_KEY = "manageDocument";

    public Map<String, Object> initialiseManageDocumentEvent(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();
        String hasHearings;
        String hasC2s;

        if (caseData.getHearingDetails() != null && !caseData.getHearingDetails().isEmpty()) {
            listAndLabel.put(MANAGE_DOCUMENTS_HEARING_LIST_KEY, caseData.buildDynamicHearingList());

            hasHearings = YES.getValue();
        } else {
            hasHearings = NO.getValue();
        }

        //If toggle not on, replicate old behaviour (always show C2 list, even if empty)
        if (caseData.hasC2DocumentBundle()) {
            listAndLabel.put(SUPPORTING_C2_LIST_KEY, caseData.buildC2DocumentDynamicList());
            hasC2s = YES.getValue();
        } else {
            hasC2s = NO.getValue();
        }

        ManageDocument manageDocument = ManageDocument.builder()
            .hasHearings(hasHearings)
            .hasC2s(hasC2s)
            .build();

        listAndLabel.put(MANAGE_DOCUMENT_KEY, manageDocument);

        return listAndLabel;
    }

    public Map<String, Object> initialiseHearingListAndLabel(CaseData caseData, boolean relatedToHearing) {
        Map<String, Object> listAndLabel = new HashMap<>();

        if (relatedToHearing) {
            UUID selectedHearingCode = getDynamicListSelectedValue(caseData.getManageDocumentsHearingList(), mapper);
            Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingCode);

            if (hearingBooking.isEmpty()) {
                throw new NoHearingBookingException(selectedHearingCode);
            }

            listAndLabel.put(MANAGE_DOCUMENTS_HEARING_LABEL_KEY, hearingBooking.get().getValue().toLabel());
            listAndLabel.put(MANAGE_DOCUMENTS_HEARING_LIST_KEY, caseData.buildDynamicHearingList(selectedHearingCode));
        }

        return listAndLabel;
    }

    public Map<String, Object> initialiseC2DocumentListAndLabel(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();

        UUID selectedC2DocumentId = getDynamicListSelectedValue(caseData.getManageDocumentsSupportingC2List(), mapper);
        List<Element<C2DocumentBundle>> c2DocumentBundle = caseData.getC2DocumentBundle();

        for (int i = 0; i < c2DocumentBundle.size(); i++) {
            if (c2DocumentBundle.get(i).getId().equals(selectedC2DocumentId)) {
                listAndLabel.put(SUPPORTING_C2_LABEL, c2DocumentBundle.get(i).getValue().toLabel(i + 1));
                break;
            }
        }

        listAndLabel.put(SUPPORTING_C2_LIST_KEY, caseData.buildC2DocumentDynamicList(selectedC2DocumentId));

        return listAndLabel;
    }

    public List<Element<SupportingEvidenceBundle>> getFurtherEvidenceCollection(
        CaseData caseData,
        boolean isDocumentRelatedToHearing,
        List<Element<SupportingEvidenceBundle>> unrelatedEvidence) {
        if (isDocumentRelatedToHearing) {
            List<Element<HearingFurtherEvidenceBundle>> bundles = caseData.getHearingFurtherEvidenceDocuments();
            if (!bundles.isEmpty()) {
                UUID selectedHearingId = getDynamicListSelectedValue(caseData.getManageDocumentsHearingList(), mapper);

                Optional<Element<HearingFurtherEvidenceBundle>> bundle = findElement(selectedHearingId, bundles);

                if (bundle.isPresent()) {
                    List<Element<SupportingEvidenceBundle>> evidenceBundle
                        = bundle.get().getValue().getSupportingEvidenceBundle();

                    setDefaultEvidenceType(evidenceBundle);
                    return getUserSpecificSupportingEvidences(evidenceBundle);
                }
            }
        } else if (isNotEmpty(unrelatedEvidence)) {
            setDefaultEvidenceType(unrelatedEvidence);
            return unrelatedEvidence;
        }

        return defaultSupportingEvidences();
    }

    public List<Element<SupportingEvidenceBundle>> getC2SupportingEvidenceBundle(CaseData caseData) {
        UUID selectedC2 = getDynamicListSelectedValue(caseData.getManageDocumentsSupportingC2List(), mapper);
        C2DocumentBundle c2DocumentBundle = caseData.getC2DocumentBundleByUUID(selectedC2);

        return getUserSpecificSupportingEvidences(c2DocumentBundle.getSupportingEvidenceBundle());
    }

    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle(
        List<Element<SupportingEvidenceBundle>> supportingEvidences) {
        return isEmpty(supportingEvidences) ? defaultSupportingEvidences() : supportingEvidences;
    }

    public List<Element<HearingFurtherEvidenceBundle>> buildHearingFurtherEvidenceCollection(
        CaseData caseData, List<Element<SupportingEvidenceBundle>> modifiedEvidence) {

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundle
            = caseData.getHearingFurtherEvidenceDocuments();

        UUID selectedHearingCode = getDynamicListSelectedValue(caseData.getManageDocumentsHearingList(), mapper);
        Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingCode);

        if (hearingBooking.isEmpty()) {
            throw new NoHearingBookingException(selectedHearingCode);
        }

        if (caseData.documentBundleContainsHearingId(selectedHearingCode)) {
            for (Element<HearingFurtherEvidenceBundle> element : hearingFurtherEvidenceBundle) {
                if (element.getId().equals(selectedHearingCode)) {
                    if (caseData.getSupportingEvidenceDocumentsTemp().isEmpty()) {
                        hearingFurtherEvidenceBundle.remove(element);
                        return hearingFurtherEvidenceBundle;
                    }
                    List<Element<SupportingEvidenceBundle>> existingEvidence =
                        new ArrayList<>(element.getValue().getSupportingEvidenceBundle());

                    updateExistingEvidenceWithChanges(existingEvidence, modifiedEvidence);
                    sortByDateUploaded(existingEvidence);

                    element.getValue().setSupportingEvidenceBundle(existingEvidence);
                }
            }
            return hearingFurtherEvidenceBundle;
        } else {
            hearingFurtherEvidenceBundle.add(buildHearingSupportingEvidenceBundle(
                selectedHearingCode,
                hearingBooking.get().getValue(),
                modifiedEvidence
            ));
            return hearingFurtherEvidenceBundle;
        }
    }

    public List<Element<SupportingEvidenceBundle>> setDateTimeUploadedOnSupportingEvidence(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle,
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleBefore) {

        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        if (!Objects.equals(supportingEvidenceBundle, supportingEvidenceBundleBefore)) {
            List<Element<SupportingEvidenceBundle>> altered = new ArrayList<>(supportingEvidenceBundle);

            // Could be null in the case of C2 supporting documents
            if (supportingEvidenceBundleBefore != null) {
                altered.removeAll(supportingEvidenceBundleBefore);
            }

            altered.forEach(bundle -> findElement(bundle.getId(), supportingEvidenceBundleBefore).ifPresent(
                previousVersion -> {
                    if (!previousVersion.getValue().getDocument().equals(bundle.getValue().getDocument())) {
                        bundle.getValue().setDateTimeUploaded(time.now());
                        bundle.getValue().setUploadedBy(uploadedBy);
                    }
                }
            ));
        }

        List<Element<SupportingEvidenceBundle>> updatedBundles = new ArrayList<>();
        for (Element<SupportingEvidenceBundle> supportingEvidenceBundleElement : supportingEvidenceBundle) {
            if (supportingEvidenceBundleElement.getValue().getDateTimeUploaded() == null) {
                supportingEvidenceBundleElement.getValue().setDateTimeUploaded(time.now());
                supportingEvidenceBundleElement.getValue().setUploadedBy(uploadedBy);
            }
            updatedBundles.add(supportingEvidenceBundleElement);
        }
        return updatedBundles;
    }

    public List<Element<C2DocumentBundle>> buildFinalC2SupportingDocuments(CaseData caseData) {
        UUID selected = getDynamicListSelectedValue(caseData.getManageDocumentsSupportingC2List(), mapper);

        C2DocumentBundle c2DocumentBundle = caseData.getC2DocumentBundleByUUID(selected);

        List<Element<SupportingEvidenceBundle>> modifiedEvidence =
            setDateTimeUploadedOnSupportingEvidence(caseData.getSupportingEvidenceDocumentsTemp(),
                c2DocumentBundle.getSupportingEvidenceBundle());

        List<Element<C2DocumentBundle>> c2Bundles = caseData.getC2DocumentBundle();
        for (Element<C2DocumentBundle> element : c2Bundles) {
            if (selected.equals(element.getId())) {
                List<Element<SupportingEvidenceBundle>> existingEvidence
                    = new ArrayList<>(element.getValue().getSupportingEvidenceBundle());

                updateExistingEvidenceWithChanges(existingEvidence, modifiedEvidence);
                sortByDateUploaded(existingEvidence);

                element.getValue().setSupportingEvidenceBundle(existingEvidence);
            }
        }
        return c2Bundles;
    }

    public List<Element<SupportingEvidenceBundle>> setDateTimeOnHearingFurtherEvidenceSupportingEvidence(
        CaseData caseData, CaseData caseDataBefore) {
        List<Element<SupportingEvidenceBundle>> currentSupportingDocuments
            = caseData.getSupportingEvidenceDocumentsTemp();

        UUID selectedHearingCode = getDynamicListSelectedValue(caseData.getManageDocumentsHearingList(), mapper);

        List<Element<SupportingEvidenceBundle>> previousSupportingDocuments =
            findElement(selectedHearingCode, caseDataBefore.getHearingFurtherEvidenceDocuments())
                .map(Element::getValue)
                .map(HearingFurtherEvidenceBundle::getSupportingEvidenceBundle)
                .orElse(List.of());

        return setDateTimeUploadedOnSupportingEvidence(currentSupportingDocuments, previousSupportingDocuments);
    }

    // Separate collection based on idam role (only show users their own documents)
    private List<Element<SupportingEvidenceBundle>> getUserSpecificSupportingEvidences(
        List<Element<SupportingEvidenceBundle>> bundles) {

        Predicate<Element<SupportingEvidenceBundle>> bundleFilter = bundle -> bundle.getValue().isUploadedByHMCTS();

        if (!user.isHmctsUser()) {
            bundleFilter = bundleFilter.negate();
        }

        List<Element<SupportingEvidenceBundle>> supportingEvidences = bundles.stream()
            .filter(bundleFilter)
            .collect(Collectors.toList());

        return isEmpty(supportingEvidences) ? defaultSupportingEvidences() : supportingEvidences;
    }

    private void setDefaultEvidenceType(List<Element<SupportingEvidenceBundle>> unrelatedEvidence) {
        unrelatedEvidence.stream()
            .map(Element::getValue)
            .filter(bundle -> bundle.getType() == null)
            .forEach(bundle -> bundle.setType(OTHER_REPORTS));
    }

    private Element<HearingFurtherEvidenceBundle> buildHearingSupportingEvidenceBundle(
        UUID hearingId, HearingBooking hearingBooking,
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {

        return element(hearingId, HearingFurtherEvidenceBundle.builder()
            .hearingName(hearingBooking.toLabel())
            .supportingEvidenceBundle(supportingEvidenceBundle)
            .build());
    }

    private List<Element<SupportingEvidenceBundle>> defaultSupportingEvidences() {
        return List.of(element(SupportingEvidenceBundle.builder().build()));
    }

    private void updateExistingEvidenceWithChanges(List<Element<SupportingEvidenceBundle>> existingEvidence,
                                                   List<Element<SupportingEvidenceBundle>> updatedEvidence) {
        List<Element<SupportingEvidenceBundle>> userSpecificDocuments
            = getUserSpecificSupportingEvidences(existingEvidence);

        existingEvidence.removeAll(userSpecificDocuments);

        existingEvidence.addAll(updatedEvidence);
    }

    private void sortByDateUploaded(List<Element<SupportingEvidenceBundle>> evidence) {
        evidence.sort((ele1, ele2) -> {
            LocalDateTime date1 = defaultIfNull(ele1.getValue().getDateTimeUploaded(), LocalDateTime.MAX);
            LocalDateTime date2 = defaultIfNull(ele2.getValue().getDateTimeUploaded(), LocalDateTime.MAX);
            return date1.compareTo(date2);
        });
    }
}
