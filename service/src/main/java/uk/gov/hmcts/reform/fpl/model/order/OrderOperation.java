package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ManageOrdersOperation", generate = true)
@Getter
@RequiredArgsConstructor
public enum OrderOperation {
    @CCD(label = "Create an order")
    CREATE,
    @CCD(label = "Upload an order")
    UPLOAD,
    @CCD(label = "Amend order under the slip rule")
    AMEND
}
