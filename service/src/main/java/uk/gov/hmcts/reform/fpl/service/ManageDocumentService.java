package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.getHearingBookingByUUID;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListValueCode;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDocumentService {
    private final ObjectMapper mapper;
    private final Time time;

    public static final String CORRESPONDING_DOCUMENTS_COLLECTION_KEY = "correspondenceDocuments";
    public static final String C2_DOCUMENTS_COLLECTION_KEY = "c2DocumentBundle";
    public static final String TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY = "furtherEvidenceDocumentsTEMP";
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

        if (caseData.getHearingDetails() != null && !caseData.getHearingDetails().isEmpty()) {
            listAndLabel.put(MANAGE_DOCUMENTS_HEARING_LIST_KEY, caseData.buildDynamicHearingList());
            hasHearings = YES.getValue();
        } else {
            hasHearings = NO.getValue();
        }

        ManageDocument manageDocument = ManageDocument.builder().hasHearings(hasHearings).build();

        if (caseData.hasC2DocumentBundle()) {
            listAndLabel.put(SUPPORTING_C2_LIST_KEY, caseData.buildC2DocumentDynamicList());
        }

        listAndLabel.put(MANAGE_DOCUMENT_KEY, manageDocument);

        return listAndLabel;
    }

    public Map<String, Object> initialiseHearingListAndLabel(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();

        if (caseData.getManageDocument().isDocumentRelatedToHearing()) {
            UUID selectedHearingCode = getDynamicListValueCode(caseData.getManageDocumentsHearingList(), mapper);
            HearingBooking hearingBooking = getHearingBookingByUUID(caseData.getHearingDetails(), selectedHearingCode);

            listAndLabel.put(MANAGE_DOCUMENTS_HEARING_LABEL_KEY, hearingBooking.toLabel(DATE));
            listAndLabel.put(MANAGE_DOCUMENTS_HEARING_LIST_KEY, caseData.buildDynamicHearingList(selectedHearingCode));
        }

        return listAndLabel;
    }

    public Map<String, Object> initialiseC2DocumentListAndLabel(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();

        UUID selectedC2DocumentCode = getDynamicListValueCode(caseData.getManageDocumentsSupportingC2List(), mapper);
        List<Element<C2DocumentBundle>> c2DocumentBundle = caseData.getC2DocumentBundle();

        IntStream.range(0, c2DocumentBundle.size())
            .filter(index -> c2DocumentBundle.get(index).getId().equals(selectedC2DocumentCode))
            .findFirst()
            .ifPresent(index -> listAndLabel.put(SUPPORTING_C2_LABEL,
                c2DocumentBundle.get(index).getValue().toLabel(index + 1)));

        listAndLabel.put(SUPPORTING_C2_LIST_KEY, caseData.buildC2DocumentDynamicList(selectedC2DocumentCode));

        return listAndLabel;
    }

    public List<Element<SupportingEvidenceBundle>> getFurtherEvidenceCollection(CaseData caseData) {
        if (caseData.getManageDocument().isDocumentRelatedToHearing()) {
            List<Element<HearingFurtherEvidenceBundle>> bundles = caseData.getHearingFurtherEvidenceDocuments();
            if (!bundles.isEmpty()) {
                UUID selectedHearingCode = getDynamicListValueCode(caseData.getManageDocumentsHearingList(), mapper);

                Optional<Element<HearingFurtherEvidenceBundle>> bundle = findElement(
                    selectedHearingCode, bundles
                );

                if (bundle.isPresent()) {
                    return bundle.get().getValue().getSupportingEvidenceBundle();
                }
            }
        } else if (caseData.getFurtherEvidenceDocuments() != null) {
            return caseData.getFurtherEvidenceDocuments();
        }

        return getSupportingEvidenceBundle(null);
    }

    public List<Element<SupportingEvidenceBundle>> getC2SupportingEvidenceBundle(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        DynamicList dynamicC2DocumentsList = mapper.convertValue(caseDetails.getData().get(SUPPORTING_C2_LIST_KEY),
            DynamicList.class);

        C2DocumentBundle c2DocumentBundle =
            caseData.getC2DocumentBundleByUUID(dynamicC2DocumentsList.getValueCode());

        if (c2DocumentBundle.getSupportingEvidenceBundle() != null) {
            return c2DocumentBundle.getSupportingEvidenceBundle();
        }

        return getSupportingEvidenceBundle(null);
    }

    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleList) {
        if (supportingEvidenceBundleList == null || supportingEvidenceBundleList.isEmpty()) {
            return List.of(element(SupportingEvidenceBundle.builder().build()));
        }

        return supportingEvidenceBundleList;
    }

    public List<Element<HearingFurtherEvidenceBundle>> buildHearingFurtherEvidenceCollection(
        CaseData caseData, List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundle;

        hearingFurtherEvidenceBundle = caseData.getHearingFurtherEvidenceDocuments();
        UUID selectedHearingCode = getDynamicListValueCode(caseData.getManageDocumentsHearingList(), mapper);
        HearingBooking hearingBooking = getHearingBookingByUUID(caseData.getHearingDetails(), selectedHearingCode);

        if (caseData.documentBundleContainsHearingId(selectedHearingCode)) {
            List<Element<HearingFurtherEvidenceBundle>> list = new ArrayList<>();
            for (Element<HearingFurtherEvidenceBundle> element : hearingFurtherEvidenceBundle) {
                if (element.getId().equals(selectedHearingCode)) {
                    element.getValue().setSupportingEvidenceBundle(supportingEvidenceBundle);
                }
                list.add(element);
            }
            return list;
        } else {
            hearingFurtherEvidenceBundle.add(buildHearingSupportingEvidenceBundle(
                selectedHearingCode,
                hearingBooking,
                supportingEvidenceBundle
            ));
            return hearingFurtherEvidenceBundle;
        }
    }

    public List<Element<SupportingEvidenceBundle>> setDateTimeUploadedOnSupportingEvidence(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle,
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleBefore) {

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
                    }
                }
            ));
        }

        return supportingEvidenceBundle.stream()
            .peek(supportingEvidenceBundleElement -> {
                if (supportingEvidenceBundleElement.getValue().getDateTimeUploaded() == null) {
                    supportingEvidenceBundleElement.getValue().setDateTimeUploaded(time.now());
                }
            }).collect(Collectors.toList());
    }

    public List<Element<C2DocumentBundle>> buildFinalC2SupportingDocuments(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        DynamicList dynamicC2DocumentsList = mapper.convertValue(caseDetails.getData().get(SUPPORTING_C2_LIST_KEY),
            DynamicList.class);
        UUID selected = dynamicC2DocumentsList.getValueCode();

        C2DocumentBundle c2DocumentBundle = caseData.getC2DocumentBundleByUUID(selected);

        List<Element<SupportingEvidenceBundle>> updatedCorrespondenceDocuments =
            setDateTimeUploadedOnSupportingEvidence(caseData.getC2SupportingDocuments(),
                c2DocumentBundle.getSupportingEvidenceBundle());

        List<Element<C2DocumentBundle>> list = new ArrayList<>();
        for (Element<C2DocumentBundle> c2DocumentBundleElement : caseData.getC2DocumentBundle()) {
            if (selected.equals(c2DocumentBundleElement.getId())) {
                c2DocumentBundleElement.getValue().setSupportingEvidenceBundle(updatedCorrespondenceDocuments);
            }
            list.add(c2DocumentBundleElement);
        }
        return list;
    }

    private Element<HearingFurtherEvidenceBundle> buildHearingSupportingEvidenceBundle(
        UUID hearingId, HearingBooking hearingBooking,
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {

        return element(hearingId, HearingFurtherEvidenceBundle.builder()
            .hearingName(hearingBooking.toLabel(DATE))
            .supportingEvidenceBundle(supportingEvidenceBundle)
            .build());
    }
}
