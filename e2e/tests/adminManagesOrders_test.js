const config = require('../config.js');
const dateFormat = require('dateformat');
const moment = require('moment');
const caseData = require('../fixtures/caseData/gatekeepingWithPastHearingDetails.json');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const caseDataWithApplication = require('../fixtures/caseData/gatekeepingWithPastHearingDetailsAndApplication.json');
const closedCaseData = require('../fixtures/caseData/closedCase.json');

const orderTitle = 'some title';
const now = moment().toDate();
const today = moment(now).subtract(2, 'hours').minutes(5).seconds(23).milliseconds(0).toDate();
const aYearAgo = moment(today).subtract(1, 'years').toDate();
const futureDate = moment(today).add(1, 'days').toDate();
const removalAddress = { buildingAndStreet: { lineOne: 'Flat 2 Caversham', town: 'Reading' }, postcode: 'RG4 7AA' };
const applicationToLink = 'C2, 16 June 2021, 11:49am';

let approvalDate = moment().year(2021).month(3).day(9).hours(10).minutes(30).seconds(15).milliseconds(0).toDate();

Feature('HMCTS Admin manages orders');

async function setupScenario(I, caseViewPage) {
  let caseId = await I.submitNewCaseWithData(caseData);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);
  approvalDate = moment(approvalDate).add(1, 'days').toDate();
}

Scenario('@prabha Create C32A care order (with pre filled hearing details)', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c32);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.yes);
  await manageOrdersEventPage.selectHearing('Case management hearing, 3 November 2012');
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();

  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.yes);
  await I.goToNextPage();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();

  await I.goToNextPage();
  //await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Care order (C32A)',
    approvalDate: approvalDate,
  //  others: 'John Doe',
  });
});

Scenario('Create 32b discharge of care order @nightlyOnly', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c32b);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterCareOrderIssuedDate(aYearAgo);
  manageOrdersEventPage.enterCareOrderIssuedVenue(hearingDetails[0]);
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await manageOrdersEventPage.selectIsFinalOrder();
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  // await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Discharge of care order (C32B)',
    approvalDate: approvalDate,
  });
});

Scenario(' Create EPO order @xBrowser', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  const newCaseId = await I.submitNewCaseWithData(caseData);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, newCaseId);
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c23);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  // need to be removed
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDateTime(today);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.selectEpoType(manageOrdersEventPage.section4.epoTypes.options.removeAccommodation);
  manageOrdersEventPage.selectIncludePhrase(manageOrdersEventPage.section4.includePhrase.options.yes);
  await manageOrdersEventPage.enterEPOEndDateTime(futureDate);
  // enter is it final order or not
  await manageOrdersEventPage.selectIsFinalOrder();
  // await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();

  await manageOrdersEventPage.checkPreview();
  //answer order close application

  await manageOrdersEventPage.selectCloseCase();
  // await I.selectCloseCase();
  await I.goToNextPage();
  //await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  //await I.goToNextPage();
  await I.see('Check your answers');

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);

  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Emergency protection order (C23)',
    approvalDateTime: today,
    // others: 'John Doe',
  });
}).tag('@test');
// need to verify the test - test fails on event submission
xScenario('Create EPO Prevent removal order @nightlyOnly', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  const newCaseId = await I.submitNewCaseWithData(caseData);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, newCaseId);
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c23);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDateTime(today);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.selectEpoType(manageOrdersEventPage.section4.epoTypes.options.preventRemoval);
  await manageOrdersEventPage.enterRemovalAddress(removalAddress);
  manageOrdersEventPage.selectExclusionRequirementEPO(manageOrdersEventPage.section4.exclusionRequirement.options.yes);
  manageOrdersEventPage.enterWhoIsExcluded('John Doe');
  await manageOrdersEventPage.enterExclusionStartDate(approvalDate);
  manageOrdersEventPage.uploadPowerOfArrest(config.testPdfFile);
  manageOrdersEventPage.selectIncludePhrase(manageOrdersEventPage.section4.includePhrase.options.yes);
  await manageOrdersEventPage.enterEPOEndDateTime(futureDate);
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await manageOrdersEventPage.selectIsFinalOrder();
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  // await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Emergency protection order (C23)',
    approvalDateTime: today,
    // others: 'John Doe',
  });
});

Scenario('Create C21 blank order', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c21);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.enterTitle(orderTitle);
  await manageOrdersEventPage.enterDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  // await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Blank order (C21)',
    orderTitle: orderTitle,
    approvalDate: approvalDate,
    // others: 'John Doe',
  });
});

