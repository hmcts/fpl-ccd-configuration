package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.C21Order;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Service
public class CreateC21OrderService {
    private final DateFormatterService dateFormatterService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    private final Time time;

    public CreateC21OrderService(DateFormatterService dateFormatterService,
                                 HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                 Time time) {
        this.dateFormatterService = dateFormatterService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.time = time;
    }

    public C21Order addDocumentToC21(C21Order c21Order, Document document) {
        return c21Order.toBuilder()
            .document(
                DocumentReference.builder()                                       //test c21 contains a document (3
                    // tests 3 assertions)
                    .url(document.links.self.href)
                    .binaryUrl(document.links.binary.href)
                    .filename(document.originalDocumentName)
                    .build())
            .build();
    }

    public C21Order buildC21Order(C21Order c21Order, JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return c21Order.toBuilder()
            .orderTitle(defaultIfBlank(c21Order.getOrderTitle(), "Order"))    //test title vs no title
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor) //test judge stuff
            .orderDate(dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(),
                "h:mma, d MMMM yyyy")) //test date stuff (instance of class if struggling)
            .build();
    }

    public Map<String, Object> getC21OrderTemplateData(CaseData caseData) {     //build case data with these things,
        // test with all data, test order title being blank
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", caseData.getFamilyManCaseNumber())
            .put("courtName", getCourtName(caseData.getCaseLocalAuthority()))
            .put("orderTitle", defaultIfNull(caseData.getC21Order().getOrderTitle(), "Order"))
            .put("orderDetails", caseData.getC21Order().getOrderDetails())
            .put("todaysDate", dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(), "d MMMM yyyy"))
            .put("judgeTitleAndName", JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(
                caseData.getJudgeAndLegalAdvisor()))
            .put("legalAdvisorName", JudgeAndLegalAdvisorHelper.getLegalAdvisorName(
                caseData.getJudgeAndLegalAdvisor()))
            .put("children", getChildrenDetails(caseData))
            .build();
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
                "gender", child.getGender(),
                "dateOfBirth", child.getDateOfBirth() != null ? dateFormatterService
                    .formatLocalDateToString(child.getDateOfBirth(), FormatStyle.LONG) : ""))
            .collect(toList());
    }
}
