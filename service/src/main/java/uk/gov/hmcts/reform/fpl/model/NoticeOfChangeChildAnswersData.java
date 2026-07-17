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
public class NoticeOfChangeChildAnswersData {

    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers0;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers1;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers2;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers3;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers4;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers5;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers6;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers7;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers8;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers9;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers10;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers11;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers12;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers13;
    @CCD(label = " ", access = {CaseworkerApproverCruPlus2RolesRlrjzrAccess.class})
    NoticeOfChangeAnswers noticeOfChangeChildAnswers14;

}
