package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {PrepareDirectionsForDataStoreService.class})
class PrepareDirectionsForDataStoreServiceTest {

    @Autowired
    private PrepareDirectionsForDataStoreService service;

    @Test
    void persistHiddenDirectionValues_shouldAddValuesHiddenInCcdUiIncludingTextWhenReadOnlyIsYes() {
        UUID uuid = randomUUID();

        List<Element<Direction>> withHiddenValues = List.of(
            element(uuid, Direction.builder()
                .directionType("direction type")
                .directionText("hidden text")
                .readOnly("Yes")
                .directionRemovable("No")
                .build()));

        List<Element<Direction>> toAddValues = List.of(
            element(uuid, Direction.builder()
                .directionType("direction type")
                .build()));

        service.persistHiddenDirectionValues(withHiddenValues, toAddValues);

        assertThat(toAddValues).isEqualTo(withHiddenValues);
    }

    @Test
    void persistHiddenDirectionValues_shouldAddValuesHiddenInCcdUiExcludingTextWhenReadOnlyIsNo() {
        UUID uuid = randomUUID();

        List<Element<Direction>> withHiddenValues = List.of(
            element(uuid, Direction.builder()
                .directionType("direction type")
                .directionText("hidden text")
                .readOnly("No")
                .directionRemovable("No")
                .build()));

        List<Element<Direction>> toAddValues = List.of(
            element(uuid, Direction.builder()
                .directionType("direction type")
                .directionText("the expected text")
                .build()));

        service.persistHiddenDirectionValues(withHiddenValues, toAddValues);

        assertThat(toAddValues.get(0).getValue()).isEqualTo(Direction.builder()
            .directionType("direction type")
            .directionText("the expected text")
            .readOnly("No")
            .directionRemovable("No")
            .build());
    }

}
