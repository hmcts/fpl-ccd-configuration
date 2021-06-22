const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

// Fields
const operations = {
  group: '#manageOrdersOperation',
  groupInClosedState: '#manageOrdersOperationClosedState',
  options: {
    create: 'CREATE',
    upload: 'UPLOAD',
  },
};

const orders = {
  group: '#manageOrdersType',
  uploadGroup: '#manageOrdersUploadType',
  options: {
    c21: 'C21_BLANK_ORDER',
    c23: 'C23_EMERGENCY_PROTECTION_ORDER',
    c32: 'C32_CARE_ORDER',
    c32b: 'C32B_DISCHARGE_OF_CARE_ORDER',
    c33: 'C33_INTERIM_CARE_ORDER',
    c35A: 'C35A_SUPERVISION_ORDER',
    c35B: 'C35B_INTERIM_SUPERVISION_ORDER',
    c47a: 'C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN',
    other: 'OTHER_ORDER',
  },
  title: {
    c21: 'Blank order (C21)',
    c23: 'Emergency protection order (C23)',
    c32: 'Care order (C32)',
    c32b: 'Discharge of care order (C32B)',
    c33: 'Interim care order (C33)',
    c35B: 'Interim supervision order (C35B)',
    c35A: 'Supervision order (C35A)',
    c47a: 'Appointment of a children\'s guardian (C47A)',
    other: 'Other',
  },
  otherOrderTitle: '#manageOrdersUploadTypeOtherTitle',
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
    children: {
      child1: 'Timothy Jones',
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
  careOrderIssuedDate:  '#manageOrdersCareOrderIssuedDate',
  careOrderIssuedVenue: '#manageOrdersCareOrderIssuedCourt',
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
  exclusionRequirementEPO: {
    group: '#manageOrdersExclusionRequirement',
    options: {
      yes: 'Yes',
      no: 'No',
    },
  },
  exclusionRequirement: {
    group: '#manageOrdersHasExclusionRequirement',
    options: {
      yes: 'Yes',
      no: 'No',
    },
  },
  orderTypeWithMonth: {
    group: '#manageOrdersEndDateTypeWithMonth',
    options: {
      calendarDay: 'CALENDAR_DAY',
      calendarDayAndTime: 'CALENDAR_DAY_AND_TIME',
      numberOfMonths: 'NUMBER_OF_MONTHS',
    },
  },
  orderTypeWithEndOfProceedings: {
    group: '#manageOrdersEndDateTypeWithEndOfProceedings',
    options: {
      calendarDay: 'CALENDAR_DAY',
      calendarDayAndTime: 'CALENDAR_DAY_AND_TIME',
      endOfProceedings: 'END_OF_PROCEEDINGS',
    },
  },
  whoIsExcluded: '#manageOrdersWhoIsExcluded',
  exclusionStartDate: '#manageOrdersExclusionStartDate',
  exclusionDetails: '#manageOrdersExclusionDetails',
  powerOfArrest: '#manageOrdersPowerOfArrest',
  manualOrder: '#manageOrdersUploadOrderFile',
  manualOrderNeedSealing: {
    group: '#manageOrdersNeedSealing',
    options: {
      yes: 'Yes',
      no: 'No',
    },
  },
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
  isFinalOrder: {
    group: '#manageOrdersIsFinalOrder',
    options: {
      yes: '#manageOrdersIsFinalOrder-Yes',
      no: '#manageOrdersIsFinalOrder-No',
    },
  },
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

const selectOperationInClosedState = async (operationType) => {
  I.click(`${operations.groupInClosedState}-${operationType}`);
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

const selectUploadOrder = async (orderType) => {
  I.click(`${orders.uploadGroup}-${orderType}`);
  await I.runAccessibilityTest();
};

const specifyOtherOrderTitle = (text) => {
  I.waitForElement(orders.otherOrderTitle);
  I.fillField(orders.otherOrderTitle, text);
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

const enterCareOrderIssuedDate = async (date) => {
  await I.fillDate(date, section4.careOrderIssuedDate);
};

const enterCareOrderIssuedVenue = (hearingDetails) => {
  I.selectOption(section4.careOrderIssuedVenue, hearingDetails.venue);
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

const selectExclusionRequirementEPO = (exclusionRequirement) => {
  I.click(`${section4.exclusionRequirementEPO.group}-${exclusionRequirement}`);
};

const enterExclusionDetails = (text) => {
  I.fillField(section4.exclusionDetails, text);
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

const uploadManualOrder = async (file) => {
  I.attachFile(section4.manualOrder, file);
  await I.runAccessibilityTest();
};

const selectManualOrderNeedSealing = (needSealing) => {
  I.click(`${section4.manualOrderNeedSealing.group}-${needSealing}`);
};

const enterRemovalAddress = (address) => {
  postcodeLookup.enterAddressManually(address);
};

const selectSupervisionType = (option) => {
  I.click(`${section4.orderTypeWithMonth.group}-${option}`);
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

const selectOrderTypeWithMonth = async (orderDateType) => {
  I.click(`${section4.orderTypeWithMonth.group}-${orderDateType}`);
};

const selectOrderTypeWithEndOfProceedings = (orderDateType) => {
  I.click(`${section4.orderTypeWithEndOfProceedings.group}-${orderDateType}`);
};

const enterFurtherDirections = async (text) => {
  I.fillField(section4.furtherDirections, text);
  await I.runAccessibilityTest();
};

const selectIsFinalOrder = async () => {
  I.checkOption(section4.isFinalOrder.options.yes);
};

const selectIsNotFinalOrder = async () => {
  I.checkOption(section4.isFinalOrder.options.no);
};

const checkPreview = async () => {
  I.see(preview.documentName);
  await I.runAccessibilityTest();
};

const selectCloseCase = async () => {
  I.checkOption(preview.closeCase.options.no);
};

const selectExclusionRequirementICO = (exclusionRequirement) => {
  I.click(`${section4.exclusionRequirement.group}-${exclusionRequirement}`);
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
  enterFurtherDirections, selectIsFinalOrder, selectIsNotFinalOrder, checkPreview, selectCloseCase, enterApprovalDateTime, selectEpoType, selectIncludePhrase, enterEPOEndDateTime,
  enterRemovalAddress, selectExclusionRequirementEPO, enterWhoIsExcluded, enterExclusionStartDate, uploadPowerOfArrest,
  selectSupervisionType, enterSuperVisionOrderEndDate, enterSuperVisionOrderEndDateAndTime, enterSuperVisionNumOfMonths,
  selectOrderTypeWithMonth, enterExclusionDetails, selectOrderTypeWithEndOfProceedings, selectExclusionRequirementICO,
  selectCafcassRegion, selectEnglandOffice, enterCareOrderIssuedVenue, enterCareOrderIssuedDate,
  selectUploadOrder, specifyOtherOrderTitle, uploadManualOrder, selectManualOrderNeedSealing, selectOperationInClosedState,
};
