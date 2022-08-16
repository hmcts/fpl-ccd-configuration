'use strict';
const {I} = inject();
/*const assert = require('assert');
const output = require('codeceptjs').output;*/

module.exports = {

  sections: {

    hearing_urgency: {
      text: 'Hearing urgency',
      you_can_save_and_return_1_text: 'You can save and return to this page at any time.',
      you_can_save_and_return_2_text: 'Questions marked with a * need to be completed',
      you_can_save_and_return_3_text: 'before you can send your application.',
    },

    hearing_needed: {
      text: 'Hearing needed',
      when_do_you_need_the_hearing_text: '*When do you need a hearing? (Optional)',
      what_type_of_hearing_text: '*What type of hearing do you need? (Optional)',
      do_you_need_a_without_notice_hearing: '*Do you need a without notice hearing? (Optional)',
      do_you_need_a_hearing_with_reduced_notice: '*Do you need a hearing with reduced notice? (Optional)',
      are_respondents_aware_of_proceedings: '*Are respondents aware of proceedings? (Optional)',
    },
  },

  locators: {
    when_do_you_need_a_hearing: {xpath: '//input[@id=\'hearing_timeFrame-Same day\']'},
    when_do_you_need_a_hearing_reason : {xpath: '//textarea[@id=\'hearing_reason\']'},
    type_of_hearing_you_need: {xpath: '//input[@id=\'hearing_type-Standard case management hearing\']'},
    type_of_hearing_you_need_reason: {xpath: '//textarea[@id=\'hearing_type_GiveReason\']'},
    do_you_need_a_without_notice_hearing: {xpath: '//input[@id=\'hearing_withoutNotice_No\']'},
    do_you_need_a_hearing_with_reduced_notice: {xpath: '//input[@id=\'hearing_reducedNotice_No\']'},
    are_respondents_aware_of_proceedings: {xpath: '//input[@id=\'hearing_respondentsAware_No\']'},
    continue_button: {xapth: '//button[@class=\'button\']'},
    save_and_continue_button: {xpath: 'xpath = //button[@class=\'button\']'},
  },

  seeCCDCaseNumber(ccdCaseNumberPrefix, ccdCaseNumber) {
    I.see(ccdCaseNumberPrefix);
    I.see(I.uiFormatted(ccdCaseNumber));
  },

  verifyHearingUrgencyPage(caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.hearing_urgency.text);
    I.see(caseName);
    I.see(this.sections.hearing_urgency.you_can_save_and_return_1_text);
    I.see(this.sections.hearing_urgency.you_can_save_and_return_2_text);
    I.see(this.sections.hearing_urgency.you_can_save_and_return_3_text);

    I.see(this.sections.hearing_needed.text);
    I.see(this.sections.hearing_needed.when_do_you_need_the_hearing_text);
    I.see(this.sections.hearing_needed.what_type_of_hearing_text);
    I.see(this.sections.hearing_needed.do_you_need_a_without_notice_hearing);
    I.see(this.sections.hearing_needed.do_you_need_a_hearing_with_reduced_notice);
    I.see(this.sections.hearing_needed.are_respondents_aware_of_proceedings);
  },

  inputValuesForHearingUrgency() {
    I.checkOption(this.locators.when_do_you_need_a_hearing);
    I.fillField(this.locators.when_do_you_need_a_hearing_reason, 'When do you need a Hearing - Test Reason');
    I.checkOption(this.locators.type_of_hearing_you_need);
    I.fillField(this.locators.type_of_hearing_you_need_reason,'Type of hearing that you need - Test Reason');
    I.checkOption(this.locators.do_you_need_a_without_notice_hearing);
    I.checkOption(this.locators.do_you_need_a_hearing_with_reduced_notice);
    I.checkOption(this.locators.are_respondents_aware_of_proceedings);
  },

  verifyHearingUrgency(caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.hearing_urgency.text);
    I.see(caseName);
    I.see('Check your answers');
    I.see('Check the information below carefully.');
    I.see(this.hearing_needed.text);
    I.see('*When do you need a hearing?');
    I.see('Same day');
    I.see('*What type of hearing do you need?');
    I.see('Standard case management hearing');
    I.see('*Do you need a without notice hearing?');
    I.see('No');
    I.see('*Do you need a hearing with reduced notice?');
    I.see('No');
    I.see('*Are respondents aware of proceedings?');
    I.see('No');
  },

  clickContinueButton() {
    I.click('Continue');
  },

  clickSaveAndContinue() {
    I.click('Save and continue');
  },
};
