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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.getHearingBookingByUUID;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListValueCode;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDocumentService {
    private final ObjectMapper mapper;
    private final Time time;

    public static final String CORRESPONDING_DOCUMENTS_COLLECTION_KEY = "correspondenceDocuments";
    public static final String TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY = "furtherEvidenceDocumentsTEMP";
    public static final String FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY = "furtherEvidenceDocuments";
    public static final String HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY = "hearingFurtherEvidenceDocuments";
    public static final String MANAGE_DOCUMENTS_HEARING_LIST_KEY = "manageDocumentsHearingList";
    public static final String SUPPORTING_C2_LIST_KEY = "manageDocumentsSupportingC2List";
    public static final String MANAGE_DOCUMENTS_HEARING_LABEL_KEY = "manageDocumentsHearingLabel";
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

    public List<Element<SupportingEvidenceBundle>> getFurtherEvidenceCollection(CaseData caseData) {

        if (caseData.getManageDocument().isDocumentRelatedToHearing()
            && caseData.getHearingFurtherEvidenceDocuments() != null
            && !caseData.getHearingFurtherEvidenceDocuments().isEmpty()) {

            UUID selectedHearingCode = getDynamicListValueCode(caseData.getManageDocumentsHearingList(), mapper);

            if (caseData.documentBundleContainsHearingId(selectedHearingCode)) {
                for (int i = 0; i < caseData.getHearingFurtherEvidenceDocuments().size(); i++) {
                    Element<HearingFurtherEvidenceBundle> bundle = caseData.getHearingFurtherEvidenceDocuments().get(i);

                    if (selectedHearingCode.equals(bundle.getId())) {
                        return bundle.getValue().getSupportingEvidenceBundle();
                    }
                }
            }

        } else if (caseData.getFurtherEvidenceDocuments() != null) {
            return caseData.getFurtherEvidenceDocuments();
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

    public void buildFinalFurtherEvidenceCollection(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceDocuments;

        if (caseData.getManageDocument().isDocumentRelatedToHearing()) {
            DynamicList hearingList = mapper.convertValue(caseDetails.getData().get(MANAGE_DOCUMENTS_HEARING_LIST_KEY),
                DynamicList.class);

            UUID selectedHearingCode = hearingList.getValue().getCode();
            HearingBooking hearingBooking = getHearingBookingByUUID(caseData.getHearingDetails(), selectedHearingCode);

            if (caseData.getHearingFurtherEvidenceDocuments() == null) {
                hearingFurtherEvidenceDocuments = List.of(
                    buildHearingSupportingEvidenceBundle(selectedHearingCode, hearingBooking,
                        caseData.getFurtherEvidenceDocumentsTEMP()));
            } else if (caseData.documentBundleContainsHearingId(selectedHearingCode)) {
                hearingFurtherEvidenceDocuments = caseData.getHearingFurtherEvidenceDocuments().stream()
                    .filter(element -> element.getId().equals(selectedHearingCode))
                    .peek(element ->
                        element.getValue().setSupportingEvidenceBundle(caseData.getFurtherEvidenceDocumentsTEMP()))
                    .collect(Collectors.toList());
            } else {
                Element<HearingFurtherEvidenceBundle> hearingFurtherEvidenceBundleElement =
                    buildHearingSupportingEvidenceBundle(selectedHearingCode, hearingBooking,
                        caseData.getFurtherEvidenceDocumentsTEMP());

                caseData.getHearingFurtherEvidenceDocuments().add(hearingFurtherEvidenceBundleElement);
                hearingFurtherEvidenceDocuments = caseData.getHearingFurtherEvidenceDocuments();
            }

            caseDetails.getData().put(HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY,
                hearingFurtherEvidenceDocuments);
        } else {
            caseDetails.getData().put(FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY,
                caseData.getFurtherEvidenceDocumentsTEMP());
        }
    }

    public List<Element<SupportingEvidenceBundle>> setDateTimeUploadedOnSupportingEvidence(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle,
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleBefore) {

        if (!supportingEvidenceBundle.equals(supportingEvidenceBundleBefore)
            && supportingEvidenceBundle.size() == supportingEvidenceBundleBefore.size()) {
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

    private Element<HearingFurtherEvidenceBundle> buildHearingSupportingEvidenceBundle(
        UUID hearingId, HearingBooking hearingBooking,
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {

        return element(hearingId, HearingFurtherEvidenceBundle.builder()
            .hearingName(hearingBooking.toLabel(DATE))
            .supportingEvidenceBundle(supportingEvidenceBundle)
            .build());
    }
}
