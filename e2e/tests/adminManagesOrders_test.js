const config = require('../config.js');
const dateFormat = require('dateformat');
const caseData = require('../fixtures/caseData/gatekeepinhWithPastHearingDetails.json');

const approvalDate = new Date(2021, 3, 9);
const allocatedJudge = {title: 'Her Honour Judge', name: 'Moley'};
const orderTitle = 'some title';
const today = new Date(Date.now());
const futureDate = new Date(Date.now() + (3600 * 1000 * 24));
const removalAddress = {buildingAndStreet: {lineOne: 'Flat 2 Caversham', town: 'Reading'}, postcode: 'RG4 7AA'};
let caseId;

Feature('HMCTS Admin manages orders');

BeforeSuite(async ({I}) => caseId = await I.submitNewCaseWithData(caseData));

Before(async ({I}) => await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId));

Scenario('Create C32 care order (with pre filled hearing details)', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c32);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.yes);
  await manageOrdersEventPage.selectHearing('Case management hearing, 3 November 2012');
  await I.goToNextPage();
  // Judge and approval date is already preFilled
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'C32 - Care order',
    approvalDate: new Date(2012, 10, 3),
    allocatedJudge: {title: 'Her Honour Judge', name: 'Reed', legalAdviserFullName: 'Jack Nickolson' },
    children: 'Timothy Jones',
  });
});

Scenario('Create EPO order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c23);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDateTime(today);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.selectEpoType(manageOrdersEventPage.section4.epoTypes.options.removeAccommodation);
  manageOrdersEventPage.selectIncludePhrase(manageOrdersEventPage.section4.includePhrase.options.yes);
  await manageOrdersEventPage.enterEPOEndDateTime(futureDate);
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'C23 - Emergency protection order',
    approvalDateTime: today,
    allocatedJudge: allocatedJudge,
    children: 'Timothy Jones',
  });
});

Scenario('Create EPO Prevent removal order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c23);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDateTime(today);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.selectEpoType(manageOrdersEventPage.section4.epoTypes.options.preventRemoval);
  manageOrdersEventPage.enterRemovalAddress(removalAddress);
  manageOrdersEventPage.selectExclusionRequirement(manageOrdersEventPage.section4.exclusionRequirement.options.yes);
  manageOrdersEventPage.enterWhoIsExcluded('John Doe');
  await manageOrdersEventPage.enterExclusionStartDate(approvalDate);
  manageOrdersEventPage.uploadPowerOfArrest(config.testPdfFile);
  manageOrdersEventPage.selectIncludePhrase(manageOrdersEventPage.section4.includePhrase.options.yes);
  await manageOrdersEventPage.enterEPOEndDateTime(futureDate);
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'C23 - Emergency protection order',
    approvalDateTime: today,
    allocatedJudge: allocatedJudge,
    children: 'Timothy Jones',
  });
});

Scenario('Create C21 blank order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c21);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
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
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 3,
    orderType: 'C21 - Blank order',
    orderTitle: orderTitle,
    approvalDate: approvalDate,
    allocatedJudge: allocatedJudge,
    children: 'Timothy Jones',
  });
});

Scenario('Create Supervision order (C35A)', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c35a);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterFurtherDirections('Supervision order further details.');
  await manageOrdersEventPage.selectSupervisionOrder(manageOrdersEventPage.section4.supervisionOrderType.options.numberOfMonths);
  await manageOrdersEventPage.enterSuperVisionNumOfMonths(12);
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 3,
    orderType: 'Supervision order (C35A)',
    approvalDate: today,
    allocatedJudge: allocatedJudge,
    children: 'Timothy Jones',
  });
});

function assertOrder(I, caseViewPage, order) {
  const orderElement = `Order ${order.orderIndex}`;
  const dateOfApproval = order.approvalDate !== undefined ? order.approvalDate : order.approvalDateTime;
  const mask = order.approvalDate !== undefined ? 'd mmm yyyy' : 'd mmm yyyy, h:MM:ss TT';

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab([orderElement, 'Type of order'], order.orderType);
  I.seeInTab([orderElement, 'Approval date'], dateFormat(dateOfApproval, mask));
  I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], order.allocatedJudge.title);
  I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Last name'], order.allocatedJudge.name);
  if (order.allocatedJudge.legalAdviserFullName) {
    I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Justices\' Legal Adviser\'s full name'], order.allocatedJudge.legalAdviserFullName);
  }
  I.seeInTab([orderElement, 'Children'], order.children);

  if (order.title !== undefined) {
    I.seeInTab([orderElement, 'Order title'], orderTitle);
  }
}
