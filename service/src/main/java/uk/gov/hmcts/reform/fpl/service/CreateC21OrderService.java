package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.C21Order;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C21;

@Service
public class CreateC21OrderService {
    private final DateFormatterService dateFormatterService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final UploadDocumentService uploadDocumentService;
    private final DocmosisDocumentGeneratorService docmosisService;

    private final Time time;

    public CreateC21OrderService(DateFormatterService dateFormatterService,
                                 HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                 UploadDocumentService uploadDocumentService,
                                 DocmosisDocumentGeneratorService docmosisService,
                                 Time time) {
        this.dateFormatterService = dateFormatterService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.uploadDocumentService = uploadDocumentService;
        this.docmosisService = docmosisService;
        this.time = time;
    }

    public C21Order addDocumentToC21(CaseData caseData, Document document) {
        return caseData.getC21Order().toBuilder()
            .orderTitle(defaultIfBlank(caseData.getC21Order().getOrderTitle(), "Order"))
            .document(DocumentReference.builder()
                .url(document.links.self.href)
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .build())
            .build();
    }

    public C21Order addJudgeAndDateToC21(CaseData caseData) {
        return caseData.getC21Order().toBuilder()
            .judgeTitleAndName(JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(caseData.getJudgeAndLegalAdvisor()))
            .orderDate(dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(),
                "h:mma, d MMMM yyyy"))
            .build();
    }

    public Document getDocument(@RequestHeader("authorization") String authorization,
                                @RequestHeader("user-id") String userId,
                                CaseData caseData) {
        DocmosisDocument document = docmosisService.generateDocmosisDocument(getC21OrderTemplateData(caseData), C21);
        String index = (caseData.getC21Orders() != null) ? Integer.toString(caseData.getC21Orders().size() + 1) : "1";

        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(),
            C21.getDocumentTitle() + index + ".pdf");
    }

    private Map<String, Object> getC21OrderTemplateData(CaseData caseData) {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", caseData.getFamilyManCaseNumber())
            .put("courtName", getCourtName(caseData.getCaseLocalAuthority()))
            .put("orderTitle", getOrderTitle(caseData.getC21Order().getOrderTitle()))
            .put("orderDetails", caseData.getC21Order().getOrderDetails())
            .put("todaysDate", dateFormatterService.formatLocalDateToString(time.now().toLocalDate(), FormatStyle.LONG))
            .put("judgeTitleAndName", JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(
                caseData.getJudgeAndLegalAdvisor()))
            .put("legalAdvisorName", JudgeAndLegalAdvisorHelper.getLegalAdvisorName(
                caseData.getJudgeAndLegalAdvisor()))
            .put("children", getChildrenDetails(caseData))
            .build();
    }

    private String getOrderTitle(String orderTitle) {
        return Optional.ofNullable(orderTitle)
            .orElse("Order");
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
                "dateOfBirth", child.getDateOfBirth() != null ? dateFormatterService
                    .formatLocalDateToString(child.getDateOfBirth(), FormatStyle.LONG) : ""))
            .collect(toList());
    }
}
