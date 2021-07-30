const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const orders = require('../../fixtures/orders.js');
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  fields: {
    title: '#order_title',
    details: '#order_details',
    orderTypeList: '#orderTypeAndDocument_type',
    orderSubtypeList: '#orderTypeAndDocument_subtype',
    orderUploadedTypeList: '#orderTypeAndDocument_uploadedOrderType',
    order: {
      name: '#orderTypeAndDocument_orderName',
      description: '#orderTypeAndDocument_orderDescription',
    },
    directionsNeeded: {
      id: '#orderFurtherDirections_directionsNeeded',
      options: {
        yes: '#orderFurtherDirections_directionsNeeded_Yes',
        no: '#orderFurtherDirections_directionsNeeded_No',
      },
    },
    directions: '#orderFurtherDirections_directions',
    exclusionClauseNeeded: {
      id: '#orderExclusionClause_exclusionClauseNeeded',
      options: {
        yes: '#orderExclusionClause_exclusionClauseNeeded_Yes',
        no: '#orderExclusionClause_exclusionClauseNeeded_No',
      },
    },
    exclusionClause: '#orderExclusionClause_exclusionClause',
    dateOfIssue: {
      id: '#dateOfIssue',
    },
    dateAndTimeOfIssue: {
      id: '#dateAndTimeOfIssue',
    },
    interimEndDate: {
      id: '#interimEndDate_interimEndDate',
      options: {
        endOfProceedings: 'At the end of the proceedings, or until a further order is made',
        namedDate: 'At the end of a named date',
        specificTimeNamedDate: 'At a specific time on a named date',
      },
      endDate: {
        day: '#interimEndDate_endDate-day',
        month: '#interimEndDate_endDate-month',
        year: '#interimEndDate_endDate-year',
      },
    },
    childSelector: {
      id: '#childSelector_childSelector',
      selector: function (index) {
        return `#childSelector_option${index}`;
      },
      selectorText: 'Yes',
    },
    othersSelector: {
      id: '#othersSelector_othersSelector',
      selector: function (index) {
        return `#othersSelector_option${index}`;
      },
      selectorText: 'Yes',
    },
    careOrderSelector: {
      id: '#careOrderSelector_careOrderSelector',
      selector: function (index) {
        return `#careOrderSelector_option${index}`;
      },
      selectorText: 'Discharge order',
    },
    allChildren: {
      id: '#orderAppliesToAllChildren',
      options: {
        yes: 'Yes',
        no: 'No',
      },
    },
    months: '#orderMonths',
    epo: {
      childrenDescription: {
        radioGroup: '#epoChildren_descriptionNeeded',
        description: '#epoChildren_description',
      },
      type: '#epoType',
      epoExclusionRequirementType: {
        differentDate: '#epoExclusionRequirementType-STARTING_ON_DIFFERENT_DATE',
        sameDate: '#epoExclusionRequirementType-STARTING_ON_SAME_DATE',
        noExclusion: '#epoExclusionRequirementType-NO_TO_EXCLUSION',
      },
      exclusionStartDate: {
        day: '#epoExclusionStartDate-day',
        month: '#epoExclusionStartDate-month',
        year: '#epoExclusionStartDate-year',
      },
      epoWhoIsExcluded: '#epoWhoIsExcluded',
      removalAddress: '#epoRemovalAddress_epoRemovalAddress',
      includePhrase: '#epoPhrase_includePhrase',
      endDate: {
        id: '#epoEndDate',
        second: '#epoEndDate-second',
        minute: '#epoEndDate-minute',
        hour: '#epoEndDate-hour',
        day: '#epoEndDate-day',
        month: '#epoEndDate-month',
        year: '#epoEndDate-year',
      },
    },
    judgeAndLegalAdvisorTitleId: '#judgeAndLegalAdvisor_judgeTitle',
    closeCase: {
      id: '#closeCaseFromOrder',
      options: {
        yes: '#closeCaseFromOrder_Yes',
        no: '#closeCaseFromOrder_No',
      },
    },
    uploadedOrder: '#uploadedOrder',
    checkYourOrder: '#checkYourOrder_label',
  },

  selectType(type, subtype, orderType) {
    I.click(type);
    if (subtype) {
      I.click(subtype);
    }
    if (orderType) {
      I.selectOption(this.fields.orderUploadedTypeList, orderType);
    }
  },

  enterOrderNameAndDescription(name, description) {
    I.fillField(this.fields.order.name, name);
    I.fillField(this.fields.order.description, description);
  },

  uploadOrder(order) {
    I.attachFile(this.fields.uploadedOrder, order);
  },

  checkOrder(orderChecks) {
    I.see(orderChecks.familyManCaseNumber);
    I.see(orderChecks.children);
    I.see(orderChecks.order);
  },

  enterC21OrderDetails() {
    I.fillField(this.fields.title, orders[0].title);
    I.fillField(this.fields.details, orders[0].details);
  },

  enterJudge(judge) {
    judge.judgeTitle = judgeAndLegalAdvisor.fields.judgeTitleRadioGroup.herHonourJudge;
    judgeAndLegalAdvisor.selectJudgeTitle('', judge.judgeTitle);
    judgeAndLegalAdvisor.enterJudgeLastName(judge.judgeLastName);
    judgeAndLegalAdvisor.enterJudgeEmailAddress(judge.judgeEmailAddress);
  },

  enterLegalAdvisor(legalAdvisorName) {
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },

  useAlternateJudge() {
    judgeAndLegalAdvisor.useAlternateJudge();
  },

  useAllocatedJudge(legalAdvisorName) {
    judgeAndLegalAdvisor.useAllocatedJudge();
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },

  enterDirections(directions) {
    I.click(this.fields.directionsNeeded.options.yes);
    I.fillField(this.fields.directions, directions);
  },

  enterExclusionClause(exclusionClause) {
    I.click(this.fields.exclusionClauseNeeded.options.yes);
    I.fillField(this.fields.exclusionClause, exclusionClause);
  },

  enterNumberOfMonths(numOfMonths) {
    I.fillField(this.fields.months, numOfMonths);
  },

  enterChildrenDescription(description) {
    I.click(this.fields.epo.childrenDescription.radioGroup + '_Yes');
    I.fillField(this.fields.epo.childrenDescription.description, description);
  },

  selectEpoType(type) {
    I.click(type);
  },

  async enterRemovalAddress(address) {
    await postcodeLookup.enterAddressManually(address);
  },

  includePhrase(option) {
    I.click(`${this.fields.epo.includePhrase}_${option}`);
  },

  async enterEpoEndDate(date) {
    await I.runAccessibilityTest();
    await I.fillDateAndTime(date, this.fields.epo.endDate.id);
  },

  selectEndOfProceedings() {
    I.click(this.fields.interimEndDate.options.endOfProceedings);
  },

  async enterDateOfIssue(date) {
    await I.fillDate(date, this.fields.dateOfIssue.id);
  },

  async enterDateAndTimeOfIssue(dateAndTime) {
    await I.fillDateAndTime(dateAndTime, this.fields.dateAndTimeOfIssue.id);
  },

  async selectAndEnterNamedDate(date) {
    I.click(this.fields.interimEndDate.options.namedDate);
    await I.fillDate(date, this.fields.interimEndDate.id);
  },

  selectChildren(children = []) {
    for (let child of children) {
      I.click(`${this.fields.childSelector.selector(child)}-SELECTED`);
    }
  },

  selectCareOrder(careOrders = []) {
    for (let order of careOrders) {
      I.click(`${this.fields.careOrderSelector.selector(order)}-SELECTED`);
    }
  },

  useAllChildren() {
    I.click(`${this.fields.allChildren.id}_${this.fields.allChildren.options.yes}`);
  },

  notAllChildren() {
    I.click(`${this.fields.allChildren.id}_${this.fields.allChildren.options.no}`);
  },

  closeCaseFromOrder(closeCase) {
    if (closeCase) {
      I.click(this.fields.closeCase.options.yes);
    } else {
      I.click(this.fields.closeCase.options.no);
    }
  },

  selectExclusionRequirement() {
    I.click(this.fields.epo.epoExclusionRequirementType.differentDate);
  },

  selectExclusionRequirementStartDate() {
    I.fillField(this.fields.epo.exclusionStartDate.day, '01');
    I.fillField(this.fields.epo.exclusionStartDate.month, '11');
    I.fillField(this.fields.epo.exclusionStartDate.year, '2021');
  },

  async selectWhoIsExcluded() {
    I.fillField(this.fields.epo.epoWhoIsExcluded, 'John Doe');
    await I.runAccessibilityTest();
  },
};
