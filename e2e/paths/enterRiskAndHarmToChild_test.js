const config = require('../config.js');

Feature('Enter risk and harm to child');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.enterRisk);
});

Scenario('complete half of the enter risk and harm to children in the c110a' +
  ' application', (I, enterRiskAndHarmToChildPage, caseViewPage) => {
  enterRiskAndHarmToChildPage.completePhyiscalHarm();
  enterRiskAndHarmToChildPage.completeEmotionalHarm();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRisk);
  caseViewPage.selectTab(caseViewPage.tabs.legalOpinion);
  I.seeAnswerInTab(1, 'Risks and harm to children', 'Physical harm including' +
    ' non-accidental injury', 'Yes');
  I.seeAnswerInTab(2, 'Risks and harm to children', 'Select all that apply', 'Past harm');
  I.seeAnswerInTab(3, 'Risks and harm to children', 'Emotional harm', 'No');
});

Scenario('complete entering risk and harm to children in the c110a' +
  ' application', (I, enterRiskAndHarmToChildPage, caseViewPage) => {
  enterRiskAndHarmToChildPage.completePhyiscalHarm();
  enterRiskAndHarmToChildPage.completeEmotionalHarm();
  enterRiskAndHarmToChildPage.completeSexualAbuse();
  enterRiskAndHarmToChildPage.completeNeglect();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRisk);
  caseViewPage.selectTab(caseViewPage.tabs.legalOpinion);
  I.seeAnswerInTab(1, 'Risks and harm to children', 'Physical harm including' +
  ' non-accidental injury', 'Yes');
  I.seeAnswerInTab(2, 'Risks and harm to children', 'Select all that apply', 'Past harm');
  I.seeAnswerInTab(3, 'Risks and harm to children', 'Emotional harm', 'No');
  I.seeAnswerInTab(4, 'Risks and harm to children', 'Sexual abuse', 'No');
  I.seeAnswerInTab(5, 'Risks and harm to children', 'Neglect', 'Yes');
  I.seeAnswerInTab(6, 'Risks and harm to children', 'Select all that apply', ['Past harm', 'Future risk of harm']);
});
