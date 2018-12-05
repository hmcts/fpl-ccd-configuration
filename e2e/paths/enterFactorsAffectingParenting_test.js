const config = require('../config.js');

Feature('EnterFactorsAffectingParenting').retry(2);

Before((I) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
});

Scenario('Complete half the factors affecting parenting section of the c110a application', (I, enterFactorsAffectingParentingPage, caseViewPage) => {
  caseViewPage.goToNewActions(config.applicationActions.enterFactorsAffectingParenting);
  enterFactorsAffectingParentingPage.completeAlcoholOrDrugAbuse();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterFactorsAffectingParenting);

  caseViewPage.goToNewActions(config.applicationActions.enterFactorsAffectingParenting);
  enterFactorsAffectingParentingPage.completeAlcoholOrDrugAbuse();
  enterFactorsAffectingParentingPage.completeDomesticViolence();
  enterFactorsAffectingParentingPage.completeAnythingElse();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterFactorsAffectingParenting);

  caseViewPage.selectTab(caseViewPage.tabs.legalOpinion);
  I.seeAnswerInTab(1, 'Factors affecting parenting', 'Alcohol or drug abuse', 'Yes');
  I.seeAnswerInTab(2, 'Factors affecting parenting', 'Give details', 'mock reason');
  I.seeAnswerInTab(3, 'Factors affecting parenting', 'Domestic violence', 'Yes');
  I.seeAnswerInTab(4, 'Factors affecting parenting', 'Give details', 'mock reason');
  I.seeAnswerInTab(5, 'Factors affecting parenting', 'Anything else', 'Yes');
  I.seeAnswerInTab(6, 'Factors affecting parenting', 'Give details', 'mock reason');
});
