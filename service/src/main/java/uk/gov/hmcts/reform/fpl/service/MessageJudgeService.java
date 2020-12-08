package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageJudgeService {
    private final Time time;
    private final IdentityService identityService;

    public Map<String, Object> initialiseCaseFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        data.put("hasC2Applications", YES.getValue());
        data.put("c2DynamicList", caseData.buildC2DocumentDynamicList());

        return data;
    }

    public Map<String, Object> buildRelatedC2DocumentFields(CaseData caseData, UUID selectedC2Id) {
        Map<String, Object> data = new HashMap<>();

        C2DocumentBundle selectedC2DocumentBundle = caseData.getC2DocumentBundleByUUID(selectedC2Id);
        List<Element<DocumentReference>> documents
            = selectedC2DocumentBundle.getMainC2AndSupportingEvidenceBundleDocumentReferences();

        data.put("relatedDocumentsLabel", documents);
        data.put("c2DynamicList", rebuildC2DynamicList(caseData, selectedC2Id));

        return data;
    }

    public List<Element<JudicialMessage>> addNewJudicialMessage(CaseData caseData) {
        List<Element<JudicialMessage>> judicialMessages = caseData.getJudicialMessages();
        JudicialMessageMetaData judicialMessageMetaData = caseData.getJudicialMessageMetaData();

        JudicialMessage newMessage = JudicialMessage.builder()
            .sender(judicialMessageMetaData.getSender())
            .recipient(judicialMessageMetaData.getRecipient())
            .note(caseData.getJudicialMessageNote())
            .dateSentAsLocalDateTime(time.now())
            .dateSent(formatLocalDateToString(time.now().toLocalDate(), "d MMMM yyyy"))
            .relatedDocuments(caseData.getRelatedDocumentsLabel())
            .status(OPEN)
            .build();

        judicialMessages.add(element(identityService.generateId(), newMessage));
        return judicialMessages;
    }

    public List<Element<JudicialMessage>> sortJudicialMessages(List<Element<JudicialMessage>> judicialMessages) {
        judicialMessages.sort(Comparator.comparing(judicialMessageElement
            -> judicialMessageElement.getValue().getDateSentAsLocalDateTime(), Comparator.reverseOrder()));

        return judicialMessages;
    }

    private DynamicList rebuildC2DynamicList(CaseData caseData, UUID selectedC2Id) {
        return caseData.buildC2DocumentDynamicList(selectedC2Id);
    }
}
