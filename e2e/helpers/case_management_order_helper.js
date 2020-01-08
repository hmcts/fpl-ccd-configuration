const config = require('../config.js');

const ids = [
  '#allPartiesLabelCMO', '#localAuthorityDirectionsLabelCMO', '#respondentsDirectionLabelCMO',
  '#cafcassDirectionsLabelCMO', '#otherPartiesDirectionLabelCMO', '#courtDirectionsLabelCMO', '#orderBasisLabel',
  '#schedule_schedule', '#caseManagementOrder_status',
];

const allOtherPartyDetails = [
  {
    email: config.hmctsAdminEmail,
    password: config.hmctsAdminPassword,
  },
  {
    email: config.cafcassEmail,
    password: config.cafcassPassword,
  },
  {
    email: config.judiciaryEmail,
    password: config.judiciaryPassword,
  }];


const assertCanSeeActionCMO = (I, caseViewPage, fileName) => {
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.see(fileName);
  I.seeAnswerInTab(1, 'Order actions', 'Is this ready to be sent to parties?', 'Yes, send this to all parties');
  I.seeAnswerInTab(2, 'Order actions', 'What is the next hearing?', 'Final hearing');
  I.seeAnswerInTab(3, 'Case management orders 1', 'Which hearing is this order for?', '1 Jan 2050');
  I.seeAnswerInTab(1, 'Directions 1', 'Title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 1', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 1', 'For', 'All parties');
  I.seeAnswerInTab(6, 'Directions 1', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 2', 'Title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 2', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 2', 'For', 'Local Authority');
  I.seeAnswerInTab(6, 'Directions 2', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 3', 'Title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 3', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 3', 'For', 'Parents and other respondents');
  I.seeAnswerInTab(6, 'Directions 3', 'Assignee', 'Respondent 1');
  I.seeAnswerInTab(7, 'Directions 3', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 4', 'Title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 4', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 4', 'For', 'Cafcass');
  I.seeAnswerInTab(6, 'Directions 4', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 5', 'Title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 5', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 5', 'For', 'Other parties');
  I.seeAnswerInTab(6, 'Directions 5', 'Assignee', 'Person 1');
  I.seeAnswerInTab(7, 'Directions 5', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 6', 'Title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 6', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 6', 'For', 'Court');
  I.seeAnswerInTab(6, 'Directions 6', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Recitals 1', 'Recital title', 'Recital 1');
  I.seeAnswerInTab(1, 'Schedule', 'Do you want to include a schedule?', 'Yes');
  I.seeAnswerInTab(2, 'Schedule', 'Allocation', 'The proceedings continue to be allocated to Paul Wilson');
  I.seeAnswerInTab(3, 'Schedule', 'Application', 'The local authority has applied for a care order');
  I.seeAnswerInTab(4, 'Schedule', 'Today\'s hearing', 'Today\'s case was listed for an INTERIM CARE ORDER HEARING');
  I.seeAnswerInTab(5, 'Schedule', 'Children\'s current arrangements', 'Mock arrangement');
  I.seeAnswerInTab(6, 'Schedule', 'Timetable for proceedings (26 weeks)', '26 weeks');
  I.seeAnswerInTab(7, 'Schedule', 'Timetable for the children', '05/05/2005 is the child\'s DOB');
  I.seeAnswerInTab(8, 'Schedule', 'Alternative carers', 'Inform the local authority in writing within 7 days');
  I.seeAnswerInTab(9, 'Schedule', 'Threshold', 'The S.31 threshold for the making of orders is in dispute');
  I.seeAnswerInTab(10, 'Schedule', 'Key issues', 'Are there any other family or friends capable of caring in the children');
  I.seeAnswerInTab(11, 'Schedule', 'Parties\' positions', 'The mother agrees section 20');
  I.seeAnswerInTab(8, 'Case management orders 1', 'Is this ready to be sent to the judge?', 'Yes, send this to the judge');
  I.seeAnswerInTab(1, 'Next Hearing', 'Which hearing is next?', '1 Jan 2050');
};

const assertCanSeeDraftCMO = (I, caseViewPage, cmoStatus) => {
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.see('draft-case_management_order.pdf');
  I.seeAnswerInTab(2, 'Case management order', 'Which hearing is this order for?', '1 Jan 2050');
  I.seeAnswerInTab(1, 'Directions 1', 'Title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 1', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 1', 'For', 'All parties');
  I.seeAnswerInTab(6, 'Directions 1', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 2', 'Title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 2', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 2', 'For', 'Local Authority');
  I.seeAnswerInTab(6, 'Directions 2', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 3', 'Title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 3', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 3', 'For', 'Parents and other respondents');
  I.seeAnswerInTab(6, 'Directions 3', 'Assignee', 'Respondent 1');
  I.seeAnswerInTab(7, 'Directions 3', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 4', 'Title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 4', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 4', 'For', 'Cafcass');
  I.seeAnswerInTab(6, 'Directions 4', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 5', 'Title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 5', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 5', 'For', 'Other parties');
  I.seeAnswerInTab(6, 'Directions 5', 'Assignee', 'Person 1');
  I.seeAnswerInTab(7, 'Directions 5', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 6', 'Title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 6', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 6', 'For', 'Court');
  I.seeAnswerInTab(6, 'Directions 6', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Recitals 1', 'Recital title', 'Recital 1');
  I.seeAnswerInTab(1, 'Schedule', 'Do you want to include a schedule?', 'Yes');
  I.seeAnswerInTab(2, 'Schedule', 'Allocation', 'The proceedings continue to be allocated to Paul Wilson');
  I.seeAnswerInTab(3, 'Schedule', 'Application', 'The local authority has applied for a care order');
  I.seeAnswerInTab(4, 'Schedule', 'Today\'s hearing', 'Today\'s case was listed for an INTERIM CARE ORDER HEARING');
  I.seeAnswerInTab(5, 'Schedule', 'Children\'s current arrangements', 'Mock arrangement');
  I.seeAnswerInTab(6, 'Schedule', 'Timetable for proceedings (26 weeks)', '26 weeks');
  I.seeAnswerInTab(7, 'Schedule', 'Timetable for the children', '05/05/2005 is the child\'s DOB');
  I.seeAnswerInTab(8, 'Schedule', 'Alternative carers', 'Inform the local authority in writing within 7 days');
  I.seeAnswerInTab(9, 'Schedule', 'Threshold', 'The S.31 threshold for the making of orders is in dispute');
  I.seeAnswerInTab(10, 'Schedule', 'Key issues', 'Are there any other family or friends capable of caring in the children');
  I.seeAnswerInTab(11, 'Schedule', 'Parties\' positions', 'The mother agrees section 20');
  I.seeAnswerInTab(7, 'Case management order', 'Is this ready to be sent to the judge?', cmoStatus);
};

const assertCanSeeDraftCMODocument = (I, caseViewPage) => {
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.see('draft-case_management_order.pdf');
};

const assertUserCannotSeeDraftOrdersTab = async (I, userDetails, caseId) => {
  await switchUserAndNavigateToCase(I, userDetails, caseId);
  I.dontSee('Draft orders', '.tabs .tabs-list');
};

const assertUserCanSeeDraftCMODocument = async (I, userDetails, caseViewPage, caseId) => {
  await switchUserAndNavigateToCase(I, userDetails, caseId);
  assertCanSeeDraftCMODocument(I, caseViewPage);
};

const switchUserAndNavigateToCase = async (I, userDetails, caseId) => {
  I.signOut();
  await I.signIn(userDetails.email, userDetails.password);
  await I.navigateToCaseDetails(caseId);
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
  await skipToSchedule(I);
  await I.retryUntilExists(() => I.click('Continue'), '#orderAction_type');
  actionCaseManagementOrderEventPage.markToBeSentToLocalAuthority();
  await I.completeEvent('Save and continue');
};

module.exports = {
  allOtherPartyDetails, skipToSchedule, skipToReview, assertCanSeeActionCMO, assertCanSeeDraftCMO,
  assertCanSeeDraftCMODocument, assertUserCannotSeeDraftOrdersTab, assertUserCanSeeDraftCMODocument,
  switchUserAndNavigateToCase, sendDraftForJudgeReview, sendDraftForSelfReview, sendDraftForPartyReview,
  actionDraft,
};
