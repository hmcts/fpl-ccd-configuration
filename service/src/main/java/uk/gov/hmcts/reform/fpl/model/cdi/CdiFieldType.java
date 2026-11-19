package uk.gov.hmcts.reform.fpl.model.cdi;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class CdiFieldType {
    public static final CdiFieldType TEXT = new CdiFieldType("Text", "Text");
    public static final CdiFieldType NUMBER = new CdiFieldType("Number", "Number");

    String id;
    String type;
}

