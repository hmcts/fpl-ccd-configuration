const { I } = inject();

module.exports = {
  fields: function (index) {
    return {
      recitals: {
        title: `#recitals_${index}_title`,
        description: `#recitals_${index}_description`,
      },
      cmoHearingDateList: '#cmoHearingDateList',
    };
  },

  draftCMO(date= '1 Jan 2050') {
    I.waitForElement('#cmoHearingDateList');
    I.selectOption('#cmoHearingDateList', date);
    I.click('Continue');
  },

  enterRecital() {
    I.click('Add new');
    I.fillField('#recitals_0_title', 'title test');
    I.fillField('#recitals_0_description', 'description');
  },

  getActiveElementIndex() {
    return I.grabNumberOfVisibleElements('//button[text()="Add New"]') - 1;
  },
};
