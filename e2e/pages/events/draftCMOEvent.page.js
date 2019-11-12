const { I } = inject();
const draftDirections = require('../../fragments/draftDirections');

module.exports = {
  fields: {
    cmoHearingDateList: '#cmoHearingDateList',
  },

  associateHearingDate(date) {
    I.waitForElement(this.fields.cmoHearingDateList);
    I.selectOption(this.fields.cmoHearingDateList, date);
  },

  async enterDatesForDirections(direction) {
    await I.addAnotherElementToCollection();
    await draftDirections.enterDate('allParties', direction);
  },
};
