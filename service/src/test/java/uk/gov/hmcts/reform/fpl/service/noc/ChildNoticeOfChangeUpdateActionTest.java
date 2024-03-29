package uk.gov.hmcts.reform.fpl.service.noc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.children.ChildRepresentationDetailsFlattener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.CHILD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class ChildNoticeOfChangeUpdateActionTest {
    private static final UUID CHILD_ID = UUID.randomUUID();
    private static final UUID ANOTHER_CHILD_ID = UUID.randomUUID();

    private static final Child ANOTHER_CHILD = mock(Child.class);
    private static final RespondentSolicitor SOLICITOR = mock(RespondentSolicitor.class);
    private static final RespondentSolicitor CAFCASS = mock(RespondentSolicitor.class);
    private static final ChildrenEventData EVENT_DATA = mock(ChildrenEventData.class);
    private static final CaseData CASE_DATA = mock(CaseData.class);

    private final ChildRepresentationDetailsFlattener flattener = mock(ChildRepresentationDetailsFlattener.class);

    private final ChildNoticeOfChangeUpdateAction underTest = new ChildNoticeOfChangeUpdateAction(flattener);

    private Child childToUpdate;
    private List<Element<Child>> children;

    @BeforeEach
    void setUp() {
        childToUpdate = Child.builder().build();
        children = List.of(
            element(CHILD_ID, childToUpdate), element(ANOTHER_CHILD_ID, ANOTHER_CHILD)
        );

        when(CASE_DATA.getAllChildren()).thenReturn(children);
        when(CASE_DATA.getChildrenEventData()).thenReturn(EVENT_DATA);
        when(EVENT_DATA.getChildrenMainRepresentative()).thenReturn(CAFCASS);
    }

    @Test
    void acceptsValid() {
        assertThat(underTest.accepts(CHILD)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Representing.class, mode = EnumSource.Mode.EXCLUDE, names = "CHILD")
    void acceptsInvalid(Representing representing) {
        assertThat(underTest.accepts(representing)).isFalse();
    }

    @Test
    void applyUpdatesAllDoNotHaveSameSolicitor() {
        when(ANOTHER_CHILD.getSolicitor()).thenReturn(CAFCASS);

        when(flattener.serialise(children, CAFCASS)).thenReturn(Map.of("flattener", "data"));

        Map<String, Object> data = underTest.applyUpdates(childToUpdate, CASE_DATA, SOLICITOR);

        Child updatedChild = Child.builder().solicitor(SOLICITOR).build();

        assertThat(data).isEqualTo(Map.of(
            "children1", List.of(element(CHILD_ID, updatedChild), element(ANOTHER_CHILD_ID, ANOTHER_CHILD)),
            "childrenHaveSameRepresentation", "No",
            "flattener", "data"
        ));
    }

    @Test
    void applyUpdatesAllUseCafcassSolicitor() {
        when(ANOTHER_CHILD.getSolicitor()).thenReturn(CAFCASS);

        when(flattener.serialise(children, CAFCASS)).thenReturn(Map.of("flattener", "data"));

        Map<String, Object> data = underTest.applyUpdates(childToUpdate, CASE_DATA, CAFCASS);

        Child updatedChild = Child.builder().solicitor(CAFCASS).build();

        assertThat(data).isEqualTo(Map.of(
            "children1", List.of(element(CHILD_ID, updatedChild), element(ANOTHER_CHILD_ID, ANOTHER_CHILD)),
            "childrenHaveSameRepresentation", "Yes",
            "flattener", "data"
        ));
    }

    @Test
    void applyUpdatesAllUseSameSolicitor() {
        when(ANOTHER_CHILD.getSolicitor()).thenReturn(SOLICITOR);

        when(flattener.serialise(children, SOLICITOR)).thenReturn(Map.of("flattener", "data"));

        Map<String, Object> data = underTest.applyUpdates(childToUpdate, CASE_DATA, SOLICITOR);

        Child updatedChild = Child.builder().solicitor(SOLICITOR).build();

        assertThat(data).isEqualTo(Map.of(
            "children1", List.of(element(CHILD_ID, updatedChild), element(ANOTHER_CHILD_ID, ANOTHER_CHILD)),
            "childrenHaveSameRepresentation", "Yes",
            "childrenMainRepresentative", SOLICITOR,
            "flattener", "data"
        ));
    }
}