Scenario('Create C21 blank order in closed case @nightlyOnly', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  const newCaseId = await I.submitNewCaseWithData(closedCaseData);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, newCaseId);

  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  I.dontSee('Upload an order');
  await manageOrdersEventPage.selectOperationInClosedState(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.enterTitle(orderTitle);
  await manageOrdersEventPage.enterDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  // await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Blank order (C21)',
    orderTitle: orderTitle,
    approvalDate: approvalDate,
    // others: 'John Doe',
  });
});

xScenario(' Create Recovery of a child (C29)', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c29);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.selectWhichOrder(manageOrdersEventPage.section4.whichOrder.options.epo);
  await manageOrdersEventPage.enterOrderMadeDate(approvalDate);
  await manageOrdersEventPage.selectOrderPermissions(manageOrdersEventPage.section4.orderPermissions.options.inform);
  await manageOrdersEventPage.selectOrderPermissions(manageOrdersEventPage.section4.orderPermissions.options.produce);
  await manageOrdersEventPage.selectOrderPermissions(manageOrdersEventPage.section4.orderPermissions.options.remove);
  await manageOrdersEventPage.enterRemovalAddress(removalAddress);
  await manageOrdersEventPage.selectIsExparte();
  await manageOrdersEventPage.enterOfficerName('Officer Barbrady');
  await manageOrdersEventPage.selectIsFinalOrder();

  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  // await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: manageOrdersEventPage.orders.title.c29,
    orderTitle: orderTitle,
    approvalDate: approvalDate,
    // others: 'John Doe',
  });
});

Scenario('Create C35a Supervision order @nightlyOnly', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c35A);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterFurtherDirections('Supervision order further details.');
  await manageOrdersEventPage.selectOrderTypeWithMonth(manageOrdersEventPage.section4.orderTypeWithMonth.options.numberOfMonths);
  await manageOrdersEventPage.enterSuperVisionNumOfMonths(12);
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  // await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Supervision order (C35A)',
    approvalDate: approvalDate,
    // others: 'John Doe',
  });
});

Scenario('Create Interim care order (C33) @nightlyOnly', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c33);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.selectExclusionRequirementICO(manageOrdersEventPage.section4.exclusionRequirement.options.yes);
  await manageOrdersEventPage.enterExclusionDetails('I need an exclusion because of X,Y and Z');
  await manageOrdersEventPage.enterFurtherDirections('Further details.');
  await manageOrdersEventPage.selectOrderTypeWithEndOfProceedings(manageOrdersEventPage.section4.orderTypeWithEndOfProceedings.options.endOfProceedings);
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  //await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: manageOrdersEventPage.orders.title.c33,
    approvalDate: approvalDate,
    //others: 'John Doe',
  });
});

xScenario('Interim supervision order (C35B) @nightlyOnly', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c35B);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterFurtherDirections('Further details.');
  await manageOrdersEventPage.selectOrderTypeWithEndOfProceedings(manageOrdersEventPage.section4.orderTypeWithEndOfProceedings.options.endOfProceedings);
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: manageOrdersEventPage.orders.title.c35B,
    approvalDate: approvalDate,
  //  others: 'John Doe',
  });
});

xScenario('Create C43a special guardianship order @nightlyOnly', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c43a);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDateTime(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.selectOrderByConsent();
  await manageOrdersEventPage.selectGuardian([0]);
  await manageOrdersEventPage.enterFurtherDirections('Further special guardianship details.');
  await manageOrdersEventPage.selectIsFinalOrder();
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Special guardianship order (C43A)',
    orderTitle: orderTitle,
    approvalDate: approvalDate,
    specialGuardian: 'Joe Bloggs',
  });
});

xScenario('Create Child arrangements, Specific issue, Prohibited steps (C43) @nightlyOnly', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c43);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select,[0]);
  await I.goToNextPage();
  await manageOrdersEventPage.selectC43Orders();
  manageOrdersEventPage.selectOrderByConsent();
  await manageOrdersEventPage.enterRecitalsAndPreambles('Recitals and Preambles');
  await manageOrdersEventPage.enterDirections('some text');
  await manageOrdersEventPage.enterFurtherDirections('Further details.');
  await manageOrdersEventPage.selectIsFinalOrder();
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I,caseViewPage,{
    orderIndex: 1,
    orderType: manageOrdersEventPage.orders.title.c43,
    approvalDate: approvalDate,
    others: 'John Doe',
  });
});

