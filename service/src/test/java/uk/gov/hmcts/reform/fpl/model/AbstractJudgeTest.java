package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractJudgeTest {
    private static final Judge EXPECTED_JUDGE = Judge.builder()
        .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
            .otherTitle(null)
            .judgeLastName("surname postNominals")
            .judgeFullName("fullName postNominals")
            .judgeEmailAddress("emailId")
            .judgeJudicialUser(JudicialUser.builder()
                .idamId("sidamId")
                .personalCode("personalCode")
                .build())
        .build();


    @Test
    void shouldMapJupTitleIfMatchWithEnumsLabel() {
        Judge actual = Judge.fromJudicialUserProfile(JudicialUserProfile.builder()
            .title("Her Honour Judge")
            .fullName("fullName")
            .surname("surname")
            .postNominals("postNominals")
            .emailId("emailId")
            .sidamId("sidamId")
            .personalCode("personalCode")
            .build(), null);

        assertThat(actual).isEqualTo(EXPECTED_JUDGE);
    }

    @Test
    void shouldMapOtherTitleIfNotMatchWithEnumsLabel() {
        Judge actual = Judge.fromJudicialUserProfile(JudicialUserProfile.builder()
            .title("test")
            .fullName("fullName")
            .surname("surname")
            .postNominals("postNominals")
            .emailId("emailId")
            .sidamId("sidamId")
            .personalCode("personalCode")
            .build(), null);

        assertThat(actual).isEqualTo(EXPECTED_JUDGE.toBuilder()
            .judgeTitle(JudgeOrMagistrateTitle.OTHER)
            .otherTitle("test").build());
    }

    @Test
    void shouldUseGivenTitle() {
        Judge actual = Judge.fromJudicialUserProfile(JudicialUserProfile.builder()
            .title("Her Honour Judge")
            .fullName("fullName")
            .surname("surname")
            .postNominals("postNominals")
            .emailId("emailId")
            .sidamId("sidamId")
            .personalCode("personalCode")
            .build(), JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE);

        assertThat(actual).isEqualTo(EXPECTED_JUDGE.toBuilder()
            .judgeTitle(JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE)
            .build());
    }
}
