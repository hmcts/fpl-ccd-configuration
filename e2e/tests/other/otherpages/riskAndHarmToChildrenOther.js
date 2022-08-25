'use strict';
const {I} = inject();
/*const assert = require('assert');
const output = require('codeceptjs').output;*/

module.exports = {

  sections: {

    risk_and_harm_to_children: {
      text: 'Risk and harm to children',
      risk_and_harm_to_children_text: 'Risks and harm to children (Optional)',
      is_there_any_evidence_of_the_following: 'Is there evidence of any of the following?',
      physical_harm: 'Physical harm including non-accidental injury (Optional)',
      emotional_harm: 'Emotional harm (Optional)',
      sexual_abuse: 'Sexual abuse (Optional)',
      neglect: 'Neglect (Optional)',
    },
  },

  locators: {
    physical_harm: {xpath: '//input[@id=\'risks_physicalHarm_No\']'},
    emotional_harm: {xpath: '//input[@id=\'risks_emotionalHarm_No\']'},
    sexual_abuse: {xpath: '//input[@id=\'risks_sexualAbuse_No\']'},
    neglect: {xpath: '//input[@id=\'risks_neglect_No\']'},
  },

  seeCCDCaseNumber(ccdCaseNumberPrefix, ccdCaseNumber) {
    I.see(ccdCaseNumberPrefix);
    I.see(I.uiFormatted(ccdCaseNumber));
  },

  verifyRiskAndHarmToChildrenPage(caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.risk_and_harm_to_children.text);
    I.see(caseName);
    I.see(this.sections.risk_and_harm_to_children.risk_and_harm_to_children_text);
    I.see(this.sections.risk_and_harm_to_children.is_there_any_evidence_of_the_following);
    I.see(this.sections.risk_and_harm_to_children.physical_harm);
    I.see(this.sections.risk_and_harm_to_children.emotional_harm);
    I.see(this.sections.risk_and_harm_to_children.sexual_abuse);
    I.see(this.sections.risk_and_harm_to_children.neglect);
  },

  inputValuesRiskAndHarmToChildren() {
    I.checkOption(this.locators.physical_harm);
    I.checkOption(this.locators.emotional_harm);
    I.checkOption(this.locators.sexual_abuse);
    I.checkOption(this.locators.neglect);
  },

  verifyRiskAndharmToChildrenCheckYourAnswers(caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.risk_and_harm_to_children.text);
    I.see(caseName);
    I.see('Check your answers');
    I.see('Check the information below carefully.');
    I.see(this.sections.risk_and_harm_to_children.text);
    I.see('Physical harm including non-accidental');
    I.see('injury');
    I.see('No');
    I.see('Emotional harm');
    I.see('No');
    I.see('Sexual abuse');
    I.see('No');
    I.see('Neglect');
    I.see('No');
  },

  clickContinueButton() {
    I.click('Continue');
  },

  clickSaveAndContinue() {
    I.click('Save and continue');
  },
};
