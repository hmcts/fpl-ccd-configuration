package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
class OthersServiceTest {
    private static final UUID ID = randomUUID();

    private final OthersService service = new OthersService();

    @Test
    void shouldBuildExpectedLabelWhenSingleElementInList() {
        Others others = Others.builder()
            .firstOther(Other.builder()
                .name("James Daniels")
                .build())
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - James Daniels\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenSingleElementInListWithEmptyName() {
        Others others = Others.builder().firstOther(Other.builder().birthPlace("birth place").build()).build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - \n");
    }

    @Test
    void shouldBuildExpectedLabelWhenManyElementsInList() {
        Others others = Others.builder()
            .firstOther(Other.builder().name("James Daniels").build())
            .additionalOthers(wrapElements((Other.builder().name("Bob Martyn").build())))
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - James Daniels\nOther person 1 - Bob Martyn\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenManyElementsInListWithEmptyName() {
        Others others = Others.builder()
            .firstOther(Other.builder().birthPlace("birth place").build())
            .additionalOthers(wrapElements(Other.builder().birthPlace("birth place").build()))
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - \nOther person 1 - \n");
    }

    @Test
    void shouldBuildExpectedLabelWhenNull() {
        String result = service.buildOthersLabel(null);

        assertThat(result).isEqualTo("No others on the case");
    }

    @Test
    void shouldBuildExpectedLabelWhenEmptyOthers() {
        String result = service.buildOthersLabel(Others.builder().build());

        assertThat(result).isEqualTo("No others on the case");
    }
}
