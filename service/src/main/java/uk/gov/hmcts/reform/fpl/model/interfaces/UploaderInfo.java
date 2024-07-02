package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;

import java.util.List;

public interface UploaderInfo {

    DocumentUploaderType getUploaderType();

    List<CaseRole> getUploaderCaseRoles();
}
