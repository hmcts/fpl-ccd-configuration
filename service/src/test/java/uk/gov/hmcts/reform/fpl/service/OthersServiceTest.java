package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class OthersServiceTest {

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
        Others others = Others.builder()
            .firstOther(Other.builder()
                .birthplace("birth place")
                .build())
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - BLANK - Please complete\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenManyElementsInList() {
        Others others = Others.builder()
            .firstOther(Other.builder()
                .name("James Daniels")
                .build())
            .additionalOthers(ImmutableList.of(Element.<Other>builder()
                .value(Other.builder()
                    .name("Bob Martyn")
                    .build())
                .build()))
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - James Daniels\nOther person 1 - Bob Martyn\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenManyElementsInListWithEmptyName() {
        Others others = Others.builder()
            .firstOther(Other.builder()
                .birthplace("birth place")
                .build())
            .additionalOthers(ImmutableList.of(Element.<Other>builder()
                .value(Other.builder()
                    .birthplace("birth place")
                    .build())
                .build()))
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - BLANK - Please complete\nOther person 1 - BLANK - Please complete\n");
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
