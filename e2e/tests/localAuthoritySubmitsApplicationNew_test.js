//const config = require('../config.js');
//const fs = require('fs');

//const apiHelper = require('../helpers/api_helper.js');
const caseWithSubmissionFormFields = require('../fixtures/caseData/mandatorySubmissionFields.json');
const caseInGateKeepingState = require('../fixtures/caseData/gatekeepingFullDetails.json');
const caseOpenStateWithMandatoryAndMaxChildren = require('../fixtures/caseData/mandatoryWithMaxChildren.json');


//const pathTo_ordersAndDirectionsNeeded = 'e2e/fixtures/caseData/data_ordersAndDirectionsNeeded.json';
// const children = require('../fixtures/children.js');
// const respondents = require('../fixtures/respondents.js');
// const applicant = require('../fixtures/applicant.js');
// const localAuthority = require('../fixtures/localAuthority.js');
// const others = require('../fixtures/others.js');
// const otherProceedings = require('../fixtures/otherProceedingData');
// const ordersAndDirectionsNeeded = require('../fixtures/ordersAndDirectionsNeeded.js');


//let caseId;

Feature('local kasi local');

// async function setupScenario(I, reuse = true) {
//   let navigateCaseId;
//   if (reuse == false) {
//     navigateCaseId = await I.submitNewCase(config.swanseaLocalAuthorityUserOne);
//   } else {
//     if (!caseId) {
//       caseId = await I.submitNewCase(config.swanseaLocalAuthorityUserOne);
//     }
//     navigateCaseId = caseId;
//   }
//   return navigateCaseId;
// }
//
// async function updateCaseWithEvent(I, user, eventName,caseId) {
//
//   const data = fs.readFileSync(pathTo_ordersAndDirectionsNeeded);
//   //console.log( ` |||||||||      data received from the FS is ....${JSON.stringify(data)}`);
//   const responseFromUpdateCall = apiHelper.updateCase(user,'orders-needed',data,caseId);
//   return responseFromUpdateCall;
//
// }

xScenario('openCase kasi', async ({I}) => {
  // caseId = await setupScenario(I);
  // console.log(` ||||||||||   user should be LA ..check this... ${config.swanseaLocalAuthorityUserOne.email}`);
  // const finalResp1 = updateCaseWithEvent(I,config.swanseaLocalAuthorityUserOne,'ordersNeeded',caseId);

  const newCaseId = await I.submitNewCaseWithData(caseOpenStateWithMandatoryAndMaxChildren);

  console.log(' New  Case in Open State  .....' + newCaseId);
  // await caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  // await caseViewPage.startTask(config.applicationActions.submitCase);
  //
  // submitApplicationEventPage.seeDraftApplicationFile();
  // await submitApplicationEventPage.giveConsent();
  // await I.completeEvent('Submit', null, true);
  //
  // I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  // caseViewPage.selectTab(caseViewPage.tabs.furtherEvidence);
  // I.see('New_case_name.pdf');
});

xScenario('script-submitted', async ({I}) => {
  // caseId = await setupScenario(I);
  // console.log(` ||||||||||   user should be LA ..check this... ${config.swanseaLocalAuthorityUserOne.email}`);
  // const finalResp1 = updateCaseWithEvent(I,config.swanseaLocalAuthorityUserOne,'ordersNeeded',caseId);

  const newCaseId = await I.submitNewCaseWithData(caseWithSubmissionFormFields);

  console.log(' New Case in Submitted STATE  .....' + newCaseId);
  // await caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  // await caseViewPage.startTask(config.applicationActions.submitCase);
  //
  // submitApplicationEventPage.seeDraftApplicationFile();
  // await submitApplicationEventPage.giveConsent();
  // await I.completeEvent('Submit', null, true);
  //
  // I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  // caseViewPage.selectTab(caseViewPage.tabs.furtherEvidence);
  // I.see('New_case_name.pdf');
});

Scenario('script-gatekeeping', async ({I}) => {
  const newCaseId = await I.submitNewCaseWithData(caseInGateKeepingState);
  console.log('Case in GateKeeping STATE  .....' + newCaseId);
});
