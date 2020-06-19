package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.StandardDirectionOrderContent;

import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassEmailContentProviderSDOIssued extends StandardDirectionOrderContent {
    private final CafcassLookupConfiguration config;
    private final ObjectMapper mapper;

    public Map<String, Object> buildCafcassStandardDirectionOrderIssuedNotification(CaseDetails caseDetails,
                                                                                    String localAuthorityCode) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return super.getSDOPersonalisationBuilder(caseDetails.getId(), caseData)
            .put("title", config.getCafcass(localAuthorityCode).getName())
            .build();
    }
}
