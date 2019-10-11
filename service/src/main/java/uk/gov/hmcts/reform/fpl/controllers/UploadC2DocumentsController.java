package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Api
@RestController
@RequestMapping("/callback/upload-c2")
public class C2UploadController {
    private final ObjectMapper mapper;

    @Autowired
    private C2UploadController(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put("uploadC2", addTemporaryC2DocumentsToCollection(caseData));
        data.remove("temp2");

        return AboutToStartOrSubmitCallbackResponse.builder().data(data).build();
    }

    private List<Element<C2DocumentBundle>> addTemporaryC2DocumentsToCollection(CaseData caseData) {
        caseData.getUploadC2().add(Element.<C2DocumentBundle>builder()
            .id(UUID.randomUUID())
            .value(caseData.getTempC2())
            .build());

        return caseData.getUploadC2();
    }
}
