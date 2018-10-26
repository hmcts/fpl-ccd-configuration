const config = require('../config.js');

Feature('Enter children in application').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.enterChildren);
});

Scenario('completing half of the enter children in the c110a application', (I, enterChildrenPage) => {
  enterChildrenPage.enterChildDetails('Timothy', '01', '08', '2015');
  enterChildrenPage.defineChildSituation('01', '11', '2017');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterChildren);
});

Scenario('completing entering child information in the c110a application', (I, enterChildrenPage) => {
  enterChildrenPage.enterChildDetails('Timothy', '01', '08', '2015');
  enterChildrenPage.defineChildSituation('01', '11', '2017');
  enterChildrenPage.enterKeyDatesAffectingHearing();
  enterChildrenPage.enterSummaryOfCarePlan();
  enterChildrenPage.defineAdoptionIntention();
  enterChildrenPage.enterParentsDetails();
  enterChildrenPage.enterSocialWorkerDetails();
  enterChildrenPage.defineChildAdditionalNeeds();
  enterChildrenPage.defineContactDetailsVisibility();
  enterChildrenPage.defineAbilityToTakePartInProceedings();
  enterChildrenPage.addChild();
  enterChildrenPage.enterChildDetails('Susan', '01', '07', '2016');
  enterChildrenPage.defineChildSituation('02', '11', '2017');
  enterChildrenPage.enterKeyDatesAffectingHearing();
  enterChildrenPage.enterSummaryOfCarePlan();
  enterChildrenPage.defineAdoptionIntention();
  enterChildrenPage.enterParentsDetails();
  enterChildrenPage.enterSocialWorkerDetails();
  enterChildrenPage.defineChildAdditionalNeeds();
  enterChildrenPage.defineContactDetailsVisibility();
  enterChildrenPage.defineAbilityToTakePartInProceedings();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterChildren);
});
