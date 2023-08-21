package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;

public interface WithDocument extends RemovableDocument {

    DocumentReference getDocument();

    DocumentUploaderType getUploaderType();

    List<String> getUploaderCaseRoles();

    String getMarkAsConfidential();

}
