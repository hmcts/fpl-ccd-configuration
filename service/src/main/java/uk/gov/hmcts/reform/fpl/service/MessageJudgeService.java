package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole;
import uk.gov.hmcts.reform.fpl.enums.OrganisationalRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.join;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.ALLOCATED_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.HEARING_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.JUDICIARY;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

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

    @Autowired
    protected RoleAssignmentService roleAssignmentService;

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

    protected String getSenderEmailAddressByRoleType(JudicialMessageRoleType roleType) {
        if (JudicialMessageRoleType.CTSC.equals(roleType)) {
            return ctscEmailLookupConfiguration.getEmail();
        }

        return userService.getUserEmail();
    }

    protected String formatLabel(JudicialMessageRoleType roleType, String email) {
        if (JudicialMessageRoleType.CTSC.equals(roleType)) {
            return JudicialMessageRoleType.CTSC.getLabel();
        } else {
            return roleType.getLabel() + (isNotEmpty(email) ? " (%s)".formatted(email) : "");
        }
    }

    protected String resolveRecipientEmailAddress(JudicialMessageRoleType roleType,
                                                  String emailFilledByUser,
                                                  CaseData caseData) {
        return switch (roleType) {
            case CTSC -> ctscEmailLookupConfiguration.getEmail();
            case ALLOCATED_JUDGE -> judicialService.getAllocatedJudge(caseData)
                .map(Judge::getJudgeEmailAddress).orElse(null);
            case HEARING_JUDGE -> judicialService.getCurrentHearingJudge(caseData)
                .map(JudgeAndLegalAdvisor::getJudgeEmailAddress).orElse(null);
            case LOCAL_COURT_ADMIN -> emailFilledByUser;
            case OTHER -> emailFilledByUser;
        };
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

    public DynamicList buildRecipientDynamicList(CaseData caseData,
                                                 JudicialMessageRoleType senderRole,
                                                 Optional<String> chosen) {
        List<RoleAssignment> currentRoles = roleAssignmentService
            .getJudicialCaseRolesAtTime(caseData.getId(), ZonedDateTime.now());

        final boolean hasAllocatedJudgeRole = currentRoles.stream()
            .anyMatch(role -> role.getRoleName().equals(JudgeCaseRole.ALLOCATED_JUDGE.getRoleName())
                || role.getRoleName().equals(LegalAdviserRole.ALLOCATED_LEGAL_ADVISER.getRoleName()));

        final boolean hasHearingJudgeRole = currentRoles.stream()
            .anyMatch(role -> role.getRoleName().equals(JudgeCaseRole.HEARING_JUDGE.getRoleName())
                || role.getRoleName().equals(LegalAdviserRole.HEARING_LEGAL_ADVISER.getRoleName()));


        List<DynamicListElement> elements = new ArrayList<>();

        elements.add(DynamicListElement.builder()
            .code(JudicialMessageRoleType.CTSC.toString())
            .label(JudicialMessageRoleType.CTSC.getLabel())
            .build());
        elements.add(DynamicListElement.builder()
            .code(JudicialMessageRoleType.LOCAL_COURT_ADMIN.toString())
            .label(JudicialMessageRoleType.LOCAL_COURT_ADMIN.getLabel()
                + (isNotEmpty(caseData.getCourt()) ? " - %s".formatted(caseData.getCourt().getName()) : "")
            )
            .build());

        Optional<Judge> allocatedJudge = judicialService.getAllocatedJudge(caseData);
        allocatedJudge.ifPresent(judge -> elements.add(DynamicListElement.builder()
            .code(JudicialMessageRoleType.ALLOCATED_JUDGE.toString())
            .label(getJudgeLabel(JudicialMessageRoleType.ALLOCATED_JUDGE, judge.toJudgeAndLegalAdvisor(),
                hasAllocatedJudgeRole))
            .build()));

        Optional<JudgeAndLegalAdvisor> hearingJudge = judicialService.getCurrentHearingJudge(caseData);
        hearingJudge.ifPresent(judge -> elements.add(DynamicListElement.builder()
            .code(JudicialMessageRoleType.HEARING_JUDGE.toString())
            .label(getJudgeLabel(JudicialMessageRoleType.HEARING_JUDGE, judge, hasHearingJudgeRole))
            .build()));

        elements.add(DynamicListElement.builder()
                .code(JudicialMessageRoleType.OTHER.toString())
                .label(JudicialMessageRoleType.OTHER.getLabel())
            .build());

        return DynamicList.builder()
            .listItems(elements.stream()
                .filter(el -> !el.getCode().equals(senderRole.toString()))
                .toList())
            .value(chosen.flatMap(s -> elements.stream().filter(el -> el.hasCode(s)).findFirst()).orElse(null))
            .build();
    }

    protected String getJudgeLabel(JudicialMessageRoleType type, JudgeAndLegalAdvisor judge, boolean hasRole) {
        return "%s %s - %s (%s)%s".formatted(
            JudicialMessageRoleType.ALLOCATED_JUDGE.equals(type) ? "Allocated" : "Hearing",
            JudgeOrMagistrateTitle.LEGAL_ADVISOR.equals(judge.getJudgeTitle()) ? "Legal Adviser" : "Judge",
            formatJudgeTitleAndName(judge),
            judge.getJudgeEmailAddress(),
            (hasRole ? "" : " *")
        );
    }
}
