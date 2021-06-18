package uk.gov.hmcts.reform.fpl.service.children;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.service.IdentityService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;


class ChildRepresentationDetailsSerializerTest {

    private static final UUID UUID_1 = UUID.randomUUID();

    private final IdentityService identityService = mock(IdentityService.class);

    private final ChildRepresentationDetailsSerializer underTest =
        new ChildRepresentationDetailsSerializer(identityService);

    @Test
    void testNullChildren() {

        Map<String, Object> actual = underTest.serialise(null);

        assertThat(actual).isEqualTo(emptyRepresentation());
    }

    @Test
    void testEmptyChildren() {
        Map<String, Object> actual = underTest.serialise(List.of());

        assertThat(actual).isEqualTo(emptyRepresentation());
    }

    @Test
    void testSingleChild() {

        when(identityService.generateId()).thenReturn(UUID_1);

        Map<String, Object> actual = underTest.serialise(
            wrapElements(
                Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Michael")
                        .lastName("Jackson")
                        .build())
                    .build()
            )
        );

        Map<String, Object> expected = new HashMap<>();
        expected.put("childRepresentationDetails0",
            element(UUID_1, ChildRepresentationDetails.builder()
                .childDescription("Child 1 - Michael Jackson")
                .build()));
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
