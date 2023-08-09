package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.HOSPITAL_SOON_TO_BE_DISCHARGED;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.NOT_SPECIFIED;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.REMOVED_BY_POLICE_POWER_ENDS;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.VOLUNTARILY_SECTION_CARE_ORDER;

class ChildLivingSituationTest {

    @Test
    void shouldReturnCorrectChildLivingSituationWhenStringValueGiven() {
        assertThat(ChildLivingSituation.fromString("In hospital and soon to be discharged"))
            .isEqualTo(HOSPITAL_SOON_TO_BE_DISCHARGED);
        assertThat(ChildLivingSituation.fromString("Removed by Police, powers ending soon"))
            .isEqualTo(REMOVED_BY_POLICE_POWER_ENDS);
        assertThat(ChildLivingSituation.fromString("Voluntarily in section 20 care order"))
            .isEqualTo(VOLUNTARILY_SECTION_CARE_ORDER);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnNotSpecifiedAsChildLivingSituationWhenNullOrEmptyValueGiven(final String value) {
        assertThat(ChildLivingSituation.fromString(value))
            .isEqualTo(NOT_SPECIFIED);
    }
}
