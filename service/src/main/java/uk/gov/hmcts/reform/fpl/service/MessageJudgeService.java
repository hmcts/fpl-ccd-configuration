package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Comparator;
import java.util.List;

import static java.lang.String.join;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.JUDICIARY;

public abstract class MessageJudgeService {
    @Autowired
    protected Time time;

    @Autowired
    protected CtscEmailLookupConfiguration ctscEmailLookupConfiguration;

    @Autowired
    protected UserService userService;

    @Autowired
    protected ManageDocumentService manageDocumentService;

    protected boolean isJudiciary() {
        return userService.hasUserRole(JUDICIARY);
    }

    protected String getNextHearingLabel(CaseData caseData) {
        return caseData.getNextHearingAfter(time.now())
            .map(hearing -> String.format("Next hearing in the case: %s", hearing.toLabel()))
            .orElse("");
    }

    public List<Element<JudicialMessage>> sortJudicialMessages(List<Element<JudicialMessage>> judicialMessages) {
        judicialMessages.sort(Comparator.comparing(judicialMessageElement
            -> judicialMessageElement.getValue().getUpdatedTime(), Comparator.reverseOrder()));

        return judicialMessages;
    }

    protected String buildMessageHistory(String message, String history, String sender) {
        String formattedLatestMessage = String.format("%s - %s", sender, message);

        if (history.isBlank()) {
            return formattedLatestMessage;
        }

        return join("\n \n", history, formattedLatestMessage);
    }

    protected String getEmailAddressByRoleType(JudicialMessageRoleType roleType) {
        if (JudicialMessageRoleType.JUDICIARY.equals(roleType)) {
            return userService.getUserEmail();
        } else if (JudicialMessageRoleType.CTSC.equals(roleType)) {
            return ctscEmailLookupConfiguration.getEmail();
        }

        return EMPTY;
    }

    protected String resolveSenderEmailAddress(JudicialMessageRoleType roleType, String emailFilledByUser) {
        String resolvedAddress = getEmailAddressByRoleType(resolveSenderRoleType(roleType));
        return isNotEmpty(resolvedAddress) ? resolvedAddress : emailFilledByUser;
    }

    protected String resolveRecipientEmailAddress(JudicialMessageRoleType roleType, String emailFilledByUser) {
        return (JudicialMessageRoleType.CTSC.equals(roleType))
            ? getEmailAddressByRoleType(JudicialMessageRoleType.CTSC) : emailFilledByUser;
    }

    protected JudicialMessageRoleType resolveSenderRoleType(JudicialMessageRoleType roleTypeSelectedByUser) {
        if (isJudiciary()) {
            return JudicialMessageRoleType.JUDICIARY;
        }
        return roleTypeSelectedByUser;
    }
}
