const I = actor();

module.exports = {

  fields: {
    interpreter: {
      yes: '#attendHearing_interpreter-Yes',
      details: '#attendHearing_interpreterDetails'
    },
    intermediaryNo: '#attendHearing_intermediary-No',
    litigationCapacityNo: '#attendHearing_litigation-No',
    learningDisability: {
      yes: '#attendHearing_learningDisability-Yes',
      details: '#attendHearing_learningDisabilityDetails'
    },
    welshNo: '#attendHearing_welsh-No',
    security: {
      yes: '#attendHearing_security-Yes',
      details: '#attendHearing_securityDetails'
    }
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

  enterSecurity(details = 'Separate waiting rooms') {
    I.click(this.fields.security.yes);
    I.fillField(this.fields.security.details, details);
  }
};
