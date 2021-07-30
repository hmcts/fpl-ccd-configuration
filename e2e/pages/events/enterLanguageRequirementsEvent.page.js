const { I } = inject();

module.exports = {
  fields: {
    languageRequirement: {
      yes: '#languageRequirement_Yes',
      no: '#languageRequirement_No',
    },
  },

  async enterLanguageRequirement() {
    I.click(this.fields.languageRequirement.yes);
    await I.runAccessibilityTest();
  },

  async disableLanguageRequirement() {
    I.click(this.fields.languageRequirement.no);
    await I.runAccessibilityTest();
  },
};
