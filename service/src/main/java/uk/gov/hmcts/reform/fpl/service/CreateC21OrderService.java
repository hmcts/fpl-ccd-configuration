package uk.gov.hmcts.reform.fpl.service;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

@Service
public class CreateC21OrderService {

    private final DateFormatterService dateFormatterService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    public CreateC21OrderService(DateFormatterService dateFormatterService,
                                 HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration) { ;
        this.dateFormatterService = dateFormatterService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
    }

    public Map<String, Object> getC21OrderTemplateData(CaseData caseData) {

        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", caseData.getFamilyManCaseNumber())
            .put("orderTitle", getOrderTitle(caseData))
            .put("todaysDate", dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG))
            .put("judgeTitleAndName", formatJudgeTitleAndName(caseData.getJudgeAndLegalAdvisor()))
            .put("legalAdvisorName", getLegalAdvisorName(caseData.getJudgeAndLegalAdvisor()))
            .put("courtName", getCourtName(caseData.getCaseLocalAuthority()))
            .put("children", getChildrenDetails(caseData))
            .put("orderDetails", caseData.getTemporaryC21Order().getOrderDetails())
            .build();
    }

    private String getOrderTitle(CaseData caseData) {
        if (caseData.getTemporaryC21Order().getOrderTitle() == null) {
            return "Order";
        }
        return caseData.getTemporaryC21Order().getOrderTitle();
    }

    private String getLegalAdvisorName(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (judgeAndLegalAdvisor == null) {
            return "";
        }

        return defaultIfNull(judgeAndLegalAdvisor.getLegalAdvisorName(), "");
    }

    private String formatJudgeTitleAndName(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (judgeAndLegalAdvisor == null || judgeAndLegalAdvisor.getJudgeTitle() == null) {
            return "";
        }

        if (judgeAndLegalAdvisor.getJudgeTitle() == MAGISTRATES) {
            return judgeAndLegalAdvisor.getJudgeFullName() + " (JP)";
        } else {
            return judgeAndLegalAdvisor.getJudgeTitle().getLabel() + " " + judgeAndLegalAdvisor.getJudgeLastName();
        }
    }

    private String getCourtName(String courtName) {
        return hmctsCourtLookupConfiguration.getCourt(courtName).getName();
    }
    private List<Map<String, String>> getChildrenDetails(CaseData caseData) {
        // children is validated as not null
        return caseData.getAllChildren().stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(child -> ImmutableMap.of(
                "name", child.getFirstName() + " " + child.getLastName(),
                "gender", child.getGender(),
                "dateOfBirth", dateFormatterService.formatLocalDateToString(child.getDateOfBirth(), FormatStyle.LONG)))
            .collect(toList());
    }
}
