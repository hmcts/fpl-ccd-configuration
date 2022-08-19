'use strict';
const {I} = inject();
/*const assert = require('assert');
const output = require('codeceptjs').output;*/

module.exports = {

  sections: {

    respondents_details: {
      text: 'Respondents\' details',
    },

    respondents: {
      text: 'Respondents',
    },

    party : {
      text : 'Party',
      first_name : '*First name (Optional)',
      last_name : '*Last name (Optional)',
      date_of_birth : 'Date of birth (Optional)',
      date_of_birth_guidance_text : 'For example, 31 3 1980',
      day : 'Day',
      month : 'Month',
      year : 'Year',
      gender : 'Gender (Optional)',
      current_address_known : '*Current address known?',
    },

    telephone: {
      text: 'Telephone (Optional)',
      telephone_number:  'Telephone number (Optional)',
      telephone_number_guidance_note:  'For example, 020 2772 5772',
    },

    relationship_to_the_child: {
      text: 'Relationship to the child',
      what_is_the_respondents_relationship:  '*What is the respondent\'s relationship to the child or children in this case? (Optional)',
      what_is_the_respondents_relationship_guidance_text:  'Include: the name of the child or children, the respondent\'s relationship to them and any details if you\'re not sure this person has parental responsibility',
      do_you_need_contact_details_hidden_from_other_parties: 'Do you need contact details hidden from other parties? (Optional)',
    },

    ability_to_take_part_in_procedings: {
      text: 'Ability to take part in proceedings',
      do_you_believe_text_1:  'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the',
      do_you_believe_text_2:  'case)? (Optional)',
      do_you_have_legal_representation : '*Do they have legal representation? (Optional)',
    },
  },

  locators: {
    first_name: {xpath: '//input[@id=\'respondents1_0_party_firstName\']'},
    last_name : {xpath : '//input[@id=\'respondents1_0_party_lastName\']'},
    date_of_birth_day: {xpath: '//input[@id=\'dateOfBirth-day\']'},
    date_of_birth_month: {xpath: '//input[@id=\'dateOfBirth-month\']'},
    date_of_birth_year: {xpath: '//input[@id=\'dateOfBirth-year\']'},
    gender : {xpath : '//select[@id=\'respondents1_0_party_gender\']'},
    current_address_known: {xpath : '//input[@id=\'respondents1_0_party_addressKnow_No\']'},
    reason_the_address_not_known : {xpath : '//select[@id=\'respondents1_0_party_addressNotKnowReason\']'},
    do_you_have_legal_representation: {xpath: '//input[@id=\'respondents1_0_legalRepresentation_No\']'},
    respondent_relationship_with_child: {xpath: '//textarea[@id=\'respondents1_0_party_relationshipToChild\']'},
  },

  seeCCDCaseNumber(ccdCaseNumberPrefix, ccdCaseNumber) {
    I.see(ccdCaseNumberPrefix);
    I.see(I.uiFormatted(ccdCaseNumber));
  },

  verifyRespondentsDetails(caseId, caseName= 'Test Case Automation') {
    I.see(this.sections.respondents_details.text);
    I.see(caseName);
    I.see(this.sections.respondents.text);
    I.see(this.sections.party.first_name);
    I.see(this.sections.party.last_name);
    I.see(this.sections.party.date_of_birth);
    I.see(this.sections.party.date_of_birth_guidance_text);
    I.see(this.sections.party.day);
    I.see(this.sections.party.month);
    I.see(this.sections.party.year);
    I.see(this.sections.party.gender);

    I.see(this.sections.telephone.text);
    I.see(this.sections.telephone.telephone_number);
    I.see(this.sections.telephone.telephone_number_guidance_note);

    I.see(this.sections.relationship_to_the_child.text);
    I.see(this.sections.relationship_to_the_child.what_is_the_respondents_relationship);
    I.see(this.sections.relationship_to_the_child.what_is_the_respondents_relationship_guidance_text);
    I.see(this.sections.relationship_to_the_child.do_you_need_contact_details_hidden_from_other_parties);

    I.see(this.sections.ability_to_take_part_in_procedings.text);
    I.see(this.sections.ability_to_take_part_in_procedings.do_you_believe_text_1);
    I.see(this.sections.ability_to_take_part_in_procedings.do_you_believe_text_2);
    I.see(this.sections.ability_to_take_part_in_procedings.do_you_have_legal_representation);
  },

  inputValuesFoRespondentsDetails () {
    I.fillField(this.locators.first_name,'Test First Name');
    I.fillField(this.locators.last_name,'Test Last Name');
    I.fillField(this.locators.date_of_birth_day,'09');
    I.fillField(this.locators.date_of_birth_month,'06');
    I.fillField(this.locators.date_of_birth_year,'1910');
    I.selectOption(this.locators.gender, '1: Boy');
    I.checkOption(this.locators.current_address_known);
    I.selectOption(this.locators.reason_the_address_not_known,'1: No fixed abode');
    I.checkOption(this.locators.do_you_have_legal_representation);
    I.fillField(this.locators.respondent_relationship_with_child,'Foster Parent');
  },

  verifyRespondentsDetailsCheckYourAnswersPage (caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.orders_and_directions_sought.text);
    I.see(caseName);
    I.see('Check your answers');
    I.see('Check the information below carefully.');
    I.see('Child');
    I.see('Child 1');
    I.see('Party');
    I.see('*First name');
    I.see('First name');
    I.see('*Last name');
    I.see('Last name');
    I.see('*Date of birth');
    I.see('9 Jun 1975');
    I.see('*Gender');
    I.see('Boy');
  },

  clickContinueButton() {
    I.click('Continue');
  },

  clickSaveAndContinue() {
    I.click('Save and continue');
  },
};
