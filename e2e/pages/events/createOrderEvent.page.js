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
    directionsNeeded: {
      id: '#orderFurtherDirections_directionsNeeded',
      options: {
        yes: '#orderFurtherDirections_directionsNeeded-Yes',
        no: '#orderFurtherDirections_directionsNeeded-No',
      },
    },
    directions: '#orderFurtherDirections_directions',
    months: '#orderMonths',
    epo: {
      childrenDescription: {
        radioGroup: '#epoChildren_descriptionNeeded',
        description: '#epoChildren_description',
      },
      type: '#epoType',
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
  },

  selectType(type, subtype) {
    within(this.fields.orderTypeList, () => {
      I.click(locate('label').withText(type));
    });
    if (subtype)
      within(this.fields.orderSubtypeList, () => {
        I.click(locate('label').withText(subtype));
      });
  },

  enterC21OrderDetails() {
    I.fillField(this.fields.title, orders[0].title);
    I.fillField(this.fields.details, orders[0].details);
  },

  async enterJudgeAndLegalAdvisor(judgeLastName, legalAdvisorName, judgeTitle = judgeAndLegalAdvisor.fields.judgeTitleRadioGroup.herHonourJudge) {
    judgeAndLegalAdvisor.selectJudgeTitle('', judgeTitle);
    judgeAndLegalAdvisor.enterJudgeLastName(judgeLastName);
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },

  enterDirections(directions) {
    I.click(this.fields.directionsNeeded.options.yes);
    I.fillField(this.fields.directions, directions);
  },

  enterNumberOfMonths(numOfMonths) {
    I.fillField(this.fields.months, numOfMonths);
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
