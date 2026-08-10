package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "CafcassEnglandOffices", generate = true)
@AllArgsConstructor
@Getter
public enum EnglandOffices {
    @CCD(label = "Birmingham")
    BIRMINGHAM("Birmingham"),
    @CCD(label = "Blackpool")
    BLACKPOOL("Blackpool"),
    @CCD(label = "Bournemouth")
    BOURNEMOUTH("Bournemouth"),
    @CCD(label = "Brighton")
    BRIGHTON("Brighton"),
    @CCD(label = "Bristol")
    BRISTOL("Bristol"),
    @CCD(label = "Chatham")
    CHATHAM("Chatham"),
    @CCD(label = "Chelmsford")
    CHELMSFORD("Chelmsford"),
    @CCD(label = "Coventry")
    COVENTRY("Coventry"),
    @CCD(label = "Croydon")
    CROYDON("Croydon"),
    @CCD(label = "Derby")
    DERBY("Derby"),
    @CCD(label = "Exeter")
    EXETER("Exeter"),
    @CCD(label = "Hull")
    HULL("Hull"),
    @CCD(label = "Leeds")
    LEEDS("Leeds"),
    @CCD(label = "Leicester")
    LEICESTER("Leicester"),
    @CCD(label = "Lincoln")
    LINCOLN("Lincoln"),
    @CCD(label = "Liverpool")
    LIVERPOOL("Liverpool"),
    @CCD(label = "London")
    LONDON("London"),
    @CCD(label = "Manchester")
    MANCHESTER("Manchester"),
    @CCD(label = "Middlesbrough")
    MIDDLESBROUGH("Middlesbrough"),
    @CCD(label = "Newcastle")
    NEWCASTLE("Newcastle"),
    @CCD(label = "Norwich")
    NORWICH("Norwich"),
    @CCD(label = "Nottingham")
    NOTTINGHAM("Nottingham"),
    @CCD(label = "Oxford")
    OXFORD("Oxford"),
    @CCD(label = "Peterborough")
    PETERBOROUGH("Peterborough"),
    @CCD(label = "Plymouth")
    PLYMOUTH("Plymouth"),
    @CCD(label = "Portsmouth")
    PORTSMOUTH("Portsmouth"),
    @CCD(label = "Reading")
    READING("Reading"),
    @CCD(label = "Sheffield")
    SHEFFIELD("Sheffield"),
    @CCD(label = "Stafford")
    STAFFORD("Stafford"),
    @CCD(label = "Stevenage")
    STEVENAGE("Stevenage"),
    @CCD(label = "Swindon")
    SWINDON("Swindon"),
    @CCD(label = "Worcester")
    WORCESTER("Worcester"),
    @CCD(label = "York")
    YORK("York");

    private final String label;
}
