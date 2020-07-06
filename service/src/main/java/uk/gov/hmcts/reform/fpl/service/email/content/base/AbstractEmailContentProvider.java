package uk.gov.hmcts.reform.fpl.service.email.content.base;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.exceptions.DocumentException;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractEmailContentProvider {

    @Autowired
    private CaseUrlService caseUrlService;

    @Autowired
    private DocumentDownloadService documentDownloadService;

    public String getCaseUrl(Long caseId) {
        return caseUrlService.getCaseUrl(caseId);
    }

    public String getCaseUrl(Long caseId, String tab) {
        return caseUrlService.getCaseUrl(caseId, tab);
    }

    protected Map<String, Object> linkToAttachedDocument(final DocumentReference documentReference) {
        return Optional.ofNullable(documentReference)
            .map(DocumentReference::getBinaryUrl)
            .map(documentDownloadService::downloadDocument)
            .flatMap(NotifyAttachedDocumentLinkHelper::generateAttachedDocumentLink)
            .map(JSONObject::toMap)
            .orElseThrow(DocumentException::new);
    }
}
