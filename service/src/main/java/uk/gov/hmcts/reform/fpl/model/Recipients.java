package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class Recipients {
    private final String name;
    private final String addressCheck;
    private final Address address;
    private final String documents;
    private final LocalDate date;
    private final String timeSent;
    private final String sentBy;
    private final String email;
    private final Address postOfficeAddress;
    private final Address givenInPerson;
}
