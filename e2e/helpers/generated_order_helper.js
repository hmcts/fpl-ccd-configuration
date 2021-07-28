const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');

const createBlankOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  createOrderEventPage.selectType(order.type);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await selectChildren(I, createOrderEventPage, order);
  await I.goToNextPage();
  createOrderEventPage.enterC21OrderDetails();
  await I.goToNextPage();
  enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.completeEvent('Save and continue');
};

const createCareOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  createOrderEventPage.selectType(order.type, order.subtype);
  await fillDateOfIssue(I, createOrderEventPage, order);
  if (order.subtype === 'Interim') {
    await fillInterimEndDate(I, createOrderEventPage, order);
  }
  await selectChildren(I, createOrderEventPage, order);

  await I.goToNextPage();
  enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.goToNextPage();
  if (order.subtype === 'Interim') {
    createOrderEventPage.enterExclusionClause('example exclusion clause');
  }
  createOrderEventPage.enterDirections('example directions');

  if (order.closeCase !== undefined) {
    await I.goToNextPage();
    createOrderEventPage.closeCaseFromOrder(order.closeCase);
  }

  await I.completeEvent('Save and continue');
};

const createSupervisionOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  createOrderEventPage.selectType(order.type, order.subtype);
  await fillDateOfIssue(I, createOrderEventPage, order);
  if (order.subtype === 'Interim') {
    await fillInterimEndDate(I, createOrderEventPage, order);
  }
  await selectChildren(I, createOrderEventPage, order);

  if (order.subtype === 'Final') {
    await I.goToNextPage();
    createOrderEventPage.enterNumberOfMonths(order.months);
  }

  await I.goToNextPage();
  enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.goToNextPage();
  createOrderEventPage.enterDirections('example directions');

  if (order.closeCase !== undefined) {
    await I.goToNextPage();
    createOrderEventPage.closeCaseFromOrder(order.closeCase);
  }

  await I.completeEvent('Save and continue');
};

const createEmergencyProtectionOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  const today = new Date(Date.now());
  const tomorrow = new Date(Date.now() + (3600 * 1000 * 24));

  createOrderEventPage.selectType(order.type);
  await fillDateAndTimeOfIssue(I, createOrderEventPage, today);
  await selectChildren(I, createOrderEventPage, order);
  await I.goToNextPage();
  createOrderEventPage.enterChildrenDescription(order.childrenDescription);
  await I.goToNextPage();
  createOrderEventPage.selectEpoType(order.epoType);
  await createOrderEventPage.enterRemovalAddress(order.removalAddress);
  createOrderEventPage.selectExclusionRequirement();
  createOrderEventPage.selectExclusionRequirementStartDate();
  await createOrderEventPage.selectWhoIsExcluded();
  await I.goToNextPage();
  createOrderEventPage.includePhrase(order.includePhrase);
  await I.goToNextPage();
  await createOrderEventPage.enterEpoEndDate(tomorrow);
  await I.goToNextPage();
  enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.goToNextPage();
  createOrderEventPage.enterDirections('example directions');

  if (order.closeCase !== undefined) {
    await I.goToNextPage();
    createOrderEventPage.closeCaseFromOrder(order.closeCase);
  }

  await I.completeEvent('Save and continue');
};

const createDischargeCareOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  createOrderEventPage.selectType(order.type);
  await selectCareOrders(I, createOrderEventPage, order);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await I.goToNextPage();
  enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.goToNextPage();
  createOrderEventPage.enterDirections('example directions');

  if (order.closeCase !== undefined) {
    await I.goToNextPage();
    createOrderEventPage.closeCaseFromOrder(order.closeCase);
  }

  await I.completeEvent('Save and continue');
};

const uploadOrder = async (I, createOrderEventPage, order) => {
  I.see(order.orderChecks.familyManCaseNumber);
  createOrderEventPage.selectType(order.type, undefined, order.uploadedOrderType);
  createOrderEventPage.enterOrderNameAndDescription(order.orderName, order.orderDescription);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await selectChildren(I, createOrderEventPage, order);
  await I.goToNextPage();
  createOrderEventPage.uploadOrder(order.orderFile);
  await I.goToNextPage();
  createOrderEventPage.checkOrder(order.orderChecks);
  await I.completeEvent('Save and continue');
};

