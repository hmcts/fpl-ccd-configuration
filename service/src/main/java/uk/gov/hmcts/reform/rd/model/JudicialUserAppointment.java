package uk.gov.hmcts.reform.rd.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class JudicialUserAppointment {
    @JsonIgnore
    public static final String APPOINTMENT_TYPE_FEE_PAID = "Fee-paid";

    private String appointment;

    @JsonProperty("appointment_id")
    private String appointmentId;

    @JsonProperty("appointment_type")
    private String appointmentType;

    @JsonProperty("base_location_id")
    private String baseLocationId;

    @JsonProperty("cft_region")
    private String cftRegion;

    @JsonProperty("cft_region_id")
    private String cftRegionId;

    @JsonProperty("contract_type_id")
    private String contractTypeId;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("epimms_id")
    private String epimmsId;

    @JsonProperty("is_principal_appointment")
    private String isPrincipalAppointment;

    @JsonProperty("role_name_id")
    private String roleNameId;

    @JsonProperty("service_codes")
    private List<String> serviceCodes;

    private String type;
}
