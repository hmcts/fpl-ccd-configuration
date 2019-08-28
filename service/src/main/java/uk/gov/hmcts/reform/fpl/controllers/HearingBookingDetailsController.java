package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBookingDetail;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Api
@RestController
@RequestMapping("/callback/add-hearing-booking")
public class HearingBookingDetailsController {
    private final MapperService mapperService;

    @Autowired
    public HearingBookingDetailsController(MapperService mapperService) {
        this.mapperService = mapperService;
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validate(caseDetails))
            .build();
    }

    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();

        CaseData caseData = mapperService.mapObject(caseDetails.getData(), CaseData.class);

        caseData.getHearingDetails().stream()
            .map(Element::getValue)
            .map(HearingBookingDetail::getHearingDate)
            .filter(Objects::nonNull)
            .filter(hearingDate -> hearingDate.isBefore(LocalDate.now()) || hearingDate.equals(LocalDate.now()))
            .findAny()
            .ifPresent(error -> errors.add("Enter a future date"));

        return errors.build();
    }
}
