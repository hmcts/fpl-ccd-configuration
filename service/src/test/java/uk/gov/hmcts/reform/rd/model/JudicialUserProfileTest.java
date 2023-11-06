package uk.gov.hmcts.reform.rd.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class JudicialUserProfileTest {

    @ParameterizedTest
    @MethodSource("judgeTitleNames")
    void shouldGetJudgeTitleFromFullName(String title) {
        JudicialUserProfile jup = JudicialUserProfile.builder()
            .fullName(title + " John Smith")
            .build();

        assertThat(jup.getTitle()).isEqualTo(title);
    }

    @Test
    void shouldLookupJudgeTitlesCaseInsensitive() {
        JudicialUserProfile jup = JudicialUserProfile.builder()
            .fullName("HIS HONOUR JUDGE John Smith")
            .build();

        assertThat(jup.getTitle()).isEqualTo("His Honour Judge");
    }

    @Test
    void shouldUseUnknownIfNoJudgeTitle() {
        JudicialUserProfile jup = JudicialUserProfile.builder()
            .fullName("Random Title Here John Smith")
            .build();

        assertThat(jup.getTitle()).isEqualTo("Judge");
    }

    @Test
    void shouldUseJudicialUserProfileTitleIfPresent() {
        JudicialUserProfile jup = JudicialUserProfile.builder()
            .title("His Honour Judge")
            .fullName("His Honour Judge John Smith")
            .build();

        assertThat(jup.getTitle()).isEqualTo("His Honour Judge");
    }

    private static Stream<Arguments> judgeTitleNames() {
        return Stream.of("Baroness",
            "Deputy District Judge",
            "District Judge (MC)",
            "District Judge",
            "Employment Judge",
            "Her Honour Judge",
            "Her Honour",
            "His Honour Judge",
            "His Honour",
            "ICC Judge",
            "Immigration Judge",
            "Recorder",
            "The Hon Mr Justice",
            "The Hon Mrs Justice",
            "The Hon Ms Justice",
            "The Hon Miss Justice",
            "Tribunal Judge",
            "Upper Tribunal Judge",
            "Mrs",
            "Mr",
            "Ms",
            "Miss",
            "Dr",
            "Judge",
            "Dame",
            "Sir"
        ).map(Arguments::of);
    }

}
