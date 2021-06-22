package uk.gov.hmcts.reform.fpl.service.children;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;


class ChildRepresentationDetailsFlattenerTest {
    private final RespondentSolicitor mainRepresentative = mock(RespondentSolicitor.class);

    private final ChildRepresentationDetailsFlattener underTest = new ChildRepresentationDetailsFlattener();

    @Test
    void testNullChildren() {
        Map<String, Object> actual = underTest.serialise(null, null);

        assertThat(actual).isEqualTo(emptyRepresentation());
    }

    @Test
    void testEmptyChildren() {
        Map<String, Object> actual = underTest.serialise(List.of(), null);

        assertThat(actual).isEqualTo(emptyRepresentation());
    }

    @Test
    void testSingleChild() {
        RespondentSolicitor childSolicitor = mock(RespondentSolicitor.class);

        Map<String, Object> actual = underTest.serialise(
            wrapElements(
                Child.builder()
                    .party(ChildParty.builder().firstName("Michael").lastName("Jackson").build())
                    .representative(childSolicitor)
                    .build()
            ),
            mainRepresentative
        );

        ChildRepresentationDetails expectedChildRepresentationDetails = ChildRepresentationDetails.builder()
            .childDescription("Child 1 - Michael Jackson")
            .useMainSolicitor("No")
            .solicitor(childSolicitor)
            .build();

        Map<String, Object> expected = emptyRepresentation();
        expected.put("childRepresentationDetails0", expectedChildRepresentationDetails);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testMultiChild() {
        RespondentSolicitor childSolicitor = mock(RespondentSolicitor.class);

        Map<String, Object> actual = underTest.serialise(
            wrapElements(
                Child.builder()
                    .party(ChildParty.builder().firstName("Michael").lastName("Jackson").build())
                    .representative(childSolicitor)
                    .build(),
                Child.builder()
                    .party(ChildParty.builder().firstName("Freddie").lastName("Mercury").build())
                    .representative(childSolicitor)
                    .build()
            ),
            mainRepresentative
        );

        ChildRepresentationDetails expectedChildRepresentationDetails0 = ChildRepresentationDetails.builder()
            .childDescription("Child 1 - Michael Jackson")
            .useMainSolicitor("No")
            .solicitor(childSolicitor)
            .build();

        ChildRepresentationDetails expectedChildRepresentationDetails1 = ChildRepresentationDetails.builder()
            .childDescription("Child 2 - Freddie Mercury")
            .useMainSolicitor("No")
            .solicitor(childSolicitor)
            .build();

        Map<String, Object> expected = emptyRepresentation();
        expected.put("childRepresentationDetails0", expectedChildRepresentationDetails0);
        expected.put("childRepresentationDetails1", expectedChildRepresentationDetails1);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testMainRepresentative() {
        Map<String, Object> actual = underTest.serialise(
            wrapElements(
                Child.builder()
                    .party(ChildParty.builder().firstName("Michael").lastName("Jackson").build())
                    .representative(mainRepresentative)
                    .build()
            ),
            mainRepresentative
        );

        ChildRepresentationDetails expectedChildRepresentationDetails = ChildRepresentationDetails.builder()
            .childDescription("Child 1 - Michael Jackson")
            .useMainSolicitor("Yes")
            .solicitor(null)
            .build();

        Map<String, Object> expected = emptyRepresentation();
        expected.put("childRepresentationDetails0", expectedChildRepresentationDetails);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testMainRepresentativeWithNoChildRepresentative() {
        Map<String, Object> actual = underTest.serialise(
            wrapElements(
                Child.builder()
                    .party(ChildParty.builder().firstName("Michael").lastName("Jackson").build())
                    .representative(null)
                    .build()
            ),
            mainRepresentative
        );

        ChildRepresentationDetails expectedChildRepresentationDetails = ChildRepresentationDetails.builder()
            .childDescription("Child 1 - Michael Jackson")
            .useMainSolicitor(null)
            .solicitor(null)
            .build();

        Map<String, Object> expected = emptyRepresentation();
        expected.put("childRepresentationDetails0", expectedChildRepresentationDetails);

        assertThat(actual).isEqualTo(expected);
    }

    private Map<String, Object> emptyRepresentation() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("childRepresentationDetails0", null);
        expected.put("childRepresentationDetails1", null);
        expected.put("childRepresentationDetails2", null);
        expected.put("childRepresentationDetails3", null);
        expected.put("childRepresentationDetails4", null);
        expected.put("childRepresentationDetails5", null);
        expected.put("childRepresentationDetails6", null);
        expected.put("childRepresentationDetails7", null);
        expected.put("childRepresentationDetails8", null);
        expected.put("childRepresentationDetails9", null);
        expected.put("childRepresentationDetails10", null);
        expected.put("childRepresentationDetails11", null);
        expected.put("childRepresentationDetails12", null);
        expected.put("childRepresentationDetails13", null);
        expected.put("childRepresentationDetails14", null);
        return expected;
    }
}
