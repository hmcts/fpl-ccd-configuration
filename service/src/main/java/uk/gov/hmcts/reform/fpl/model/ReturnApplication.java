package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ReturnApplication {
    private final List<ReturnedApplicationReasons> reason;
    private final String note;
}
