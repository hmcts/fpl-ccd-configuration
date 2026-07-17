package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerApproverCruPlus2RolesRlrjzrAccess;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NoticeOfChangeAnswersData {
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeAnswers0;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeAnswers1;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeAnswers2;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeAnswers3;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeAnswers4;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeAnswers5;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeAnswers6;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeAnswers7;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeAnswers8;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeAnswers9;
}
