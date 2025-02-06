package uk.gov.hmcts.reform.fpl.controllers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseViewField;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;


@Slf4j
@RestController
@RequestMapping("/callback/getCase")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class MetadataController extends CallbackController {

    @PostMapping("/metadata")
    public Map<String, Object> getMetaData(@RequestBody CallbackRequest request) {
        CaseData caseData = getCaseData(request);

        return Map.of("metadataFields", List.of(
            CaseViewField.builder()
                .id("[INJECTED_DATA.HAS_3RD_PARTY]")
                .label("Has a respondent local authority been added to the case?")
                .value(isEmpty(caseData.getRespondentLocalAuthority()) ? "No" : "Yes")
                .fieldType(CaseViewField.FieldType.TEXT)
                .build()
        ));
    }

}

