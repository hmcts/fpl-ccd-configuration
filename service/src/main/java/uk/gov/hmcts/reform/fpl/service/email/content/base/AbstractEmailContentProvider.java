package uk.gov.hmcts.reform.fpl.service.email.content.base;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.exceptions.DocumentException;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public abstract class AbstractEmailContentProvider {

    @Autowired
    private CaseUrlService caseUrlService;

    @Autowired
    private DocumentDownloadService documentDownloadService;

    public String getCaseUrl(Long caseId) {
        return caseUrlService.getCaseUrl(caseId);
    }

    public String getCaseUrl(Long caseId, TabUrlAnchor tab) {
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

    protected String getDocumentUrl(DocumentReference document) {
        String binaryUrl = document.getBinaryUrl();
        try {
            URI uri = new URI(binaryUrl);
            return caseUrlService.getBaseUrl() + uri.getPath();
        } catch (URISyntaxException e) {
            log.error(binaryUrl + " url incorrect.", e);
        }
        return "";
    }

    protected String getJudgeName(AbstractJudge judgeAndLegalAdvisor) {
        if (MAGISTRATES.equals(judgeAndLegalAdvisor.getJudgeTitle())) {
            return Optional.ofNullable(judgeAndLegalAdvisor.getJudgeName())
                .map(name -> name.concat(" (JP)"))
                .orElse("");
        }

        return defaultIfNull(judgeAndLegalAdvisor.getJudgeName(), "");
    }

    protected String getJudgeTitle(AbstractJudge judgeAndLegalAdvisor) {
        if (MAGISTRATES.equals(judgeAndLegalAdvisor.getJudgeTitle())) {
            return judgeAndLegalAdvisor.getJudgeName() == null ? "Justice of the Peace" : "";
        }

        return defaultIfNull(judgeAndLegalAdvisor.getJudgeOrMagistrateTitle(), "");
    }
}
