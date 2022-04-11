package uk.gov.hmcts.reform.fpl.service.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

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
    public static final String HEARING_DOCUMENT_HEARING_LIST_KEY = "hearingDocumentsHearingList";
    public static final String FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY = "furtherEvidenceDocumentsLA";
    public static final String DOCUMENT_SUB_TYPE = "manageDocumentSubtypeListLA";
    public static final String RELATED_TO_HEARING = "manageDocumentsRelatedToHearing";
    public static final String HEARING_DOCUMENT_TYPE = "manageDocumentsHearingDocumentType";
    public static final String COURT_BUNDLE_KEY = "manageDocumentsCourtBundle";
    public static final String CASE_SUMMARY_KEY = "manageDocumentsCaseSummary";
    public static final String POSITION_STATEMENT_CHILD_KEY = "manageDocumentsPositionStatementChild";
    public static final String COURT_BUNDLE_LIST_KEY = "courtBundleList";
    public static final String CASE_SUMMARY_LIST_KEY = "caseSummaryList";
    public static final String POSITION_STATEMENT_CHILD_LIST_KEY = "positionStatementChildList";
    public static final String CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY = "correspondenceDocumentsLA";
    public static final String SUPPORTING_C2_LIST_KEY = "manageDocumentsSupportingC2List";
    public static final String RESPONDENTS_LIST_KEY = "respondentStatementList";
    public static final String CHILDREN_LIST_KEY = "manageDocumentsChildrenList";

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
        }

        if (isNotEmpty(caseData.getAllChildren())) {
            listAndLabel.put(CHILDREN_LIST_KEY, caseData.buildDynamicChildrenList());
        }

        return listAndLabel;
    }

    public Map<String, Object> initialiseHearingDocumentFields(CaseData caseData) {
        Map<String, Object> map = new HashMap<>();
        map.put(HEARING_DOCUMENT_HEARING_LIST_KEY, initialiseHearingDocumentsHearingList((caseData)));

        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getHearingDocumentsHearingList(), mapper);
        switch(caseData.getManageDocumentsHearingDocumentType()) {
            case COURT_BUNDLE ->
                map.put(COURT_BUNDLE_KEY, getCourtBundleForHearing(caseData, selectedHearingId));
            case CASE_SUMMARY ->
                map.put(CASE_SUMMARY_KEY, getCaseSummaryForHearing(caseData, selectedHearingId));
            case POSITION_STATEMENT_CHILD ->
                map.put(POSITION_STATEMENT_CHILD_KEY, getPositionStatementChildForHearing(caseData, selectedHearingId));
        }
        return map;
    }


    public Map<String, Object> buildHearingDocumentList(CaseData caseData) {
        Map<String, Object> map = new HashMap<>();
        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getHearingDocumentsHearingList(), mapper);

        switch(caseData.getManageDocumentsHearingDocumentType()) {
            case COURT_BUNDLE ->
                map.put(COURT_BUNDLE_LIST_KEY, buildHearingDocumentList(caseData, selectedHearingId,
                    caseData.getCourtBundleList(), caseData.getManageDocumentsCourtBundle()));
            case CASE_SUMMARY ->
                map.put(CASE_SUMMARY_LIST_KEY, buildHearingDocumentList(caseData, selectedHearingId,
                    caseData.getCaseSummaryList(), caseData.getManageDocumentsCaseSummary()));
            case POSITION_STATEMENT_CHILD ->
                map.put(POSITION_STATEMENT_CHILD_LIST_KEY, buildHearingDocumentList(caseData, selectedHearingId,
                    caseData.getPositionStatementChildList(),
                    caseData.getManageDocumentsPositionStatementChild().toBuilder()
                        .childId(caseData.getManageDocumentsChildrenList().getValueCodeAsUUID())
                        .childName(caseData.getManageDocumentsChildrenList().getValueLabel())
                        .build()));
        }

        return map;
    }

    private <T> List<Element<T>> buildHearingDocumentList(CaseData caseData, UUID selectedHearingId,
                                                       List<Element<T>> hearingDocumentList, T hearingDocument) {
        if (isNotEmpty(caseData.getHearingDetails())) {
            return List.of(element(selectedHearingId, hearingDocument));
        }

        Optional<Element<T>> editedBundle = findElement(selectedHearingId, hearingDocumentList);
        editedBundle.ifPresentOrElse(
            hearingDocumentElement -> hearingDocumentList.set(hearingDocumentList.indexOf(hearingDocumentElement),
                element(selectedHearingId, hearingDocument)),
            () -> hearingDocumentList.add(element(selectedHearingId, hearingDocument)));

        return hearingDocumentList;
    }

    private DynamicList initialiseHearingDocumentsHearingList(CaseData caseData) {
        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getHearingDocumentsHearingList(), mapper);
        Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(
            selectedHearingId);

        if (hearingBooking.isEmpty()) {
            throw new NoHearingBookingException(selectedHearingId);
        }
        return caseData.buildDynamicHearingList(selectedHearingId);
    }

    private <T> T getHearingDocumentForSelectedHearing(CaseData caseData, List<Element<T>> documents,
                                                       UUID selectedHearingId) {
        Optional<Element<T>> hearingDocument = findElement(selectedHearingId, documents);

        if (hearingDocument.isPresent()) {
            return hearingDocument.get().getValue();
        } else {
            return null;
        }
    }

    private HearingBooking getHearingBooking(CaseData caseData, UUID selectedHearingId) {
        Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingId);

        if (hearingBooking.isEmpty()) {
            throw new NoHearingBookingException(selectedHearingId);
        }

        return hearingBooking.get().getValue();
    }

    private CourtBundle getCourtBundleForHearing(CaseData caseData, UUID selectedHearingId) {
        CourtBundle courtBundle = getHearingDocumentForSelectedHearing(caseData, caseData.getCourtBundleList(),
            selectedHearingId);
        if (courtBundle == null) {
            courtBundle = CourtBundle.builder()
                .hearing(getHearingBooking(caseData, selectedHearingId).toLabel()).build();
        }
        return courtBundle;
    }

    private CaseSummary getCaseSummaryForHearing(CaseData caseData, UUID selectedHearingId) {
        CaseSummary caseSummary = getHearingDocumentForSelectedHearing(caseData, caseData.getCaseSummaryList(),
            selectedHearingId);
        if(caseSummary == null){
            caseSummary = CaseSummary.builder()
                .hearing(getHearingBooking(caseData, selectedHearingId).toLabel()).build();
        }
        return caseSummary;
    }

    private PositionStatementChild getPositionStatementChildForHearing(CaseData caseData, UUID selectedHearingId) {
        PositionStatementChild positionStatementChild = getHearingDocumentForSelectedHearing(caseData,
            caseData.getPositionStatementChildList(),
            selectedHearingId);
        if(positionStatementChild == null){
            positionStatementChild = PositionStatementChild.builder()
                .hearing(getHearingBooking(caseData, selectedHearingId).toLabel()).build();
        }
        return positionStatementChild;
    }
}
