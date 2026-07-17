package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Court {
    @CCD(label = "Name")
    private final String name;
    @CCD(label = "Email")
    private final String email;
    @CCD(label = "Code")
    private final String code;
    @CCD(label = "Region")
    private final String region;
    @CCD(label = "ePIMMS ID")
    private final String epimmsId;
    @CCD(label = "Region ID")
    private final String regionId;
    @CCD(label = "Date Transferred")
    private LocalDateTime dateTransferred;

    public void setDateTransferred(LocalDateTime dateTransferred) {
        this.dateTransferred = dateTransferred;
    }
}
