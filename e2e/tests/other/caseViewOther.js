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
      guidance_text_2: 'In emergency cases, you can send your application without this information',
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
      other_proceedings_text: 'Other proceedings',
      international_element_text: 'International element',
      other_people_in_the_case_text: 'Other people in the case',
      court_services_needed_text: 'Court services needed',
      welsh_language_requirements_text: 'Welsh language requirements',
    },

    send_application: {
      text: 'Send application',
      submit_application_text: 'Submit application',
      why_cant_i_submit_my_application_text: 'Why can\'t I submit my application?',
    },

    submit_application: {
      text: 'Submit application',
      declaration_text: 'Declaration',
      declaration_undertaking_text: 'I, kurt@swansea.gov.uk (local-authority), believe that the facts stated in this application are true.',
      application_fee_to_pay: 'Application fee to pay',
      fee_to_pay  : '£2,215.00',
    },

    application_sent: {
      text: 'Application sent',
      declaration_text: 'Declaration',
      what_happens_next_text: 'What happens next',
      we_will_check_your_application_text_1: 'We’ll check your application – we might need to ask you more questions,',
      we_will_check_your_application_text_2: 'or send it back to you to amend.',
      if_we_have_no_questions_text_1 : 'If we have no questions, we’ll send your application to the local court',
      if_we_have_no_questions_text_2  : 'gatekeeper.',
      you_can_contact_us : 'You can contact us at contactFPL@justice.gov.uk.',
      help_us_improve_this_service : 'Help us improve this service',
      tell_us_how_this_service : 'Tell us how this service was today on our feedback form.',
    },
  },

  locators: {
    test_case: {xpath: '//h1[.=\'Test case \']'},
    ccd_id: {xpath: '//strong[.=\'CCD ID:\']'},
    start_application_tab: {xpath: '//div[contains(text(),\'Start application\')]'},
    why_cant_i_submit_my_application: {xpath: '//p[.="Why can\'t I submit my application?"]'},
    change_case_name: {xpath: '//a[.=\'Change case name\']'},
    orders_and_directions_sought: {xpath: '//div[@class=\'width-50\']//a[.=\'Orders and directions sought\']'},
    grounds_for_application: {xpath: '//div[@class=\'width-50\']//a[.=\'Grounds for the application\']'},
    hearing_urgency_link: {xpath: '//div[@class=\'width-50\']//a[.=\'Hearing urgency\']'},
    local_authority_page_link: {xpath: '//p[11]/a[.="Local authority\'s details"]'},
    childs_details_link: {xpath: '//p[12]/a[.="Child\'s details"]'},
    respondents_details_link: {xpath: '//p[13]/a[.="Respondents\' details"]'},
    allocation_proposal_link: {xpath: '//p[14]/a[.=\'Allocation proposal\']'},
    risks_and_harm_to_children_link : {xpath: '//a[.=\'Risk and harm to children\']'},
    view_application_tab: {xpath: '//div[contains(text(),\'View application\')]'},
    submit_application_link: {xpath: '//a[contains(text(),\'Submit application\')]'},
    submission_consent : {xpath: '//input[@id=\'submissionConsent-agree\']'},
    close_and_return_to_case_details : {xpath : '//button[contains(text(),\'Close and Return to case details\')]'},
    cancel_link: {xpath: '//a[.=\'Cancel\']'},
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

  clickStartApplicationTab() {
    I.click(this.locators.start_application_tab);
  },

  clickWhyCantISubmitMyApplication() {
    I.click(this.locators.why_cant_i_submit_my_application);
  },

  clickChangeCaseName() {
    I.click(this.locators.change_case_name);
  },

  clickOrdersAndDirectionsLink() {
    I.click(this.locators.orders_and_directions_sought);
  },

  clickHearingsUrgencyLink() {
    I.click(this.locators.hearing_urgency_link);
  },

  clickGroundsForApplicationLink() {
    I.click(this.locators.grounds_for_application);
  },

  clickLocalAuthorityLink() {
    I.click(this.locators.local_authority_page_link);
  },

  clickChildsDetailsLink() {
    I.click(this.locators.childs_details_link);
  },

  clickRespondentsDetailsLink() {
    I.click(this.locators.respondents_details_link);
  },

  clickAllocationDetailsLink() {
    I.click(this.locators.allocation_proposal_link);
  },

  clickApplicationViewTab() {
    I.click(this.locators.view_application_tab);
  },

  clickSubmitApplicationLink() {
    I.click(this.locators.submit_application_link);
  },

  checkSubmissionConfirmation() {
    I.checkOption(this.locators.submission_consent);
  },

  clickCloseAndReturnToCaseDetails() {
    I.click(this.locators.close_and_return_to_case_details);
  },

  clickRisksAndHarmToChildren() {
    I.click(this.locators.risks_and_harm_to_children_link);
  },

  clickContinueButton() {
    I.click('Continue');
  },

  clickSubmitButton() {
    I.click('Submit');
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

  verifySendApplicationSection(checkWhyCantISubmitMyApplication = true) {
    I.see(this.sections.send_application.text);
    I.see(this.sections.send_application.submit_application_text);
    if (checkWhyCantISubmitMyApplication) {
      I.see(this.sections.send_application.why_cant_i_submit_my_application_text);
    }
  },

  verifyStartApplicationTabDetails(bannerText,whyCantISubmitMyApplication = true) {
    if (bannerText !== '') {
      I.see(bannerText);
    }
    this.verifyAddApplicationDetailsSection();
    this.verifyAddGroundsForApplicationSection();
    this.verifyAddApplicationDocumentsSection();
    this.verifyInformationAboutPartiesSection();
    this.verifyAddCourtRequirementsSection();
    this.verifyAddAdditionalInformationSection();
    this.verifySendApplicationSection(whyCantISubmitMyApplication);
  },

  verifyMandatoryRequiredErrorMessages(errors) {
    for (const error of errors) {
      I.see(error);
    }
  },

  verifyViewApplicationScreen() {

    I.see('Application details');
    I.see('Orders and directions sought');
    I.see('Orders and directions needed');
    I.see('Which orders do you need?');
    I.see('Care order');
    I.see('Directions');
    I.see('Do you need any other directions?');
    I.see('No');
    I.see('Which court are you issuing for?');
    I.see('Swansea');
    I.see('Make changes to orders and directions sought');
    I.see('Hearing urgency');
    I.see('Hearing needed');
    I.see('When do you need a hearing?');
    I.see('Same day');
    I.see('What type of hearing do you need?');
    I.see('Give reason');
    I.see('When do you need a Hearing - Test Reason');
    I.see('What type of hearing do you need?');
    I.see('Standard case management hearing');
    I.see('Give reason');
    I.see('Type of hearing that you need - Test Reason');
    I.see('Do you need a without notice hearing?');
    I.see('No');
    I.see('Do you need a hearing with reduced notice?');
    I.see('No');
    I.see('Are respondents aware of proceedings?');
    I.see('No');
    I.see('Make changes to hearing urgency');
    I.see('Grounds for the application');
    I.see('Threshold criteria');
    I.see('How does this case meet the threshold criteria?');
    I.see('The child concerned is suffering or is likely to suffer significant harm because they are:');
    I.see('Beyond parental control');
    I.see('Give details of how this case meets the threshold criteria');
    I.see('Grounds For An Application - Test Reason');
    I.see('Make changes to threshold criteria');
    I.see('Information about the parties');
    I.see('Your organisation details');
    I.see('Local authority');
    I.see('Local authority 1');
    I.see('Name');
    I.see('fpla_test_friday27');
    I.see('PBA number');
    I.see('PBA0082848');
    I.see('Address');
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
    I.see('Postcode/Zipcode');
    I.see('CR0 2GE');
    I.see('Country');
    I.see('United Kingdom');
    I.see('Make changes to local authority details');
    I.see('The child\'s details');
    I.see('Child');
    I.see('Child 1');
    I.see('Party');
    I.see('First name');
    I.see('Test First Name');
    I.see('Last name');
    I.see('Test Last Name');
    I.see('Date of birth');
    I.see('9 Jun 1910');
    I.see('Gender');
    I.see('Boy');
    I.see('Make changes to the child\'s details');
    I.see('Respondents\' details');
    I.see('Respondents');
    I.see('Respondents 1');
    I.see('Party');
    I.see('First name');
    I.see('Test First Name');
    I.see('Last name');
    I.see('Test Last Name');
    I.see('Date of birth');
    I.see('9 Jun 1910');
    I.see('*Current address known?');
    I.see('No');
    I.see('*Reason the address is not known');
    I.see('No fixed abode');
    I.see('Do they have legal representation?');
    I.see('No');
    I.see('Make changes to the respondents\' details');
  },

  verifySubmitApplicationScreen() {
    I.see(this.sections.submit_application.text);
    I.see(this.sections.submit_application.declaration_text);
    I.see(this.sections.submit_application.declaration_undertaking_text);
    I.see(this.sections.submit_application.application_fee_to_pay);
    I.see(this.sections.submit_application.fee_to_pay);
  },

  verifyApplicationSentScreen() {
    I.see(this.sections.submit_application.text);
    I.see(this.sections.application_sent.text);
    //I.see(caseName);
    I.see(this.sections.application_sent.what_happens_next_text);
    I.see(this.sections.application_sent.we_will_check_your_application_text_1);
    I.see(this.sections.application_sent.we_will_check_your_application_text_2);
    I.see(this.sections.application_sent.if_we_have_no_questions_text_1);
    I.see(this.sections.application_sent.if_we_have_no_questions_text_2);
    I.see(this.sections.application_sent.you_can_contact_us);
    I.see(this.sections.application_sent.help_us_improve_this_service);
    I.see(this.sections.application_sent.tell_us_how_this_service);
  },
};
