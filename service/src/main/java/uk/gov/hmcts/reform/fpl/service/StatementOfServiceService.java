package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipients;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StatementOfServiceService {
    public List<Element<Recipients>> expandRecipientCollection(CaseData caseData) {
        if (caseData.getStatementOfService() == null) {
            List<Element<Recipients>> populatedRecipient = new ArrayList<>();

            populatedRecipient.add(Element.<Recipients>builder()
                .id(UUID.randomUUID())
                .value(Recipients.builder().name("").build())
                .build());
            return populatedRecipient;
        } else {
            return caseData.getStatementOfService();
        }
    }
}
