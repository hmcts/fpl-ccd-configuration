const config = require('../config.js');

Feature('EnterFactorsAffectingParenting');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.enterFactorsAffectingParenting);
});

Scenario('Complete half the factors affecting parenting section of the c110a application', (I, enterFactorsAffectingParentingPage) => {
  enterFactorsAffectingParentingPage.completeAlcoholOrDrugAbuse();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterFactorsAffectingParenting);
});

Scenario('Filling in factors affecting parenting sections of c110a', (I, enterFactorsAffectingParentingPage) => {
  enterFactorsAffectingParentingPage.completeAlcoholOrDrugAbuse();
  enterFactorsAffectingParentingPage.completeDomesticViolence();
  enterFactorsAffectingParentingPage.completeAnythingElse();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterFactorsAffectingParenting);
});
