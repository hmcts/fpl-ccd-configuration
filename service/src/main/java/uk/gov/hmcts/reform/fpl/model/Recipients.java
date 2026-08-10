package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@AllArgsConstructor
public class Recipients {
    @CCD(label = "Name of recipient")
    private final String name;
    @CCD(label = "Do you have the recipient's address?", typeOverride = FieldType.YesOrNo)
    private final String addressCheck;
    @CCD(
            label = "Recipient's address",
            hint = "Enter a UK postcode",
            showCondition = "addressCheck=\"Yes\"",
            typeOverride = FieldType.AddressUK
    )
    private final Address address;
    @CCD(label = "Documents", hint = "For example, standard directions order or c6", typeOverride = FieldType.TextArea)
    private final String documents;
    @CCD(label = "Date sent", hint = "For example, 21 9 2019")
    private final LocalDate date;
    @CCD(label = "Time sent", hint = "For example, 2:30pm")
    private final String timeSent;
    @CCD(
            label = "How were they sent?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "SentBy",
            typeParameterClass = SentBy.class
    )
    private final String sentBy;
    @CCD(label = "Recipient's email address", showCondition = "sentBy=\"EMAIL\"", typeOverride = FieldType.Email)
    private final String email;
    @CCD(
            label = "Post office address",
            hint = "Enter a UK postcode",
            showCondition = "sentBy=\"POST\"",
            typeOverride = FieldType.AddressUK
    )
    private final Address postOfficeAddress;
    @CCD(
            label = "Where did you serve the documents to the recipient?",
            hint = "Enter a UK postcode",
            showCondition = "sentBy=\"GIVEN_IN_PERSON\"",
            typeOverride = FieldType.AddressUK
    )
    private final Address givenInPerson;
}
