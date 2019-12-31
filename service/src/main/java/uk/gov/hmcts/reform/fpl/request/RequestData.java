package uk.gov.hmcts.reform.fpl.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RequestData {
    private final String authorization;
}
