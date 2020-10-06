const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');

const createBlankOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  await createOrderEventPage.selectType(order.type);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await selectChildren(I, createOrderEventPage, order);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.title);
  await createOrderEventPage.enterC21OrderDetails();
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.judgeAndLegalAdvisorTitleId);
  await enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.completeEvent('Save and continue');
};

const createCareOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  await createOrderEventPage.selectType(order.type, order.subtype);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await selectChildren(I, createOrderEventPage, order);

  if (order.subtype === 'Interim') {
    await fillInterimEndDate(I, createOrderEventPage, order);
  }

  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.directionsNeeded.id);
  await createOrderEventPage.enterDirections('example directions');

  if (order.closeCase !== undefined) {
    await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.closeCase.id);
    await createOrderEventPage.closeCaseFromOrder(order.closeCase);
  }

  await I.completeEvent('Save and continue');
};

const createSupervisionOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  await createOrderEventPage.selectType(order.type, order.subtype);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await selectChildren(I, createOrderEventPage, order);

  if (order.subtype === 'Final') {
    await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.months);
    createOrderEventPage.enterNumberOfMonths(order.months);
  } else {
    await fillInterimEndDate(I, createOrderEventPage, order);
  }

  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.directionsNeeded.id);
  await createOrderEventPage.enterDirections('example directions');

  if (order.closeCase !== undefined) {
    await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.closeCase.id);
    await createOrderEventPage.closeCaseFromOrder(order.closeCase);
  }

  await I.completeEvent('Save and continue');
};

const createEmergencyProtectionOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
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
  await enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.directionsNeeded.id);
  await createOrderEventPage.enterDirections('example directions');

  if (order.closeCase !== undefined) {
    await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.closeCase.id);
    await createOrderEventPage.closeCaseFromOrder(order.closeCase);
  }

  await I.completeEvent('Save and continue');
};

const createDischargeCareOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  await createOrderEventPage.selectType(order.type);
  await selectCareOrders(I, createOrderEventPage, order);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.directionsNeeded.id);
  await createOrderEventPage.enterDirections('example directions');

  if (order.closeCase !== undefined) {
    await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.closeCase.id);
    await createOrderEventPage.closeCaseFromOrder(order.closeCase);
  }

  await I.completeEvent('Save and continue');
};

const uploadOrder = async (I, createOrderEventPage, order) => {
  I.see(order.orderChecks.familyManCaseNumber);
  await createOrderEventPage.selectType(order.type, undefined, order.uploadedOrderType);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await selectChildren(I, createOrderEventPage, order);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.uploadedOrder);
  await createOrderEventPage.uploadOrder(order.orderFile);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.checkYourOrder);
  createOrderEventPage.checkOrder(order.orderChecks);
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
  if (order.children === 'Single') {
    return I.click('Continue');
  }
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.allChildren.id);
  if (order.children === 'All') {
    await createOrderEventPage.useAllChildren();
  } else {
    await createOrderEventPage.notAllChildren();
    await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.childSelector.id);
    await createOrderEventPage.selectChildren(order.children);
  }
};

const selectCareOrders = async (I, createOrderEventPage, order) => {
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.careOrderSelector.id);
  await createOrderEventPage.selectCareOrder(order.careOrders);
};

const enterJudgeAndLegalAdvisor =  (I, createOrderEventPage, order, hasAllocatedJudge) => {
  if (hasAllocatedJudge) {
    createOrderEventPage.useAllocatedJudge(order.judgeAndLegalAdvisor.legalAdvisorName);
  } else {
    createOrderEventPage.useAlternateJudge();
    createOrderEventPage.enterJudgeAndLegalAdvisor(order.judgeAndLegalAdvisor.judgeLastName, order.judgeAndLegalAdvisor.legalAdvisorName, order.judgeAndLegalAdvisor.judgeTitle,
      order.judgeAndLegalAdvisor.judgeEmailAddress);
  }
};

module.exports = {
  async createOrder(I, createOrderEventPage, order, hasAllocatedJudge) {
    switch (order.type) {
      case 'Blank order (C21)':
        await createBlankOrder(I, createOrderEventPage, order, hasAllocatedJudge);
        break;
      case 'Care order':
        await createCareOrder(I, createOrderEventPage, order, hasAllocatedJudge);
        break;
      case 'Supervision order':
        await createSupervisionOrder(I, createOrderEventPage, order, hasAllocatedJudge);
        break;
      case 'Emergency protection order':
        await createEmergencyProtectionOrder(I, createOrderEventPage, order, hasAllocatedJudge);
        break;
      case 'Discharge of care order':
        await createDischargeCareOrder(I, createOrderEventPage, order, hasAllocatedJudge);
        break;
      case 'Upload':
        await uploadOrder(I, createOrderEventPage, order);
        break;
    }
  },

  async assertOrder(I, caseViewPage, order, defaultIssuedDate, hasAllocatedJudge = false, isOrderRemoved = false) {
    caseViewPage.selectTab(caseViewPage.tabs.orders);
    const numberOfOrders = await I.grabNumberOfVisibleElements('//*[text() = \'Type of order\']');
    const orderHeading = isOrderRemoved ? `Removed orders ${numberOfOrders}` : `Order ${numberOfOrders}`;

    if (order.type === 'Blank order (C21)') {
      I.seeInTab([orderHeading, 'Order title'], order.title);
      I.seeInTab([orderHeading, 'Order document'], order.document);
      I.seeInTab([orderHeading, 'Date of issue'], dateFormat(defaultIssuedDate, 'd mmmm yyyy'));
    } else {
      I.seeInTab([orderHeading, 'Order document'], order.document);
      I.seeInTab([orderHeading, 'Date of issue'], dateFormat(dateToString(order.dateOfIssue), 'd mmmm yyyy'));
    }

    if (order.type !== 'Upload') {
      if (hasAllocatedJudge) {
        I.seeInTab([orderHeading, 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], 'Her Honour Judge');
        I.seeInTab([orderHeading, 'Judge and Justices\' Legal Adviser', 'Last name'], 'Moley');
      } else {
        I.seeInTab([orderHeading, 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], order.judgeAndLegalAdvisor.judgeTitle);
        I.seeInTab([orderHeading, 'Judge and Justices\' Legal Adviser', 'Last name'], order.judgeAndLegalAdvisor.judgeLastName);
        I.seeInTab([orderHeading, 'Judge and Justices\' Legal Adviser', 'Justices\' Legal Adviser\'s full name'], order.judgeAndLegalAdvisor.legalAdvisorName);
      }
    } else {
      I.seeTextInTab([orderHeading, 'Date and time of upload']);
      I.seeTextInTab([orderHeading, 'Uploaded by']);
    }

    isOrderRemoved && I.seeInTab([orderHeading, 'Reason for removal'], order.reasonForRemoval);
  },

  async assertOrderSentToParty(I, caseViewPage, partyName, order) {
    caseViewPage.selectTab(caseViewPage.tabs.documentsSentToParties);
    const numberOfDocuments = await I.grabNumberOfVisibleElements(`//*[text() = '${partyName}']/ancestor::ccd-read-complex-field-table//ccd-read-complex-field-table`);
    I.seeInTab(['Party 1', 'Representative name'], partyName);
    I.seeInTab(['Party 1', `Document ${numberOfDocuments}`, 'File'], order.document);
  },
};
