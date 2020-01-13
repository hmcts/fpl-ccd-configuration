const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const orders = require('../../fixtures/orders.js');
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  fields: {
    title: '#order_title',
    details: '#order_details',
    orderTypeList: '#orderTypeAndDocument_type',
    epo: {
      childrenDescription: {
        radioGroup: '#epoChildren_descriptionNeeded',
        description: '#epoChildren_description',
      },
      type: '#epoType',
      removalAddress: '#epoRemovalAddress_epoRemovalAddress',
      includePhrase: '#epoPhrase_includePhrase',
      endDate: {
        second: '#epoEndDate-second',
        minute: '#epoEndDate-minute',
        hour: '#epoEndDate-hour',
        day: '#epoEndDate-day',
        month: '#epoEndDate-month',
        year: '#epoEndDate-year',
      },
    },
  },

  selectType(type) {
    within(this.fields.orderTypeList, () => {
      I.click(locate('label').withText(type));
    });
  },

  enterC21OrderDetails() {
    I.fillField(this.fields.title, orders[0].title);
    I.fillField(this.fields.details, orders[0].details);
  },

  async enterJudgeAndLegalAdvisor(judgeLastName, legalAdvisorName) {
    judgeAndLegalAdvisor.selectJudgeTitle();
    judgeAndLegalAdvisor.enterJudgeLastName(judgeLastName);
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },

  async enterChildrenDescription(description) {
    within(this.fields.epo.childrenDescription.radioGroup, () => {
      I.click(locate('label').withText('Yes'));
    });

    await I.fillField(this.fields.epo.childrenDescription.description, description);
  },

  selectEpoType(type) {
    within(this.fields.epo.type, () => {
      I.click(locate('label').withText(type));
    });
  },

  enterRemovalAddress(address) {
    within(this.fields.epo.removalAddress, () => {
      postcodeLookup.enterAddressManually(address);
    });
  },

  includePhrase(option) {
    within(this.fields.epo.includePhrase, () => {
      I.click(locate('label').withText(option));
    });
  },

  enterEpoEndDate(date) {
    I.fillField(this.fields.epo.endDate.day, date.getDate());
    I.fillField(this.fields.epo.endDate.month, date.getMonth() + 1);
    I.fillField(this.fields.epo.endDate.year, date.getFullYear());
    I.fillField(this.fields.epo.endDate.hour, date.getHours());
    I.fillField(this.fields.epo.endDate.minute, date.getMinutes());
    I.fillField(this.fields.epo.endDate.second, date.getSeconds());
  },
};
