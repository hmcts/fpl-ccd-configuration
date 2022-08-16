const config = require('../../config.js');
//const moment = require('moment');
const FPLConstants = require('./FPLConstants.js');
const apiHelper = require('../../helpers/api_helper.js');
//const output = require('codeceptjs').output;

/*const children = require('../fixtures/children.js');
const respondents = require('../fixtures/respondents.js');
const applicant = require('../fixtures/applicant.js');
const localAuthority = require('../fixtures/localAuthority.js');
const others = require('../fixtures/others.js');
const otherProceedings = require('../fixtures/otherProceedingData');
const ordersAndDirectionsNeeded = require('../fixtures/ordersAndDirectionsNeeded.js');*/

let caseId;

Feature('Local authority creates application Other');

/*BeforeSuite(async I => {
  await setupScenario(I);
});*/

/*
function login(email, password) {
  this.amOnPage(config.baseUrl);
  this.wait(FPLConstants.twoSecondWaitTime);
  if (config.e2e.testForCrossbrowser !== 'true') {
    this.resizeWindow(FPLConstants.windowsSizeX, FPLConstants.windowsSizeY);
    this.wait(FPLConstants.twoSecondWaitTime);
  }
  this.fillField('Email address', email);
  this.fillField('Password', password);
  this.wait(FPLConstants.twoSecondWaitTime);
  this.click({ css: '[type="submit"]' });
  this.wait(FPLConstants.fiveSecondWaitTime);
}
*/

/*function logout() {
  this.wait(FPLConstants.fiveSecondWaitTime);
  this.click('Logout');
  this.wait(FPLConstants.fiveSecondWaitTime);
}*/

/*async function navigateToCaseDetails (I,user,navigateCaseId) {
  login(`${user.email}`,
    `${user.password}`);
  await I.navigateToCaseDetails(navigateCaseId);
}*/


async function setupScenario(I, reuse = true) {
  let navigateCaseId;
  if (reuse == false) {
    navigateCaseId = await I.submitNewCase(config.swanseaLocalAuthorityUserOne);
  } else {
    if (!caseId) {
      caseId = await I.submitNewCase(config.swanseaLocalAuthorityUserOne);
    }
    navigateCaseId = caseId;
  }
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, navigateCaseId);
}

xScenario('Local authority verifies case created for a Case But Application not Completed', async ({I, caseViewOtherPage}) => {
  await setupScenario(I);
  I.dontSeeEvent(config.applicationActions.selectCourt);
  caseViewOtherPage.verifyCaseViewHeaderSection(caseId);
  caseViewOtherPage.clickStartApplicationTab();
  caseViewOtherPage.verifyStartApplicationTabDetails('');
  caseViewOtherPage.clickWhyCantISubmitMyApplication();
  caseViewOtherPage.verifyMandatoryRequiredErrorMessages([
    'Add the orders and directions sought in the Orders and directions sought',
    'Add the hearing urgency details in the Hearing urgency',
    'Add the grounds for the application in the Grounds for the application',
    'Add local authority\'s details in the Local authority\'s details',
    'Add the child\'s details in the Child\'s details',
    'Add the respondents\' details in the Respondents\' details',
    'Add the allocation proposal in the Allocation proposal']);
}).tag('@pipeline @nightly @crossbrowser');

Scenario('Local authority Changes the Case Name of Case.', async ({I,caseViewOtherPage, changeCaseViewOtherPage, ordersAndDirectionsOtherPage, hearingUrgencyOtherPage}) => {
  await setupScenario(I,true);
  caseViewOtherPage.clickChangeCaseName();
  I.wait(FPLConstants.defaultPageClickWaitTime);
  /*//Testing the cancel Link on the Change Case Name Page...
  caseViewOtherPage.clickCancelLink();
  I.wait(FPLConstants.defaultPageClickWaitTime);
  caseViewOtherPage.verifyStartApplicationTabDetails('');
  caseViewOtherPage.clickChangeCaseName();
  I.wait(FPLConstants.defaultPageClickWaitTime);
  changeCaseViewOtherPage.verifyChangeCaseNameScreen(caseId);
  I.wait(FPLConstants.defaultPageClickWaitTime);

  //Testing the cancel Link on the Change Case Name Check Your Answers Page...
  changeCaseViewOtherPage.clickCancelLink();
  I.wait(FPLConstants.defaultPageClickWaitTime);
  caseViewOtherPage.verifyStartApplicationTabDetails('');
  I.wait(FPLConstants.defaultPageClickWaitTime);
  caseViewOtherPage.clickChangeCaseName();
  I.wait(FPLConstants.defaultPageClickWaitTime);*/
  changeCaseViewOtherPage.verifyChangeCaseNameScreen(caseId);

  //@todo write code to test the Previous Button from the Change Case Name Check Your Answers Page...(Only if this is required by the team)
  I.wait(FPLConstants.defaultPageClickWaitTime);
  await changeCaseViewOtherPage.verifyChangeYourCaseNameCheckYourAnswersPage(caseId);

  I.wait(FPLConstants.defaultPageClickWaitTime);
  //@todo Sort out the I.uiFormatted method....
  caseViewOtherPage.verifyStartApplicationTabDetails('has been updated with event: Change case name');
  caseViewOtherPage.clickOrdersAndDirectionsLink();

  I.wait(FPLConstants.defaultPageClickWaitTime);
  ordersAndDirectionsOtherPage.verifyOrdersAndDirectionsPage(caseId);
  ordersAndDirectionsOtherPage.inputValuesForOrdersSought('orders_orderType-CARE_ORDER');
  ordersAndDirectionsOtherPage.clickContinueButton();
  I.wait(FPLConstants.defaultPageClickWaitTime);
  ordersAndDirectionsOtherPage.verifyOrdersAndDirections(caseId);
  ordersAndDirectionsOtherPage.clickSaveAndContinue();


  I.wait(FPLConstants.defaultPageClickWaitTime);
  caseViewOtherPage.verifyStartApplicationTabDetails('has been updated with event: Orders and directions sought');
  I.wait(FPLConstants.defaultPageClickWaitTime);
  caseViewOtherPage.clickHearingsUrgencyLink();
  I.wait(FPLConstants.defaultPageClickWaitTime);
  hearingUrgencyOtherPage.verifyHearingUrgencyPage();
  hearingUrgencyOtherPage.inputValuesForHearingUrgency();
  hearingUrgencyOtherPage.clickContinueButton();
  I.wait(FPLConstants.defaultPageClickWaitTime);
  hearingUrgencyOtherPage.verifyHearingUrgency();
  hearingUrgencyOtherPage.clickSaveAndContinue();
  I.wait(FPLConstants.defaultPageClickWaitTime);
  caseViewOtherPage.verifyStartApplicationTabDetails('has been updated with event: Hearing urgency');
  pause();


}).tag('@nightly @crossbrowser');

xScenario('Local authority Adds an Order to an Application', async ({I}) => {
  const authToken = await apiHelper.getAuthToken(config.swanseaLocalAuthorityUserOne);
  console.log('Auth Token : '+authToken);
  I.wait(1);

}).tag('@nightly @crossbrowser');

