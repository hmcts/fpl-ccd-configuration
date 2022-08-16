'use strict';
const {I} = inject();
/*const assert = require('assert');
const output = require('codeceptjs').output;*/

module.exports = {

  sections: {

    orders_and_directions_sought: {
      text: 'Orders and directions sought',
      you_can_save_and_return_1_text: 'You can save and return to this page at any time.',
      you_can_save_and_return_2_text: 'Questions marked with a * need to be completed',
      you_can_save_and_return_3_text: 'before you can send your application.',
    },

    orders_and_directions_needed: {
      text: 'Orders and directions needed',
      which_orders_do_you_need_text:  '*Which orders do you need? (Optional)',
    },

    directions: {
      text: 'Directions',
      do_you_need_any_other_directions: '*Do you need any other directions? (Optional)',
      which_court_are_you_isssuing_for: '*Which court are you issuing for? (Optional)',
    },


  },

  locators: {
    order_type: {xpath: '//input[@id=\'orders_orderType-CARE_ORDER\']'},
    directions_no: {xpath: '//input[@id=\'orders_directions_No\']'},
    applying_court: {xpath: '//select[@id=\'orders_court\']'},
    continue_button: {xapth: '//button[@class=\'button\']'},
    save_and_continue_button: {xpath : 'xpath = //button[@class=\'button\']'},
  },

  seeCCDCaseNumber(ccdCaseNumberPrefix, ccdCaseNumber) {
    I.see(ccdCaseNumberPrefix);
    I.see(I.uiFormatted(ccdCaseNumber));
  },

  verifyOrdersAndDirectionsPage(caseId, caseName= 'Test Case Automation') {
    I.see(this.sections.orders_and_directions_sought.text);
    I.see(caseName);
    I.see(this.sections.orders_and_directions_sought.you_can_save_and_return_1_text);
    I.see(this.sections.orders_and_directions_sought.you_can_save_and_return_2_text);
    I.see(this.sections.orders_and_directions_sought.you_can_save_and_return_3_text);

    I.see(this.sections.orders_and_directions_needed.text);
    I.see(this.sections.orders_and_directions_needed.which_orders_do_you_need_text);

    I.see(this.sections.directions.text);
    I.see(this.sections.directions.do_you_need_any_other_directions);
    I.see(this.sections.directions.which_court_are_you_isssuing_for);
  },

  inputValuesForOrdersSought (orderType) {
    I.checkOption(`//input[@id='${orderType}']`);
    I.checkOption('//input[@id=\'orders_directions_No\']');
    I.selectOption(this.locators.applying_court, '58: 344');
  },

  verifyOrdersAndDirections (caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.orders_and_directions_sought.text);
    I.see(caseName);
    I.see('Check your answers');
    I.see('Check the information below carefully.');
    I.see(this.orders_and_directions_needed.text);
    I.see('*Which orders do you need?');
    I.see('Care order');
    I.see('*Do you need any other directions?');
    I.see('No');
    I.see('*Which court are you issuing for?');
    I.see('Swansea');
  },

  clickContinueButton() {
    I.click('Continue');
  },

  clickSaveAndContinue() {
    I.click('Save and continue');
  },
};
