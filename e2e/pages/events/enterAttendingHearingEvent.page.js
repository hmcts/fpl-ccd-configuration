const { I } = inject();

module.exports = {

  fields: {
    interpreter: {
      yes: '#hearingPreferences_interpreter-Yes',
      details: '#hearingPreferences_interpreterDetails',
    },
    intermediaryNo: '#hearingPreferences_intermediary-No',
    disabilityAssistance: {
      yes: '#hearingPreferences_disabilityAssistance-Yes',
      details: '#hearingPreferences_disabilityAssistanceDetails',
    },
    welshNo: '#hearingPreferences_welsh-No',
    extraSecurityMeasures: {
      yes: '#hearingPreferences_extraSecurityMeasures-Yes',
      details: '#hearingPreferences_extraSecurityMeasuresDetails',
    },
    somethingElse: {
      yes: '#hearingPreferences_somethingElse-Yes',
      details: '#hearingPreferences_somethingElseDetails',
    },
  },

  async enterInterpreter(details = 'French translator') {
    await I.runAccessibilityTest();
    console.log('enter attending hearing 1');
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
