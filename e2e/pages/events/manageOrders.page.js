const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');

// Fields
const operations = {
  group: '#manageOrdersOperation',
  options: {
    create: 'CREATE',
  },
};

const orders = {
  group: '#manageOrdersType',
  options: {
    c32: 'C32_CARE_ORDER',
    c23: 'C23_EMERGENCY_PROTECTION_ORDER',
  },
};

const section2 = {
  judge: '#judgeAndLegalAdvisor_judgeAndLegalAdvisor',
  approvalDate: '#manageOrdersApprovalDate',
  approvalDateTime: '#manageOrdersApprovalDateTime',
};

const section3 = {
  allChildren: {
    group: '#orderAppliesToAllChildren',
    options: {
      all: 'Yes',
      select: 'No',
    },
  },
  childSelector: {
    selector: index => `#childSelector_option${index}`,
  },
};

const section4 = {
  furtherDirections: '#manageOrdersFurtherDirections',
  epoTypes: {
    group: '#manageOrdersEpoType',
    options: {
      removeAccommodation: 'REMOVE_TO_ACCOMMODATION',
      preventRemoval: 'PREVENT_REMOVAL',
    },
  },
  includePhrase: {
    group: '#manageOrdersIncludePhrase',
    options: {
      yes: 'Yes',
      no: 'No',
    },
  },
  removalAddress: {
    addressLine1: '#manageOrdersEpoRemovalAddress_AddressLine1',
    postcode: '#manageOrdersEpoRemovalAddress_PostCode',
  },
  exclusionRequirement: {
    group: '#manageOrdersExclusionRequirement',
    options: {
      yes: 'Yes',
      no: 'No',
    },
  },
  whoIsExcluded: '#manageOrdersWhoIsExcluded',
  exclusionStartDate: '#manageOrdersExclusionStartDate',
  powerOfArrest: '#manageOrdersPowerOfArrest',
  endDate: '#manageOrdersEndDateTime',
};

const preview = {
  label: '#orderPreviewSectionHeader',
  documentName: 'Preview order.pdf',
};

// Actions
const selectOperation = async (operationType) => {
  I.click(`${operations.group}-${operationType}`);
  await I.runAccessibilityTest();
};

const selectOrder = async (orderType) => {
  I.click(`${orders.group}-${orderType}`);
  await I.runAccessibilityTest();
};

const enterJudge = () => {
  judgeAndLegalAdvisor.useAllocatedJudge();
};

const enterApprovalDate = async (date) => {
  await I.fillDate(date, section2.approvalDate);
  await I.runAccessibilityTest();
};

const enterApprovalDateTime = async (dateTime) => {
  await I.fillDateAndTime(dateTime, section2.approvalDateTime);
  await I.runAccessibilityTest();
};

const enterEPOEndDateTime = async (dateTime) => {
  await I.fillDateAndTime(dateTime, section4.endDate);
  await I.runAccessibilityTest();
};

const selectChildren = async (option, indexes = []) => {
  I.click(`${section3.allChildren.group}-${option}`);

  if (option === section3.allChildren.options.select) {
    indexes.forEach((selectorIndex) => {
      I.checkOption(section3.childSelector.selector(selectorIndex));
    });
  }

  await I.runAccessibilityTest();
};

const selectIncludePhrase = async (includePhrase) => {
  I.click(`${section4.includePhrase.group}-${includePhrase}`);
  await I.runAccessibilityTest();
};

const selectEpoType = async (epoType) => {
  I.click(`${section4.epoTypes.group}-${epoType}`);
  await I.runAccessibilityTest();
};

const selectExclusionRequirement = async (exclusionRequirement) => {
  I.click(`${section4.exclusionRequirement.group}-${exclusionRequirement}`);
  await I.runAccessibilityTest();
};

const enterWhoIsExcluded = async (text) => {
  I.fillField(section4.whoIsExcluded, text);
  await I.runAccessibilityTest();
};

const enterExclusionStartDate = async (date) => {
  await I.fillDate(date, section4.exclusionStartDate);
  await I.runAccessibilityTest();
};

const uploadPowerOfArrest = async (file) => {
  I.attachFile(section4.powerOfArrest, file);
  await I.runAccessibilityTest();
};

const clickPostCodeLink = async (linkLabel) => {
  I.click(locate(`//a[text()="${linkLabel}"]`));
  await I.runAccessibilityTest();
};

const enterRemovalAddress = async (address1, postcode) => {
  I.fillField(section4.removalAddress.addressLine1, address1);
  I.fillField(section4.removalAddress.postcode, postcode);
  await I.runAccessibilityTest();
};

const enterFurtherDirections = async (text) => {
  I.fillField(section4.furtherDirections, text);
  await I.runAccessibilityTest();
};

const checkPreview = async () => {
  I.see(preview.documentName);
  await I.runAccessibilityTest();
};

module.exports = {
  operations, orders, section2, section3, section4,
  selectOperation, selectOrder, enterJudge, enterApprovalDate, selectChildren, enterFurtherDirections, checkPreview,
  enterApprovalDateTime, selectEpoType, selectIncludePhrase, enterEPOEndDateTime, clickPostCodeLink, enterRemovalAddress,
  selectExclusionRequirement,enterWhoIsExcluded, enterExclusionStartDate, uploadPowerOfArrest,
};
