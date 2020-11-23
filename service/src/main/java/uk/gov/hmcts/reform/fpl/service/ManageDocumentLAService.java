package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDocumentLAService {

    public static final String MANAGE_DOCUMENTS_HEARING_LIST_KEY = "manageDocumentsHearingList";
    public static final String COURT_BUNDLE_HEARING_LIST_KEY = "manageDocumentsCourtBundleHearingList";
    public static final String SUPPORTING_C2_LIST_KEY = "manageDocumentsSupportingC2List";
    public static final String MANAGE_DOCUMENT_KEY = "manageDocumentLA";

    public Map<String, Object> initialiseManageDocumentEvent(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();
        String hasHearings;

        if (caseData.getHearingDetails() != null && !caseData.getHearingDetails().isEmpty()) {
            listAndLabel.put(MANAGE_DOCUMENTS_HEARING_LIST_KEY, caseData.buildDynamicHearingList());
            listAndLabel.put(COURT_BUNDLE_HEARING_LIST_KEY, caseData.buildDynamicHearingList());
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
}
