package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.DocumentRouter;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.DocumentRouter.AMEND;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.DocumentRouter.DELETE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.DocumentRouter.UPLOAD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getSelectedIdFromDynamicList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsService {

    private final ObjectMapper mapper;

    public DynamicList buildDocumentDynamicList(CaseData caseData) {
        List<Element<CourtAdminDocument>> courtAdminDocuments = caseData.getOtherCourtAdminDocuments();
        DocumentRouter router = caseData.getUploadDocumentsRouter();

        if (AMEND == router || DELETE == router) {
            return asDynamicList(courtAdminDocuments, CourtAdminDocument::getDocumentTitle);
        } else {
            return null;
        }
    }

    public Map<String, Object> getDocumentToDisplay(CaseData caseData) {
        List<Element<CourtAdminDocument>> otherCourtAdminDocuments = caseData.getOtherCourtAdminDocuments();
        DocumentRouter router = caseData.getUploadDocumentsRouter();

        Object courtDocumentList = caseData.getCourtDocumentList();

        UUID selectedId = getSelectedIdFromDynamicList(courtDocumentList, mapper);

        Map<String, Object> data = new HashMap<>();

        findElement(selectedId, otherCourtAdminDocuments).ifPresent(
            courtAdminDocument -> {
                if (AMEND == router) {
                    data.put("originalCourtDocument", courtAdminDocument.getValue().getDocument());
                } else {
                    data.put("deletedCourtDocument", courtAdminDocument.getValue());
                }
            }
        );
        return data;
    }

    public DynamicList regenerateDynamicList(CaseData caseData) {
        List<Element<CourtAdminDocument>> otherCourtAdminDocuments = caseData.getOtherCourtAdminDocuments();
        Object courtDocumentList = caseData.getCourtDocumentList();
        UUID selectedId = getSelectedIdFromDynamicList(courtDocumentList, mapper);

        return asDynamicList(otherCourtAdminDocuments, selectedId, CourtAdminDocument::getDocumentTitle);
    }

    public List<Element<CourtAdminDocument>> updateDocumentCollection(CaseData caseData) {
        List<Element<CourtAdminDocument>> otherCourtAdminDocuments = caseData.getOtherCourtAdminDocuments();
        DocumentRouter router = caseData.getUploadDocumentsRouter();

        // Will be null if pageShow was NO in which case upload is the only option
        if (UPLOAD == router || null == router) {
            List<Element<CourtAdminDocument>> limitedCourtAdminDocuments = caseData.getLimitedCourtAdminDocuments();
            otherCourtAdminDocuments.addAll(limitedCourtAdminDocuments);
        } else {
            Object courtDocumentList = caseData.getCourtDocumentList();
            UUID selectedId = getSelectedIdFromDynamicList(courtDocumentList, mapper);
            int index = getSelectedIdIndex(selectedId, otherCourtAdminDocuments);

            if (AMEND == router) {
                otherCourtAdminDocuments.set(index, element(selectedId, caseData.getEditedCourtDocument()));
            } else if (DELETE == router) {
                otherCourtAdminDocuments.remove(index);
            }
        }
        return otherCourtAdminDocuments;
    }

    private int getSelectedIdIndex(UUID id, List<Element<CourtAdminDocument>> documentCollection) {
        for (int i = 0; i < documentCollection.size(); i++) {
            Element<?> element = documentCollection.get(i);
            if (id.equals(element.getId())) {
                return i;
            }
        }
        return -1;
    }
}
