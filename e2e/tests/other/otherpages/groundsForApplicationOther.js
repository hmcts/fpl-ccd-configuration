'use strict';
const {I} = inject();
/*const assert = require('assert');
const output = require('codeceptjs').output;*/

module.exports = {

  sections: {

    grounds_for_application: {
      text: 'Grounds for the application',
      you_can_save_and_return_1_text: 'You can save and return to this page at any time.',
      you_can_save_and_return_2_text: 'Questions marked with a * need to be completed',
      you_can_save_and_return_3_text: 'before you can send your application.',
    },

    how_does_this_case_meet: {
      text: 'How does this case meet the threshold criteria?',
      the_child_concerned_text: '*The child concerned is suffering or is likely to suffer significant harm because they are: (Optional)',
      the_child_concerned_guidance_notes: 'Select all that apply',
      give_details_of_how_this_case : '*Give details of how this case meets the threshold criteria (Optional)',
    },
  },

  locators: {
    threshold_criteria: {xpath: '//input[@id=\'grounds_thresholdReason-beyondControl\']'},
    details_of_threshold_criteria : {xpath: '//textarea[@id=\'grounds_thresholdDetails\']'},
    continue_button: {xapth: '//button[@class=\'button\']'},
    save_and_continue_button: {xpath: 'xpath = //button[@class=\'button\']'},
  },

  seeCCDCaseNumber(ccdCaseNumberPrefix, ccdCaseNumber) {
    I.see(ccdCaseNumberPrefix);
    I.see(I.uiFormatted(ccdCaseNumber));
  },

  verifyGroundsForApplicationPage(caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.grounds_for_application.text);
    I.see(caseName);
    I.see(this.sections.grounds_for_application.you_can_save_and_return_1_text);
    I.see(this.sections.grounds_for_application.you_can_save_and_return_2_text);
    I.see(this.sections.grounds_for_application.you_can_save_and_return_3_text);

    I.see(this.sections.how_does_this_case_meet.text);
    I.see(this.sections.how_does_this_case_meet.the_child_concerned_text);
    I.see(this.sections.how_does_this_case_meet.the_child_concerned_guidance_notes);
    I.see(this.sections.how_does_this_case_meet.give_details_of_how_this_case);
  },

  inputValuesGroundsForApplication() {
    I.checkOption(this.locators.threshold_criteria);
    I.fillField(this.locators.details_of_threshold_criteria, 'Grounds For An Application - Test Reason');
  },

  verifyGroundsForApplicationCheckYourAnswers(caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.grounds_for_application.text);
    I.see(caseName);
    I.see('Check your answers');
    I.see('Check the information below carefully.');
    I.see(this.how_does_this_case_meet.text);
    I.see(this.the_child_concerned_text);
    I.see('*The child concerned is suffering or is likely to suffer significant harm because they are:');
    I.see('*Give details of how this case meets the threshold criteria');
  },

  clickContinueButton() {
    I.click('Continue');
  },

  clickSaveAndContinue() {
    I.click('Save and continue');
  },
};
