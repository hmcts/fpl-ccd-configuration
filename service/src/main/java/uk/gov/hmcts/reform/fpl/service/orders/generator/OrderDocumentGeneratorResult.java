package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;

@Value
@Builder
public class OrderDocumentGeneratorResult {

    byte[] bytes;
    RenderFormat renderFormat;

}
