package uk.gov.hmcts.reform.fpl.service.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.exceptions.RespondentNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.interfaces.ApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.representativeSolicitors;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.OTHER_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDocumentService {
    private final ObjectMapper mapper;
    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;
    private final UserService user;

    public static final String CORRESPONDING_DOCUMENTS_COLLECTION_KEY = "correspondenceDocuments";
    public static final String CORRESPONDING_DOCUMENTS_COLLECTION_SOLICITOR_KEY = "correspondenceDocumentsSolicitor";
    public static final String C2_DOCUMENTS_COLLECTION_KEY = "c2DocumentBundle";
    public static final String TEMP_EVIDENCE_DOCUMENTS_KEY = "supportingEvidenceDocumentsTemp";
    public static final String FURTHER_EVIDENCE_DOCUMENTS_KEY = "furtherEvidenceDocuments";
    public static final String FURTHER_EVIDENCE_DOCUMENTS_SOLICITOR_KEY = "furtherEvidenceDocumentsSolicitor";
    public static final String HEARING_FURTHER_EVIDENCE_DOCUMENTS_KEY = "hearingFurtherEvidenceDocuments";
    public static final String C2_SUPPORTING_DOCUMENTS_COLLECTION = "c2SupportingDocuments";
    public static final String MANAGE_DOCUMENTS_HEARING_LIST_KEY = "manageDocumentsHearingList";
    public static final String SUPPORTING_C2_LIST_KEY = "manageDocumentsSupportingC2List";
    public static final String MANAGE_DOCUMENTS_HEARING_LABEL_KEY = "manageDocumentsHearingLabel";
    public static final String SUPPORTING_C2_LABEL = "manageDocumentsSupportingC2Label";
    public static final String MANAGE_DOCUMENT_KEY = "manageDocument";
    public static final String ADDITIONAL_APPLICATIONS_BUNDLE_KEY = "additionalApplicationsBundle";
    public static final String RESPONDENTS_LIST_KEY = "respondentStatementList";
    public static final String CHILDREN_LIST_KEY = "manageDocumentsChildrenList";
    public static final String HEARING_DOCUMENT_RESPONDENT_LIST_KEY = "hearingDocumentsRespondentList";
    public static final String HEARING_DOCUMENT_HEARING_LIST_KEY = "hearingDocumentsHearingList";
    public static final String HEARING_DOCUMENT_TYPE = "manageDocumentsHearingDocumentType";
    public static final String COURT_BUNDLE_HEARING_LABEL_KEY = "manageDocumentsCourtBundleHearingLabel";
    public static final String COURT_BUNDLE_KEY = "manageDocumentsCourtBundle";
    public static final String CASE_SUMMARY_KEY = "manageDocumentsCaseSummary";
    public static final String POSITION_STATEMENT_CHILD_KEY = "manageDocumentsPositionStatementChild";
    public static final String POSITION_STATEMENT_RESPONDENT_KEY = "manageDocumentsPositionStatementRespondent";
    public static final String COURT_BUNDLE_LIST_KEY = "courtBundleListV2";
    public static final String CASE_SUMMARY_LIST_KEY = "caseSummaryList";
    public static final String POSITION_STATEMENT_CHILD_LIST_KEY = "positionStatementChildList";
    public static final String POSITION_STATEMENT_RESPONDENT_LIST_KEY = "positionStatementRespondentList";

    private static final Predicate<Element<SupportingEvidenceBundle>> HMCTS_FILTER =
        bundle -> bundle.getValue().isUploadedByHMCTS();
    private static final Predicate<Element<SupportingEvidenceBundle>> SOLICITOR_FILTER =
        bundle -> bundle.getValue().isUploadedByRepresentativeSolicitor();

    public Map<String, Object> baseEventData(CaseData caseData) {
        Map<String, Object> eventData = new HashMap<>();

        final YesNo hasHearings = YesNo.from(isNotEmpty(caseData.getHearingDetails()));
        final YesNo hasC2s = YesNo.from(caseData.hasApplicationBundles());
        final YesNo hasRespondents = YesNo.from(isNotEmpty(caseData.getAllRespondents()));

        ManageDocument manageDocument = defaultIfNull(caseData.getManageDocument(), ManageDocument.builder().build())
            .toBuilder()
            .hasHearings(hasHearings.getValue())
            .hasC2s(hasC2s.getValue())
            .build();

        eventData.put(MANAGE_DOCUMENT_KEY, manageDocument);

        if (hasHearings == YES) {
            eventData.put(MANAGE_DOCUMENTS_HEARING_LIST_KEY, caseData.buildDynamicHearingList());
            eventData.put(HEARING_DOCUMENT_HEARING_LIST_KEY, caseData.buildDynamicHearingList());
        }

        if (hasC2s == YES) {
            eventData.put(SUPPORTING_C2_LIST_KEY, caseData.buildApplicationBundlesDynamicList());
        }

        if (hasRespondents == YES) {
            eventData.put(RESPONDENTS_LIST_KEY, caseData.buildRespondentDynamicList());
            eventData.put(HEARING_DOCUMENT_RESPONDENT_LIST_KEY, caseData.buildRespondentDynamicList());
        }

        if (isNotEmpty(caseData.getAllChildren())) {
            eventData.put(CHILDREN_LIST_KEY, caseData.buildDynamicChildrenList());
        }

        return eventData;
    }

    public Map<String, Object> initialiseHearingListAndLabel(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();

        if (YES.getValue().equals(caseData.getManageDocumentsRelatedToHearing())) {
            UUID selectedHearingCode = getDynamicListSelectedValue(caseData.getManageDocumentsHearingList(), mapper);
            Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingCode);

            if (hearingBooking.isEmpty()) {
                throw new NoHearingBookingException(selectedHearingCode);
            }

            listAndLabel.put(MANAGE_DOCUMENTS_HEARING_LABEL_KEY, hearingBooking.get().getValue().toLabel());
            listAndLabel.put(MANAGE_DOCUMENTS_HEARING_LIST_KEY, caseData.buildDynamicHearingList(selectedHearingCode));
        }

        return listAndLabel;
    }

    public Map<String, Object> initialiseApplicationBundlesListAndLabel(CaseData caseData) {
        Map<String, Object> listAndLabel = new HashMap<>();

        UUID selectedBundleId = getDynamicListSelectedValue(caseData.getManageDocumentsSupportingC2List(), mapper);
        List<Element<ApplicationsBundle>> applicationsBundles = caseData.getAllApplicationsBundles();

        Element<ApplicationsBundle> selectedBundle = applicationsBundles.stream()
            .filter(bundle -> selectedBundleId.equals(bundle.getId()))
            .findFirst().orElseThrow(() -> new IllegalArgumentException(
                "No application bundle found for the selected bundle id, " + selectedBundleId.toString()));

        listAndLabel.put(SUPPORTING_C2_LABEL, selectedBundle.getValue().toLabel());
        listAndLabel.put(SUPPORTING_C2_LIST_KEY, caseData.buildApplicationBundlesDynamicList(selectedBundleId));

        return listAndLabel;
    }

    public List<Element<SupportingEvidenceBundle>> getFurtherEvidences(
        CaseData caseData,
        List<Element<SupportingEvidenceBundle>> unrelatedEvidence) {
        if (YES.getValue().equals(caseData.getManageDocumentsRelatedToHearing())) {
            List<Element<HearingFurtherEvidenceBundle>> bundles = caseData.getHearingFurtherEvidenceDocuments();
            if (!bundles.isEmpty()) {
                UUID selectedHearingId = getDynamicListSelectedValue(caseData.getManageDocumentsHearingList(), mapper);

                Optional<Element<HearingFurtherEvidenceBundle>> bundle = findElement(selectedHearingId, bundles);

                if (bundle.isPresent()) {
                    List<Element<SupportingEvidenceBundle>> evidenceBundle
                        = bundle.get().getValue().getSupportingEvidenceBundle();

                    setDefaultEvidenceType(evidenceBundle);
                    return getUserSpecificSupportingEvidences(evidenceBundle, caseData.getId());
                }
            }
        } else if (isNotEmpty(unrelatedEvidence)) {
            setDefaultEvidenceType(unrelatedEvidence);
            return unrelatedEvidence;
        }

        return defaultSupportingEvidences();
    }

    public List<Element<SupportingEvidenceBundle>> getApplicationsSupportingEvidenceBundles(CaseData caseData) {
        UUID selectedC2 = getDynamicListSelectedValue(caseData.getManageDocumentsSupportingC2List(), mapper);
        ApplicationsBundle selectedBundle = caseData.getApplicationBundleByUUID(selectedC2);

        return getUserSpecificSupportingEvidences(selectedBundle.getSupportingEvidenceBundle(), caseData.getId());
    }

    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle(
        List<Element<SupportingEvidenceBundle>> supportingEvidences) {
        return isEmpty(supportingEvidences) ? defaultSupportingEvidences() : supportingEvidences;
    }

    public List<Element<HearingFurtherEvidenceBundle>> buildHearingFurtherEvidenceCollection(
        CaseData caseData, List<Element<SupportingEvidenceBundle>> modifiedEvidence) {

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceBundle
            = caseData.getHearingFurtherEvidenceDocuments();

        UUID selectedHearingCode = getDynamicListSelectedValue(caseData.getManageDocumentsHearingList(), mapper);
        Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingCode);

        if (hearingBooking.isEmpty()) {
            throw new NoHearingBookingException(selectedHearingCode);
        }

        if (caseData.documentBundleContainsHearingId(selectedHearingCode)) {
            for (Element<HearingFurtherEvidenceBundle> element : hearingFurtherEvidenceBundle) {
                if (element.getId().equals(selectedHearingCode)) {
                    if (caseData.getSupportingEvidenceDocumentsTemp().isEmpty()) {
                        hearingFurtherEvidenceBundle.remove(element);
                        return hearingFurtherEvidenceBundle;
                    }
                    List<Element<SupportingEvidenceBundle>> existingEvidence =
                        new ArrayList<>(element.getValue().getSupportingEvidenceBundle());

                    updateExistingEvidenceWithChanges(existingEvidence, modifiedEvidence, caseData.getId());
                    sortByDateUploaded(existingEvidence);

                    element.getValue().setSupportingEvidenceBundle(existingEvidence);
                }
            }
            return hearingFurtherEvidenceBundle;
        } else {
            hearingFurtherEvidenceBundle.add(buildHearingSupportingEvidenceBundle(
                selectedHearingCode,
                hearingBooking.get().getValue(),
                modifiedEvidence
            ));
            return hearingFurtherEvidenceBundle;
        }
    }

    public List<Element<SupportingEvidenceBundle>> setDateTimeUploadedOnSupportingEvidence(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle,
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleBefore,
        boolean setSolicitorUploaded) {

        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

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
                        bundle.getValue().setUploadedBy(uploadedBy);
                        if (setSolicitorUploaded) {
                            bundle.getValue().setUploadedBySolicitor("Yes");
                        }
                    }
                }
            ));
        }

        List<Element<SupportingEvidenceBundle>> updatedBundles = new ArrayList<>();
        for (Element<SupportingEvidenceBundle> supportingEvidenceBundleElement : supportingEvidenceBundle) {
            if (supportingEvidenceBundleElement.getValue().getDateTimeUploaded() == null) {
                supportingEvidenceBundleElement.getValue().setDateTimeUploaded(time.now());
                supportingEvidenceBundleElement.getValue().setUploadedBy(uploadedBy);
                if (setSolicitorUploaded) {
                    supportingEvidenceBundleElement.getValue().setUploadedBySolicitor("Yes");
                }
            }
            updatedBundles.add(supportingEvidenceBundleElement);
        }
        return updatedBundles;
    }

    public Map<String, Object> buildFinalApplicationBundleSupportingDocuments(CaseData caseData,
                                                                              boolean setSolicitorUploaded) {
        HashMap<String, Object> data = new HashMap<>();
        UUID selected = getDynamicListSelectedValue(caseData.getManageDocumentsSupportingC2List(), mapper);

        if (caseData.getC2DocumentBundleByUUID(selected) != null) {
            data.put(C2_DOCUMENTS_COLLECTION_KEY, updatedC2DocumentBundle(caseData, selected, setSolicitorUploaded));
        } else {
            data.put(ADDITIONAL_APPLICATIONS_BUNDLE_KEY,
                updateAdditionalDocumentsBundle(caseData, selected, setSolicitorUploaded));
        }
        return data;
    }

    public Map<String, Object> initialiseHearingDocumentFields(CaseData caseData) {
        Map<String, Object> map = new HashMap<>();
        map.put(HEARING_DOCUMENT_HEARING_LIST_KEY, initialiseHearingDocumentsHearingList((caseData)));

        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getHearingDocumentsHearingList(), mapper);
        switch (caseData.getManageDocumentsHearingDocumentType()) {
            case COURT_BUNDLE :
                map.put(COURT_BUNDLE_KEY, getCourtBundleForHearing(caseData, selectedHearingId));
                map.put(COURT_BUNDLE_HEARING_LABEL_KEY, getHearingBooking(caseData, selectedHearingId).toLabel());
                break;
            case CASE_SUMMARY :
                map.put(CASE_SUMMARY_KEY, getCaseSummaryForHearing(caseData, selectedHearingId));
                break;
            case POSITION_STATEMENT_CHILD :
                map.put(POSITION_STATEMENT_CHILD_KEY, getPositionStatementChildForHearing(caseData, selectedHearingId));
                break;
            case POSITION_STATEMENT_RESPONDENT :
                map.put(POSITION_STATEMENT_RESPONDENT_KEY,
                    getPositionStatementRespondentForHearing(caseData, selectedHearingId));
        }
        return map;
    }

    public Map<String, Object> buildHearingDocumentList(CaseData caseData) {
        Map<String, Object> map = new HashMap<>();
        UUID selectedHearingId = getDynamicListSelectedValue(caseData.getHearingDocumentsHearingList(), mapper);

        switch (caseData.getManageDocumentsHearingDocumentType()) {
            case COURT_BUNDLE :
                map.put(COURT_BUNDLE_LIST_KEY, buildCourtBundleList(caseData, selectedHearingId));
                break;
            case CASE_SUMMARY :
                map.put(CASE_SUMMARY_LIST_KEY, buildHearingDocumentList(caseData, selectedHearingId,
                    caseData.getCaseSummaryList(), caseData.getManageDocumentsCaseSummary()));
                break;
            case POSITION_STATEMENT_CHILD :
                map.put(POSITION_STATEMENT_CHILD_LIST_KEY, buildHearingDocumentList(caseData, selectedHearingId,
                    caseData.getPositionStatementChildList(),
                    caseData.getManageDocumentsPositionStatementChild().toBuilder()
                        .childId(caseData.getManageDocumentsChildrenList().getValueCodeAsUUID())
                        .childName(caseData.getManageDocumentsChildrenList().getValueLabel())
                        .build()));
                break;
            case POSITION_STATEMENT_RESPONDENT :
                map.put(POSITION_STATEMENT_RESPONDENT_LIST_KEY, buildHearingDocumentList(caseData, selectedHearingId,
                    caseData.getPositionStatementRespondentList(),
                    caseData.getManageDocumentsPositionStatementRespondent().toBuilder()
                        .respondentId(caseData.getHearingDocumentsRespondentList().getValueCodeAsUUID())
                        .respondentName(caseData.getHearingDocumentsRespondentList().getValueLabel())
                        .build()));
        }

        return map;
    }

    private <T> List<Element<T>> buildHearingDocumentList(CaseData caseData, UUID selectedHearingId,
                                                          List<Element<T>> hearingDocumentList, T hearingDocument) {
        if (isNotEmpty(caseData.getHearingDetails())) {
            return List.of(element(selectedHearingId, hearingDocument));
        }

        return hearingDocumentList;
    }

    private List<Element<HearingCourtBundle>> buildCourtBundleList(CaseData caseData, UUID selectedHearingId) {
        HearingBooking hearingBooking = getHearingBooking(caseData, selectedHearingId);
        List<Element<CourtBundle>> courtBundleNC = caseData.getManageDocumentsCourtBundle().stream()
            .filter(doc -> !doc.getValue().isConfidentialDocument())
            .collect(Collectors.toList());

        return buildHearingDocumentList(caseData, selectedHearingId,
            caseData.getCourtBundleListV2(),
            HearingCourtBundle.builder()
                .hearing(hearingBooking.toLabel())
                .courtBundle(caseData.getManageDocumentsCourtBundle())
                .courtBundleNC(courtBundleNC)
                .build()
            );
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

    private <T> T getHearingDocumentForSelectedHearing(List<Element<T>> documents,
                                                       UUID selectedHearingId) {
        Optional<Element<T>> hearingDocument = findElement(selectedHearingId, documents);
        return hearingDocument.map(Element::getValue).orElse(null);
    }

    private HearingBooking getHearingBooking(CaseData caseData, UUID selectedHearingId) {
        Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingId);

        if (hearingBooking.isEmpty()) {
            throw new NoHearingBookingException(selectedHearingId);
        }

        return hearingBooking.get().getValue();
    }

    private List<Element<CourtBundle>> getCourtBundleForHearing(CaseData caseData, UUID selectedHearingId) {
        HearingCourtBundle hearingCourtBundle = getHearingDocumentForSelectedHearing(caseData.getCourtBundleListV2(),
            selectedHearingId);
        if (hearingCourtBundle == null) {
            return List.of(element(CourtBundle.builder().build()));
        } else {
            return hearingCourtBundle.getCourtBundle();
        }
    }

    private CaseSummary getCaseSummaryForHearing(CaseData caseData, UUID selectedHearingId) {
        CaseSummary caseSummary = getHearingDocumentForSelectedHearing(caseData.getCaseSummaryList(),
            selectedHearingId);
        if (caseSummary == null) {
            caseSummary = CaseSummary.builder()
                .hearing(getHearingBooking(caseData, selectedHearingId).toLabel()).build();
        }
        return caseSummary;
    }

    private PositionStatementChild getPositionStatementChildForHearing(CaseData caseData, UUID selectedHearingId) {
        PositionStatementChild positionStatementChild = getHearingDocumentForSelectedHearing(
            caseData.getPositionStatementChildList(),
            selectedHearingId);
        if (positionStatementChild == null) {
            positionStatementChild = PositionStatementChild.builder()
                .hearing(getHearingBooking(caseData, selectedHearingId).toLabel()).build();
        }
        return positionStatementChild;
    }

    private PositionStatementRespondent getPositionStatementRespondentForHearing(CaseData caseData,
                                                                                 UUID selectedHearingId) {
        PositionStatementRespondent positionStatementRespondent = getHearingDocumentForSelectedHearing(
            caseData.getPositionStatementRespondentList(),
            selectedHearingId);
        if (positionStatementRespondent == null) {
            positionStatementRespondent = PositionStatementRespondent.builder()
                .hearing(getHearingBooking(caseData, selectedHearingId).toLabel()).build();
        }
        return positionStatementRespondent;
    }

    private List<Element<AdditionalApplicationsBundle>> updateAdditionalDocumentsBundle(
        CaseData caseData, UUID selectedBundleId, boolean setSolicitorUploaded) {

        List<Element<AdditionalApplicationsBundle>> applicationsBundles = caseData.getAdditionalApplicationsBundle();

        for (Element<AdditionalApplicationsBundle> element : applicationsBundles) {
            C2DocumentBundle c2DocumentBundle = element.getValue().getC2DocumentBundle();
            OtherApplicationsBundle otherApplicationsBundle = element.getValue().getOtherApplicationsBundle();

            if (!isNull(c2DocumentBundle) && selectedBundleId.equals(c2DocumentBundle.getId())) {

                List<Element<SupportingEvidenceBundle>> updatedSupportingDocuments = updateSupportingEvidenceBundle(
                    c2DocumentBundle.getSupportingEvidenceBundle(),
                    caseData.getSupportingEvidenceDocumentsTemp(),
                    caseData.getId(),
                    setSolicitorUploaded);

                c2DocumentBundle.setSupportingEvidenceBundle(updatedSupportingDocuments);
            } else if (!isNull(otherApplicationsBundle) && selectedBundleId.equals(otherApplicationsBundle.getId())) {

                List<Element<SupportingEvidenceBundle>> updatedSupportingDocuments = updateSupportingEvidenceBundle(
                    otherApplicationsBundle.getSupportingEvidenceBundle(),
                    caseData.getSupportingEvidenceDocumentsTemp(),
                    caseData.getId(),
                    setSolicitorUploaded);

                otherApplicationsBundle.setSupportingEvidenceBundle(updatedSupportingDocuments);
            }
        }
        return applicationsBundles;
    }

    private List<Element<C2DocumentBundle>> updatedC2DocumentBundle(CaseData caseData,
                                                                    UUID selected,
                                                                    boolean setSolicitorUploaded) {
        List<Element<C2DocumentBundle>> c2Bundles = caseData.getC2DocumentBundle();

        for (Element<C2DocumentBundle> element : c2Bundles) {
            if (selected.equals(element.getId())) {
                List<Element<SupportingEvidenceBundle>> updatedBundle = updateSupportingEvidenceBundle(
                    element.getValue().getSupportingEvidenceBundle(), caseData.getSupportingEvidenceDocumentsTemp(),
                    caseData.getId(), setSolicitorUploaded);
                element.getValue().setSupportingEvidenceBundle(updatedBundle);
            }
        }
        return c2Bundles;
    }

    private List<Element<SupportingEvidenceBundle>> updateSupportingEvidenceBundle(
        List<Element<SupportingEvidenceBundle>> existingSupportingEvidenceBundle,
        List<Element<SupportingEvidenceBundle>> updatedSupportingEvidenceBundle,
        Long id,
        boolean setSolicitorUploaded
    ) {
        List<Element<SupportingEvidenceBundle>> modifiedEvidence = setDateTimeUploadedOnSupportingEvidence(
            updatedSupportingEvidenceBundle, existingSupportingEvidenceBundle, setSolicitorUploaded);

        updateExistingEvidenceWithChanges(existingSupportingEvidenceBundle, modifiedEvidence, id);
        sortByDateUploaded(existingSupportingEvidenceBundle);

        return existingSupportingEvidenceBundle;
    }

    public List<Element<SupportingEvidenceBundle>> setDateTimeOnHearingFurtherEvidenceSupportingEvidence(
        CaseData caseData, CaseData caseDataBefore, boolean setSolicitorUploaded) {
        List<Element<SupportingEvidenceBundle>> currentSupportingDocuments
            = caseData.getSupportingEvidenceDocumentsTemp();

        UUID selectedHearingCode = getDynamicListSelectedValue(caseData.getManageDocumentsHearingList(), mapper);

        List<Element<SupportingEvidenceBundle>> previousSupportingDocuments =
            findElement(selectedHearingCode, caseDataBefore.getHearingFurtherEvidenceDocuments())
                .map(Element::getValue)
                .map(HearingFurtherEvidenceBundle::getSupportingEvidenceBundle)
                .orElse(List.of());

        return setDateTimeUploadedOnSupportingEvidence(currentSupportingDocuments, previousSupportingDocuments,
            setSolicitorUploaded);
    }

    public List<Element<SupportingEvidenceBundle>> sortCorrespondenceDocumentsByUploadedDate(
        List<Element<SupportingEvidenceBundle>> documents) {
        return defaultIfNull(documents, new ArrayList<Element<SupportingEvidenceBundle>>()).stream()
            .sorted(comparing(bundle -> bundle.getValue().getDateTimeUploaded(), nullsLast(reverseOrder())))
            .collect(Collectors.toList());
    }

    //This is unfiltered, LA can see/edit HMCTS confidential statements for a respondent
    public List<Element<SupportingEvidenceBundle>> getRespondentStatements(CaseData caseData,
                                                                           UUID id) {
        return caseData.getRespondentStatementByRespondentId(id)
            .map(Element::getValue)
            .map(RespondentStatement::getSupportingEvidenceBundle)
            .orElse(defaultSupportingEvidences());
    }

    public List<Element<RespondentStatement>> getUpdatedRespondentStatements(CaseData caseData,
                                                                             boolean setSolicitorUploaded) {
        List<Element<RespondentStatement>> respondentStatementDocuments = caseData.getRespondentStatements();
        UUID selectedRespondentId = getSelectedRespondentId(caseData);
        String respondentFullName = getRespondentFullName(caseData, selectedRespondentId);
        List<Element<SupportingEvidenceBundle>> newBundle = caseData.getSupportingEvidenceDocumentsTemp();

        Element<RespondentStatement> respondentStatement
            = caseData.getRespondentStatementByRespondentId(selectedRespondentId)
            .orElseGet(() -> {
                Element<RespondentStatement> newRespondentStatement = element(RespondentStatement.builder()
                    .respondentId(selectedRespondentId)
                    .respondentName(respondentFullName)
                    .build());

                respondentStatementDocuments.add(newRespondentStatement);
                return newRespondentStatement;
            });

        if (newBundle.isEmpty()) {
            respondentStatementDocuments.remove(respondentStatement);
        } else {
            List<Element<SupportingEvidenceBundle>> existingBundle
                = respondentStatement.getValue().getSupportingEvidenceBundle();
            List<Element<SupportingEvidenceBundle>> updatedBundle
                = setDateTimeUploadedOnSupportingEvidence(newBundle, existingBundle, setSolicitorUploaded);
            respondentStatement.getValue().setSupportingEvidenceBundle(updatedBundle);
        }

        return respondentStatementDocuments;
    }

    public UUID getSelectedRespondentId(CaseData caseData) {
        return getDynamicListSelectedValue(caseData.getRespondentStatementList(), mapper);
    }

    // Separate collection based on idam role (only show users their own documents)
    private List<Element<SupportingEvidenceBundle>> getUserSpecificSupportingEvidences(
        List<Element<SupportingEvidenceBundle>> bundles, Long caseId) {

        Predicate<Element<SupportingEvidenceBundle>> selectedFilter;

        if (!user.isHmctsUser()) {
            if (user.hasAnyCaseRoleFrom(representativeSolicitors(), caseId)) {
                selectedFilter = SOLICITOR_FILTER;
            } else {
                //local authority
                selectedFilter = HMCTS_FILTER.negate().and(SOLICITOR_FILTER.negate());
            }
        } else {
            selectedFilter = HMCTS_FILTER;
        }

        List<Element<SupportingEvidenceBundle>> supportingEvidences = bundles.stream()
            .filter(selectedFilter)
            .collect(Collectors.toList());

        return isEmpty(supportingEvidences) ? defaultSupportingEvidences() : supportingEvidences;
    }

    private void setDefaultEvidenceType(List<Element<SupportingEvidenceBundle>> unrelatedEvidence) {
        unrelatedEvidence.stream()
            .map(Element::getValue)
            .filter(bundle -> bundle.getType() == null)
            .forEach(bundle -> bundle.setType(OTHER_REPORTS));
    }

    private Element<HearingFurtherEvidenceBundle> buildHearingSupportingEvidenceBundle(
        UUID hearingId, HearingBooking hearingBooking,
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {

        return element(hearingId, HearingFurtherEvidenceBundle.builder()
            .hearingName(hearingBooking.toLabel())
            .supportingEvidenceBundle(supportingEvidenceBundle)
            .build());
    }

    private List<Element<SupportingEvidenceBundle>> defaultSupportingEvidences() {
        return List.of(element(SupportingEvidenceBundle.builder().build()));
    }

    private void updateExistingEvidenceWithChanges(List<Element<SupportingEvidenceBundle>> existingEvidence,
                                                   List<Element<SupportingEvidenceBundle>> updatedEvidence,
                                                   Long id) {
        List<Element<SupportingEvidenceBundle>> userSpecificDocuments
            = getUserSpecificSupportingEvidences(existingEvidence, id);

        existingEvidence.removeAll(userSpecificDocuments);

        existingEvidence.addAll(updatedEvidence);
    }

    private void sortByDateUploaded(List<Element<SupportingEvidenceBundle>> evidence) {
        evidence.sort((ele1, ele2) -> {
            LocalDateTime date1 = defaultIfNull(ele1.getValue().getDateTimeUploaded(), LocalDateTime.MAX);
            LocalDateTime date2 = defaultIfNull(ele2.getValue().getDateTimeUploaded(), LocalDateTime.MAX);
            return date1.compareTo(date2);
        });
    }

    private String getRespondentFullName(CaseData caseData, UUID respondentId) {
        return caseData.findRespondent(respondentId)
            .map(Element::getValue)
            .map(Respondent::getParty)
            .map(RespondentParty::getFullName)
            .orElseThrow(() -> new RespondentNotFoundException(respondentId));
    }
}
