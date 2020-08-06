package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/manage-docs")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDocumentsController {

    private final ObjectMapper mapper;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();

        CaseData caseData = mapper.convertValue(data, CaseData.class);

        List<Element<CourtAdminDocument>> otherCourtAdminDocuments = caseData.getOtherCourtAdminDocuments();
        List<Element<CourtAdminDocument>> limitedCourtAdminDocuments = caseData.getLimitedCourtAdminDocuments();

        otherCourtAdminDocuments.addAll(limitedCourtAdminDocuments);

        data.put("otherCourtAdminDocuments", otherCourtAdminDocuments);
        data.remove("limitedCourtAdminDocuments");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
