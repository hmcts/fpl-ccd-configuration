package uk.gov.hmcts.reform.fpl.model.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.HearingVacatedTemplate;

@SuperBuilder(toBuilder = true)
public class VacateOfHearingCafcassData extends HearingVacatedTemplate implements CafcassData {}
