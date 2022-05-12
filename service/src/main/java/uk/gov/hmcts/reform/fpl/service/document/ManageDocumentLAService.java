package uk.gov.hmcts.reform.fpl.service.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.CHILDREN_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_DOCUMENT_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.RESPONDENT_LIST_KEY;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDocumentLAService {
    private final ObjectMapper mapper;

    public static final String MANAGE_DOCUMENT_LA_KEY = "manageDocumentLA";
    public static final String FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY = "furtherEvidenceDocumentsLA";
    public static final String DOCUMENT_SUB_TYPE = "manageDocumentSubtypeListLA";
    public static final String RELATED_TO_HEARING = "manageDocumentsRelatedToHearing";
    public static final String CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY = "correspondenceDocumentsLA";
    public static final String SUPPORTING_C2_LIST_KEY = "manageDocumentsSupportingC2List";
    public static final String RESPONDENTS_LIST_KEY = "respondentStatementList";

    public Map<String, Object> baseEventData(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();

        ManageDocumentLA manageDocument = defaultIfNull(caseData.getManageDocumentLA(),
            ManageDocumentLA.builder().build())
            .toBuilder()
            .hasHearings(YesNo.from(isNotEmpty(caseData.getHearingDetails())).getValue())
            .hasC2s(YesNo.from(caseData.hasApplicationBundles()).getValue())
            .build();

        listAndLabel.put(MANAGE_DOCUMENT_LA_KEY, manageDocument);

        if (isNotEmpty(caseData.getHearingDetails())) {
            listAndLabel.put(HEARING_DOCUMENT_HEARING_LIST_KEY, caseData.buildDynamicHearingList());
        }

        if (caseData.hasApplicationBundles()) {
            listAndLabel.put(SUPPORTING_C2_LIST_KEY, caseData.buildApplicationBundlesDynamicList());
        }

        if (isNotEmpty(caseData.getAllRespondents())) {
            listAndLabel.put(RESPONDENTS_LIST_KEY, caseData.buildRespondentDynamicList());
            listAndLabel.put(RESPONDENT_LIST_KEY, caseData.buildRespondentDynamicList());
        }

        if (isNotEmpty(caseData.getAllChildren())) {
            listAndLabel.put(CHILDREN_LIST_KEY, caseData.buildDynamicChildrenList());
        }

        return listAndLabel;
    }

//    public Map<String, Object> initialiseCourtBundleFields(CaseData caseData) {
//        Map<String, Object> map = new HashMap<>();
//        map.put(COURT_BUNDLE_KEY, getCourtBundleForHearing((caseData)));
//        return map;
//    }

//    public List<Element<HearingCourtBundle>> buildCourtBundleList(CaseData caseData) {
//        List<Element<HearingCourtBundle>> courtBundleList = caseData.getCourtBundleListV2();
//
//        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getCourtBundleHearingList(), mapper);
//
//        Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingId);
//        if (hearingBooking.isEmpty()) {
//            throw new NoHearingBookingException(selectedHearingId);
//        }
//
//        if (isNotEmpty(caseData.getHearingDetails())) {
//            List<Element<CourtBundle>> courtBundleNC = caseData.getManageDocumentsCourtBundle().stream()
//                .filter(doc -> !doc.getValue().isConfidentialDocument())
//                .collect(Collectors.toList());
//
//            return List.of(element(selectedHearingId, HearingCourtBundle.builder()
//                .hearing(hearingBooking.get().getValue().toLabel())
//                .courtBundle(caseData.getManageDocumentsCourtBundle())
//                .courtBundleNC(courtBundleNC)
//                .build()));
//        }
//        return courtBundleList;
//    }

//    public Map<String, Object> initialiseCourtBundleHearingListAndLabel(CaseData caseData) {
//        Map<String, Object> listAndLabel = new HashMap<>();
//        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getCourtBundleHearingList(), mapper);
//        Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(
//            selectedHearingId);
//
//        if (hearingBooking.isEmpty()) {
//            throw new NoHearingBookingException(selectedHearingId);
//        }
//        listAndLabel.put(COURT_BUNDLE_HEARING_LABEL_KEY, hearingBooking.get().getValue().toLabel());
//        listAndLabel.put(COURT_BUNDLE_HEARING_LIST_KEY, caseData.buildDynamicHearingList(selectedHearingId));
//
//        return listAndLabel;
//    }

//    private List<Element<CourtBundle>> getCourtBundleForHearing(CaseData caseData) {
//        List<Element<HearingCourtBundle>> bundles = caseData.getCourtBundleListV2();
//
//        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getCourtBundleHearingList(), mapper);
//
//        Optional<Element<HearingCourtBundle>> bundle = findElement(selectedHearingId, bundles);
//
//        if (bundle.isPresent()) {
//            return bundle.get().getValue().getCourtBundle();
//        } else {
//            Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingId);
//
//            if (hearingBooking.isEmpty()) {
//                throw new NoHearingBookingException(selectedHearingId);
//            }
//
//            return List.of(element(CourtBundle.builder().build()));
//        }
//    }
}
