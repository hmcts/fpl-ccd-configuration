package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NoticeOfChangeAnswersData {
    NoticeOfChangeAnswers noticeOfChangeAnswers0;
    NoticeOfChangeAnswers noticeOfChangeAnswers1;
    NoticeOfChangeAnswers noticeOfChangeAnswers2;
    NoticeOfChangeAnswers noticeOfChangeAnswers3;
    NoticeOfChangeAnswers noticeOfChangeAnswers4;
    NoticeOfChangeAnswers noticeOfChangeAnswers5;
    NoticeOfChangeAnswers noticeOfChangeAnswers6;
    NoticeOfChangeAnswers noticeOfChangeAnswers7;
    NoticeOfChangeAnswers noticeOfChangeAnswers8;
    NoticeOfChangeAnswers noticeOfChangeAnswers9;
}
