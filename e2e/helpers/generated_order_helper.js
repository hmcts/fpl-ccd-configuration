const dateFormat = require('dateformat');

const createBlankOrder = async (I, createOrderEventPage, order) => {
  await createOrderEventPage.selectType(order.type);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.title);
  await createOrderEventPage.enterC21OrderDetails();
  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await createOrderEventPage.enterJudgeAndLegalAdvisor(order.judgeAndLegalAdvisor.judgeLastName, order.judgeAndLegalAdvisor.legalAdvisorName);
  await I.completeEvent('Save and continue');
};

const createCareOrder = async (I, createOrderEventPage, order) => {
  await createOrderEventPage.selectType(order.type);
  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await createOrderEventPage.enterJudgeAndLegalAdvisor(order.judgeAndLegalAdvisor.judgeLastName, order.judgeAndLegalAdvisor.legalAdvisorName);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.directionsNeeded.id);
  await createOrderEventPage.enterDirections('example directions');
  await I.completeEvent('Save and continue');
};

const createSupervisionOrder = async (I, createOrderEventPage, order) => {
  await createOrderEventPage.selectType(order.type);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.months);
  await createOrderEventPage.enterNumberOfMonths(order.months);
  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await createOrderEventPage.enterJudgeAndLegalAdvisor(order.judgeAndLegalAdvisor.judgeLastName, order.judgeAndLegalAdvisor.legalAdvisorName, order.judgeAndLegalAdvisor.judgeTitle);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.directionsNeeded.id);
  await createOrderEventPage.enterDirections('example directions');
  await I.completeEvent('Save and continue');
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
    }
  },

  async assertOrder(I, caseViewPage, order, orderNum, orderTime, expiryDate) {
    caseViewPage.selectTab(caseViewPage.tabs.orders);
    I.seeAnswerInTab(1, 'Order ' + orderNum, 'Type of order', order.type);

    if (order.type === 'Blank order (C21)') {
      I.seeAnswerInTab(2, 'Order 1', 'Order title', order.title);
      I.seeAnswerInTab(4, 'Order 1', 'Order document', order.document);
      I.seeAnswerInTab(5, 'Order 1', 'Date and time of upload', dateFormat(orderTime, 'h:MMtt, d mmmm yyyy'));
    } else {
      I.seeAnswerInTab(2, 'Order ' + orderNum, 'Order document', order.document);
      I.seeAnswerInTab(3, 'Order ' + orderNum, 'Date and time of upload', dateFormat(orderTime, 'h:MMtt, d mmmm yyyy'));
    }

    if (expiryDate) {
      I.seeAnswerInTab(4, 'Order 3', 'Order expires on', dateFormat(expiryDate, 'h:MMtt, d mmmm yyyy'));
    }

    I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', order.judgeAndLegalAdvisor.judgeTitle);
    I.seeAnswerInTab(2, 'Judge and legal advisor', 'Last name', order.judgeAndLegalAdvisor.judgeLastName);
    I.seeAnswerInTab(3, 'Judge and legal advisor', 'Legal advisor\'s full name',  order.judgeAndLegalAdvisor.legalAdvisorName);
  },
};
