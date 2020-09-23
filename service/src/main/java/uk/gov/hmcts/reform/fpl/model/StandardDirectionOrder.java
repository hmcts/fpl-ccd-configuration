package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.IssuableOrder;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

@Getter
public class StandardDirectionOrder extends OrderForHearing implements IssuableOrder {
    private final OrderStatus orderStatus;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final LocalDate dateOfUpload;
    private final String uploader;

    @Builder
    public StandardDirectionOrder(String hearingDate,
                                  String dateOfIssue,
                                  List<Element<Direction>> directions,
                                  DocumentReference orderDoc,
                                  OrderStatus orderStatus,
                                  JudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                  LocalDate dateOfUpload,
                                  String uploader) {
        super(hearingDate, dateOfIssue, directions, orderDoc);
        this.orderStatus = orderStatus;
        this.judgeAndLegalAdvisor = judgeAndLegalAdvisor;
        this.dateOfUpload = dateOfUpload;
        this.uploader = uploader;
    }

    @JsonIgnore
    public boolean isSealed() {
        return SEALED == orderStatus;
    }

    @JsonIgnore
    public void setDirectionsToEmptyList() {
        this.directions = emptyList();
    }
}
