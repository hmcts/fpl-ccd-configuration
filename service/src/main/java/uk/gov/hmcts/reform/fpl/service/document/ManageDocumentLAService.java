package uk.gov.hmcts.reform.fpl.service.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
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
    public static final String DOCUMENT_SUB_TYPE = "manageDocumentSubtypeListLA";
    public static final String RELATED_TO_HEARING = "manageDocumentsRelatedToHearing";
    public static final String COURT_BUNDLE_HEARING_LABEL_KEY = "manageDocumentsCourtBundleHearingLabel";
    public static final String COURT_BUNDLE_KEY = "manageDocumentsCourtBundle";
    public static final String COURT_BUNDLE_LIST_KEY = "courtBundleList";
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
            listAndLabel.put(COURT_BUNDLE_HEARING_LIST_KEY, caseData.buildDynamicHearingList());
        }

        if (caseData.hasApplicationBundles()) {
            listAndLabel.put(SUPPORTING_C2_LIST_KEY, caseData.buildApplicationBundlesDynamicList());
        }

        if (isNotEmpty(caseData.getAllRespondents())) {
            listAndLabel.put(RESPONDENTS_LIST_KEY, caseData.buildRespondentDynamicList());
        }

        return listAndLabel;
    }

    public Map<String, Object> initialiseCourtBundleFields(CaseData caseData) {
        Map<String, Object> map = new HashMap<>();
        map.put(COURT_BUNDLE_KEY, getCourtBundleForHearing((caseData)));
        return map;
    }

    public List<Element<HearingCourtBundle>> buildCourtBundleList(CaseData caseData) {
        List<Element<HearingCourtBundle>> courtBundleList = caseData.getCourtBundleList();
        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getCourtBundleHearingList(), mapper);

        Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingId);
        if (hearingBooking.isEmpty()) {
            throw new NoHearingBookingException(selectedHearingId);
        }

        if (isNotEmpty(caseData.getHearingDetails())) {
            return List.of(element(selectedHearingId, HearingCourtBundle.builder()
                .hearing(hearingBooking.get().getValue().toLabel())
                .courtBundle(caseData.getManageDocumentsCourtBundle())
                .build()));
        }

        return courtBundleList;
    }

    private HearingCourtBundle constructHearingCourtBundle(Element<HearingBooking> hearingBooking,
                                                           List<Element<CourtBundle>> courtBundle) {
        return HearingCourtBundle.builder()
            .hearing(hearingBooking.getValue().toLabel())
            .courtBundle(courtBundle)
            .build();
    }

    public Map<String, Object> initialiseCourtBundleHearingListAndLabel(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();
        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getCourtBundleHearingList(), mapper);
        Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(
            selectedHearingId);

        if (hearingBooking.isEmpty()) {
            throw new NoHearingBookingException(selectedHearingId);
        }
        listAndLabel.put(COURT_BUNDLE_HEARING_LABEL_KEY, hearingBooking.get().getValue().toLabel());
        listAndLabel.put(COURT_BUNDLE_HEARING_LIST_KEY, caseData.buildDynamicHearingList(selectedHearingId));

        return listAndLabel;
    }

    private List<Element<CourtBundle>> getCourtBundleForHearing(CaseData caseData) {
        List<Element<HearingCourtBundle>> bundles = caseData.getCourtBundleList();

        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getCourtBundleHearingList(), mapper);

        Optional<Element<HearingCourtBundle>> bundle = findElement(selectedHearingId, bundles);

        if (bundle.isPresent()) {
            return bundle.get().getValue().getCourtBundle();
        } else {
            Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingId);

            if (hearingBooking.isEmpty()) {
                throw new NoHearingBookingException(selectedHearingId);
            }

            return List.of(element(CourtBundle.builder().build()));
        }
    }
}
