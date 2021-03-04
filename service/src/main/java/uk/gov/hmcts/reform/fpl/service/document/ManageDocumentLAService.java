package uk.gov.hmcts.reform.fpl.service.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDocumentLAService {
    private final ObjectMapper mapper;

    public static final String MANAGE_DOCUMENT_LA_KEY = "manageDocumentLA";
    public static final String COURT_BUNDLE_HEARING_LIST_KEY = "courtBundleHearingList";
    public static final String FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY = "furtherEvidenceDocumentsLA";
    public static final String FURTHER_EVIDENCE_TYPE = "furtherEvidenceTypeListLA";
    public static final String RELATED_TO_HEARING = "manageDocumentsRelatedToHearing";
    public static final String COURT_BUNDLE_KEY = "manageDocumentsCourtBundle";
    public static final String COURT_BUNDLE_LIST_KEY = "courtBundleList";
    public static final String CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY = "correspondenceDocumentsLA";
    public static final String SUPPORTING_C2_LIST_KEY = "manageDocumentsSupportingC2List";

    public Map<String, Object> initialiseManageDocumentLAEvent(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();
        String hasHearings;
        String hasC2s;

        if (caseData.getHearingDetails() != null && !caseData.getHearingDetails().isEmpty()) {
            listAndLabel.put(COURT_BUNDLE_HEARING_LIST_KEY, caseData.buildDynamicHearingList());

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

        ManageDocumentLA manageDocument = ManageDocumentLA.builder()
            .hasHearings(hasHearings)
            .hasC2s(hasC2s)
            .build();

        listAndLabel.put(MANAGE_DOCUMENT_LA_KEY, manageDocument);

        return listAndLabel;
    }

    public Map<String, Object> initialiseCourtBundleFields(CaseData caseData) {
        Map<String, Object> map = new HashMap<>();
        map.put(COURT_BUNDLE_HEARING_LIST_KEY, initialiseCourtBundleHearingList((caseData)));
        map.put(COURT_BUNDLE_KEY, getCourtBundleForHearing((caseData)));
        return map;
    }

    public List<Element<CourtBundle>> buildCourtBundleList(CaseData caseData) {
        List<Element<CourtBundle>> courtBundleList = caseData.getCourtBundleList();

        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getCourtBundleHearingList(), mapper);

        if (courtBundleList == null || courtBundleList.isEmpty()) {
            return List.of(element(selectedHearingId, caseData.getManageDocumentsCourtBundle()));
        }

        Optional<Element<CourtBundle>> editedBundle = findElement(selectedHearingId, courtBundleList);
        editedBundle.ifPresentOrElse(
            courtBundleElement -> courtBundleList.set(courtBundleList.indexOf(courtBundleElement),
                element(selectedHearingId, caseData.getManageDocumentsCourtBundle())),
            () -> courtBundleList.add(element(selectedHearingId, caseData.getManageDocumentsCourtBundle())));

        return courtBundleList;
    }

    private DynamicList initialiseCourtBundleHearingList(CaseData caseData) {
        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getCourtBundleHearingList(), mapper);
        Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(
            selectedHearingId);

        if (hearingBooking.isEmpty()) {
            throw new NoHearingBookingException(selectedHearingId);
        }
        return caseData.buildDynamicHearingList(selectedHearingId);
    }

    private CourtBundle getCourtBundleForHearing(CaseData caseData) {
        List<Element<CourtBundle>> bundles = caseData.getCourtBundleList();

        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getCourtBundleHearingList(), mapper);

        Optional<Element<CourtBundle>> bundle = findElement(selectedHearingId, bundles);

        if (bundle.isPresent()) {
            return bundle.get().getValue();
        } else {
            Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingId);

            if (hearingBooking.isEmpty()) {
                throw new NoHearingBookingException(selectedHearingId);
            }

            return CourtBundle.builder().hearing(hearingBooking.get().getValue().toLabel()).build();
        }
    }
}
