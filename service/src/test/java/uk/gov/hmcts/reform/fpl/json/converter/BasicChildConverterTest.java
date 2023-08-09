package uk.gov.hmcts.reform.fpl.json.converter;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testEmail;

class BasicChildConverterTest {

    private BasicChildConverter converter = new BasicChildConverter();

    @Test
    void shouldReturnNullIfConvertedElementIsNull() {
        assertThat(converter.convert(null)).isNull();
    }

    @Test
    void shouldReturnSameElementIfChildIsNull() {
        Element<Child> element = element(null);
        assertThat(converter.convert(element)).isEqualTo(element);
    }

    @Test
    void shouldConvertChildElementAndPreservesOnlyBasicFields() {

        ChildParty childParty = ChildParty.builder()
            .firstName("Alex")
            .lastName("Smith")
            .dateOfBirth(LocalDate.now().minusYears(8))
            .gender(ChildGender.BOY.getLabel())
            .email(testEmail())
            .address(testAddress())
            .additionalNeeds(randomAlphanumeric(5))
            .careAndContactPlan(randomAlphanumeric(5))
            .fathersName(randomAlphanumeric(5))
            .livingSituation(randomAlphanumeric(5))
            .build();

        ChildParty expectedChildParty = ChildParty.builder()
            .firstName("Alex")
            .lastName("Smith")
            .dateOfBirth(LocalDate.now().minusYears(8))
            .gender(ChildGender.BOY.getLabel())
            .build();

        Child child = Child.builder()
            .party(childParty)
            .finalOrderIssued("Yes")
            .build();

        Child expectedChild = Child.builder()
            .party(expectedChildParty)
            .finalOrderIssued("Yes")
            .build();

        Element<Child> childElement = element(child);
        Element<Child> expectedChildElement = element(childElement.getId(), expectedChild);

        Element<Child> converted = converter.convert(childElement);

        assertThat(converted).isEqualTo(expectedChildElement);
    }
}
