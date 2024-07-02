package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class GroundsForContactWithChild {
    @NotBlank(message = "Please state whether you are a parent or a guardian")
    private String parentOrGuardian;

    @NotBlank(message = "Please state whether you hold a residence order which was in force "
                        + "immediately before the care order was made (Section 34(1)(c) Children Act 1989)")
    private String residenceOrder;

    @NotBlank(message = "Please state whether you had care of the child(ren) through an order which was in force "
                        + "immediately before the care order was made (Section 34(1)(d) Children Act 1989)")
    private String hadCareOfChildrenBeforeCareOrder;

    @NotBlank(message = "Please provide reasons for application")
    private String reasonsForApplication;
}
