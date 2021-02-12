const { I } = inject();

module.exports = {

  fields: {
    alcoholOrDrugAbuse: {
      yes: '#factorsParenting_alcoholDrugAbuse-Yes',
      reason: '#factorsParenting_alcoholDrugAbuseReason',
    },
    domesticViolence: {
      yes: '#factorsParenting_domesticViolence-Yes',
      reason: '#factorsParenting_domesticViolenceReason',

    },
    anythingElse: {
      yes: '#factorsParenting_anythingElse-Yes',
      reason: '#factorsParenting_anythingElseReason',
    },
  },

  completeAlcoholOrDrugAbuse() {
    I.click(this.fields.alcoholOrDrugAbuse.yes);
    //I.runAccessibilityTest();
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
