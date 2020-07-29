package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FactorsAffectingParentingValidator.class})
class FactorsAffectingParentingValidatorTest {

    @Autowired
    private FactorsAffectingParentingValidator factorsAffectingParentingValidator;

    @Test
    void shouldReturnErrorWhenFactorsAffectingParentingNotProvided() {
        final CaseData caseData = CaseData.builder().build();

        final List<String> errors = factorsAffectingParentingValidator.validate(caseData);

        assertThat(errors).containsExactly("You need to add factors affecting parenting");
    }

    @Test
    void shouldReturnErrorWhenFactorsAffectingParentingDetailsNotProvided() {
        final CaseData caseData = CaseData.builder()
            .factorsParenting(FactorsParenting.builder().build())
            .build();

        final List<String> errors = factorsAffectingParentingValidator.validate(caseData);

        assertThat(errors).containsExactly("You need to add factors affecting parenting");
    }

    @Test
    void shouldReturnEmptyErrorsWhenAtLeastAlcoholDrugAbuseIsProvided() {
        final CaseData caseData = CaseData.builder()
            .factorsParenting(FactorsParenting.builder()
                .alcoholDrugAbuse("Yes")
                .build())
            .build();

        final List<String> errors = factorsAffectingParentingValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnEmptyErrorsWhenAtLeastDomesticViolenceIsProvided() {
        final CaseData caseData = CaseData.builder()
            .factorsParenting(FactorsParenting.builder()
                .domesticViolence("No")
                .build())
            .build();

        final List<String> errors = factorsAffectingParentingValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnEmptyErrorsWhenAnythingElseIsProvided() {
        final CaseData caseData = CaseData.builder()
            .factorsParenting(FactorsParenting.builder()
                .anythingElse("Yes")
                .build())
            .build();

        final List<String> errors = factorsAffectingParentingValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }

}
