package uk.gov.hmcts.reform.rd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@Jacksonized
@AllArgsConstructor
public class JudicialUserAuthorisations {

    @JsonProperty("appointment_id")
    private String appointmentId;

    @JsonProperty("authorisation_id")
    private String authorisationId;

    @JsonProperty("end_date")
    private LocalDate endDate;

    private String jurisdiction;

    @JsonProperty("jurisdiction_id")
    private String jurisdictionId;

    @JsonProperty("service_codes")
    private List<String> serviceCodes;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("ticket_code")
    private String ticketCode;

    @JsonProperty("ticket_description")
    private String ticketDescription;
}
