package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.getHearingBookingByUUID;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Service
public class ManageDocumentService {
    private ObjectMapper mapper;
    private Time time;

    public static final String CORRESPONDING_DOCUMENTS_COLLECTION_KEY = "correspondenceDocuments";
    public static final String FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY = "furtherEvidenceDocuments";
    public static final String MANAGE_DOCUMENTS_HEARING_LIST_KEY = "manageDocumentsHearingList";
    public static final String MANAGE_DOCUMENTS_HEARING_LABEL_KEY = "manageDocumentsHearingLabel";

    @Autowired
    public ManageDocumentService(ObjectMapper objectMapper, Time time) {
        this.mapper = objectMapper;
        this.time = time;
    }

    public void initialiseManageDocumentBundleCollectionManageDocumentFields(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        switch (caseData.getManageDocument().getType()) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                initialiseFurtherEvidenceFields(caseDetails);
                initialiseManageDocumentBundleCollection(caseDetails, FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY);
                break;
            case CORRESPONDENCE:
                initialiseManageDocumentBundleCollection(caseDetails, CORRESPONDING_DOCUMENTS_COLLECTION_KEY);
                break;
            case C2:
                // TODO
                // Populate data for case type is C2
                break;
        }
    }

    private void initialiseFurtherEvidenceFields(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getManageDocument().isDocumentRelatedToHearing()) {
            UUID selectedHearingCode = mapper.convertValue(caseDetails.getData().get(MANAGE_DOCUMENTS_HEARING_LIST_KEY),
                UUID.class);
            HearingBooking hearingBooking = getHearingBookingByUUID(caseData.getHearingDetails(), selectedHearingCode);

            caseDetails.getData().put(MANAGE_DOCUMENTS_HEARING_LABEL_KEY, hearingBooking.toLabel(DATE));
            caseDetails.getData().put(MANAGE_DOCUMENTS_HEARING_LIST_KEY,
                caseData.buildDynamicHearingList(selectedHearingCode));
        }
    }

    private void initialiseManageDocumentBundleCollection(CaseDetails caseDetails, String collectionKey) {
        if (caseDetails.getData().get(collectionKey) == null) {
            List<Element<ManageDocumentBundle>> documentBundle = wrapElements(ManageDocumentBundle.builder()
                .build());
            caseDetails.getData().put(collectionKey, documentBundle);
        }
    }
}
