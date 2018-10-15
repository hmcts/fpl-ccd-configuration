const I = actor();

module.exports = {

  fields: {
    interpreter: {
      yes: '#hearingPreferences_interpreter-Yes',
      details: '#hearingPreferences_interpreterDetails',
    },
    intermediaryNo: '#hearingPreferences_intermediary-No',
    litigationCapacityNo: '#hearingPreferences_litigation-No',
    learningDisability: {
      yes: '#hearingPreferences_learningDisability-Yes',
      details: '#hearingPreferences_learningDisabilityDetails',
    },
    welshNo: '#hearingPreferences_welsh-No',
    security: {
      yes: '#hearingPreferences_extraSecurityMeasures-Yes',
      details: '#hearingPreferences_extraSecurityMeasuresDetails',
    },
  },

  enterInterpreter(details = 'French translator') {
    I.click(this.fields.interpreter.yes);
    I.fillField(this.fields.interpreter.details, details);
  },

  enterIntermediary() {
    I.click(this.fields.intermediaryNo);
  },

  enterLitigationIssues() {
    I.click(this.fields.litigationCapacityNo);
  },

  enterLearningDisability(details = 'learning difficulty') {
    I.click(this.fields.learningDisability.yes);
    I.fillField(this.fields.learningDisability.details, details);
  },

  enterWelshProceedings() {
    I.click(this.fields.welshNo);
  },

  enterExtraSecurityMeasures(details = 'Separate waiting rooms') {
    I.click(this.fields.security.yes);
    I.fillField(this.fields.security.details, details);
  },
};
