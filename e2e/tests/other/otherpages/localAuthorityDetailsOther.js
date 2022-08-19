'use strict';
const {I} = inject();
/*const assert = require('assert');
const output = require('codeceptjs').output;*/

module.exports = {

  sections: {

    local_authority_details: {
      text: 'Local authority\'s details',
    },

    applicant_local_authority: {
      text: 'Applicant local authority',
      local_authority_name: '*Local authority\'s name (Optional)',
      group_email_address: 'Group email address (Optional)',
      legal_team_managers_name: 'Legal team manager\'s name and last name (Optional)',
      pba_number: '*PBA number (Optional)',
      pba_number_guidance_text: 'For example, PBA1234567',
      customer_reference: 'Customer reference (Optional)',
      client_code: 'Client code (Optional)',
    },

    address: {
      text: '*Address (Optional)',
      enter_a_uk_postcode: 'Enter a UK postcode',
      building_and_street: 'Building and Street (Optional)',
      address_line_2: 'Address Line 2 (Optional)',
      address_line_3: 'Address Line 3 (Optional)',
      town_or_city: 'Town or City (Optional)',
      county: 'County (Optional)',
      post_code_zip_code: 'Postcode/Zipcode (Optional)',
      country: 'Country (Optional)',
      phone_number: '*Phone number (Optional)',
    },
  },

  locators: {
    pba_number: {xpath: '//input[@id=\'localAuthority_pbaNumber\']'},
    phone_number: {xpath: '//input[@id=\'localAuthority_phone\']'},
    add_new: {xpath: '//button[.=\'Add new\']'},
    social_worker : {xpath :'//input[@id=\'localAuthorityColleagues_0_role-SOCIAL_WORKER\']'},
    full_name : {xpath : '//input[@id=\'localAuthorityColleagues_0_fullName\']'},
    email : {xpath : '//input[@id=\'localAuthorityColleagues_0_email\']'},
    colleague_phone_number : {xpath : '//input[@id=\'localAuthorityColleagues_0_phone\']'},
    send_them_case_update_notifications : {xpath : '//input[@id=\'localAuthorityColleagues_0_notificationRecipient_No\']'},
  },

  seeCCDCaseNumber(ccdCaseNumberPrefix, ccdCaseNumber) {
    I.see(ccdCaseNumberPrefix);
    I.see(I.uiFormatted(ccdCaseNumber));
  },

  verifyLocalAuthorityDetails(caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.applicant_local_authority.text);
    I.see(caseName);
    I.see(this.sections.applicant_local_authority.local_authority_name);
    I.see(this.sections.applicant_local_authority.group_email_address);
    I.see(this.sections.applicant_local_authority.legal_team_managers_name);
    I.see(this.sections.applicant_local_authority.pba_number);
    I.see(this.sections.applicant_local_authority.pba_number_guidance_text);
    I.see(this.sections.applicant_local_authority.customer_reference);
    I.see(this.sections.applicant_local_authority.client_code);
  },

  verifyLocalAuthorityDetailsColleagueScreen(caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.local_authority_details.text);
    I.see(caseName);
    I.see('Colleague');
  },

  verifyLocalAuthorityDetailsAddColleagueScreen() {
    I.see('Colleague');
    I.see('*Select their role (Optional)');
    I.see('*Full name');
    I.see('*Email (Optional)');
    I.see('Phone number (Optional)');
    I.see('*Send them case update notifications? (Optional)');
  },

  inputValuesForLocalAuthorityDetailsAddColleagueScreen() {
    I.checkOption(this.locators.social_worker);
    I.fillField(this.locators.full_name, 'Local Authority Full Name');
    I.fillField(this.locators.email, 'local_authority@hmcts.co.uk');
    I.checkOption(this.locators.send_them_case_update_notifications);
  },

  inputValuesForLocalAuthorityDetails() {
    I.fillField(this.locators.pba_number, 'PBA0082848');
    I.fillField(this.locators.phone_number, '+44 711784134154');
  },

  verifyLocalAuthorityCheckYourAnswersPage(caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.orders_and_directions_sought.text);
    I.see(caseName);
    I.see('Check your answers');
    I.see('Check the information below carefully.');
    I.see(this.applicant_local_authority.text);
    I.see('*Local authority\'s name');
    I.see('fpla_test_friday27');
    I.see('*Address');
    I.see('Building and Street');
    I.see('Flat 1, Swansea Apartments');
    I.see('Address Line 2');
    I.see('Swansea Central Square 11');
    I.see('Address Line 3');
    I.see('40 Fleet street');
    I.see('Town or City');
    I.see('Swansea');
    I.see('County');
    I.see('Swansea');
    I.see('CR0 2GE');
    I.see('Country');
    I.see('United Kingdom');
    I.see('*Phone number');
    I.see('+44 711784134154');
  },

  clickContinueButton() {
    I.click('Continue');
  },

  clickSaveAndContinue() {
    I.click('Save and continue');
  },

  clickAddNewColleague() {
    I.click(this.locators.add_new);
  },
};
