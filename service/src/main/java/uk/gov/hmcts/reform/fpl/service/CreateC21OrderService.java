package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.C21Order;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.C21OrderBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Service
public class CreateC21OrderService {

    private final DateFormatterService dateFormatterService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    public CreateC21OrderService(DateFormatterService dateFormatterService,
                                 HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration) {
        this.dateFormatterService = dateFormatterService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
    }

    public Map<String, Object> getC21OrderTemplateData(CaseData caseData) {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", caseData.getFamilyManCaseNumber())
            .put("courtName", getCourtName(caseData.getCaseLocalAuthority()))
            .put("orderTitle", getOrderTitle(caseData))
            .put("orderDetails", caseData.getTemporaryC21Order().getOrderDetails())
            .put("todaysDate", dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG))
            .put("judgeTitleAndName", JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(
                caseData.getJudgeAndLegalAdvisor()))
            .put("legalAdvisorName", JudgeAndLegalAdvisorHelper.getLegalAdvisorName(
                caseData.getJudgeAndLegalAdvisor()))
            .put("children", getChildrenDetails(caseData))
            .build();
    }

    public List<Element<C21OrderBundle>> addToC21OrderBundle(C21Order tempC21,
                                                             JudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                                             List<Element<C21OrderBundle>> c21OrderBundle) {
        c21OrderBundle = defaultIfNull(c21OrderBundle, Lists.newArrayList());

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        c21OrderBundle.add(Element.<C21OrderBundle>builder()
            .id(UUID.randomUUID())
            .value(C21OrderBundle.builder()
                .c21OrderDocument(tempC21.getC21OrderDocument())
                .orderTitle(tempC21.getOrderTitle())
                .orderDate(dateFormatterService.formatLocalDateTimeBaseUsingFormat(zonedDateTime
                    .toLocalDateTime(), "h:mma, d MMMM yyyy"))
                .judgeTitleAndName(JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor))
                .build())
            .build());

        return c21OrderBundle;
    }

    private String getOrderTitle(CaseData caseData) {
        if (caseData.getTemporaryC21Order() == null || caseData.getTemporaryC21Order().getOrderTitle() == null) {
            return "Order";
        }

        return caseData.getTemporaryC21Order().getOrderTitle();
    }

    private String getCourtName(String courtName) {
        return hmctsCourtLookupConfiguration.getCourt(courtName).getName();
    }

    private List<Map<String, String>> getChildrenDetails(CaseData caseData) {
        return caseData.getAllChildren().stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(child -> ImmutableMap.of(
                "name", child.getFirstName() + " " + child.getLastName(),
                "gender", defaultIfNull(child.getGender(), ""),
                "dateOfBirth", defaultIfNull(dateFormatterService.formatLocalDateToString(
                    child.getDateOfBirth(), FormatStyle.LONG), "")))
            .collect(toList());
    }
}
