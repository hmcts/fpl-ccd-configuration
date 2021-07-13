package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderOperation {
    CREATE,
    UPLOAD,
    AMEND
}
