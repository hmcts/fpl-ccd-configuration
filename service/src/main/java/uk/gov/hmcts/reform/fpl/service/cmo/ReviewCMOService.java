package uk.gov.hmcts.reform.fpl.service.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewCMOService {

    public DynamicList buildDynamicList(List<Element<CaseManagementOrder>> cmos) {
        return buildDynamicList(cmos, null);
    }

    public DynamicList buildDynamicList(List<Element<CaseManagementOrder>> cmos, UUID selected) {
        return asDynamicList(cmos, selected, uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder::getHearing);
    }
}
