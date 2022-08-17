'use strict';
const {I} = inject();
/*const assert = require('assert');
const output = require('codeceptjs').output;*/

module.exports = {

  sections: {

    add_application_details: {
      text: 'Add application details',
      change_case_name_text: 'Change case name',
      order_and_directions_text: 'Orders and directions sought',
      hearing_urgency_text: 'Hearing urgency',

    },

    add_grounds_for_the_application: {
      text: 'Add grounds for the application',

      risk_and_harm_to_children_text: 'Risk and harm to children',
      risk_and_harm_to_children_guidance_text: 'In emergency cases, you can send your application without this information',

      factors_affecting_parenting_text: 'Factors affecting parenting',
      factors_affecting_parenting_guidance_text: 'In emergency cases, you can send your application without this information',

    },

    add_application_documents: {
      text: 'Add application documents',
      guidance_text_1: 'For example, SWET, social work chronology and care plan',
      guidance_text_2:  'In emergency cases, you can send your application without this information',
      upload_documents_text: 'Upload documents',
    },

    add_information_about_the_parties: {
      text: 'Add information about the parties',
      local_authoritys_details_text: 'Local authority\'s details',
      childs_details_text: 'Child\'s details',
      respondents_details_text: 'Respondents\' details',
    },

    add_court_requirements: {
      text: 'Add court requirements',
      allocation_proposal_text: 'Allocation proposal',
    },

    add_additional_information: {
      text: 'Add additional information',
      guidance_text: 'Only complete if relevant',
      other_proceedings_text : 'Other proceedings',
      international_element_text : 'International element',
      other_people_in_the_case_text : 'Other people in the case',
      court_services_needed_text : 'Court services needed',
      welsh_language_requirements_text: 'Welsh language requirements',
    },

    send_application: {
      text: 'Send application',
      submit_application_text : 'Submit application',
      why_cant_i_submit_my_application_text : 'Why can\'t I submit my application?',
    },
  },

  locators: {
    test_case: {xpath: '//h1[.=\'Test case \']'},
    ccd_id: {xpath: '//strong[.=\'CCD ID:\']'},
    start_application_tab: {xpath: '//div[contains(text(),\'Start application\')]'},
    why_cant_i_submit_my_application: {xpath: '//p[.="Why can\'t I submit my application?"]'},
    change_case_name : {xpath: '//a[.=\'Change case name\']'},
    orders_and_directions_sought: {xpath : '//div[@class=\'width-50\']//a[.=\'Orders and directions sought\']'},
    grounds_for_application: {xpath: '//div[@class=\'width-50\']//a[.=\'Grounds for the application\']'},
    hearing_urgency_link : {xpath : '//div[@class=\'width-50\']//a[.=\'Hearing urgency\']'},
    cancel_link : {xpath: '//a[.=\'Cancel\']'},
  },

  seeCCDCaseNumber(ccdCaseNumberPrefix, ccdCaseNumber) {
    I.see(ccdCaseNumberPrefix);
    I.see(I.uiFormatted(ccdCaseNumber));
  },

  verifyCaseViewHeaderSection(caseId) {
    I.see('Test case');
    this.seeCCDCaseNumber('CCD ID: #', caseId);
  },

  clickCancelLink()  {
    I.click(this.locators.cancel_link);
  },

  clickStartApplicationTab()  {
    I.click(this.locators.start_application_tab);
  },

  clickWhyCantISubmitMyApplication() {
    I.click(this.locators.why_cant_i_submit_my_application);
  },

  clickChangeCaseName() {
    I.click(this.locators.change_case_name);
  },

  clickOrdersAndDirectionsLink()  {
    I.click(this.locators.orders_and_directions_sought);
  },

  clickHearingsUrgencyLink()  {
    I.click(this.locators.hearing_urgency_link);
  },

  clickGroundsForApplicationLink()  {
    I.click(this.locators.grounds_for_application);
  },

  verifyAddApplicationDetailsSection() {
    I.see(this.sections.add_application_details.text);
    I.see(this.sections.add_application_details.change_case_name_text);
    I.see(this.sections.add_application_details.order_and_directions_text);
    I.see(this.sections.add_application_details.hearing_urgency_text);
  },

  verifyAddGroundsForApplicationSection() {
    I.see(this.sections.add_grounds_for_the_application.text);
    I.see(this.sections.add_grounds_for_the_application.risk_and_harm_to_children_text);
    I.see(this.sections.add_grounds_for_the_application.risk_and_harm_to_children_guidance_text);
    I.see(this.sections.add_grounds_for_the_application.factors_affecting_parenting_text);
    I.see(this.sections.add_grounds_for_the_application.factors_affecting_parenting_guidance_text);
  },

  verifyAddApplicationDocumentsSection() {
    I.see(this.sections.add_application_documents.text);
    I.see(this.sections.add_application_documents.guidance_text_1);
    I.see(this.sections.add_application_documents.guidance_text_2);
    I.see(this.sections.add_application_documents.upload_documents_text);
  },

  verifyInformationAboutPartiesSection() {
    I.see(this.sections.add_information_about_the_parties.text);
    I.see(this.sections.add_information_about_the_parties.childs_details_text);
    I.see(this.sections.add_information_about_the_parties.respondents_details_text);
  },

  verifyAddCourtRequirementsSection() {
    I.see(this.sections.add_court_requirements.text);
    I.see(this.sections.add_court_requirements.allocation_proposal_text);
  },

  verifyAddAdditionalInformationSection() {
    I.see(this.sections.add_additional_information.text);
    I.see(this.sections.add_additional_information.guidance_text);
    I.see(this.sections.add_additional_information.other_proceedings_text);
    I.see(this.sections.add_additional_information.international_element_text);
    I.see(this.sections.add_additional_information.other_people_in_the_case_text);
    I.see(this.sections.add_additional_information.court_services_needed_text);
    I.see(this.sections.add_additional_information.welsh_language_requirements_text);
  },

  verifySendApplicationSection() {
    I.see(this.sections.send_application.text);
    I.see(this.sections.send_application.submit_application_text);
    I.see(this.sections.send_application.why_cant_i_submit_my_application_text);
  },

  verifyStartApplicationTabDetails(bannerText) {
    if (bannerText !== '') {
      I.see(bannerText);
    }
    this.verifyAddApplicationDetailsSection();
    this.verifyAddGroundsForApplicationSection();
    this.verifyAddApplicationDocumentsSection();
    this.verifyInformationAboutPartiesSection();
    this.verifyAddCourtRequirementsSection();
    this.verifyAddAdditionalInformationSection();
    this.verifySendApplicationSection();
  },

  verifyMandatoryRequiredErrorMessages(errors) {
    for (const error of errors) {
      I.see(error);
    }
  },
};
