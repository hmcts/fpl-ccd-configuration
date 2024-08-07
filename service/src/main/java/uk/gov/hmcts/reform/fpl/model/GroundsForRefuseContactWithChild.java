package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class GroundsForRefuseContactWithChild {
    @NotBlank(message = "Please state the full name(s) of each person who has contact with each child "
                        + "and the current arrangements for contact")
    private String personHasContactAndCurrentArrangement;

    @NotBlank(message = "Please state whether the local authority has refused contact for 7 days or less")
    private String laHasRefusedContact;

    @NotBlank(message = "Please state the full name and relationship of any person in respect of whom authority to "
                        + "refuse contact with each child is sought")
    private String personsBeingRefusedContactWithChild;

    @NotBlank(message = "Please provide reasons for application")
    private String reasonsOfApplication;
}
