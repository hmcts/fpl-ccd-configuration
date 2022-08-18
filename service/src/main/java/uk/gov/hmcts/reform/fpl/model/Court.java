package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Court {
    private final String name;
    private final String email;
    private final String code;
    private final String region;
    private final String epimmsId;
    private final String regionId;
    private LocalDateTime dateTransferred;

    public void setDateTransferred(LocalDateTime dateTransferred) {
        this.dateTransferred = dateTransferred;
    }
}
