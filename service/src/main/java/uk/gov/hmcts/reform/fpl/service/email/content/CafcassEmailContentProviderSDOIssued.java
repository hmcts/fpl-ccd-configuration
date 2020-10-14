package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.StandardDirectionOrderContent;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.concatUrlAndMostRecentUploadedDocumentPath;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassEmailContentProviderSDOIssued extends StandardDirectionOrderContent {
    private final CafcassLookupConfiguration config;
    @Value("${manage-case.ui.base.url}")
    private String xuiBaseUrl;

    public Map<String, Object> buildCafcassStandardDirectionOrderIssuedNotification(CaseData caseData) {
        return super.getSDOPersonalisationBuilder(caseData)
            .put("title", config.getCafcass(caseData.getCaseLocalAuthority()).getName())
            .put("documentLink", concatUrlAndMostRecentUploadedDocumentPath(xuiBaseUrl,
                caseData.getStandardDirectionOrder().getOrderDoc().getBinaryUrl()))
            .build();
    }
}
