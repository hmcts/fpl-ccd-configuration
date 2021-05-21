package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CafcassEnglandRegions {
    BIRMINGHAM("Birmingham"),
    BLACKPOOL("Blackpool"),
    BOURNEMOUTH("Bournemouth"),
    BRIGHTON("Brighton"),
    BRISTOL("Bristol"),
    CHATHAM("Chatham"),
    CHELMSFORD("Chelmsford"),
    COVENTRY("Coventry"),
    CROYDON("Croydon"),
    DERBY("Derby"),
    EXETER("Exeter"),
    HULL("Hull"),
    LEEDS("Leeds"),
    LEICESTER("Leicester"),
    LINCOLN("Lincoln"),
    LIVERPOOL("Liverpool"),
    LONDON("London"),
    MANCHESTER("Manchester"),
    MIDDLESBROUGH("Middlesbrough"),
    NEWCASTLE("Newcastle"),
    NORWICH("Norwich"),
    NOTTINGHAM("Nottingham"),
    OXFORD("Oxford"),
    PETERBOROUGH("Peterborough"),
    PLYMOUTH("Plymouth"),
    PORTSMOUTH("Portsmouth"),
    READING("Reading"),
    SHEFFIELD("Sheffield"),
    STAFFORD("Stafford"),
    STEVENAGE("Stevenage"),
    SWINDON("Swindon"),
    WORCESTER("Worcester"),
    YORK("York");

    private final String label;
}
