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

import java.util.Arrays;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

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
    @CCD(label = "Judge or magistrate's type")
    private final JudgeType judgeType;
    @CCD(label = "Judge or Magistrate's title")
    private JudgeOrMagistrateTitle judgeTitle;
    @CCD(label = "Title", showCondition = "judgeTitle=\"OTHER\" AND useAllocatedJudge!=\"Yes\"")
    private String otherTitle;
    @CCD(label = "Last name", showCondition = "judgeTitle!=\"MAGISTRATES\"")
    private final String judgeLastName;
    @CCD(label = "Full name", showCondition = "judgeTitle=\"MAGISTRATES\"")
    private final String judgeFullName;
    @CCD(label = "Email Address", typeOverride = FieldType.Email)
    private final String judgeEmailAddress;

    @CCD(
            label = "Add legal adviser details",
            showCondition = "judgeEmailAddress=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Deprecated
    private final YesNo judgeEnterManually;

    @CCD(
            label = "Search for Judge",
            showCondition = "judgeEmailAddress=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.JudicialUser
    )
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

    @JsonIgnore
    public boolean isDetailsEnterManually() {
        return YesNo.YES.equals(judgeEnterManually) // historical data
               || (judgeType == null && judgeEnterManually == null) // historical data
               || JudgeType.LEGAL_ADVISOR.equals(judgeType);
    }

    public static <T extends AbstractJudge> T fromJudicialUserProfile(AbstractJudgeBuilder<T,?> builder,
                                                                      JudicialUserProfile jup,
                                                                      JudgeOrMagistrateTitle title) {
        String postNominals = isNotEmpty(jup.getPostNominals())
            ? (" " + jup.getPostNominals())
            : "";

        JudgeOrMagistrateTitle judgeTitle = (title != null)
            ? title
            : Arrays.stream(JudgeOrMagistrateTitle.values())
                .filter(titleEnum -> titleEnum.getLabel().equalsIgnoreCase(jup.getTitle()))
                .findFirst()
                .orElse(null);

        return builder
            .judgeTitle((judgeTitle == null) ? JudgeOrMagistrateTitle.OTHER : judgeTitle)
            .otherTitle((judgeTitle == null) ? jup.getTitle() : null)
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

