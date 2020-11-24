package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDocumentLAService {
    private final ObjectMapper mapper;
    private final ManageDocumentService manageDocumentService;

    public static final String MANAGE_DOCUMENT_LA_KEY = "manageDocumentLA";
    public static final String MANAGE_DOCUMENTS_COURT_BUNDLE_HEARING_LIST_KEY = "manageDocumentsCourtBundleHearingList";
    public static final String FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY = "furtherEvidenceDocumentsLA";

    public Map<String, Object> initialiseManageDocumentEvent(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();
        String hasHearings;

        if (caseData.getHearingDetails() != null && !caseData.getHearingDetails().isEmpty()) {
            listAndLabel.put(MANAGE_DOCUMENTS_HEARING_LIST_KEY, caseData.buildDynamicHearingList());
            listAndLabel.put(MANAGE_DOCUMENTS_COURT_BUNDLE_HEARING_LIST_KEY, caseData.buildDynamicHearingList());
            hasHearings = YES.getValue();
        } else {
            hasHearings = NO.getValue();
        }

        ManageDocument manageDocument = ManageDocument.builder().hasHearings(hasHearings).build();

        if (caseData.hasC2DocumentBundle()) {
            listAndLabel.put(SUPPORTING_C2_LIST_KEY, caseData.buildC2DocumentDynamicList());
        }

        listAndLabel.put(MANAGE_DOCUMENT_LA_KEY, manageDocument);

        return listAndLabel;
    }

    public Map<String, Object> initialiseHearingListAndLabel(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();

        if (caseData.getManageDocumentLA().isDocumentRelatedToHearing()) {
            UUID selectedHearingCode = getDynamicListSelectedValue(caseData.getManageDocumentsHearingList(), mapper);
            Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingCode);

            if (hearingBooking.isEmpty()) {
                throw new IllegalStateException(
                    manageDocumentService.formatHearingBookingExceptionMessage(selectedHearingCode));
            }

            listAndLabel.put(MANAGE_DOCUMENTS_HEARING_LABEL_KEY, hearingBooking.get().getValue().toLabel());
            listAndLabel.put(MANAGE_DOCUMENTS_HEARING_LIST_KEY, caseData.buildDynamicHearingList(selectedHearingCode));
        }

        return listAndLabel;
    }

    public List<Element<SupportingEvidenceBundle>> getFurtherEvidenceCollection(CaseData caseData) {
        if (caseData.getManageDocumentLA().isDocumentRelatedToHearing()) {
            List<Element<HearingFurtherEvidenceBundle>> bundles = caseData.getHearingFurtherEvidenceDocuments();
            if (!bundles.isEmpty()) {
                UUID selectedHearingId = getDynamicListSelectedValue(caseData.getManageDocumentsHearingList(), mapper);

                Optional<Element<HearingFurtherEvidenceBundle>> bundle = findElement(
                    selectedHearingId, bundles
                );

                if (bundle.isPresent()) {
                    return bundle.get().getValue().getSupportingEvidenceBundle();
                }
            }
        } else if (caseData.getFurtherEvidenceDocumentsLA() != null) {
            return caseData.getFurtherEvidenceDocumentsLA();
        }

        return manageDocumentService.getEmptySupportingEvidenceBundle();
    }
}
