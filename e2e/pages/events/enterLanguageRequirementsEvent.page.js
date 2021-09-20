const { I } = inject();

module.exports = {
  fields: {
    languageRequirement: {
      yes: '#languageRequirement_Yes',
      no: '#languageRequirement_No',
    },
    applicationLanguage: language => `#languageRequirementApplication-${language}`,
    applicationNeedEnglishTranslation: {
      yes: '#languageRequirementApplicationNeedEnglish_Yes',
      no: '#languageRequirementApplicationNeedEnglish_No',
    },
    applicationNeedWelshTranslation: {
      yes: '#languageRequirementApplicationNeedWelsh_Yes',
      no: '#languageRequirementApplicationNeedWelsh_No',
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

  selectApplicationLanguage(language) {
    I.click(this.fields.applicationLanguage(language));
  },

  selectNeedEnglishTranslation() {
    I.click(this.fields.applicationNeedEnglishTranslation.yes);
  },
};
