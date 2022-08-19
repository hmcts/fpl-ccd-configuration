'use strict';
const {I} = inject();
/*const assert = require('assert');
const output = require('codeceptjs').output;*/

module.exports = {

  sections: {

    local_authority_details: {
      text: 'Child\'s details',
    },

    child: {
      text: 'Child',
    },

    party : {
      text : 'Party',
      first_name : '*First name (Optional)',
      last_name : '*Last name (Optional)',
      date_of_birth : '*Date of birth (Optional)',
      date_of_birth_guidance_text : 'For example, 31 3 2016',
      day : 'Day',
      month : 'Month',
      year : 'Year',
      gender : '*Gender (Optional)',
      childs_living_situation : 'Child\'s living situation (Optional)',
      key_dates_for_this_child : 'Key dates for this child (Optional)',
      key_dates_for_this_child_guidance_text_1 : 'List any events HMCTS will need to take into account when scheduling hearings. For example, child starting primary school or',
      key_dates_for_this_child_guidance_text_2 : 'taking GCSEs.',
      brief_summary_of_care : 'Brief summary of care and contact plan (Optional)',
      brief_summary_of_care_guidance_text_1 : 'For example, place baby in local authority foster care until further assessments are completed. Supervised contact for parents',
      brief_summary_of_care_guidance_text_2 : 'will be arranged.',
      are_you_considering_adoption_at_this_stage : 'Are you considering adoption at this stage? (Optional)',
      mothers_full_name : 'Mother\'s full name (Optional)',
      fathers_full_name : 'Father\'s full name (Optional)',
      does_the_father_have_parental_responsibility : 'Does the father have parental responsibility? (Optional)',
      name_of_social_worker : 'Name of social worker (Optional)',
    },

    social_workers_telephone_number: {
      text: 'Social worker\'s telephone number (Optional)',
      telephone_number:  'Telephone number (Optional)',
      telephone_number_guidance_note:  'For example, 020 2772 5772',
      name_of_person_to_contact:  'Name of person to contact (Optional)',
      does_the_child_have_any_additional_needs : 'Does the child have any additional needs? (Optional)',
      does_the_child_have_any_additional_needs_guidance_notes : 'For example, child has severe autism and learning disabilities.Special needs for autism should be taken into account',
      do_you_need_contact_details_hidden_from_other_parties : 'Do you need contact details hidden from other parties? (Optional)',
    },

    ability_to_take_part_in_procedings: {
      text: 'Ability to take part in proceedings',
      do_you_believe_text_1:  'Do you believe this child will have problems with litigation capacity (understanding what\'s hapenning in the',
      do_you_believe_text_2:  'case)? (Optional)',
    },
  },

  locators: {
    first_name: {xpath: '//input[@id=\'children1_0_party_firstName\']'},
    last_name : {xpath : '//input[@id=\'children1_0_party_lastName\']'},
    date_of_birth_day: {xpath: '//input[@id=\'dateOfBirth-day\']'},
    date_of_birth_month: {xpath: '//input[@id=\'dateOfBirth-month\']'},
    date_of_birth_year: {xpath: '//input[@id=\'dateOfBirth-year\']'},
    gender : {xpath : '//select[@id=\'children1_0_party_gender\']'},

  },

  seeCCDCaseNumber(ccdCaseNumberPrefix, ccdCaseNumber) {
    I.see(ccdCaseNumberPrefix);
    I.see(I.uiFormatted(ccdCaseNumber));
  },

  verifyChildDetails(caseId, caseName= 'Test Case Automation') {
    I.see(this.party.text);
    I.see(caseName);
    I.see(this.sections.party.first_name);
    I.see(this.sections.party.last_name);
    I.see(this.sections.party.date_of_birth);
    I.see(this.sections.party.date_of_birth_guidance_text);
    I.see(this.sections.party.day);
    I.see(this.sections.party.month);
    I.see(this.sections.party.year);
    I.see(this.sections.party.gender);
    I.see(this.sections.party.childs_living_situation);
    I.see(this.sections.party.key_dates_for_this_child);
    I.see(this.sections.party.key_dates_for_this_child_guidance_text_1);
    I.see(this.sections.party.key_dates_for_this_child_guidance_text_2);
    I.see(this.sections.party.brief_summary_of_care);
    I.see(this.sections.party.brief_summary_of_care_guidance_text_1);
    I.see(this.sections.party.brief_summary_of_care_guidance_text_2);
    I.see(this.sections.party.are_you_considering_adoption_at_this_stage);
    I.see(this.sections.party.mothers_full_name);
    I.see(this.sections.party.fathers_full_name);
    I.see(this.sections.party.does_the_father_have_parental_responsibility);
    I.see(this.sections.party.name_of_social_worker);
    I.see(this.sections.ability_to_take_part_in_procedings.text);
    I.see(this.sections.ability_to_take_part_in_procedings.do_you_believe_text_1);
    I.see(this.sections.ability_to_take_part_in_procedings.do_you_believe_text_2);
  },

  inputValuesForChildDetails () {
    I.fillField(this.locators.first_name,'Test First Name');
    I.fillField(this.locators.last_name,'Test Last Name');
    I.fillField(this.locators.date_of_birth_day,'09');
    I.fillField(this.locators.date_of_birth_month,'06');
    I.fillField(this.locators.date_of_birth_year,'1910');
    I.selectOption(this.locators.gender, '1: Boy');

  },

  verifyChildDetailsCheckYourAnswersPage (caseId, caseName = 'Test Case Automation') {
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
