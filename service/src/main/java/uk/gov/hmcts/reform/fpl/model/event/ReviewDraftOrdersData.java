package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import static org.springframework.util.ObjectUtils.isEmpty;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ReviewDraftOrdersData {
    String judgeTitleAndName;

    String draftCMOExists;
    String draftBlankOrdersCount;

    ReviewDecision reviewDecision1;
    ReviewDecision reviewDecision2;
    ReviewDecision reviewDecision3;
    ReviewDecision reviewDecision4;
    ReviewDecision reviewDecision5;
    ReviewDecision reviewDecision6;
    ReviewDecision reviewDecision7;
    ReviewDecision reviewDecision8;
    ReviewDecision reviewDecision9;
    ReviewDecision reviewDecision10;

    String cmoDraftOrderTitle;
    String draftOrder1Title;
    String draftOrder2Title;
    String draftOrder3Title;
    String draftOrder4Title;
    String draftOrder5Title;
    String draftOrder6Title;
    String draftOrder7Title;
    String draftOrder8Title;
    String draftOrder9Title;
    String draftOrder10Title;

    DocumentReference cmoDraftOrderDocument;
    DocumentReference draftOrder1Document;
    DocumentReference draftOrder2Document;
    DocumentReference draftOrder3Document;
    DocumentReference draftOrder4Document;
    DocumentReference draftOrder5Document;
    DocumentReference draftOrder6Document;
    DocumentReference draftOrder7Document;
    DocumentReference draftOrder8Document;
    DocumentReference draftOrder9Document;
    DocumentReference draftOrder10Document;

    public static String[] transientFields() {
        return new String[]{
            "numDraftCMOs", "cmoToReviewList", "draftCMOExists", "draftBlankOrdersCount", "cmoDraftOrderTitle",
            "draftOrder1Title", "draftOrder2Title", "draftOrder3Title", "draftOrder4Title", "draftOrder5Title",
            "draftOrder6Title", "draftOrder7Title", "draftOrder8Title", "draftOrder9Title", "draftOrder10Title",
            "cmoDraftOrderDocument", "draftOrder1Document", "draftOrder2Document", "draftOrder3Document",
            "draftOrder4Document", "draftOrder5Document", "draftOrder6Document", "draftOrder7Document",
            "draftOrder8Document", "draftOrder9Document", "draftOrder10Document", "reviewDraftOrdersTitles",
            "draftOrdersTitlesInBundle", "draftOrdersApproved", "judgeTitleAndName"
        };
    }

    public static String[] reviewDecisionFields() {
        return new String[]{
            "reviewCMODecision", "reviewDecision1", "reviewDecision2", "reviewDecision3", "reviewDecision4",
            "reviewDecision5", "reviewDecision6", "reviewDecision7", "reviewDecision8", "reviewDecision9",
            "reviewDecision10"
        };
    }

    @JsonIgnore
    public boolean hasADraftBeenApproved() {
        return (!isEmpty(reviewDecision1) && reviewDecision1.hasBeenApproved())
            || (!isEmpty(reviewDecision2) && reviewDecision2.hasBeenApproved())
            || (!isEmpty(reviewDecision3) && reviewDecision3.hasBeenApproved())
            || (!isEmpty(reviewDecision4) && reviewDecision4.hasBeenApproved())
            || (!isEmpty(reviewDecision5) && reviewDecision5.hasBeenApproved())
            || (!isEmpty(reviewDecision6) && reviewDecision6.hasBeenApproved())
            || (!isEmpty(reviewDecision7) && reviewDecision7.hasBeenApproved())
            || (!isEmpty(reviewDecision8) && reviewDecision8.hasBeenApproved())
            || (!isEmpty(reviewDecision9) && reviewDecision9.hasBeenApproved())
            || (!isEmpty(reviewDecision10) && reviewDecision10.hasBeenApproved());
    }

}
