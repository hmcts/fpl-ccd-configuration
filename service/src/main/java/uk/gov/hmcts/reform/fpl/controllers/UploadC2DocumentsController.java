package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Api
@RestController
@RequestMapping("/callback/upload-c2")
public class UploadC2DocumentsController {
    private final ObjectMapper mapper;

    @Autowired
    private UploadC2DocumentsController(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        Map<String, Object> data = callbackrequest.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put("c2DocumentBundle", buildC2DocumentBundle(caseData));
        data.remove("temporaryC2Document");

        return AboutToStartOrSubmitCallbackResponse.builder().data(data).build();
    }

    private List<Element<C2DocumentBundle>> buildC2DocumentBundle(CaseData caseData) {
        List<Element<C2DocumentBundle>> c2DocumentBundle = defaultIfNull(caseData.getC2DocumentBundle(),
            Lists.newArrayList());

        c2DocumentBundle.add(Element.<C2DocumentBundle>builder()
            .id(UUID.randomUUID())
            .value(caseData.getTemporaryC2Document())
            .build());

        return c2DocumentBundle;
    }
}
