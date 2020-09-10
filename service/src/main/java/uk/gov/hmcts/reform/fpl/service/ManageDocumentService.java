package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            return hearingFurtherEvidenceBundle.stream()
                .filter(element -> element.getId().equals(selectedHearingCode))
                .peek(element -> element.getValue().setSupportingEvidenceBundle(supportingEvidenceBundle))
                .collect(Collectors.toList());
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

        if (isSupportingEvidenceCollectionModified(supportingEvidenceBundle, supportingEvidenceBundleBefore)) {
            for (int i = 0; i < supportingEvidenceBundle.size(); i++) {
                if (!supportingEvidenceBundle.get(i).getValue().getDocument()
                    .equals(supportingEvidenceBundleBefore.get(i).getValue().getDocument())) {
                    supportingEvidenceBundle.get(i).getValue().setDateTimeUploaded(time.now());
                }
            }
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

        C2DocumentBundle c2DocumentBundle =
            caseData.getC2DocumentBundleByUUID(dynamicC2DocumentsList.getValueCode());

        List<Element<SupportingEvidenceBundle>> updatedCorrespondenceDocuments =
            setDateTimeUploadedOnSupportingEvidence(caseData.getC2SupportingDocuments(),
                c2DocumentBundle.getSupportingEvidenceBundle());

        return caseData.getC2DocumentBundle().stream()
            .peek(c2DocumentBundleElement -> {
                if (dynamicC2DocumentsList.getValue().getCode().equals(c2DocumentBundleElement.getId())) {
                    c2DocumentBundleElement.getValue().setSupportingEvidenceBundle(updatedCorrespondenceDocuments);
                }
            }).collect(Collectors.toList());
    }

    private Element<HearingFurtherEvidenceBundle> buildHearingSupportingEvidenceBundle(
        UUID hearingId, HearingBooking hearingBooking,
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {

        return element(hearingId, HearingFurtherEvidenceBundle.builder()
            .hearingName(hearingBooking.toLabel(DATE))
            .supportingEvidenceBundle(supportingEvidenceBundle)
            .build());
    }

    private boolean isSupportingEvidenceCollectionModified(List<Element<SupportingEvidenceBundle>> currentCollection,
                                                           List<Element<SupportingEvidenceBundle>> previousCollection) {
        return currentCollection != null && previousCollection != null && !currentCollection.equals(previousCollection)
            && currentCollection.size() == previousCollection.size();
    }
}
