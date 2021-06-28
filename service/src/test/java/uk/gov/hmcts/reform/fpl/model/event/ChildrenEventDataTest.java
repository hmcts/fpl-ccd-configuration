package uk.gov.hmcts.reform.fpl.model.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChildrenEventDataTest {

    @Test
    void getTransientDataNoRepresentation() {
        ChildrenEventData eventData = ChildrenEventData.builder()
            .childrenHaveRepresentation("No")
            .build();

        assertThat(eventData.getTransientFields()).containsExactly(
            "childrenMainRepresentative", "childrenHaveSameRepresentation", "childRepresentationDetails0",
            "childRepresentationDetails1", "childRepresentationDetails2", "childRepresentationDetails3",
            "childRepresentationDetails4", "childRepresentationDetails5", "childRepresentationDetails6",
            "childRepresentationDetails7", "childRepresentationDetails8", "childRepresentationDetails9",
            "childRepresentationDetails10", "childRepresentationDetails11", "childRepresentationDetails12",
            "childRepresentationDetails13", "childRepresentationDetails14", "optionCount"
        );
    }

    @Test
    void getTransientDataWithAllUsingMainRepresentation() {
        ChildrenEventData eventData = ChildrenEventData.builder()
            .childrenHaveRepresentation("Yes")
            .childrenHaveSameRepresentation("Yes")
            .build();

        assertThat(eventData.getTransientFields()).containsExactly(
            "childRepresentationDetails0", "childRepresentationDetails1", "childRepresentationDetails2",
            "childRepresentationDetails3", "childRepresentationDetails4", "childRepresentationDetails5",
            "childRepresentationDetails6", "childRepresentationDetails7", "childRepresentationDetails8",
            "childRepresentationDetails9", "childRepresentationDetails10", "childRepresentationDetails11",
            "childRepresentationDetails12", "childRepresentationDetails13", "childRepresentationDetails14",
            "optionCount"
        );
    }

    @Test
    void getTransientDataWithRepresentation() {
        ChildrenEventData eventData = ChildrenEventData.builder()
            .childrenHaveRepresentation("Yes")
            .childrenHaveSameRepresentation("No")
            .build();

        assertThat(eventData.getTransientFields()).containsExactly("optionCount");
    }
}
