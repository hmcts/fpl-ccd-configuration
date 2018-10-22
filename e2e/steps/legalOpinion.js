const grounds = require('../pages/enterGrounds/enterGrounds.js');
const risks = require('../pages/enterRiskAndHarmToChild/enterRiskAndHarmToChild.js');
const factorsAffectingParenting = require('../pages/enterFactorsAffectingParenting/enterFactorsAffectingParenting.js');
const internationalElement = require('../pages/internationalElements/enterInternationalElements.js');
const otherProceedings = require('../pages/enterOtherProceedings/enterOtherProceedings.js');
const allocationProposal = require('../pages/enterAllocationProposal/enterAllocationProposal.js');
const attendingHearing = require('../pages/attendingHearing/attendingHearing.js');
const otherProceedingData = require('../fixtures/otherProceedingData');
const config = require('../config.js');
const caseView = require('../pages/caseView/caseView.js');
const addEventDetails = require('../pages/createCase/addEventSummary');


const I = actor();

module.exports = {

  continueAndSubmit(summary, description) {
    I.click('Continue');
    I.waitForElement('.check-your-answers', 10);
    addEventDetails.submitCase(summary, description);
    I.waitForElement('.tabs', 10);
  },

  completeGrounds() {
    caseView.goToNewActions(config.applicationActions.enterGrounds);
    grounds.enterThresholdCriteriaDetails();
    this.continueAndSubmit(config.eventSummary, config.eventDescription);
  },

  completeRisks() {
    caseView.goToNewActions(config.applicationActions.enterRisk);
    risks.completePhyiscalHarm();
    risks.completeEmotionalHarm();
    risks.completeSexualAbuse();
    risks.completeNeglect();
    this.continueAndSubmit(config.eventSummary, config.eventDescription);
  },

  completeFactorsAffectingParenting() {
    caseView.goToNewActions(config.applicationActions.enterFactorsAffectingParenting);
    factorsAffectingParenting.completeAlcoholOrDrugAbuse();
    factorsAffectingParenting.completeDomesticViolence();
    factorsAffectingParenting.completeAnythingElse();
    this.continueAndSubmit(config.eventSummary, config.eventDescription);
  },

  completeInternationalElements() {
    caseView.goToNewActions(config.applicationActions.enterInternationalElement);
    internationalElement.fillForm();
    this.continueAndSubmit(config.eventSummary, config.eventDescription);
  },

  completeOtherProceedings() {
    caseView.goToNewActions(config.applicationActions.enterOtherProceedings);
    otherProceedings.selectYesForProceeding();
    otherProceedings.enterProceedingInformation(otherProceedingData);
    this.continueAndSubmit(config.eventSummary, config.eventDescription);
  },

  completeAllocationProposal() {
    caseView.goToNewActions(config.applicationActions.enterAllocationProposal);
    allocationProposal.selectAllocationProposal('Lay justices');
    allocationProposal.enterProposalReason('mock reason');
    this.continueAndSubmit(config.eventSummary, config.eventDescription);
  },

  completeAttendingHearing() {
    caseView.goToNewActions(config.applicationActions.attendingHearing);
    attendingHearing.enterInterpreter();
    attendingHearing.enterIntermediary();
    attendingHearing.enterLitigationIssues();
    attendingHearing.enterLearningDisability();
    attendingHearing.enterWelshProceedings();
    attendingHearing.enterExtraSecurityMeasures();
    this.continueAndSubmit(config.eventSummary, config.eventDescription);
  },

  completeLegalOpinion() {
    this.completeGrounds();
    this.completeRisks();
    this.completeFactorsAffectingParenting();
    this.completeInternationalElements();
    this.completeOtherProceedings();
    this.completeAllocationProposal();
    this.completeAttendingHearing();
  },
};
