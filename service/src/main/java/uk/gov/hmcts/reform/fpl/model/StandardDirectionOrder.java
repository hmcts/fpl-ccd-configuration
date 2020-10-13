package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardDirectionOrder extends OrderForHearing implements IssuableOrder {
    private final OrderStatus orderStatus;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final LocalDate dateOfUpload;
    private final String uploader;
    private final NoticeOfProceedings noticeOfProceedings;

    @Builder
    public StandardDirectionOrder(String hearingDate,
                                  String dateOfIssue,
                                  List<Element<Direction>> directions,
                                  DocumentReference orderDoc,
                                  OrderStatus orderStatus,
                                  JudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                  LocalDate dateOfUpload,
                                  String uploader,
                                  NoticeOfProceedings noticeOfProceedings) {
        super(hearingDate, dateOfIssue, directions, orderDoc);
        this.orderStatus = orderStatus;
        this.judgeAndLegalAdvisor = judgeAndLegalAdvisor;
        this.dateOfUpload = dateOfUpload;
        this.uploader = uploader;
        this.noticeOfProceedings = noticeOfProceedings;
    }

    @JsonIgnore
    public boolean isSealed() {
        return SEALED == orderStatus;
    }

    @JsonIgnore
    public void setDirectionsToEmptyList() {
        this.directions = emptyList();
    }

    @JsonIgnore
    public boolean isSendingNoticeOfProceedings() {
        return noticeOfProceedings != null && !noticeOfProceedings.getProceedingTypes().isEmpty();
    }
}
