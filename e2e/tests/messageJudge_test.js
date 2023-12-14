const config = require('../config.js');
const mandatoryWithAdditionalApplicationsBundle = require('../fixtures/caseData/mandatoryWithAdditionalApplicationsBundle.json');
const judgeMessageData = require('../fixtures/caseData/caseWithJudgeMessage.json');
const judgeReply = require('../fixtures/caseData/caseWithJudicialMessageReply.json');
let caseId;
const message = 'Some note';
const reply = 'This is a reply';
const messageHistoryInitial = `${config.ctscEmail} - ${message}`;
const messageHistoryReply = `${messageHistoryInitial}\n \n${config.judicaryUser.email} - ${reply}`;

Feature('Message judge or legal adviser');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(mandatoryWithAdditionalApplicationsBundle); }
}

Scenario('HMCTS admin messages the judge @fixed    @cross-browser', async ({I, caseViewPage, messageJudgeOrLegalAdviserEventPage}) => {
  await setupScenario(I);
  // pause();
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId.caseId);
  await caseViewPage.goToNewActions(config.applicationActions.messageJudge);
  await I.waitForText('Message subject');
  // await I.wait(60);
  messageJudgeOrLegalAdviserEventPage.selectMessageRelatedToAdditionalApplication();
  await messageJudgeOrLegalAdviserEventPage.selectAdditionalApplication();
  //add sender
  await messageJudgeOrLegalAdviserEventPage.selectSenderType('CTSC');
  // add new select whom to send message
  await messageJudgeOrLegalAdviserEventPage.selectRecipientType('Allocated Judge');
  messageJudgeOrLegalAdviserEventPage.enterRecipientEmail('recipient@fpla.com');
  messageJudgeOrLegalAdviserEventPage.enterSubject('Subject 1');
  messageJudgeOrLegalAdviserEventPage.enterUrgency('High');
  await I.goToNextPage();
  messageJudgeOrLegalAdviserEventPage.enterMessage(message);
  //await I.goToNextPage();
  await I.submitEventWithCYA('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.messageJudge);
  I.selectTabFunction(caseViewPage.tabs.judicialMessages);
  I.seeInTab(['Message 1', 'From'], config.ctscEmail);
  I.seeInTab(['Message 1', 'Sent to'], 'recipient@fpla.com');
  I.seeInTab(['Message 1', 'Message subject'], 'Subject 1');
  I.seeInTab(['Message 1', 'Urgency'], 'High');
  I.seeInTab(['Message 1', 'Latest message'], 'Some note');
  I.seeInTab(['Message 1', 'Status'], 'Open');
  I.seeInTab(['Message 1', 'Related documents'], 'Test.txt');
  I.seeInTab(['Message 1', 'Application'], 'C2, 25 March 2021, 3:16pm');
  I.seeInTab(['Message 1', 'Message history'], messageHistoryInitial);
  I.dontSeeInTab(['Closed messages']);
});

Scenario('Judge replies to HMCTS admin @fixed  @nightlyOnly', async ({I, caseViewPage, messageJudgeOrLegalAdviserEventPage}) => {
  // await setupScenario(I);
  caseId = await I.submitNewCaseWithData(judgeMessageData);
  //pause();
  // console.log(caseId);
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId.caseId);
  await caseViewPage.goToNewActions(config.applicationActions.replyMessage);
  //messageJudgeOrLegalAdviserEventPage.selectReplyToMessage();
  await messageJudgeOrLegalAdviserEventPage.selectJudicialMessage();
  await I.goToNextPage();
  messageJudgeOrLegalAdviserEventPage.selectReplyingToJudicialMessage();
  messageJudgeOrLegalAdviserEventPage.enterMessageReply(reply);
  await I.submitEventWithCYA('Save and continue');
  await I.waitForText('Next step');
  //await I.completeEvent('Save and continue');
  //  I.completeEvent(config.applicationActions.messageJudge);
  I.selectTabFunction(caseViewPage.tabs.judicialMessages);
  // I.selectTab(caseViewPage.tabs.judicialMessages)
  I.waitForText('Judicial messages',2);
  I.seeInTab(['Message 1', 'From'], config.judicaryUser.email);
  console.log ('message\n' + messageHistoryReply );
  I.seeInTab(['Message 1', 'Sent to'], config.ctscEmail);
  I.seeInTab(['Message 1', 'Message subject'], 'Subject 1');
  I.seeInTab(['Message 1', 'Urgency'], 'High');
  I.seeInTab(['Message 1', 'Latest message'], reply);
  I.seeInTab(['Message 1', 'Status'], 'Open');
  I.seeInTab(['Message 1', 'Related documents'], 'Test.txt');
  I.seeInTab(['Message 1', 'Application'], 'C2, 25 March 2021, 3:16pm');
  I.seeInTab(['Message 1', 'Message history'], messageHistoryReply);

  I.dontSeeInTab(['Closed']);
});

Scenario('HMCTS admin closes the message @fixed  @nightlyOnly', async ({I, caseViewPage, messageJudgeOrLegalAdviserEventPage}) => {
  // await setupScenario(I);
  caseId = await I.submitNewCaseWithData(judgeReply);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId.caseId);
  await caseViewPage.goToNewActions(config.applicationActions.replyMessage);
  // messageJudgeOrLegalAdviserEventPage.selectReplyToMessage();
  await messageJudgeOrLegalAdviserEventPage.selectJudicialMessage();
  await I.goToNextPage();
  messageJudgeOrLegalAdviserEventPage.selectClosingJudicialMessage();
  I.see('This message will now be marked as closed');
  await I.submitEventWithCYA('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.messageJudge);
  I.selectTabFunction(caseViewPage.tabs.judicialMessages);

  I.see('Closed messages');
  I.seeInTab(['Message 1', 'From'], config.judicaryUser.email);
  I.seeInTab(['Message 1', 'Sent to'], config.ctscEmail);
  I.seeInTab(['Message 1', 'Message subject'], 'Subject 1');
  I.seeInTab(['Message 1', 'Urgency'], 'High');
  I.seeInTab(['Message 1', 'Status'], 'Closed');
  I.seeInTab(['Message 1', 'Related documents'], 'Test.txt');
  I.seeInTab(['Message 1', 'Application'], 'C2, 25 March 2021, 3:16pm');
  I.seeInTab(['Message 1', 'Message history'], messageHistoryReply);
});

Scenario('Judge messages court admin @fixed @nightlyOnly', async ({I, caseViewPage, messageJudgeOrLegalAdviserEventPage}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId.caseId);
  await caseViewPage.goToNewActions(config.applicationActions.messageJudge);
  await I.waitForText('Message subject');
  messageJudgeOrLegalAdviserEventPage.selectMessageNotRelatedToAdditionalApplication();
  messageJudgeOrLegalAdviserEventPage.selectRecipientType('CTSC');
  messageJudgeOrLegalAdviserEventPage.enterSubject('Judge subject');
  await I.goToNextPage();
  messageJudgeOrLegalAdviserEventPage.enterMessage('Judge message');
  await I.submitEventWithCYA('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.messageJudge);
  I.selectTabFunction(caseViewPage.tabs.judicialMessages);

  I.seeInTab(['Message 1', 'From'], config.judicaryUser.email);
  I.seeInTab(['Message 1', 'Sent to'], config.ctscEmail);
  I.seeInTab(['Message 1', 'Message subject'], 'Judge subject');
  I.seeInTab(['Message 1', 'Latest message'], 'Judge message');
  I.seeInTab(['Message 1', 'Status'], 'Open');
  I.seeInTab(['Message 1', 'Message history'], messageHistoryInitial);
});
