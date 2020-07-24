package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.CMOCodeNotFound;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder.sealFrom;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewCMOService {

    private final ObjectMapper mapper;
    private final Time time;

    public DynamicList buildDynamicList(List<Element<CaseManagementOrder>> cmos) {
        return buildDynamicList(cmos, null);
    }

    public DynamicList buildDynamicList(List<Element<CaseManagementOrder>> cmos, UUID selected) {
        return asDynamicList(cmos, selected, uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder::getHearing);
    }

    public Element<CaseManagementOrder> getCMOToSeal(CaseData caseData, Element<CaseManagementOrder> cmo) {
        Element<HearingBooking> cmoHearing = getCmoHearingFromId(caseData.getHearingDetails(), cmo.getId());
        return element(sealFrom(cmo.getValue().getOrder(), cmoHearing.getValue(), time.now().toLocalDate()));
    }

    private Element<HearingBooking> getCmoHearingFromId(List<Element<HearingBooking>> hearings, UUID cmoId) {
        return hearings
            .stream()
            .filter(hearing -> cmoId.equals(hearing.getValue().getCaseManagementOrderId()))
            .findFirst()
            .orElseThrow(NoHearingBookingException::new);
    }

    public List<Element<CaseManagementOrder>> getCMOsReadyForApproval(List<Element<CaseManagementOrder>> draftCMOs) {
        return draftCMOs.stream().filter(cmo -> cmo.getValue().getStatus().equals(SEND_TO_JUDGE)).collect(
            Collectors.toList());
    }

    public Element<CaseManagementOrder> getSelectedCMO(CaseData caseData) {
        if (("MULTI").equals(caseData.getNumDraftCMOs())) {
            UUID selectedCMOCode = getSelectedCMOId(caseData.getCmoToReviewList());

            return caseData.getDraftUploadedCMOs().stream()
                .filter(element -> element.getId().equals(selectedCMOCode))
                .findFirst()
                .orElseThrow(() -> new CMOCodeNotFound("Could not find draft cmo with id " + selectedCMOCode));
        } else {
            return caseData.getDraftUploadedCMOs().get(caseData.getDraftUploadedCMOs().size() - 1);
        }
    }

    public UUID getSelectedCMOId(Object dynamicList) {
        //see RDM-5696 and RDM-6651
        if (dynamicList instanceof String) {
            return UUID.fromString(dynamicList.toString());
        }
        return mapper.convertValue(dynamicList, DynamicList.class).getValueCode();
    }

    public CaseManagementOrder getLatestSealedCMO(List<Element<CaseManagementOrder>> cmos) {
        return cmos.get(cmos.size() - 1).getValue();
    }
}
