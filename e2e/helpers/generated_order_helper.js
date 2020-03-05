const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');

const createBlankOrder = async (I, createOrderEventPage, order) => {
  await createOrderEventPage.selectType(order.type);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await selectChildren(I, createOrderEventPage, order);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.title);
  await createOrderEventPage.enterC21OrderDetails();
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.judgeAndLegalAdvisorTitleId);
  await createOrderEventPage.enterJudgeAndLegalAdvisor(order.judgeAndLegalAdvisor.judgeLastName, order.judgeAndLegalAdvisor.legalAdvisorName);
  await I.completeEvent('Save and continue');
};

const createCareOrder = async (I, createOrderEventPage, order) => {
  await createOrderEventPage.selectType(order.type, order.subtype);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await selectChildren(I, createOrderEventPage, order);
  if (order.subtype === 'Interim') {
    await fillInterimEndDate(I, createOrderEventPage, order);
  }
  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await createOrderEventPage.enterJudgeAndLegalAdvisor(order.judgeAndLegalAdvisor.judgeLastName, order.judgeAndLegalAdvisor.legalAdvisorName);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.directionsNeeded.id);
  await createOrderEventPage.enterDirections('example directions');
  await I.completeEvent('Save and continue');
};

const createSupervisionOrder = async (I, createOrderEventPage, order) => {
  await createOrderEventPage.selectType(order.type, order.subtype);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await selectChildren(I, createOrderEventPage, order);
  if (order.subtype === 'Final') {
    await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.months);
    await createOrderEventPage.enterNumberOfMonths(order.months);
  } else {
    await fillInterimEndDate(I, createOrderEventPage, order);
  }
  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await createOrderEventPage.enterJudgeAndLegalAdvisor(order.judgeAndLegalAdvisor.judgeLastName, order.judgeAndLegalAdvisor.legalAdvisorName, order.judgeAndLegalAdvisor.judgeTitle);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.directionsNeeded.id);
  await createOrderEventPage.enterDirections('example directions');
  await I.completeEvent('Save and continue');
};

const createEmergencyProtectionOrder = async (I, createOrderEventPage, order) => {
  const tomorrow = new Date(Date.now() + (3600 * 1000 * 24));

  await createOrderEventPage.selectType(order.type);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await selectChildren(I, createOrderEventPage, order);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.epo.childrenDescription.radioGroup);
  await createOrderEventPage.enterChildrenDescription(order.childrenDescription);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.epo.type);
  createOrderEventPage.selectEpoType(order.epoType);
  createOrderEventPage.enterRemovalAddress(order.removalAddress);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.epo.includePhrase);
  createOrderEventPage.includePhrase(order.includePhrase);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.epo.endDate.id);
  createOrderEventPage.enterEpoEndDate(tomorrow);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.judgeAndLegalAdvisorTitleId);
  await createOrderEventPage.enterJudgeAndLegalAdvisor(order.judgeAndLegalAdvisor.judgeLastName, order.judgeAndLegalAdvisor.legalAdvisorName);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.directionsNeeded.id);
  await createOrderEventPage.enterDirections('example directions');
  await I.completeEvent('Save and continue');
};

const fillInterimEndDate = async (I, createOrderEventPage, order) => {
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.interimEndDate.id);
  if (order.interimEndDate.isNamedDate) {
    await createOrderEventPage.selectAndEnterNamedDate(order.interimEndDate.endDate);
  } else {
    await createOrderEventPage.selectEndOfProceedings();
  }
};

const fillDateOfIssue = async (I, createOrderEventPage, order) => {
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.dateOfIssue.id);
  await createOrderEventPage.enterDateOfIssue(order.dateOfIssue);
};

const selectChildren = async (I, createOrderEventPage, order) => {
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.allChildren.id);
  if (order.children === 'All') {
    await createOrderEventPage.useAllChildren();
  } else {
    await createOrderEventPage.notAllChildren();
    await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.childSelector.id);
    await createOrderEventPage.selectChildren(order.children);
  }
};

module.exports = {
  async createOrder(I, createOrderEventPage, order) {
    switch (order.type) {
      case 'Blank order (C21)':
        await createBlankOrder(I, createOrderEventPage, order);
        break;
      case 'Care order':
        await createCareOrder(I, createOrderEventPage, order);
        break;
      case 'Supervision order':
        await createSupervisionOrder(I, createOrderEventPage, order);
        break;
      case 'Emergency protection order':
        await createEmergencyProtectionOrder(I, createOrderEventPage, order);
        break;
    }
  },

  async assertOrder(I, caseViewPage, order, orderNum) {
    const orderHeading = 'Order ' + orderNum;
    caseViewPage.selectTab(caseViewPage.tabs.orders);
    I.seeAnswerInTab(1, orderHeading, 'Type of order', order.fullType);

    if (order.type === 'Blank order (C21)') {
      I.seeAnswerInTab(2, orderHeading, 'Order title', order.title);
      I.seeAnswerInTab(4, orderHeading, 'Order document', order.document);
      I.seeAnswerInTab(5, orderHeading, 'Date of issue', dateFormat(new Date(), 'd mmmm yyyy'));
    } else {
      I.seeAnswerInTab(2, orderHeading, 'Order document', order.document);
      I.seeAnswerInTab(3, orderHeading, 'Date of issue', dateFormat(dateToString(order.dateOfIssue), 'd mmmm yyyy'));
    }

    I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', order.judgeAndLegalAdvisor.judgeTitle);
    I.seeAnswerInTab(2, 'Judge and legal advisor', 'Last name', order.judgeAndLegalAdvisor.judgeLastName);
    I.seeAnswerInTab(3, 'Judge and legal advisor', 'Legal advisor\'s full name', order.judgeAndLegalAdvisor.legalAdvisorName);
  },

  async assertOrderSentToParty(I, caseViewPage, partyName, order, orderNum) {
    caseViewPage.selectTab(caseViewPage.tabs.documentsSentToParties);
    I.seeAnswerInTab(1, 'Party 1', 'Representative name', partyName);
    I.seeNestedAnswerInTab(2, 'Party 1', `Document ${orderNum}`, 'File', order.document);
  },
};
