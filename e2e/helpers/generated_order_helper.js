const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');

const createBlankOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  await createOrderEventPage.selectType(order.type);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await selectChildren(I, createOrderEventPage, order);
  await I.goToNextPage();
  await createOrderEventPage.enterC21OrderDetails();
  await I.goToNextPage();
  await enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.completeEvent('Save and continue');
};

const createCareOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  await createOrderEventPage.selectType(order.type, order.subtype);
  await fillDateOfIssue(I, createOrderEventPage, order);
  if (order.subtype === 'Interim') {
    await fillInterimEndDate(I, createOrderEventPage, order);
  }
  await selectChildren(I, createOrderEventPage, order);

  await I.goToNextPage();
  await enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.goToNextPage();
  if (order.subtype === 'Interim') {
    await createOrderEventPage.enterExclusionClause('example exclusion clause');
  }
  await createOrderEventPage.enterDirections('example directions');

  if (order.closeCase !== undefined) {
    await I.goToNextPage();
    await createOrderEventPage.closeCaseFromOrder(order.closeCase);
  }

  await I.completeEvent('Save and continue');
};

const createSupervisionOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  await createOrderEventPage.selectType(order.type, order.subtype);
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
  await enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.goToNextPage();
  await createOrderEventPage.enterDirections('example directions');

  if (order.closeCase !== undefined) {
    await I.goToNextPage();
    await createOrderEventPage.closeCaseFromOrder(order.closeCase);
  }

  await I.completeEvent('Save and continue');
};

const createEmergencyProtectionOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  const today = new Date(Date.now());
  const tomorrow = new Date(Date.now() + (3600 * 1000 * 24));

  await createOrderEventPage.selectType(order.type);
  await fillDateAndTimeOfIssue(I, createOrderEventPage, today);
  await selectChildren(I, createOrderEventPage, order);
  await I.goToNextPage();
  await createOrderEventPage.enterChildrenDescription(order.childrenDescription);
  await I.goToNextPage();
  createOrderEventPage.selectEpoType(order.epoType);
  createOrderEventPage.enterRemovalAddress(order.removalAddress);
  await I.goToNextPage();
  createOrderEventPage.includePhrase(order.includePhrase);
  await I.goToNextPage();
  createOrderEventPage.enterEpoEndDate(tomorrow);
  await I.goToNextPage();
  await enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.goToNextPage();
  await createOrderEventPage.enterDirections('example directions');

  if (order.closeCase !== undefined) {
    await I.goToNextPage();
    await createOrderEventPage.closeCaseFromOrder(order.closeCase);
  }

  await I.completeEvent('Save and continue');
};

const createDischargeCareOrder = async (I, createOrderEventPage, order, hasAllocatedJudge = false) => {
  await createOrderEventPage.selectType(order.type);
  await selectCareOrders(I, createOrderEventPage, order);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await I.goToNextPage();
  await enterJudgeAndLegalAdvisor(I, createOrderEventPage, order, hasAllocatedJudge);
  await I.goToNextPage();
  await createOrderEventPage.enterDirections('example directions');

  if (order.closeCase !== undefined) {
    await I.goToNextPage();
    await createOrderEventPage.closeCaseFromOrder(order.closeCase);
  }

  await I.completeEvent('Save and continue');
};

const uploadOrder = async (I, createOrderEventPage, order) => {
  I.see(order.orderChecks.familyManCaseNumber);
  await createOrderEventPage.selectType(order.type, undefined, order.uploadedOrderType);
  createOrderEventPage.enterOrderNameAndDescription(order.orderName, order.orderDescription);
  await fillDateOfIssue(I, createOrderEventPage, order);
  await selectChildren(I, createOrderEventPage, order);
  await I.goToNextPage();
  await createOrderEventPage.uploadOrder(order.orderFile);
  await I.goToNextPage();
  createOrderEventPage.checkOrder(order.orderChecks);
  await I.completeEvent('Save and continue');
};

const fillInterimEndDate = async (I, createOrderEventPage, order) => {
  await I.goToNextPage();
  if (order.interimEndDate.isNamedDate) {
    await createOrderEventPage.selectAndEnterNamedDate(order.interimEndDate.endDate);
  } else {
    await createOrderEventPage.selectEndOfProceedings();
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
  if (order.children === 'Single') {
    return I.goToNextPage();
  }
  await I.goToNextPage();
  if (order.children === 'All') {
    await createOrderEventPage.useAllChildren();
  } else {
    await createOrderEventPage.notAllChildren();
    await I.goToNextPage();
    await createOrderEventPage.selectChildren(order.children);
  }
};

const selectCareOrders = async (I, createOrderEventPage, order) => {
  await I.goToNextPage();
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
      I.seeInTab([orderHeading, 'Starts on'], dateFormat(defaultIssuedDate, 'd mmmm yyyy'));
    } else if (order.type === 'Emergency protection order') {
      I.seeInTab([orderHeading, 'Order document'], order.document);
      I.seeTextInTab([orderHeading, 'Starts on']);
      I.seeTextInTab([orderHeading, 'Ends on']);
    } else {
      I.seeInTab([orderHeading, 'Order document'], order.document);
      I.seeInTab([orderHeading, 'Starts on'], dateFormat(dateToString(order.dateOfIssue), 'd mmmm yyyy'));
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
      I.seeInTab([orderHeading, 'Order description'], order.orderDescription);
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
