const { I } = inject();
const money = require('../../helpers/money_helper');
const supportingDocumentsFragment = require('../../fragments/supportingDocuments.js');
const supplementsFragment = require('../../fragments/supplements.js');
module.exports = {
  fields: {
    uploadC2: '#temporaryC2Document_document',
    description: '#temporaryC2Document_description',
    supportingDocuments: supportingDocumentsFragment.supportingDocuments(0, 'temporaryC2Document_supportingEvidenceBundle'),
    supplements: supplementsFragment.supplements(0, 'temporaryC2Document_supplementsBundle'),
    c2AdditionalOrdersRequested:'#temporaryC2Document_c2OrdersRequested-',
    usePbaPayment: {
      yes: '#usePbaPayment-Yes',
      no: '#usePbaPayment-No',
    },
    pbaNumber: '#pbaNumber',
    clientCode: '#clientCode',
    customerReference: '#fileReference',
    applicationType: {
      c2TypePrefix: '#c2Type-',
      additionalApplicationTypePrefix: '#additionalApplicationType-',
    },
  },

  selectAdditionalApplicationType(type) {
    I.click(this.fields.applicationType.additionalApplicationTypePrefix + type);
  },

  selectC2Type(type) {
    I.click(this.fields.applicationType.c2TypePrefix + type);
  },

  uploadC2Document(file) {
    I.attachFile(this.fields.uploadC2, file);
  },

  async uploadSupplement(document) {
    await I.addAnotherElementToCollection('Supplements');
    I.fillField(this.fields.supplements.name, document.name);
    I.fillField(this.fields.supplements.notes, document.notes);
    I.attachFile(this.fields.supplements.document, document.document);
  },

  async uploadSupportingDocument(document) {
    await I.addAnotherElementToCollection('Supporting Documents');
    I.fillField(this.fields.supportingDocuments.name, document.name);
    I.fillField(this.fields.supportingDocuments.notes, document.notes);
    I.attachFile(this.fields.supportingDocuments.document, document.document);
  },

  selectC2AdditionalOrdersRequested(ordersRequested) {
    I.click(this.fields.c2AdditionalOrdersRequested + ordersRequested);
  },

  usePbaPayment(usePbaPayment=true) {
    if (usePbaPayment) {
      I.click(this.fields.usePbaPayment.yes);
    } else {
      I.click(this.fields.usePbaPayment.no);
    }
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
