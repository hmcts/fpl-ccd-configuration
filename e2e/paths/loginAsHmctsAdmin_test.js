const config = require('../config.js');
let caseId;

Feature('Login and edit case as hmcts admin');

Before(async (I, caseViewPage, submitApplicationPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-h1');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationPage.giveConsent();
  I.continueAndSubmit();
  I.signOut();
});

Scenario('HMCTS admin can login and add a FamilyMan case number to a submitted case', (I, caseViewPage, loginPage, enterFamilyManPage, enterOtherProceedingsPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
  caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  enterFamilyManPage.enterCaseID();
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);


  // Children
  // Respondents
  // Other parties to be given notice
  // International Element
  // Other proceedings
  // Attending the Hearing

  function I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(event, summary, description,
    I_doActionsOnEditPage = () => {}) {

    caseViewPage.goToNewActions(event);
    I_doActionsOnEditPage();
    I.continueAndProvideSummary(summary, description);
    I.seeEventSubmissionConfirmation(event);
    I.see(summary);
    I.see(description);
  }

  const summary = 'Summary of change';
  const description = 'Description of change';

  I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendChildren,
    summary, description);

  I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendRespondents,
    summary, description);

  I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendOther,
    summary, description);

  I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendInternationalElement,
    summary, description);

  I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendOtherProceedings,
    summary, description, () => enterOtherProceedingsPage.selectNoForProceeding());

  I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendAttendingHearing,
    summary, description);

});
