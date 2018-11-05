const I = actor();

module.exports = {

  enterInterpreter(details = 'French translator') {
    I.click('#hearingPreferences_interpreter-Yes');
    I.fillField('#hearingPreferences_interpreterDetails', details);
  },

  enterIntermediary() {
    I.click('#hearingPreferences_intermediary-No');
  },

  enterLitigationIssues() {
    I.click('#hearingPreferences_litigation-No');
  },

  enterLearningDisability(details = 'learning difficulty') {
    I.click('#hearingPreferences_learningDisability-Yes');
    I.fillField('#hearingPreferences_learningDisabilityDetails', details);
  },

  enterWelshProceedings() {
    I.click('#hearingPreferences_welsh-No');
  },

  enterExtraSecurityMeasures(details = 'Separate waiting rooms') {
    I.click('#hearingPreferences_extraSecurityMeasures-Yes');
    I.fillField('#hearingPreferences_extraSecurityMeasuresDetails', details);
  },
};
