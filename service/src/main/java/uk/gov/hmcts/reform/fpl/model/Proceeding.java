package uk.gov.hmcts.reform.fpl.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.model.common.Element;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class Proceeding {
    private final String onGoingProceeding;
    private final String proceedingStatus;
    private final String caseNumber;
    private final String started;
    private final String ended;
    private final String ordersMade;
    private final String judge;
    private final String children;
    private final String guardian;
    private final String sameGuardianNeeded;
    private final String sameGuardianDetails;
    private final List<Element<OtherProceeding>> additionalProceedings;

    @JsonIgnore
    public OtherProceeding getFirstProceeding() {
        if (StringUtils.isNotEmpty(onGoingProceeding)) {
           return OtherProceeding.builder()
                .onGoingProceeding(this.getOnGoingProceeding())
                .proceedingStatus(this.getProceedingStatus())
                .caseNumber(this.getCaseNumber())
                .started(this.getStarted())
                .ended(this.getEnded())
                .ordersMade(this.getOrdersMade())
                .judge(this.getJudge())
                .children(this.getChildren())
                .guardian(this.getGuardian())
                .sameGuardianNeeded(this.getSameGuardianNeeded())
                .sameGuardianDetails(this.getSameGuardianDetails())
                .build();
        }
        return null;
    }
}
