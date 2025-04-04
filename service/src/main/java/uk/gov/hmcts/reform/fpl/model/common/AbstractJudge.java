package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.JudgeType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;

@JsonSubTypes({
    @JsonSubTypes.Type(value = Judge.class),
    @JsonSubTypes.Type(value = JudgeAndLegalAdvisor.class)
})
@Jacksonized
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@SuperBuilder(toBuilder = true)
public class AbstractJudge {
    private final JudgeType judgeType;
    private JudgeOrMagistrateTitle judgeTitle;
    private String otherTitle;
    private final String judgeLastName;
    private final String judgeFullName;
    private final String judgeEmailAddress;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Deprecated
    private final YesNo judgeEnterManually;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final JudicialUser judgeJudicialUser;


    @JsonIgnore
    public String getJudgeOrMagistrateTitle() {
        if (judgeTitle == OTHER) {
            return otherTitle;
        }
        return judgeTitle.getLabel();
    }

    @JsonIgnore
    public String getJudgeName() {
        if (judgeTitle == MAGISTRATES) {
            return judgeFullName;
        }
        return judgeLastName;
    }

    public static <T extends AbstractJudge> T fromJudicialUserProfile(AbstractJudgeBuilder<T,?> builder,
                                                                      JudicialUserProfile jup,
                                                                      JudgeOrMagistrateTitle title) {
        String postNominals = isNotEmpty(jup.getPostNominals())
            ? (" " + jup.getPostNominals())
            : "";

        return builder
            .judgeTitle((title == null) ? JudgeOrMagistrateTitle.OTHER : title)
            .otherTitle((title == null) ? jup.getTitle() : null)
            .judgeLastName(jup.getSurname() + postNominals)
            .judgeFullName(jup.getFullName() + postNominals)
            .judgeEmailAddress(jup.getEmailId())
            .judgeJudicialUser(JudicialUser.builder()
                .idamId(jup.getSidamId())
                .personalCode(jup.getPersonalCode())
                .build())
            .build();
    }
}

