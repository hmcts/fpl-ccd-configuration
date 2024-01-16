

module.exports = {
  fields: {
    isMessageRegardingAdditionalApplications: {
      yes: '#isMessageRegardingAdditionalApplications_Yes',
      no: '#isMessageRegardingAdditionalApplications_No',
    },
    eventOptions: {
      newMessage: '#messageJudgeOption-NEW_MESSAGE',
      reply: '#messageJudgeOption-REPLY',
    },
    additionalApplicationsList: '#additionalApplicationsDynamicList',
    existingMessagesList: '#judicialMessageDynamicList',
    senderEmail: '#judicialMessageMetaData_sender',
    recipientEmail: '#judicialMessageMetaData_recipient',
    senderType:{
      id:'#judicialMessageMetaData_senderType',
      options:{
        CTCS:'1: CTSC',
        localCourtadmin:'2: LOCAL_COURT_ADMIN',
        legalAdvisor:'3: OTHER',
        hearingJudge:'4: HEARING_JUDGE',
      },
    },
    recipientType:{
      id: '#judicialMessageMetaData_recipientType',
      options:{
        CTCS:'1: CTSC',
        allocateJudge: '2: JUDICIARY',
        localCourtAdmin:'3: LOCAL_COURT_ADMIN',
        legalAdvisor:'4: OTHER',
        HearingJudge:'5: HEARING_JUDGE',
      },
    },
    replyingToMessage: {
      id: '#judicialMessageReply_isReplying',
      options: {
        yes: '#judicialMessageReply_isReplying_Yes',
        no: '#judicialMessageReply_isReplying_No',
      },
    },
    subject: '#judicialMessageMetaData_requestedBy',
    urgency: '#judicialMessageMetaData_urgency',
    latestMessage: '#judicialMessageNote',
    replyMessage: '#judicialMessageReply_latestMessage',
    replyFrom: '#judicialMessageReply_replyFrom',
    replyTo: '#judicialMessageReply_replyTo',
  },
};
