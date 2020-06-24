package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentFullName;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnedCaseContentProvider extends AbstractEmailContentProvider {
    private final ObjectMapper mapper;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookup;
    private final DocumentDownloadService documentDownloadService;

    public ReturnedCaseTemplate parametersWithCaseUrl(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return templateData(caseData)
            .caseUrl(getCaseUrl(caseDetails.getId()))
            .build();
    }

    public ReturnedCaseTemplate parametersWithApplicationLink(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return templateData(caseData)
            .applicationDocumentUrl(linkToAttachedDocument(caseData.getSubmittedForm()))
            .build();
    }

    private ReturnedCaseTemplate.ReturnedCaseTemplateBuilder templateData(CaseData caseData) {
        ReturnApplication returnApplication = caseData.getReturnApplication();

        return ReturnedCaseTemplate.builder()
            .localAuthority(localAuthorityNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .respondentFullName(getFirstRespondentFullName(caseData.getRespondents1()))
            .respondentLastName(getFirstRespondentLastName(caseData.getRespondents1()))
            .familyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
            .returnedReasons(returnApplication.getFormattedReturnReasons())
            .returnedNote(returnApplication.getNote());
    }

    private Map<String, Object> linkToAttachedDocument(final DocumentReference documentReference) {
        byte[] content = documentDownloadService.downloadDocument(documentReference.getBinaryUrl());
        return generateAttachedDocumentLink(content).map(JSONObject::toMap).orElse(null);
    }
}
