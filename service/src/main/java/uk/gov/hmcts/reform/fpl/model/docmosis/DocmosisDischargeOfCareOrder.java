package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@Getter
@SuperBuilder()
@EqualsAndHashCode(callSuper = true)
public class DocmosisDischargeOfCareOrder extends DocmosisGeneratedOrder {
    private final List<DocmosisOrder> careOrders;
}
