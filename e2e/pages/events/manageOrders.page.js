const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

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
    c47a: 'C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN',
    c32: 'C32_CARE_ORDER',
    c23: 'C23_EMERGENCY_PROTECTION_ORDER',
    c21: 'C21_BLANK_ORDER',
    c35a: 'C35A_SUPERVISION_ORDER',
  },
};

const hearingDetails = {
  linkedToHearing: {
    group: '#manageOrdersApprovedAtHearing',
    options: {
      yes: 'Yes',
      no: 'No',
    },
  },
  hearingList: '#manageOrdersApprovedAtHearingList',
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
  title: '#manageOrdersTitle',
  directions: '#manageOrdersDirections',
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
  exclusionRequirement: {
    group: '#manageOrdersExclusionRequirement',
    options: {
      yes: 'Yes',
      no: 'No',
    },
  },
  supervisionOrderType: {
    group: '#manageSupervisionOrderEndDateType',
    options: {
      calendarDay: 'SET_CALENDAR_DAY',
      calendarDayAndTime: 'SET_CALENDAR_DAY_AND_TIME',
      numberOfMonths: 'SET_NUMBER_OF_MONTHS',
    },
  },
  whoIsExcluded: '#manageOrdersWhoIsExcluded',
  exclusionStartDate: '#manageOrdersExclusionStartDate',
  powerOfArrest: '#manageOrdersPowerOfArrest',
  endDate: '#manageOrdersEndDateTime',
  supervisionOrderEndDate: '#manageOrdersEndDateTime',
  supervisionOrderEndDateAndTime: '#manageOrdersSetDateAndTimeEndDate',
  supervisionOrderNumOfMonths: '#manageOrdersSetMonthsEndDate',
  cafcassRegion: {
    group: '#manageOrdersCafcassRegion',
    options: {
      england: 'ENGLAND',
      wales: 'WALES',
    },
  },
  englandOffices: '#manageOrdersCafcassOfficesEngland',
  walesOffices: '#manageOrdersCafcassOfficesWales',
};

const preview = {
  label: '#orderPreviewSectionHeader',
  documentName: 'Preview order.pdf',
  closeCase: {
    group: '#manageOrdersCloseCase',
    options: {
      yes: '#manageOrdersCloseCase-Yes',
      no: '#manageOrdersCloseCase-No',
    },
  },
};

// Actions
const selectOperation = async (operationType) => {
  I.click(`${operations.group}-${operationType}`);
  await I.runAccessibilityTest();
};

const selectRelatedToHearing = (answer) => {
  I.click(`${hearingDetails.linkedToHearing.group}-${answer}`);
};

const selectHearing = async (hearing) => {
  I.waitForElement(hearingDetails.hearingList);
  I.selectOption(hearingDetails.hearingList, hearing);
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

const enterTitle = (text) => {
  I.fillField(section4.title, text);
};

const enterDirections = async (text) => {
  I.fillField(section4.directions, text);
  await I.runAccessibilityTest();
};

const selectIncludePhrase = (includePhrase) => {
  I.click(`${section4.includePhrase.group}-${includePhrase}`);
};

const selectEpoType = (epoType) => {
  I.click(`${section4.epoTypes.group}-${epoType}`);
};

const selectExclusionRequirement = (exclusionRequirement) => {
  I.click(`${section4.exclusionRequirement.group}-${exclusionRequirement}`);
};

const enterWhoIsExcluded = (text) => {
  I.fillField(section4.whoIsExcluded, text);
};

const enterExclusionStartDate = async (date) => {
  await I.fillDate(date, section4.exclusionStartDate);
};

const uploadPowerOfArrest = (file) => {
  I.attachFile(section4.powerOfArrest, file);
};

const enterRemovalAddress = (address) => {
  postcodeLookup.enterAddressManually(address);
};

const selectSupervisionType = (option) => {
  I.click(`${section4.supervisionOrderType.group}-${option}`);
};

const enterSuperVisionOrderEndDate = async (date) => {
  I.fillDate(date, section4.supervisionOrderEndDate);
};

const enterSuperVisionOrderEndDateAndTime = async (date) => {
  I.fillDateAndTime(date, section4.supervisionOrderEndDateAndTime);
};

const enterSuperVisionNumOfMonths = async (months) => {
  I.fillField(section4.supervisionOrderNumOfMonths, months);
};

const selectSupervisionOrder = async (orderDateType) => {
  I.click(`${section4.supervisionOrderType.group}-${orderDateType}`);
};

const enterFurtherDirections = async (text) => {
  I.fillField(section4.furtherDirections, text);
  await I.runAccessibilityTest();
};

const checkPreview = async () => {
  I.see(preview.documentName);
  await I.runAccessibilityTest();
};

const selectCloseCase = async () => {
  I.checkOption(preview.closeCase.options.yes);
};

const selectCafcassRegion = region => {
  I.click(`${section4.cafcassRegion.group}-${region}`);
};

const selectEnglandOffice= office => {
  I.selectOption(section4.englandOffices, office);
};

module.exports = {
  operations, hearingDetails, orders, section2, section3, section4,
  selectOperation, selectOrder, selectRelatedToHearing, selectHearing, enterJudge, enterApprovalDate, selectChildren, enterTitle, enterDirections,
  enterFurtherDirections, checkPreview, selectCloseCase, enterApprovalDateTime, selectEpoType, selectIncludePhrase, enterEPOEndDateTime,
  enterRemovalAddress, selectExclusionRequirement, enterWhoIsExcluded, enterExclusionStartDate, uploadPowerOfArrest,
  selectSupervisionType, enterSuperVisionOrderEndDate, enterSuperVisionOrderEndDateAndTime, enterSuperVisionNumOfMonths,
  selectSupervisionOrder, selectCafcassRegion, selectEnglandOffice,
};
