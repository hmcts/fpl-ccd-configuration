'use strict';
const {I} = inject();
/*const assert = require('assert');
const output = require('codeceptjs').output;*/

module.exports = {

  sections: {

    allocation_proposal: {
      text: 'Allocation proposal',
      you_can_save_and_return_1_text: 'You can save and return to this page at any time.',
      you_can_save_and_return_2_text: 'Questions marked with a * need to be completed',
      you_can_save_and_return_3_text: 'before you can send your application.',

      completed_by_a_solicitor_1_text : 'This should be completed by a solicitor with good',
      completed_by_a_solicitor_2_text : 'knowledge of the case. Use the President\'s Guidance',
      completed_by_a_solicitor_3_text : 'and schedule on allocation and gatekeeping to make',
      completed_by_a_solicitor_4_text : 'your recommendation.',

      which_level_of_judge : '*Which level of judge do you recommend for this case?',
      give_reason : '*Give reason (Optional)',
    },
  },

  locators: {
    allocation_proposal: {xpath: '//input[@id=\'allocationProposal_proposal-Circuit judge\']'},
    proposal_reason : {xpath: '//textarea[@id=\'allocationProposal_proposalReason\']'},
    continue_button: {xpath: '//button[@class=\'button\']'},
    save_and_continue_button: {xpath: 'xpath = //button[@class=\'button\']'},
  },

  seeCCDCaseNumber(ccdCaseNumberPrefix, ccdCaseNumber) {
    I.see(ccdCaseNumberPrefix);
    I.see(I.uiFormatted(ccdCaseNumber));
  },

  verifyAllocationProposalPage(caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.allocation_proposal.text);
    I.see(caseName);
    I.see(this.sections.allocation_proposal.you_can_save_and_return_1_text);
    I.see(this.sections.allocation_proposal.you_can_save_and_return_2_text);
    I.see(this.sections.allocation_proposal.you_can_save_and_return_3_text);

    I.see(this.sections.allocation_proposal.text);
    I.see(this.sections.allocation_proposal.which_level_of_judge);
    I.see(this.sections.allocation_proposal.give_reason);
  },

  inputValuesAllocationProposal() {
    I.checkOption(this.locators.allocation_proposal);
    I.fillField(this.locators.proposal_reason, 'Proposal Reason - Test Reason');
  },

  verifyProposalReasonCheckYourAnswers(caseId, caseName = 'Test Case Automation') {
    I.see(this.sections.grounds_for_application.text);
    I.see(caseName);
    I.see('Check your answers');
    I.see('Check the information below carefully.');
    I.see(this.sections.allocation_proposal.which_level_of_judge);
    I.see('Circuit Judge');
    I.see(this.sections.allocation_proposal.give_reason);
    I.see('Test Reason');
  },

  clickContinueButton() {
    I.click('Continue');
  },

  clickSaveAndContinue() {
    I.click('Save and continue');
  },
};
