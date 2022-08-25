package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum CourtRegion {
    LONDON(0, "London"),
    MIDLANDS(10, "Midlands"),
    NORTH_EAST(20, "North East"),
    NORTH_WEST(30, "North West"),
    SOUTH_EAST(40, "South East"),
    SOUTH_WEST(50, "South West"),
    WALES(60, "Wales");

    private final int seq;
    private final String name;

    CourtRegion(int seq, String name) {
        this.seq = seq;
        this.name = name;
    }
}
