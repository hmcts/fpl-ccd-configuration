package uk.gov.hmcts.reform.fpl.service.noc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;

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

    private final ChildNoticeOfChangeUpdateAction underTest = new ChildNoticeOfChangeUpdateAction();

    private Child childToUpdate;

    @BeforeEach
    void setUp() {
        childToUpdate = Child.builder().build();
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
        when(CASE_DATA.getAllChildren()).thenReturn(List.of(
            element(CHILD_ID, childToUpdate), element(ANOTHER_CHILD_ID, ANOTHER_CHILD)
        ));
        when(ANOTHER_CHILD.getSolicitor()).thenReturn(CAFCASS);

        Map<String, Object> data = underTest.applyUpdates(childToUpdate, CASE_DATA, SOLICITOR);

        assertThat(data).isEqualTo(Map.of(
            "children1", List.of(element(CHILD_ID, childToUpdate), element(ANOTHER_CHILD_ID, ANOTHER_CHILD)),
            "childrenHaveSameRepresentation", "No"
        ));
        assertThat(childToUpdate.getSolicitor()).isEqualTo(SOLICITOR);
    }

    @Test
    void applyUpdatesAllUseCafcassSolicitor() {
        when(CASE_DATA.getAllChildren()).thenReturn(List.of(
            element(CHILD_ID, childToUpdate), element(ANOTHER_CHILD_ID, ANOTHER_CHILD)
        ));
        when(ANOTHER_CHILD.getSolicitor()).thenReturn(CAFCASS);

        Map<String, Object> data = underTest.applyUpdates(childToUpdate, CASE_DATA, CAFCASS);

        assertThat(data).isEqualTo(Map.of(
            "children1", List.of(element(CHILD_ID, childToUpdate), element(ANOTHER_CHILD_ID, ANOTHER_CHILD)),
            "childrenHaveSameRepresentation", "Yes"
        ));
        assertThat(childToUpdate.getSolicitor()).isEqualTo(CAFCASS);
    }

    @Test
    void applyUpdatesAllUseSameSolicitor() {
        when(CASE_DATA.getAllChildren()).thenReturn(List.of(
            element(CHILD_ID, childToUpdate), element(ANOTHER_CHILD_ID, ANOTHER_CHILD)
        ));
        when(ANOTHER_CHILD.getSolicitor()).thenReturn(SOLICITOR);

        Map<String, Object> data = underTest.applyUpdates(childToUpdate, CASE_DATA, SOLICITOR);

        assertThat(data).isEqualTo(Map.of(
            "children1", List.of(element(CHILD_ID, childToUpdate), element(ANOTHER_CHILD_ID, ANOTHER_CHILD)),
            "childrenHaveSameRepresentation", "Yes",
            "childrenMainRepresentative", SOLICITOR
        ));
        assertThat(childToUpdate.getSolicitor()).isEqualTo(SOLICITOR);
    }
}