const fillInterimEndDate = async (I, createOrderEventPage, order) => {
  if (order.interimEndDate.isNamedDate) {
    await createOrderEventPage.selectAndEnterNamedDate(order.interimEndDate.endDate);
  } else {
    createOrderEventPage.selectEndOfProceedings();
  }
};

const fillDateOfIssue = async (I, createOrderEventPage, order) => {
  await I.goToNextPage();
  await createOrderEventPage.enterDateOfIssue(order.dateOfIssue);
};

const fillDateAndTimeOfIssue = async (I, createOrderEventPage, dateAndTime) => {
  await I.goToNextPage();
  await createOrderEventPage.enterDateAndTimeOfIssue(dateAndTime);
};

const selectChildren = async (I, createOrderEventPage, order) => {
  await I.goToNextPage();
  if (order.children === 'Single') {
    return ;
  }
  if (order.children === 'All') {
    createOrderEventPage.useAllChildren();
  } else {
    createOrderEventPage.notAllChildren();
    await I.goToNextPage();
    createOrderEventPage.selectChildren(order.children);
  }
};

const selectCareOrders = async (I, createOrderEventPage, order) => {
  await I.goToNextPage();
  createOrderEventPage.selectCareOrder(order.careOrders);
};

const enterJudgeAndLegalAdvisor =  (I, createOrderEventPage, order, hasAllocatedJudge) => {
  if (hasAllocatedJudge) {
    createOrderEventPage.useAllocatedJudge(order.judgeAndLegalAdvisor.legalAdvisorName);
  } else {
    createOrderEventPage.useAlternateJudge();
    createOrderEventPage.enterJudge(order.judgeAndLegalAdvisor);
    createOrderEventPage.enterLegalAdvisor(order.judgeAndLegalAdvisor.legalAdvisorName);
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

  async assertOrder(I, caseViewPage, order, defaultIssuedDate, isOrderRemoved = false) {
    caseViewPage.selectTab(caseViewPage.tabs.orders);
    const numberOfOrders = await I.grabNumberOfVisibleElements('//*[text() = \'Type of order\']');
    const orderHeading = isOrderRemoved ? `Other removed orders ${numberOfOrders}` : `Order ${numberOfOrders}`;

    if (order.type === 'Blank order (C21)') {
      I.seeInTab([orderHeading, 'Order title'], order.title);
      I.seeInTab([orderHeading, 'Order document'], order.document);
      I.seeInTab([orderHeading, 'Starts on'], dateFormat(defaultIssuedDate, 'd mmmm yyyy'));
    } else if (order.type === 'Emergency protection order') {
      I.seeInTab([orderHeading, 'Order document'], order.document);
      I.seeTextInTab([orderHeading, 'Starts on']);
      I.seeTextInTab([orderHeading, 'Ends on']);
    } else {
      I.seeInTab([orderHeading, 'Order document'], order.document);
      I.seeInTab([orderHeading, 'Starts on'], dateFormat(dateToString(order.dateOfIssue), 'd mmmm yyyy'));
    }

    if (order.type === 'Upload') {
      I.seeInTab([orderHeading, 'Order description'], order.orderDescription);
      I.seeTextInTab([orderHeading, 'Date and time of upload']);
      I.seeTextInTab([orderHeading, 'Uploaded by']);
    }

    isOrderRemoved && I.seeInTab([orderHeading, 'Reason for removal'], order.reasonForRemoval);
  },

  async assertOrderSentToParty(I, caseViewPage, partyName, order, index = 1) {
    caseViewPage.selectTab(caseViewPage.tabs.documentsSentToParties);
    const numberOfDocuments = await I.grabNumberOfVisibleElements(`//*[text() = '${partyName}']/ancestor::ccd-read-complex-field-table//ccd-read-complex-field-table`);
    I.seeInTab([`Party ${index}`, 'Recipient'], partyName);
    I.seeInTab([`Party ${index}`, `Document ${numberOfDocuments}`, 'File'], order.document);
  },
};
