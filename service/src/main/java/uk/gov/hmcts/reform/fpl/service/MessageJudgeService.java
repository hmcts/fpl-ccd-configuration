package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.enums.OrganisationalRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.join;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.ALLOCATED_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.HEARING_LEGAL_ADVISER;
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

    @Autowired
    protected JudicialService judicialService;


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
        if (JudicialMessageRoleType.CTSC.equals(roleType)) {
            return ctscEmailLookupConfiguration.getEmail();
        }

        return userService.getUserEmail();
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
            return JudicialMessageRoleType.ALLOCATED_JUDGE;
        }
        return roleTypeSelectedByUser;
    }

    public JudicialMessageRoleType getSenderRole(CaseData caseData) {
        Set<OrganisationalRole> roles = userService.getOrgRoles();
        Set<RoleCategory> categories = roles.stream().map(OrganisationalRole::getRoleCategory)
            .collect(Collectors.toSet());

        if (categories.contains(RoleCategory.JUDICIAL) || categories.contains(RoleCategory.LEGAL_OPERATIONS)) {
            // need to identify if allocated or hearing judge/legal adviser
            Set<String> judicialCaseRoles = userService.getJudicialCaseRoles(caseData.getId());

            // if user has both allocated and hearing roles, prefer allocated, as less likely to change
            if (judicialCaseRoles.contains(ALLOCATED_JUDGE.getRoleName())
                || judicialCaseRoles.contains(ALLOCATED_LEGAL_ADVISER.getRoleName())) {
                return JudicialMessageRoleType.ALLOCATED_JUDGE;
            } else if (judicialCaseRoles.contains(HEARING_JUDGE.getRoleName())
                || judicialCaseRoles.contains(HEARING_LEGAL_ADVISER.getRoleName())) {
                return JudicialMessageRoleType.HEARING_JUDGE;
            } else {
                return JudicialMessageRoleType.OTHER;
            }

        } else if (categories.contains(RoleCategory.CTSC)) {
            return JudicialMessageRoleType.CTSC;
        } else if (categories.contains(RoleCategory.ADMIN)) {
            return JudicialMessageRoleType.LOCAL_COURT_ADMIN;
        }
        return JudicialMessageRoleType.OTHER;
    }
}
