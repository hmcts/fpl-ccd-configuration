package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.*;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.*;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Api
@RestController
@RequestMapping("/callback/draft-cmo")
public class DraftCMOController {
    private final ObjectMapper mapper;
    private final DocmosisDocumentGeneratorService docmosisService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final DirectionHelperService directionHelperService;
    private final OrdersLookupService ordersLookupService;
    private final CoreCaseDataService coreCaseDataService;

    @Autowired
    public DraftCMOController(ObjectMapper mapper,
                              DocmosisDocumentGeneratorService docmosisService,
                              UploadDocumentService uploadDocumentService,
                              CaseDataExtractionService caseDataExtractionService,
                              DirectionHelperService directionHelperService,
                              OrdersLookupService ordersLookupService,
                              CoreCaseDataService coreCaseDataService) {
        this.mapper = mapper;
        this.docmosisService = docmosisService;
        this.uploadDocumentService = uploadDocumentService;
        this.caseDataExtractionService = caseDataExtractionService;
        this.directionHelperService = directionHelperService;
        this.ordersLookupService = ordersLookupService;
        this.coreCaseDataService = coreCaseDataService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<HearingBooking>>hearingDetails = caseData.getHearingDetails();

        List<String> hearingDates = hearingDetails
            .stream()
            .map(Element::getValue)
            .map(HearingBooking::getDate)
            .map(LocalDate::toString)
            .collect(Collectors.toList());

        List<MultiList.CodeLabel> hearingDatesCodes = new ArrayList<>();

        hearingDates.forEach(hearingDate
            -> hearingDatesCodes.add(
                MultiList.CodeLabel
                    .builder().code(hearingDate).label(hearingDate).build()));

        MultiList cmoHearingDateList = MultiList.builder()
            .list_items(hearingDatesCodes)
            .value(MultiList.CodeLabel.builder().build())
            .build();

        caseDetails.getData().put("cmoHearingDateList", cmoHearingDateList );

        System.out.println("Hearing dates are" + caseDetails.getData());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
