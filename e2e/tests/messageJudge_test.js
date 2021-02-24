const config = require('../config.js');
const mandatoryWithC2DocumentBundle = require('../fixtures/caseData/mandatoryWithC2DocumentBundle.json');

let caseId;
let message = 'Some note';
let reply = 'This is a reply';

Feature('Message judge or legal adviser');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(mandatoryWithC2DocumentBundle);
});

Scenario('HMCTS admin messages the judge', async ({I, caseViewPage, messageJudgeOrLegalAdviserEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.messageJudge);
  messageJudgeOrLegalAdviserEventPage.selectMessageRelatedToC2();
  await messageJudgeOrLegalAdviserEventPage.selectC2();
  messageJudgeOrLegalAdviserEventPage.enterRecipientEmail('recipient@fpla.com');
  messageJudgeOrLegalAdviserEventPage.enterSubject('Subject 1');
  messageJudgeOrLegalAdviserEventPage.enterUrgency('High');
  await I.goToNextPage();
  messageJudgeOrLegalAdviserEventPage.enterMessage(message);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.messageJudge);
  caseViewPage.selectTab(caseViewPage.tabs.judicialMessages);
  I.seeInTab(['Message 1', 'From'], config.hmctsAdminUser);
  I.seeInTab(['Message 1', 'Sent to'], 'recipient@fpla.com');
  I.seeInTab(['Message 1', 'Message subject'], 'Subject 1');
  I.seeInTab(['Message 1', 'Urgency'], 'High');
  I.seeInTab(['Message 1', 'Message'], 'Some note');
  I.seeInTab(['Message 1', 'Status'], 'Open');
  I.dontSeeInTab(['Closed messages']);
});

Scenario('Judge replies to HMCTS admin', async ({I, caseViewPage, messageJudgeOrLegalAdviserEventPage}) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.messageJudge);
  messageJudgeOrLegalAdviserEventPage.selectReplyToMessage();
  await messageJudgeOrLegalAdviserEventPage.selectJudicialMessage();
  await I.goToNextPage();
  messageJudgeOrLegalAdviserEventPage.selectReplyingToJudicialMessage();
  messageJudgeOrLegalAdviserEventPage.enterMessageReply(reply);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.messageJudge);
  caseViewPage.selectTab(caseViewPage.tabs.judicialMessages);
  I.seeInTab(['Message 1', 'From'], config.judicaryUser);
  I.seeInTab(['Message 1', 'Sent to'], config.hmctsAdminUser);
  I.seeInTab(['Message 1', 'Message subject'], 'Subject 1');
  I.seeInTab(['Message 1', 'Urgency'], 'High');
  I.seeInTab(['Message 1', 'Message'], reply);
  I.seeInTab(['Message 1', 'Status'], 'Open');
  I.dontSeeInTab(['Closed messages']);
});

Scenario('HMCTS admin closes the message', async ({I, caseViewPage, messageJudgeOrLegalAdviserEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.messageJudge);
  messageJudgeOrLegalAdviserEventPage.selectReplyToMessage();
  await messageJudgeOrLegalAdviserEventPage.selectJudicialMessage();
  await I.goToNextPage();
  messageJudgeOrLegalAdviserEventPage.selectClosingJudicialMessage();
  I.see('This message will now be marked as closed');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.messageJudge);
  caseViewPage.selectTab(caseViewPage.tabs.judicialMessages);
  const history = config.ctscEmail + ' - ' +  message + '\n \n' + config.judicaryUser.email + ' - ' + reply;
  I.see('Closed messages');
  I.seeInTab(['Message 1', 'From'], config.judicaryUser);
  I.seeInTab(['Message 1', 'Sent to'], config.hmctsAdminUser);
  I.seeInTab(['Message 1', 'Message subject'], 'Subject 1');
  I.seeInTab(['Message 1', 'Urgency'], 'High');
  I.seeInTab(['Message 1', 'Status'], 'Closed');
  I.seeInTab(['Message 1', 'Message history'], history);
});

Scenario('Judge messages court admin', async ({I, caseViewPage, messageJudgeOrLegalAdviserEventPage}) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);

  await caseViewPage.goToNewActions(config.applicationActions.messageJudge);
  messageJudgeOrLegalAdviserEventPage.selectMessageNotRelatedToC2();
  messageJudgeOrLegalAdviserEventPage.enterSubject('Judge subject');
  await I.goToNextPage();

  messageJudgeOrLegalAdviserEventPage.enterMessage('Judge message');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.messageJudge);

  caseViewPage.selectTab(caseViewPage.tabs.judicialMessages);
  I.seeInTab(['Message 1', 'From'], config.hmctsAdminUser);
  I.seeInTab(['Message 1', 'Sent to'], config.ctscEmail);
  I.seeInTab(['Message 1', 'Message subject'], 'Judge subject');
  I.seeInTab(['Message 1', 'Message'], 'Judge message');
  I.seeInTab(['Message 1', 'Status'], 'Open');
});
