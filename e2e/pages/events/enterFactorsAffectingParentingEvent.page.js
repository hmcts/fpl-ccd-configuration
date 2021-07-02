const { I } = inject();

module.exports = {

  fields: {
    alcoholOrDrugAbuse: {
      yes: '#factorsParenting_alcoholDrugAbuse_Yes',
      reason: '#factorsParenting_alcoholDrugAbuseReason',
    },
    domesticViolence: {
      yes: '#factorsParenting_domesticViolence_Yes',
      reason: '#factorsParenting_domesticViolenceReason',

    },
    anythingElse: {
      yes: '#factorsParenting_anythingElse_Yes',
      reason: '#factorsParenting_anythingElseReason',
    },
  },

  async completeAlcoholOrDrugAbuse() {
    I.click(this.fields.alcoholOrDrugAbuse.yes);
    await I.runAccessibilityTest();
    I.fillField(this.fields.alcoholOrDrugAbuse.reason, 'mock reason');
  },

  completeDomesticViolence() {
    I.click(this.fields.domesticViolence.yes);
    I.fillField(this.fields.domesticViolence.reason, 'mock reason');
  },

  completeAnythingElse() {
    I.click(this.fields.anythingElse.yes);
    I.fillField(this.fields.anythingElse.reason, 'mock reason');
  },
};
