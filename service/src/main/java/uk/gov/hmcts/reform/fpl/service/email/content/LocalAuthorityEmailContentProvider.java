package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.StandardDirectionOrderContent;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityEmailContentProvider extends StandardDirectionOrderContent {
    private final LocalAuthorityNameLookupConfiguration config;

    public Map<String, Object> buildLocalAuthorityStandardDirectionOrderIssuedNotification(CaseData caseData) {
        return super.getSDOPersonalisationBuilder(caseData)
            .put("title", config.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .build();
    }

    public Map<String, Object> buildNoticeOfPlacementOrderUploadedNotification(CaseData caseData) {
        return Map.of(
            "respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()),
            "caseUrl", getCaseUrl(caseData.getId(), "PlacementTab"));
    }
}
