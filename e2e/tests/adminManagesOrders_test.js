const config = require('../config.js');
const formatDate = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');
const caseData = require('../fixtures/caseData/gatekeepingFullDetails.json');

const dateTimeFormat = 'd mmm yyyy, h:MM:ss TT';
const dateFormat = 'd mmm yyyy';
const approvalDate = {year: 2021, month: 4, day: 9};
const today = new Date(Date.now());
const yesterday = new Date(Date.now() - (3600 * 1000 * 24));
const allocatedJudge = {title: 'Her Honour Judge', name: 'Moley'};
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
  assertOrder(I, caseViewPage, 1, 'C32 - Care order', formatDate(dateToString(approvalDate), dateFormat), allocatedJudge, 'Timothy Jones');
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
  await manageOrdersEventPage.selectEpoType(manageOrdersEventPage.section4.epoTypes.options.removeAccommodation);
  await manageOrdersEventPage.selectIncludePhrase(manageOrdersEventPage.section4.includePhrase.options.yes);
  await manageOrdersEventPage.enterEPOEndDateTime(yesterday);
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, 1, 'C23 - Emergency protection order', formatDate(today, dateTimeFormat), allocatedJudge, 'Timothy Jones');
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
  await manageOrdersEventPage.selectEpoType(manageOrdersEventPage.section4.epoTypes.options.preventRemoval);
  await manageOrdersEventPage.clickPostCodeLink('I can\'t enter a UK postcode');
  await manageOrdersEventPage.enterRemovalAddress('Flat 2 Caversham', 'RG4 7AA');
  await manageOrdersEventPage.selectExclusionRequirement(manageOrdersEventPage.section4.exclusionRequirement.options.yes);
  await manageOrdersEventPage.enterWhoIsExcluded('John Doe');
  await manageOrdersEventPage.enterExclusionStartDate(approvalDate);
  await manageOrdersEventPage.uploadPowerOfArrest(config.testPdfFile);

  await manageOrdersEventPage.selectIncludePhrase(manageOrdersEventPage.section4.includePhrase.options.yes);
  await manageOrdersEventPage.enterEPOEndDateTime(yesterday);
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, 1, 'C23 - Emergency protection order', formatDate(today, dateTimeFormat), allocatedJudge, 'Timothy Jones');
});

function assertOrder(I, caseViewPage, orderIndex, orderType, approvalDate, judge, children) {
  const orderElement = `Order ${orderIndex}`;
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab([orderElement, 'Order title'], orderType);
  I.seeInTab([orderElement, 'Approval date'], approvalDate);
  I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], judge.title);
  I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Last name'], judge.name);
  I.seeInTab([orderElement, 'Children'], children);
}
