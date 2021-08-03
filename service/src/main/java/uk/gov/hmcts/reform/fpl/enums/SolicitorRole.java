package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.CHILD;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.RESPONDENT;

@RequiredArgsConstructor
@Getter
public enum SolicitorRole {
    SOLICITORA("[SOLICITORA]", 0, RESPONDENT),
    SOLICITORB("[SOLICITORB]", 1, RESPONDENT),
    SOLICITORC("[SOLICITORC]", 2, RESPONDENT),
    SOLICITORD("[SOLICITORD]", 3, RESPONDENT),
    SOLICITORE("[SOLICITORE]", 4, RESPONDENT),
    SOLICITORF("[SOLICITORF]", 5, RESPONDENT),
    SOLICITORG("[SOLICITORG]", 6, RESPONDENT),
    SOLICITORH("[SOLICITORH]", 7, RESPONDENT),
    SOLICITORI("[SOLICITORI]", 8, RESPONDENT),
    SOLICITORJ("[SOLICITORJ]", 9, RESPONDENT),
    CHILDSOLICITORA("[CHILDSOLICITORA]", 0, CHILD),
    CHILDSOLICITORB("[CHILDSOLICITORB]", 1, CHILD),
    CHILDSOLICITORC("[CHILDSOLICITORC]", 2, CHILD),
    CHILDSOLICITORD("[CHILDSOLICITORD]", 3, CHILD),
    CHILDSOLICITORE("[CHILDSOLICITORE]", 4, CHILD),
    CHILDSOLICITORF("[CHILDSOLICITORF]", 5, CHILD),
    CHILDSOLICITORG("[CHILDSOLICITORG]", 6, CHILD),
    CHILDSOLICITORH("[CHILDSOLICITORH]", 7, CHILD),
    CHILDSOLICITORI("[CHILDSOLICITORI]", 8, CHILD),
    CHILDSOLICITORJ("[CHILDSOLICITORJ]", 9, CHILD),
    CHILDSOLICITORK("[CHILDSOLICITORK]", 10, CHILD),
    CHILDSOLICITORL("[CHILDSOLICITORL]", 11, CHILD),
    CHILDSOLICITORM("[CHILDSOLICITORM]", 12, CHILD),
    CHILDSOLICITORN("[CHILDSOLICITORN]", 13, CHILD),
    CHILDSOLICITORO("[CHILDSOLICITORO]", 14, CHILD);

    private final String caseRoleLabel;
    private final int index;
    private final Representing representing;

    public static Optional<SolicitorRole> from(String label) {
        return Arrays.stream(SolicitorRole.values())
            .filter(role -> role.caseRoleLabel.equals(label))
            .findFirst();
    }

    public static List<SolicitorRole> values(Representing representing) {
        return Arrays.stream(SolicitorRole.values())
            .filter(role -> role.representing == representing)
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public enum Representing {
        RESPONDENT(caseData -> (List) caseData.getAllRespondents(),
            "respondentPolicy%d",
            "noticeOfChangeAnswers%d",
            "respondents1"),
        CHILD(caseData -> (List) caseData.getAllChildren(),
            "childPolicy%d",
            "noticeOfChangeChildAnswers%d",
            "children1");

        private final Function<CaseData, List<Element<WithSolicitor>>> target;
        private final String policyFieldTemplate;
        private final String nocAnswersTemplate;
        private final String caseField;

        Representing(Function<CaseData, List<Element<WithSolicitor>>> target,
                     String policyFieldTemplate, String nocAnswersTemplate, String caseField) {
            this.target = target;
            this.policyFieldTemplate = policyFieldTemplate;
            this.nocAnswersTemplate = nocAnswersTemplate;
            this.caseField = caseField;
        }

        public Function<CaseData, List<Element<WithSolicitor>>> getTarget() {
            return target;
        }

        public String getPolicyFieldTemplate() {
            return policyFieldTemplate;
        }

        public String getNocAnswersTemplate() {
            return nocAnswersTemplate;
        }

    }
}
