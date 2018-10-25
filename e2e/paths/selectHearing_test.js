const config = require('../config.js');

Feature('Select hearing');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.selectHearing);
});

Scenario('completing half the fields in the Select hearing section of the c110a application', (I, caseViewPage, selectHearingPage) => {
  selectHearingPage.enterTimeFrame();
  selectHearingPage.enterHearingType();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.selectHearing);
  caseViewPage.selectTab(caseViewPage.tabs.ordersHearing);
  I.seeAnswerInTab('Hearing', 'When do you need a hearing?', selectHearingPage.fields.timeFrame.sameDay);  
  I.seeAnswerInTab('Hearing', 'Give reason', 'test reason');
  I.seeAnswerInTab('Hearing', 'What type of hearing do you need?', selectHearingPage.fields.hearingType.contestedICO);
});

Scenario('completing the Select hearing section of the c110a application', (I, caseViewPage, selectHearingPage) => {
  selectHearingPage.enterTimeFrame();
  selectHearingPage.enterHearingType();
  selectHearingPage.enterWithoutNoticeHearing();
  selectHearingPage.enterReducedHearing();
  selectHearingPage.enterRespondentsAware();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.selectHearing);  
  caseViewPage.selectTab(caseViewPage.tabs.ordersHearing);
  I.seeAnswerInTab('Hearing', 'When do you need a hearing?', selectHearingPage.fields.timeFrame.sameDay);
  I.seeAnswerInTab('Hearing', 'Give reason', 'test reason');
  I.seeAnswerInTab('Hearing', 'What type of hearing do you need?', selectHearingPage.fields.hearingType.contestedICO);
  I.seeAnswerInTab('Hearing', 'Do you need a without notice hearing?', 'Yes');
  I.seeAnswerInTab('Hearing', 'Do you need a hearing with reduced notice?', 'No');
  I.seeAnswerInTab('Hearing', 'Are respondents aware of proceedings?', 'Yes');
});
