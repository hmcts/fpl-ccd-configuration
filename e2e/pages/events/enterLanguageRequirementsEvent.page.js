const { I } = inject();

module.exports = {
  fields: {
    languageRequirement: {
      yes: '#languageRequirement-Yes',
      no: '#languageRequirement-No',
    },
  },

  async enterLanguageRequirement() {
    I.click(this.fields.languageRequirement.yes);
    await I.runAccessibilityTest();
  },
};
