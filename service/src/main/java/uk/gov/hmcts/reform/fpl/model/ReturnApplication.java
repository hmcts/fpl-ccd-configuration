package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.capitalize;

@Data
@Builder
@AllArgsConstructor
public class ReturnApplication {
    private final List<ReturnedApplicationReasons> reason;
    private final String note;
    private String submittedDate;
    private String returnedDate;
    private DocumentReference document;

    @JsonIgnore
    public String getFormattedReturnReasons() {
        if (reason != null) {
            String formattedReasons = reason.stream()
                .map(ReturnedApplicationReasons::getLabel)
                .collect(Collectors.joining(", "));

            return capitalize(formattedReasons.toLowerCase());
        }

        return "";
    }
}
