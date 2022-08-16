'use strict';
const {I} = inject();
//const assert = require('assert');

module.exports = {

  sections: {

    change_case_name: {
      title_text: 'Change case name',
      test_case_text: 'Test case',
      case_name_pre_text_1: 'You can save and return to this page at any time. Questions marked with a * need to be ',
      case_name_pre_text_2: 'completed before you can send your application.',
      case_name_label: '*Case name (Optional)',
      case_name_guidance_text: 'Include the local authority name and respondent\'s last name. For example, Endley Council v Smith/Tate/Jones',
    },

    change_case_name_confirmation: {
      check_your_answers_text: 'Check your answers',
      check_the_information_below_text: 'Check the information below carefully.',
      case_name_text: '*Case name',
    },
  },

  locators: {
    case_name: {xpath: '//input[@id=\'caseName\']'},
    previous_button: {xpath: '//button[@class=\'button button-secondary\']'},
    cancel_link: {xpath: '//a[.=\'Cancel\']'},
    continue_button: {xpath: '//button[@class=\'button\']'},
    change_link: {xpath: '//span[.=\'Change\']'},
    save_and_continue_button: {xpath: '//button[@class=\'button\']'},
  },

  seeCCDCaseNumber(ccdCaseNumberPrefix, ccdCaseNumber) {
    I.see(ccdCaseNumberPrefix);
    I.see(I.uiFormatted(ccdCaseNumber));
  },

  verifyCaseViewHeaderSection(caseId) {
    I.see('Test case');
    this.seeCCDCaseNumber('CCD ID: #', caseId);
  },

  clickCancelLink() {
    I.click(this.locators.cancel_link);
  },

  clickContinueButton() {
    I.click(this.locators.continue_button);
  },

  clickChangeLink() {
    I.click(this.locators.change_link);
  },

  verifyChangeCaseNameScreen(caseId, caseName = 'Test Case Automation') {
    I.see('Change case name');
    I.see('Test case');
    this.verifyCaseViewHeaderSection(caseId);
    I.see(this.sections.change_case_name.case_name_pre_text_1);
    I.see(this.sections.change_case_name.case_name_pre_text_2);
    I.see(this.sections.change_case_name.case_name_guidance_text);
    I.fillField(this.locators.case_name, caseName);
    this.clickContinueButton(this.locators.continue_button);
  },

  async verifyChangeYourCaseNameCheckYourAnswersPage(caseId, caseName = 'Test Case Automation', changeRequiredFlag = false) {
    I.see(this.sections.change_case_name.title_text);
    /*const pageHeading = await I.grabTextFrom('//h1');
    console.log('The value of the pageHeading : '+pageHeading);
    if (pageHeading !== `${caseName}`) {
      throw new Error('The Case Name is not as expected...');
    }*/
    I.see(caseName);
    this.seeCCDCaseNumber('CCD ID: #', caseId);
    I.see(this.sections.change_case_name_confirmation.check_your_answers_text);
    I.see(this.sections.change_case_name_confirmation.check_the_information_below_text);
    I.see(caseName);
    if (changeRequiredFlag) {
      this.clickChangeLink();
    } else {
      this.clickContinueButton();
    }
  },
};
