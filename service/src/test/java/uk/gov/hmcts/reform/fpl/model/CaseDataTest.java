package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseDataTest {

    @Test
    public void shouldGetAllOthers() {
        Other other1 = Other.builder().build();
        Other other2 = Other.builder().build();
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other1)
                .additionalOthers(ElementUtils.wrapElements(other2))
                .build())
            .build();

        assertThat(caseData.getAllOthers()).containsExactly(other1, other2);
    }

    @Test
    public void shouldGetEmptyListOfOthersWhenOthersIsNull() {
        CaseData caseData = CaseData.builder().build();
        assertThat(caseData.getAllOthers()).isEmpty();
    }

    @Test
    public void shouldGetEmptyListOfOthersWhenOthersAreEmpty() {
        CaseData caseData = CaseData.builder()
            .others(Others.builder().build())
            .build();
        assertThat(caseData.getAllOthers()).isEmpty();
    }

    @Test
    public void shouldGetFirstOtherWhenNoAdditionalOthers() {
        Other other1 = Other.builder().build();
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other1)
                .build())
            .build();
        assertThat(caseData.getAllOthers()).containsExactly(other1);
    }

    @Test
    public void shouldFindFirstOther() {
        Other other1 = Other.builder().build();
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other1)
                .build())
            .build();
        assertThat(caseData.findOther(0)).isEqualTo(Optional.of(other1));
    }

    @Test
    public void shouldNotFindNonExistingOther() {
        Other other1 = Other.builder().build();
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other1)
                .build())
            .build();
        assertThat(caseData.findOther(1)).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldFindExistingOther() {
        Other other1 = Other.builder().build();
        Other other2 = Other.builder().build();
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other1)
                .additionalOthers(ElementUtils.wrapElements(other2))
                .build())
            .build();
        assertThat(caseData.findOther(1)).isEqualTo(Optional.of(other2));
    }

    @Test
    public void shouldFindExistingRespondent() {
        Respondent respondent = Respondent.builder().build();
        CaseData caseData = CaseData.builder()
            .respondents1(ElementUtils.wrapElements(respondent))
            .build();

        assertThat(caseData.findRespondent(0)).isEqualTo(Optional.of(respondent));
    }

    @Test
    public void shouldNotFindNonExistingRespondent() {
        Respondent respondent = Respondent.builder().build();
        CaseData caseData = CaseData.builder()
            .respondents1(ElementUtils.wrapElements(respondent))
            .build();

        assertThat(caseData.findRespondent(1)).isEqualTo(Optional.empty());
    }

}
