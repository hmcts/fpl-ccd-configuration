package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.deserializer.DynamicListDeserializer;
import uk.gov.hmcts.reform.fpl.json.deserializer.YesNoDeserializer;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;

@Value
@Builder
@Jacksonized
public class ConfirmApplicationReviewedEventData {
    @JsonDeserialize(using = YesNoDeserializer.class)
    YesNo hasApplicationToBeReviewed;

    @JsonDeserialize(using = YesNoDeserializer.class)
    YesNo onlyOneApplicationToBeReviewed;

    @JsonDeserialize(using = DynamicListDeserializer.class)
    DynamicList additionalApplicationToBeReviewedList;

    AdditionalApplicationsBundle additionalApplicationsBundleToBeReviewed;
    List<String> confirmApplicationReviewed;

    public static List<String> eventFields() {
        return List.of("hasApplicationToBeReviewed",
            "onlyOneApplicationToBeReviewed",
            "additionalApplicationToBeReviewedList",
            "additionalApplicationsBundleToBeReviewed",
            "confirmApplicationReviewed");
    }
}
