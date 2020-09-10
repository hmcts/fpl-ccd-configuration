package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.getHearingBookingByUUID;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class ManageDocumentService {
    private ObjectMapper mapper;
    private Time time;

    public static final String CORRESPONDING_DOCUMENTS_COLLECTION_KEY = "correspondenceDocuments";
    public static final String TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY = "furtherEvidenceDocumentsTEMP";
    public static final String FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY = "furtherEvidenceDocuments";
    public static final String HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY = "hearingFurtherEvidenceDocuments";
    public static final String C2_SUPPORTING_DOCUMENTS_COLLECTION = "c2SupportingDocuments";
    public static final String MANAGE_DOCUMENTS_HEARING_LIST_KEY = "manageDocumentsHearingList";
    public static final String SUPPORTING_C2_LIST_KEY = "manageDocumentsSupportingC2List";
    public static final String MANAGE_DOCUMENTS_HEARING_LABEL_KEY = "manageDocumentsHearingLabel";
    public static final String SUPPORTING_C2_LABEL = "manageDocumentsSupportingC2Label";
    public static final String MANAGE_DOCUMENT_KEY = "manageDocument";

    @Autowired
    public ManageDocumentService(ObjectMapper objectMapper, Time time) {
        this.mapper = objectMapper;
        this.time = time;
    }

    public void initialiseHearingListAndLabel(CaseDetails caseDetails) {
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

    public void initialiseC2DocumentListAndLabel(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        UUID selectedC2DocumentCode = mapper.convertValue(caseDetails.getData().get(SUPPORTING_C2_LIST_KEY),
            UUID.class);

        IntStream.range(0, caseData.getC2DocumentBundle().size())
            .filter(index -> caseData.getC2DocumentBundle().get(index).getId().equals(selectedC2DocumentCode))
            .findFirst()
            .ifPresent(index -> caseDetails.getData().put(SUPPORTING_C2_LABEL, String.format("Application %s: %s",
                index + 1, caseData.getC2DocumentBundle().get(index).getValue().getUploadedDateTime())));

        caseDetails.getData().put(SUPPORTING_C2_LIST_KEY, caseData.buildC2DocumentDynamicList(selectedC2DocumentCode));
    }

    public List<Element<SupportingEvidenceBundle>> getFurtherEvidenceCollection(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getManageDocument().isDocumentRelatedToHearing()
            && caseData.getHearingFurtherEvidenceDocuments() != null
            && !caseData.getHearingFurtherEvidenceDocuments().isEmpty()) {
            DynamicList dynamicList = mapper.convertValue(caseDetails.getData().get(MANAGE_DOCUMENTS_HEARING_LIST_KEY),
                DynamicList.class);

            UUID selectedHearingCode = dynamicList.getValueCode();

            if (caseData.documentBundleContainsHearingId(selectedHearingCode)) {
                for (int i = 0; i < caseData.getHearingFurtherEvidenceDocuments().size(); i++) {
                    Element<HearingFurtherEvidenceBundle> currentHearingEvidenceBundle
                        = caseData.getHearingFurtherEvidenceDocuments().get(i);

                    if (selectedHearingCode.equals(currentHearingEvidenceBundle.getId())) {
                        return currentHearingEvidenceBundle.getValue().getSupportingEvidenceBundle();
                    }
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

    public List<Element<SupportingEvidenceBundle>> setDateTimeUploadedOnSupporingEvidene(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle,
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleBefore) {

        if (isMatchingSupportingEvidenceCollection(supportingEvidenceBundle, supportingEvidenceBundleBefore)) {
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

        List<Element<SupportingEvidenceBundle>> updatedCorrespondenceDocuments = setDateTimeUploadedOnSupporingEvidene(
            caseData.getC2SupportingDocuments(), c2DocumentBundle.getSupportingEvidenceBundle());

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
        return Element.<HearingFurtherEvidenceBundle>builder()
            .id(hearingId)
            .value(HearingFurtherEvidenceBundle.builder()
                .hearingName(hearingBooking.toLabel(DATE))
                .supportingEvidenceBundle(supportingEvidenceBundle)
                .build())
            .build();
    }

    private boolean isMatchingSupportingEvidenceCollection(List<Element<SupportingEvidenceBundle>> currentCollection,
                                                           List<Element<SupportingEvidenceBundle>> previousCollection) {
        return currentCollection != null && previousCollection != null && !currentCollection.equals(previousCollection)
            && currentCollection.size() == previousCollection.size();
    }
}
