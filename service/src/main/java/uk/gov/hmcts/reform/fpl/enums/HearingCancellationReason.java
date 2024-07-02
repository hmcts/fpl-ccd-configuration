package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

@Getter
@RequiredArgsConstructor
@Slf4j
public enum HearingCancellationReason {
    CA1("CAFCASS not allocated/present"),
    CA2("No/poor CAFCASS analysis"),
    EX1("Late expert report/assessment/ Poor expert report/assessment"),
    EX2("New expert report/assessment required following a change in circumstances"),
    HE1("No/poor medical records etc from other agency"),
    CR1("Police disclosure/documents incomplete/not available"),
    HM1("No courtroom available"),
    HM2("No special measures"),
    HM3("Interpreter not available"),
    JU1("Lack of judicial continuity"),
    JU2("Insufficient time listed or to complete hearing"),
    LW1("Lawyers not instructed, present or ready, party or witness fail to attend"),
    LW2("No key issue analysis"),
    LW3("No/poor parental evidence"),
    OS1("Official Solicitor not instructed/ready"),
    LS1("Prior authority from LSC not available"),
    LS2("Other legal aid"),
    LA1("No/poor pre-proceedings preparation by LA, other than (core) social work assessment of the family"),
    LA2("No friends/family identified before the hearing by LA"),
    LA3("No/poor kinship assessments by LA"),
    LA4("No expert instructed by LA"),
    LA5("No/poor/late (core) social work assessment of the family by LA"),
    LA6("New social work report/assessment required following a change in circumstances"),
    LA7("No timetable for the child"),
    LA8("New/alternative care   plan"),
    LA9("Placement order proceedings delay"),
    LA10("No/poor placement evidence by LA"),
    LA11("No threshold document"),
    OT1("Case transferred"),
    OT2("Need for an interim contested hearing"),
    OT3("Other non compliance with directions"),
    OT4("Consolidation with other family proceedings"),
    OT5("Parallel proceedings"),
    OT6("New baby/pregnancy"),
    OT7("New Party joined"),
    IN1("Immigration and international difficulties"),
    OT8("Severe weather"),
    OT9("Industrial action"),
    OT10("Covid 19");

    private final String label;

    public static String getHearingCancellationReasonLabel(HearingBooking hearinhBooking) {
        try {
            return HearingCancellationReason.valueOf(hearinhBooking.getCancellationReason()).getLabel();
        } catch (IllegalArgumentException e) {
            log.warn("CancellationReason invalid : {}", hearinhBooking.getCancellationReason());
            return hearinhBooking.getCancellationReason();
        }
    }
}
