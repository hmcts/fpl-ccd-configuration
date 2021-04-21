const config = require('../config.js');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');
const caseData = require('../fixtures/caseData/gatekeepingFullDetails.json');

const approvalDate = {year: 2021, month: 4, day: 9};
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
  assertOrder(I, caseViewPage, 1, 'C32 - Care order', approvalDate, allocatedJudge, 'Timothy Jones');
});

function assertOrder(I, caseViewPage, orderIndex, orderType, approvalDate, judge, children) {
  const orderElement = `Order ${orderIndex}`;
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab([orderElement, 'Order title'], orderType);
  I.seeInTab([orderElement, 'Approval date'], dateFormat(dateToString(approvalDate), 'd mmm yyyy'));
  I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], judge.title);
  I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Last name'], judge.name);
  I.seeInTab([orderElement, 'Children'], children);
}