xScenario('Create C47A appointment of a Children\'s Guardian @nightlyOnly', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  const newCaseId = await I.submitNewCaseWithData(caseDataWithApplication);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, newCaseId);

  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c47a);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);

  manageOrdersEventPage.linkApplication(applicationToLink);

  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  manageOrdersEventPage.selectCafcassRegion('ENGLAND');
  manageOrdersEventPage.selectEnglandOffice('Hull');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Appointment of a children\'s guardian (C47A)',
    orderTitle: orderTitle,
    approvalDate: approvalDate,
    others: 'John Doe',
  });
});

xScenario('Upload Manual order (other order) @nightlyOnly', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.upload);
  await I.goToNextPage();
  await manageOrdersEventPage.selectUploadOrder(manageOrdersEventPage.orders.options.other);
  manageOrdersEventPage.specifyOtherOrderTitle('Order F789s');
  await I.goToNextPage();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.uploadManualOrder(config.testPdfFile);
  manageOrdersEventPage.selectManualOrderNeedSealing(manageOrdersEventPage.section4.manualOrderNeedSealing.options.yes);
  await manageOrdersEventPage.selectIsFinalOrder();
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Other',
    orderTitle: 'Order F789s',
    approvalDate: approvalDate,
  });
});

xScenario('Create (C26) Secure accommodation order (deprivation of liberty) @nightlyOnly', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  const newCaseId = await I.submitNewCaseWithData(caseDataWithApplication);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, newCaseId);

  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c26);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.yes);
  await manageOrdersEventPage.selectHearing('Case management hearing, 3 November 2012');
  manageOrdersEventPage.linkApplication(applicationToLink);
  await I.goToNextPage();
  await manageOrdersEventPage.enterApprovalDateTime(approvalDate);
  await I.goToNextPage();

  await manageOrdersEventPage.selectSingleChild('Timothy Jones');

  I.see(manageOrdersEventPage.orders.title.c26);
  manageOrdersEventPage.selectOrderByConsent('Yes');
  manageOrdersEventPage.selectReasonForSecureAccommodation('ABSCOND');
  manageOrdersEventPage.selectWhetherChildIsRepresented('Yes');
  manageOrdersEventPage.selectJurisdiction('ENGLAND');
  manageOrdersEventPage.selectOrderTypeWithMonth(manageOrdersEventPage.section4.orderTypeWithMonth.options.numberOfMonths);
  manageOrdersEventPage.enterSuperVisionNumOfMonths(12);
  manageOrdersEventPage.selectIsFinalOrder();
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();

  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Authority to keep a child in secure accommodation (C26)',
    approvalDate: approvalDate,
    documentName: 'c26_secure_accommodation_order.pdf',
    others: 'John Doe',
  });
});

xScenario('Create Parental responsibility order (C45A) @nightlyOnly', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c45a);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDateTime(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.selectOrderByConsent();
  await manageOrdersEventPage.enterNameOfParentResponsible('T.J. Detweiler');
  await manageOrdersEventPage.selectFatherAsResponsible();
  await manageOrdersEventPage.enterFurtherDirections('Further details. '.repeat(10));
  await manageOrdersEventPage.selectIsFinalOrder();
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Parental responsibility order (C45A)',
    approvalDate: approvalDate,
    //others: 'John Doe',
  });
});

function assertOrder(I, caseViewPage, order) {
  const orderElement = `Order ${order.orderIndex}`;
  const dateOfApproval = order.approvalDate !== undefined ? order.approvalDate : order.approvalDateTime;
  const mask = order.approvalDate !== undefined ? 'd mmm yyyy' : 'd mmm yyyy, h:MM:ss TT';
  //pause();
  //I.waitForNavigation();
  //I.wait(10);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab([orderElement, 'Type of order'], order.orderType);
  I.seeInTab([orderElement, 'Approval date'], dateFormat(dateOfApproval, mask));
  // Judge details will be removed anyway based on https://tools.hmcts.net/jira/browse/FPLA-3084
  if (order.allocatedJudge !== undefined) {
    I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], order.allocatedJudge.title);
    I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Last name'], order.allocatedJudge.name);

    if (order.allocatedJudge.legalAdviserFullName !== undefined) {
      I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Justices\' Legal Adviser\'s full name'], order.allocatedJudge.legalAdviserFullName);
    }
  }

  if (order.specialGuardian !== undefined) {
    I.seeInTab([orderElement, 'Special guardians'], order.specialGuardian);
  }
  if (order.children !== undefined){
    I.seeInTab([orderElement, 'Children'], order.children);
  }

  if (order.others !== undefined){
    I.seeInTab([orderElement, 'Others notified'], order.others);
  }
  if (order.title !== undefined) {
    I.seeInTab([orderElement, 'Order title'], orderTitle);
  }
  if (order.documentName !== undefined) {
    I.seeInTab([orderElement, 'Order document'], order.documentName);
  }
}
