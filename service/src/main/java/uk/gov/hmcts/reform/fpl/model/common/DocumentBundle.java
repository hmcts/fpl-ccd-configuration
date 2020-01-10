package uk.gov.hmcts.reform.fpl.model.common;

import ccd.sdk.types.ComplexType;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
@ComplexType(name = "NoticeOfProceedingsBundle")
public class DocumentBundle {
    private final DocumentReference document;
}
