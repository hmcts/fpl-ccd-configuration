const { I } = inject();
const money = require('../../helpers/money_helper');
const supportingDocumentsFragment = require('../../fragments/supportingDocuments.js');
const supplementsFragment = require('../../fragments/supplements.js');
module.exports = {
  fields: {
    uploadC2: '#temporaryC2Document_document',
    description: '#temporaryC2Document_description',
    c2SupportingDocuments: supportingDocumentsFragment.supportingDocuments(0, 'temporaryC2Document_supportingEvidenceBundle'),
    c2Supplements: supplementsFragment.supplements(0, 'temporaryC2Document_supplementsBundle'),
    otherSupportingDocuments: supportingDocumentsFragment.supportingDocuments(0, 'temporaryOtherApplicationsBundle_supportingEvidenceBundle'),
    otherSupplements: supplementsFragment.supplements(0, 'temporaryOtherApplicationsBundle_supplementsBundle'),
    c2AdditionalOrdersRequested:'#temporaryC2Document_c2AdditionalOrdersRequested-',
    usePbaPayment: {
      yes: '#temporaryPbaPayment_usePbaPayment_Yes',
      no: '#temporaryPbaPayment_usePbaPayment_No',
    },
    pbaNumber: '#temporaryPbaPayment_pbaNumber',
    clientCode: '#temporaryPbaPayment_clientCode',
    customerReference: '#temporaryPbaPayment_fileReference',
    applicationType: {
      c2TypePrefix: '#c2Type-',
      additionalApplicationTypePrefix: '#additionalApplicationType-',
    },
    applicantsList: '#applicantsList',
    otherApplicant: '#otherApplicant',
    otherApplicationPrefix: '#temporaryOtherApplicationsBundle_applicationType',
    uploadOtherApplication: '#temporaryOtherApplicationsBundle_document',
    otherSecureAccommodationTypePrefix: '#temporaryOtherApplicationsBundle_supplementsBundle_0_secureAccommodationType-',
    otherParentalResponsibilityTypePrefix: '#temporaryOtherApplicationsBundle_parentalResponsibilityType-',
    c2ParentalResponsibilityTypePrefix: '#temporaryC2Document_parentalResponsibilityType-',
    allOthers: {
      group: '#notifyApplicationsToAllOthers',
      options: {
        all: 'Yes',
        select: 'No',
      },
    },
    personSelector: {
      selector: index => `#personSelector_option${index}-SELECTED`,
    },
  },

  selectAdditionalApplicationType(type) {
    I.click(this.fields.applicationType.additionalApplicationTypePrefix + type);
  },

  selectPeople(option, indexes = []) {
    I.click(`${this.fields.allOthers.group}_${option}`);

    indexes.forEach((selectorIndex) => {
      I.checkOption(this.fields.personSelector.selector(selectorIndex));
    });
  },

  selectApplicantList(applicantName) {
    I.waitForElement(this.fields.applicantsList);
    I.selectOption(this.fields.applicantsList, applicantName);
  },

  enterOtherApplicantName(applicantName) {
    I.fillField(this.fields.otherApplicant,applicantName);
  },

  selectC2Type(type) {
    I.click(this.fields.applicationType.c2TypePrefix + type);
  },

  uploadC2Document(file) {
    I.attachFile(this.fields.uploadC2, file);
  },

  async uploadC2Supplement(document) {
    await I.addAnotherElementToCollection('Supplements');
    I.fillField(this.fields.c2Supplements.name, document.name);
    I.click(this.fields.c2Supplements.secureAccommodationType + document.secureAccommodationType);
    I.fillField(this.fields.c2Supplements.notes, document.notes);
    I.attachFile(this.fields.c2Supplements.document, document.document);
  },

  async uploadC2SupportingDocument(document) {
    await I.addAnotherElementToCollection('Supporting Documents');
    I.fillField(this.fields.c2SupportingDocuments.name, document.name);
    I.fillField(this.fields.c2SupportingDocuments.notes, document.notes);
    I.attachFile(this.fields.c2SupportingDocuments.document, document.document);
  },

  selectC2AdditionalOrdersRequested(ordersRequested) {
    I.click(this.fields.c2AdditionalOrdersRequested + ordersRequested);
  },

  selectOtherApplication(type) {
    I.fillField(this.fields.otherApplicationPrefix, type);
  },

  async uploadOtherSupplement(document) {
    await I.addAnotherElementToCollection('Supplements');
    I.fillField(this.fields.otherSupplements.name, document.name);
    I.click(this.fields.otherSupplements.secureAccommodationType + document.secureAccommodationType);
    I.fillField(this.fields.otherSupplements.notes, document.notes);
    I.attachFile(this.fields.otherSupplements.document, document.document);
  },

  async uploadOtherSupportingDocument(document) {
    await I.addAnotherElementToCollection('Supporting Documents');
    I.fillField(this.fields.otherSupportingDocuments.name, document.name);
    I.fillField(this.fields.otherSupportingDocuments.notes, document.notes);
    I.attachFile(this.fields.otherSupportingDocuments.document, document.document);
  },

  usePbaPayment(usePbaPayment=true) {
    if (usePbaPayment) {
      I.click(this.fields.usePbaPayment.yes);
    } else {
      I.click(this.fields.usePbaPayment.no);
    }
  },

  uploadDocument(file) {
    I.attachFile(this.fields.uploadOtherApplication, file);
  },

  selectOtherParentalResponsibilityType(type) {
    I.click(this.fields.otherParentalResponsibilityTypePrefix + type);
  },

  selectC2ParentalResponsibilityType(type) {
    I.click(this.fields.c2ParentalResponsibilityTypePrefix + type);
  },

  enterPbaPaymentDetails(payment) {
    I.fillField(this.fields.pbaNumber, payment.pbaNumber);
    I.fillField(this.fields.clientCode, payment.clientCode);
    I.fillField(this.fields.customerReference, payment.customerReference);
  },

  async getFeeToPay(){
    await I.runAccessibilityTest();
    return money.parse(await I.grabTextFrom('ccd-read-money-gbp-field'));
  },
};
