package uk.gov.hmcts.reform.fpl.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Display {
    public enum Due {
        ON,
        BY,
        ASAP
    }

    /**
     * Indicates if this direction should be treated (and displayed where appropriate)
     * as "ON", "BY" or "As soon as possible".
     */
    private Due due;

    private String templateDateFormat;
    /**
     * What fields should be used to prepopulate dueBy (optional sa it can be null so no prepopulation should happen.
     * It looks like for now we will have only hearing.
     */
    private String relativeToDateType;

    /**
     * How many units due date is shifted by.
     */
    private String delta;

    /**
     * I don't think this one makes sense - what would be point of shifting deadline post the hearing date?.
     */
    private boolean isBefore;

    /**
     * If the direction can be removed (thus is optional) - should be rendered as yes / no question.
     */
    private boolean directionRemovable;

    /**
     * If true the body (directionText) of the direction should be hidden.
     */
    private boolean showDateOnly;
}
