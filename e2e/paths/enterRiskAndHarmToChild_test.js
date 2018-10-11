const config = require('../config.js');

Feature('Enter risk and harm to child');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.enterRisk);
});

Scenario('complete half of the enter risk and harm to children in the c110a application', (I, enterRiskAndHarmToChildPage) => {
  enterRiskAndHarmToChildPage.completePhyiscalHarm();
  enterRiskAndHarmToChildPage.completeEmotionalHarm();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRisk);
});

Scenario('complete entering risk and harm to children in the c110a application', (I, enterRiskAndHarmToChildPage) => {
  enterRiskAndHarmToChildPage.completePhyiscalHarm();
  enterRiskAndHarmToChildPage.completeEmotionalHarm();
  enterRiskAndHarmToChildPage.completeSexualAbuse();
  enterRiskAndHarmToChildPage.completeNeglect();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRisk);
});
