const { I } = inject();

const allocationDecisionRadioGroup = '#allocationDecision_proposal';
const allocationDecisionRadioPrefix = 'allocationDecision_proposal-';
const proposalReason = '#allocationDecision_proposalReason';
const judgeLevelRadioNo = '#allocationDecision_judgeLevelRadio-';
const allocationDecisionRadioGroupCorrected = '#allocationDecision_proposalCorrectedl';
const allocationDecisionCorrectedRadioPrefix = 'allocationDecision_proposalCorrected-';
const proposalReasonCorrected = '#allocationDecision_proposalReasonCorrected';

module.exports = {

  selectAllocationDecision(proposal) {
    I.waitForElement(allocationDecisionRadioGroup);
    I.click(locate('input').withAttr({id: allocationDecisionRadioPrefix + proposal}));
  },

  enterProposalReason(reason) {
    I.fillField(proposalReason, reason);
  },

  selectCorrectLevelOfJudge(radioSelection) {
    I.click(judgeLevelRadioNo + radioSelection);
  },

  selectAllocationDecisionCorrect(proposal) {
    I.waitForElement(allocationDecisionRadioGroupCorrected);
    I.click(locate('input').withAttr({id: allocationDecisionCorrectedRadioPrefix + proposal}));
  },

  enterProposalReasonCorrected(reason) {
    I.fillField(proposalReasonCorrected, reason);
  },
};
