const config = require('../config.js');

const ids = [
  '#allPartiesLabelCMO', '#localAuthorityDirectionsLabelCMO', '#respondentsDirectionLabelCMO',
  '#cafcassDirectionsLabelCMO', '#otherPartiesDirectionLabelCMO', '#courtDirectionsLabelCMO', '#orderBasisLabel',
  '#schedule_schedule', '#caseManagementOrder_status',
];

const allOtherPartyDetails = [ config.hmctsAdminUser, config.cafcassUser, config.judicaryUser ];

const assertCanSeeActionCMO = (I, caseViewPage, fileName) => {
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.see(fileName);
  I.seeInTab(['Order actions', 'Is this ready to be sent to parties?'], 'Yes, send this to all parties');
  I.seeInTab(['Order actions', 'What is the next hearing?'], 'Final hearing');
  I.seeInTab(['Case management orders 1', 'Date of issue'], '12 December 2019');
  I.seeInTab(['Case management orders 1', 'Which hearing is this order for?'], '1 Jan 2050');
  I.seeInTab(['Directions 1', 'Title'], 'Mock title');
  I.seeInTab(['Directions 1', 'Description'], 'Mock description');
  I.seeInTab(['Directions 1', 'For'], 'All parties');
  I.seeInTab(['Directions 1', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Directions 2', 'Title'], 'Mock title');
  I.seeInTab(['Directions 2', 'Description'], 'Mock description');
  I.seeInTab(['Directions 2', 'For'], 'Local Authority');
  I.seeInTab(['Directions 2', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Directions 3', 'Title'], 'Mock title');
  I.seeInTab(['Directions 3', 'Description'], 'Mock description');
  I.seeInTab(['Directions 3', 'For'], 'Parents and other respondents');
  I.seeInTab(['Directions 3', 'Assignee'], 'Respondent 1');
  I.seeInTab(['Directions 3', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Directions 4', 'Title'], 'Mock title');
  I.seeInTab(['Directions 4', 'Description'], 'Mock description');
  I.seeInTab(['Directions 4', 'For'], 'Cafcass');
  I.seeInTab(['Directions 4', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Directions 5', 'Title'], 'Mock title');
  I.seeInTab(['Directions 5', 'Description'], 'Mock description');
  I.seeInTab(['Directions 5', 'For'], 'Other parties');
  I.seeInTab(['Directions 5', 'Assignee'], 'Person 1');
  I.seeInTab(['Directions 5', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Directions 6', 'Title'], 'Mock title');
  I.seeInTab(['Directions 6', 'Description'], 'Mock description');
  I.seeInTab(['Directions 6', 'For'], 'Court');
  I.seeInTab(['Directions 6', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Recitals 1', 'Recital title'], 'Recital 1');
  I.seeInTab(['Schedule', 'Do you want to include a schedule?'], 'Yes');
  I.seeInTab(['Schedule', 'Allocation'], 'The proceedings continue to be allocated to Paul Wilson');
  I.seeInTab(['Schedule', 'Application'], 'The local authority has applied for a care order');
  I.seeInTab(['Schedule', 'Today\'s hearing'], 'Today\'s case was listed for an INTERIM CARE ORDER HEARING');
  I.seeInTab(['Schedule', 'Children\'s current arrangements'], 'Mock arrangement');
  I.seeInTab(['Schedule', 'Timetable for proceedings (26 weeks)'], '26 weeks');
  I.seeInTab(['Schedule', 'Timetable for the children'], '05/05/2005 is the child\'s DOB');
  I.seeInTab(['Schedule', 'Alternative carers'], 'Inform the local authority in writing within 7 days');
  I.seeInTab(['Schedule', 'Threshold'], 'The S.31 threshold for the making of orders is in dispute');
  I.seeInTab(['Schedule', 'Key issues'], 'Are there any other family or friends capable of caring in the children');
  I.seeInTab(['Schedule', 'Parties\' positions'], 'The mother agrees section 20');
  I.seeInTab(['Case management orders 1', 'Is this ready to be sent to the judge?'], 'Yes, send this to the judge');
  I.seeInTab(['Next Hearing', 'Which hearing is next?'], '1 Jan 2050');
};

const assertCanSeeDraftCMO = (I, caseViewPage, details = {}) => {
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.see('draft-case_management_order.pdf');

  if (details.orderActions) {
    I.seeInTab(['Order actions', 'Is this ready to be sent to parties?'], details.orderActions.type);
    I.seeInTab(['Order actions', 'What do they need to change?'], details.orderActions.reason);
  }

  if (details.hasIssuedDate) {
    I.seeInTab(['Case management order', 'Date of issue'], '12 December 2019');
  }

  I.seeInTab(['Case management order', 'Which hearing is this order for?'], '1 Jan 2050');
  I.seeInTab(['Directions 1', 'Title'], 'Mock title');
  I.seeInTab(['Directions 1', 'Description'], 'Mock description');
  I.seeInTab(['Directions 1', 'For'], 'All parties');
  I.seeInTab(['Directions 1', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Directions 2', 'Title'], 'Mock title');
  I.seeInTab(['Directions 2', 'Description'], 'Mock description');
  I.seeInTab(['Directions 2', 'For'], 'Local Authority');
  I.seeInTab(['Directions 2', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Directions 3', 'Title'], 'Mock title');
  I.seeInTab(['Directions 3', 'Description'], 'Mock description');
  I.seeInTab(['Directions 3', 'For'], 'Parents and other respondents');
  I.seeInTab(['Directions 3', 'Assignee'], 'Respondent 1');
  I.seeInTab(['Directions 3', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Directions 4', 'Title'], 'Mock title');
  I.seeInTab(['Directions 4', 'Description'], 'Mock description');
  I.seeInTab(['Directions 4', 'For'], 'Cafcass');
  I.seeInTab(['Directions 4', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Directions 5', 'Title'], 'Mock title');
  I.seeInTab(['Directions 5', 'Description'], 'Mock description');
  I.seeInTab(['Directions 5', 'For'], 'Other parties');
  I.seeInTab(['Directions 5', 'Assignee'], 'Person 1');
  I.seeInTab(['Directions 5', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Directions 6', 'Title'], 'Mock title');
  I.seeInTab(['Directions 6', 'Description'], 'Mock description');
  I.seeInTab(['Directions 6', 'For'], 'Court');
  I.seeInTab(['Directions 6', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Recitals 1', 'Recital title'], 'Recital 1');
  I.seeInTab(['Schedule', 'Do you want to include a schedule?'], 'Yes');
  I.seeInTab(['Schedule', 'Allocation'], 'The proceedings continue to be allocated to Paul Wilson');
  I.seeInTab(['Schedule', 'Application'], 'The local authority has applied for a care order');
  I.seeInTab(['Schedule', 'Today\'s hearing'], 'Today\'s case was listed for an INTERIM CARE ORDER HEARING');
  I.seeInTab(['Schedule', 'Children\'s current arrangements'], 'Mock arrangement');
  I.seeInTab(['Schedule', 'Timetable for proceedings (26 weeks)'], '26 weeks');
  I.seeInTab(['Schedule', 'Timetable for the children'], '05/05/2005 is the child\'s DOB');
  I.seeInTab(['Schedule', 'Alternative carers'], 'Inform the local authority in writing within 7 days');
  I.seeInTab(['Schedule', 'Threshold'], 'The S.31 threshold for the making of orders is in dispute');
  I.seeInTab(['Schedule', 'Key issues'], 'Are there any other family or friends capable of caring in the children');
  I.seeInTab(['Schedule', 'Parties\' positions'], 'The mother agrees section 20');
  I.seeInTab(['Case management order', 'Is this ready to be sent to the judge?'], details.status);
};

const assertCanSeeDraftCMODocument = (I, caseViewPage) => {
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.see('draft-case_management_order.pdf');
};

const assertUserCannotSeeDraftOrdersTab = async (I, user, caseId) => {
  await I.navigateToCaseDetailsAs(user, caseId);
  I.dontSee('Draft orders', '.tabs .tabs-list');
};

const assertUserCanSeeDraftCMODocument = async (I, user, caseViewPage, caseId) => {
  await I.navigateToCaseDetailsAs(user, caseId);
  assertCanSeeDraftCMODocument(I, caseViewPage);
};

const skipToReview = async (I) => {
  for (let id of ids) {
    await I.retryUntilExists(() => I.click('Continue'), id);
  }
};

const skipToSchedule = async (I) => {
  for (let id of ids.slice(1, ids.length - 1)) {
    await I.retryUntilExists(() => I.click('Continue'), id);
  }
};

const sendDraftForJudgeReview = async (I, draftCaseManagementOrderEventPage) => {
  await skipToReview(I);
  draftCaseManagementOrderEventPage.markToBeSentToJudge();
  await I.completeEvent('Submit');
};

const sendDraftForSelfReview = async (I, draftCaseManagementOrderEventPage) => {
  await skipToReview(I);
  draftCaseManagementOrderEventPage.markToReviewedBySelf();
  await I.completeEvent('Submit');
};

const sendDraftForPartyReview = async (I, draftCaseManagementOrderEventPage) => {
  await skipToReview(I);
  draftCaseManagementOrderEventPage.markToBeReviewedByParties();
  await I.completeEvent('Submit');
};

const actionDraft = async (I, actionCaseManagementOrderEventPage) => {
  await actionCaseManagementOrderEventPage.enterDateOfIssue({day: 12, month: 12, year: 2019});
  await skipToSchedule(I);
  await I.retryUntilExists(() => I.click('Continue'), '#orderAction_type');
  await actionCaseManagementOrderEventPage.markToBeSentToLocalAuthority();
  await I.completeEvent('Save and continue');
};

module.exports = {
  allOtherPartyDetails, skipToSchedule, skipToReview, assertCanSeeActionCMO, assertCanSeeDraftCMO,
  assertCanSeeDraftCMODocument, assertUserCannotSeeDraftOrdersTab, assertUserCanSeeDraftCMODocument,
  sendDraftForJudgeReview, sendDraftForSelfReview, sendDraftForPartyReview, actionDraft,
};
