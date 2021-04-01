package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisTemplateDataGeneration;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstApplicantName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsTemplateDataGenerationService
    extends DocmosisTemplateDataGeneration<DocmosisNoticeOfProceeding> {

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final CaseDataExtractionService caseDataExtractionService;
    private final Time time;

    @Override
    public DocmosisNoticeOfProceeding getTemplateData(CaseData caseData) {
        HearingBooking hearing = caseData.getFirstHearing().orElseThrow(NoHearingBookingException::new);

        return DocmosisNoticeOfProceeding.builder()
            .courtName(getCourtName(caseData.getCaseLocalAuthority()))
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .todaysDate(formatLocalDateToString(time.now().toLocalDate(), FormatStyle.LONG))
            .applicantName(getFirstApplicantName(caseData.getApplicants()))
            .orderTypes(getOrderTypes(caseData.getOrders()))
            .childrenNames(getAllChildrenNames(caseData.getAllChildren()))
            .hearingBooking(getHearingBooking(hearing))
            .crest(getCrestData())
            .courtseal(getCourtSealData())
            .build();
    }

    private DocmosisHearingBooking getHearingBooking(HearingBooking hearing) {
        return caseDataExtractionService.getHearingBookingData(hearing).toBuilder()
            .hearingLegalAdvisorName(null) // wipe unwanted data
            .hearingJudgeTitleAndName(null)
            .build();
    }

    private String getCourtName(String courtName) {
        return hmctsCourtLookupConfiguration.getCourt(courtName).getName();
    }

    private String getOrderTypes(Orders orders) {
        return orders.getOrderType().stream()
            .map(OrderType::getLabel)
            .collect(Collectors.joining(", "));
    }

    private String getAllChildrenNames(List<Element<Child>> children) {
        String childrenNames = children.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Child::getParty)
            .filter(Objects::nonNull)
            .map(Party::getFullName)
            .collect(Collectors.joining(", "));

        if (childrenNames.contains(",")) {
            StringBuilder stringBuilder = new StringBuilder(childrenNames);
            stringBuilder.replace(childrenNames.lastIndexOf(','),
                childrenNames.lastIndexOf(',') + 1, " and");

            childrenNames = stringBuilder.toString();
        }

        return childrenNames;
    }
}
