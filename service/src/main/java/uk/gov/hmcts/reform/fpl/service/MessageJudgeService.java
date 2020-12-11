package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageJudgeService {
    private final Time time;
    private final IdentityService identityService;
    private final ObjectMapper mapper;

    public Map<String, Object> initialiseCaseFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (hasC2Documents(caseData)) {
            data.put("hasC2Applications", YES.getValue());
            data.put("c2DynamicList", caseData.buildC2DocumentDynamicList());
        }

        if (hasJudicialMessages(caseData)) {
            data.put("hasJudicialMessages", YES.getValue());
            data.put("judicialMessageDynamicList", caseData.buildJudicialMessageDynamicList());
        }

        return data;
    }

    public Map<String, Object> buildRelatedC2DocumentFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (hasSelectedC2(caseData)) {
            UUID selectedC2Id = getDynamicListSelectedValue(
                caseData.getMessageJudgeEventData().getC2DynamicList(), mapper
            );

            C2DocumentBundle selectedC2DocumentBundle = caseData.getC2DocumentBundleByUUID(selectedC2Id);
            String documentFileNames = selectedC2DocumentBundle.getAllC2DocumentFileNames();

            data.put("relatedDocumentsLabel", documentFileNames);
            data.put("c2DynamicList", rebuildC2DynamicList(caseData, selectedC2Id));
        }

        return data;
    }

    public List<Element<JudicialMessage>> addNewJudicialMessage(CaseData caseData) {
        List<Element<JudicialMessage>> judicialMessages = caseData.getJudicialMessages();
        MessageJudgeEventData messageJudgeEventData = caseData.getMessageJudgeEventData();
        JudicialMessageMetaData judicialMessageMetaData = messageJudgeEventData.getJudicialMessageMetaData();

        JudicialMessage.JudicialMessageBuilder<?, ?> judicialMessageBuilder = JudicialMessage.builder()
            .sender(judicialMessageMetaData.getSender())
            .recipient(judicialMessageMetaData.getRecipient())
            .note(messageJudgeEventData.getJudicialMessageNote())
            .dateSentAsLocalDateTime(time.now())
            .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
            .urgency(judicialMessageMetaData.getUrgency())
            .status(OPEN);

        if (hasSelectedC2(caseData)) {
            UUID selectedC2Id = getDynamicListSelectedValue(messageJudgeEventData.getC2DynamicList(), mapper);
            C2DocumentBundle selectedC2DocumentBundle = caseData.getC2DocumentBundleByUUID(selectedC2Id);

            judicialMessageBuilder.relatedDocuments(selectedC2DocumentBundle.getAllC2DocumentReferences());
            judicialMessageBuilder.relatedC2Identifier(selectedC2Id);
        }

        judicialMessages.add(element(identityService.generateId(), judicialMessageBuilder.build()));
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

    private boolean hasC2Documents(CaseData caseData) {
        return caseData.getC2DocumentBundle() != null;
    }

    private boolean hasSelectedC2(CaseData caseData) {
        return hasC2Documents(caseData)
            && caseData.getMessageJudgeEventData().getC2DynamicList() != null;
    }

    private boolean hasJudicialMessages(CaseData caseData) {
        return caseData.getJudicialMessages() != null;
    }
}
