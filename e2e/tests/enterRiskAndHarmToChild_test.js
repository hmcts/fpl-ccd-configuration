const config = require('../config.js');

Feature('Enter risk and harm to child');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterRisk);
});

Scenario('complete half of the enter risk and harm to children in the c110a' +
  ' application', (I, enterRiskAndHarmToChildrenEventPage, caseViewPage) => {
  enterRiskAndHarmToChildrenEventPage.completePhysicalHarm();
  enterRiskAndHarmToChildrenEventPage.completeEmotionalHarm();
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRisk);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'Risks and harm to children', 'Physical harm including' +
    ' non-accidental injury', 'Yes');
  I.seeAnswerInTab(2, 'Risks and harm to children', 'Select all that apply', 'Past harm');
  I.seeAnswerInTab(3, 'Risks and harm to children', 'Emotional harm', 'No');
});

Scenario('complete entering risk and harm to children in the c110a' +
  ' application', (I, enterRiskAndHarmToChildrenEventPage, caseViewPage) => {
  enterRiskAndHarmToChildrenEventPage.completePhysicalHarm();
  enterRiskAndHarmToChildrenEventPage.completeEmotionalHarm();
  enterRiskAndHarmToChildrenEventPage.completeSexualAbuse();
  enterRiskAndHarmToChildrenEventPage.completeNeglect();
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRisk);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'Risks and harm to children', 'Physical harm including' +
  ' non-accidental injury', 'Yes');
  I.seeAnswerInTab(2, 'Risks and harm to children', 'Select all that apply', 'Past harm');
  I.seeAnswerInTab(3, 'Risks and harm to children', 'Emotional harm', 'No');
  I.seeAnswerInTab(4, 'Risks and harm to children', 'Sexual abuse', 'No');
  I.seeAnswerInTab(5, 'Risks and harm to children', 'Neglect', 'Yes');
  I.seeAnswerInTab(6, 'Risks and harm to children', 'Select all that apply', ['Past harm', 'Future risk of harm']);
});
