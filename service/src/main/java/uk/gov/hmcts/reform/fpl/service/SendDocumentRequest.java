package uk.gov.hmcts.reform.fpl.service;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReferenceWithLanguage;

import java.util.List;

@Value
public class SendDocumentRequest {
    CaseData caseData;
    List<DocumentReferenceWithLanguage> documentToBeSent;
    List<Recipient> parties;
}
