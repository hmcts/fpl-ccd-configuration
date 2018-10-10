const config = require('../config.js');

Feature('EnterFactorsAffectingParenting');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.enterParentingFactors);
});

Scenario('Clicking yes to a question produces a textarea in factors affecting parenting section', (I, enterFactorsAffectingParentingPage) => {
  enterFactorsAffectingParentingPage.enterYesForAlcoholOrDrugAbuse();
  I.seeElement('textArea');
});

Scenario('Filling in factors affecting parenting sections of c110a', (I, enterFactorsAffectingParentingPage) => {
  enterFactorsAffectingParentingPage.enterFactorsAffectingParenting();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterParentingFactors);
});
