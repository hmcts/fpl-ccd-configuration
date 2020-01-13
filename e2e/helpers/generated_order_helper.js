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
};
