const { I } = inject();

module.exports = {

  fields: {
    interpreter: {
      yes: '#hearingPreferences_interpreter_Yes',
      details: '#hearingPreferences_interpreterDetails',
    },
    intermediaryNo: '#hearingPreferences_intermediary_No',
    disabilityAssistance: {
      yes: '#hearingPreferences_disabilityAssistance_Yes',
      details: '#hearingPreferences_disabilityAssistanceDetails',
    },
    welshNo: '#hearingPreferences_welsh_No',
    extraSecurityMeasures: {
      yes: '#hearingPreferences_extraSecurityMeasures_Yes',
      details: '#hearingPreferences_extraSecurityMeasuresDetails',
    },
    somethingElse: {
      yes: '#hearingPreferences_somethingElse_Yes',
      details: '#hearingPreferences_somethingElseDetails',
    },
  },

  async enterInterpreter(details = 'French translator') {
    await I.runAccessibilityTest();
    I.click(this.fields.interpreter.yes);
    I.fillField(this.fields.interpreter.details, details);
  },

  enterIntermediary() {
    I.click(this.fields.intermediaryNo);
  },

  enterDisabilityAssistance(details = 'learning difficulty') {
    I.click(this.fields.disabilityAssistance.yes);
    I.fillField(this.fields.disabilityAssistance.details, details);
  },

  enterWelshProceedings() {
    I.click(this.fields.welshNo);
  },

  enterExtraSecurityMeasures(details = 'Separate waiting rooms') {
    I.click(this.fields.extraSecurityMeasures.yes);
    I.fillField(this.fields.extraSecurityMeasures.details, details);
  },

  enterSomethingElse(details = 'I need this for this person') {
    I.click(this.fields.somethingElse.yes);
    I.fillField(this.fields.somethingElse.details, details);
  },
};
