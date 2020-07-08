package uk.gov.hmcts.reform.fpl.controllers.cmo;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping("upload-cmo")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftCaseManagementOrderController {

}
