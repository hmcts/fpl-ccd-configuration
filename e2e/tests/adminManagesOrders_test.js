const config = require('../config.js');
const dateFormat = require('dateformat');
const caseData = require('../fixtures/caseData/gatekeepingFullDetails.json');

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

Scenario('Create C32 care order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c32);
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
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
    approvalDate: approvalDate,
    allocatedJudge: allocatedJudge,
    children: 'Timothy Jones',
  });
});

Scenario('Create EPO order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c23);
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

Scenario('Create C35a Supervision order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c35A);
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterFurtherDirections('Supervision order further details.');
  await manageOrdersEventPage.selectOrderTypeWithMonth(manageOrdersEventPage.section4.orderTypeWithMonth.options.numberOfMonths);
  await manageOrdersEventPage.enterSuperVisionNumOfMonths(10);
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

Scenario('Create Interim care order (C33)', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c33);
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select,[0]);
  await I.goToNextPage();
  await manageOrdersEventPage.selectExclusionRequirementICO(manageOrdersEventPage.section4.ICOExclusionRequirement.options.yes);
  await manageOrdersEventPage.enterExclusionDetails('I need an exclusion because of X,Y and Z');
  await manageOrdersEventPage.enterFurtherDirections('Further details.');
  await manageOrdersEventPage.selectOrderTypeWithEndOfProceedings(manageOrdersEventPage.section4.orderTypeWithEndOfProceedings.options.endOfProceedings);
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I,caseViewPage,{
    orderIndex:3,
    orderType:manageOrdersEventPage.orders.title.c33,
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
  I.seeInTab([orderElement, 'Children'], order.children);

  if (order.title !== undefined) {
    I.seeInTab([orderElement, 'Order title'], orderTitle);
  }
}
