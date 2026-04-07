package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.deserializer.DynamicListDeserializer;
import uk.gov.hmcts.reform.fpl.json.deserializer.YesNoDeserializer;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;

@Value
@Builder
@Jacksonized
public class ConfirmApplicationReviewedEventData {
    @Temp
    @JsonDeserialize(using = YesNoDeserializer.class)
    YesNo hasApplicationToBeReviewed;

    @Temp
    @JsonDeserialize(using = YesNoDeserializer.class)
    YesNo onlyOneApplicationToBeReviewed;

    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    DynamicList additionalApplicationToBeReviewedList;

    @Temp
    @JsonDeserialize(using = YesNoDeserializer.class)
    YesNo hasC2ToBeReview;

    @Temp
    @JsonDeserialize(using = YesNoDeserializer.class)
    YesNo hasOtherToBeReview;

    @Temp
    C2AdditionalApplicationEventData c2AdditionalApplicationToBeReview;

    @Temp
    OtherApplicationsBundle otherAdditionalApplicationToBeReview;

    public static List<String> eventFields() {
        return List.of("hasApplicationToBeReviewed",
            "onlyOneApplicationToBeReviewed",
            "additionalApplicationToBeReviewedList",
            "hasC2ToBeReview", "hasOtherToBeReview",
            "c2AdditionalApplicationToBeReview",
            "otherAdditionalApplicationToBeReview");
    }
}
