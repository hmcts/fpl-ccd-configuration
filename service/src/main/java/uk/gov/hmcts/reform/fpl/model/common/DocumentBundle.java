package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.types.ComplexType;

@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
@ComplexType(name = "NoticeOfProceedingsBundle")
public class DocumentBundle {
    private final DocumentReference document;
}
