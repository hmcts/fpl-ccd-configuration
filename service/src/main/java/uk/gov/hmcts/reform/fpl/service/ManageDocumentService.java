package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public static final String MANAGE_DOCUMENTS_HEARING_LIST_KEY = "manageDocumentsHearingList";
    public static final String MANAGE_DOCUMENTS_HEARING_LABEL_KEY = "manageDocumentsHearingLabel";

    @Autowired
    public ManageDocumentService(ObjectMapper objectMapper, Time time) {
        this.mapper = objectMapper;
        this.time = time;
    }

    public void initialiseFurtherEvidenceFields(CaseDetails caseDetails) {
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

    public List<Element<ManageDocumentBundle>> initialiseFurtherDocumentBundleCollection(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getManageDocument().isDocumentRelatedToHearing()
            && !caseData.getHearingFurtherEvidenceDocuments().isEmpty()) {
            UUID selectedHearingCode = mapper.convertValue(caseDetails.getData().get(MANAGE_DOCUMENTS_HEARING_LIST_KEY),
                UUID.class);

            if (caseData.documentBundleContainsHearingId(selectedHearingCode)) {
                for (int i = 0; i < caseData.getHearingFurtherEvidenceDocuments().size(); i++) {
                    Element<HearingFurtherEvidenceBundle> currentHearingEvidenceBundle
                        = caseData.getHearingFurtherEvidenceDocuments().get(i);

                    if (selectedHearingCode.equals(currentHearingEvidenceBundle.getId())) {
                        return currentHearingEvidenceBundle.getValue().getManageDocumentBundle();
                    }
                }
            }

        } else if (caseData.getFurtherEvidenceDocuments() != null) {
            return caseData.getFurtherEvidenceDocuments();
        }

        return initialiseManageDocumentBundleCollection(null);
    }

    public List<Element<ManageDocumentBundle>> initialiseManageDocumentBundleCollection(
        List<Element<ManageDocumentBundle>> manageDocumentBundleListCollection) {
        if (manageDocumentBundleListCollection == null || manageDocumentBundleListCollection.isEmpty()) {
            return List.of(element(ManageDocumentBundle.builder().build()));
        }

        return manageDocumentBundleListCollection;
    }

    public void buildFurtherEvidenceCollection(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceDocuments;

        if (caseData.getManageDocument().isDocumentRelatedToHearing()) {
            DynamicList hearingList = mapper.convertValue(caseDetails.getData().get(MANAGE_DOCUMENTS_HEARING_LIST_KEY),
                DynamicList.class);

            UUID selectedHearingCode = hearingList.getValue().getCode();
            HearingBooking hearingBooking = getHearingBookingByUUID(caseData.getHearingDetails(), selectedHearingCode);

            if (caseData.getHearingFurtherEvidenceDocuments() == null) {
                hearingFurtherEvidenceDocuments = List.of(
                    buildHearingFurtherEvidenceBundle(selectedHearingCode, hearingBooking,
                        caseData.getFurtherEvidenceDocumentsTEMP()));
            } else if (caseData.documentBundleContainsHearingId(selectedHearingCode)) {
                hearingFurtherEvidenceDocuments = caseData.getHearingFurtherEvidenceDocuments().stream()
                    .filter(element -> element.getId().equals(selectedHearingCode))
                    .peek(element -> element.getValue().getManageDocumentBundle()
                        .addAll(caseData.getFurtherEvidenceDocumentsTEMP()))
                    .collect(Collectors.toList());
            } else {
                Element<HearingFurtherEvidenceBundle> hearingFurtherEvidenceBundleElement =
                    buildHearingFurtherEvidenceBundle(selectedHearingCode, hearingBooking,
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

    public List<Element<ManageDocumentBundle>> setDateTimeUploadedOnManageDocumentCollection(
        List<Element<ManageDocumentBundle>> manageDocumentBundle,
        List<Element<ManageDocumentBundle>> manageDocumentBundleBefore) {

        if (!manageDocumentBundle.equals(manageDocumentBundleBefore)
            && manageDocumentBundle.size() == manageDocumentBundleBefore.size()) {
            for (int i = 0; i < manageDocumentBundle.size(); i++) {
                if (!manageDocumentBundle.get(i).getValue().getDocument()
                    .equals(manageDocumentBundleBefore.get(i).getValue().getDocument())) {
                    manageDocumentBundle.get(i).getValue().setDateTimeUploaded(time.now());
                }
            }
        }

        return manageDocumentBundle.stream()
            .peek(manageDocumentBundleElement -> {
                if (manageDocumentBundleElement.getValue().getDateTimeUploaded() == null) {
                    manageDocumentBundleElement.getValue().setDateTimeUploaded(time.now());
                }
            }).collect(Collectors.toList());
    }

    private Element<HearingFurtherEvidenceBundle> buildHearingFurtherEvidenceBundle(
        UUID hearingId, HearingBooking hearingBooking, List<Element<ManageDocumentBundle>> manageDocumentBundle) {
        return Element.<HearingFurtherEvidenceBundle>builder()
            .id(hearingId)
            .value(HearingFurtherEvidenceBundle.builder()
                .hearingName(hearingBooking.toLabel(DATE))
                .manageDocumentBundle(manageDocumentBundle)
                .build())
            .build();
    }
}
