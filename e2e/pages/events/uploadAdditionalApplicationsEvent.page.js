const { I } = inject();
const money = require('../../helpers/money_helper');
const supportingDocumentsFragment = require('../../fragments/supportingDocuments.js');
const supplementsFragment = require('../../fragments/supplements.js');
module.exports = {
  fields: {
    uploadC2: '#temporaryC2Document_document',
    description: '#temporaryC2Document_description',
    usePbaPayment: {
      yes: '#usePbaPayment-Yes',
      no: '#usePbaPayment-No',
    },
    pbaNumber: '#pbaNumber',
    clientCode: '#clientCode',
    customerReference: '#fileReference',
    supportingDocuments: supportingDocumentsFragment.supportingDocuments(0, 'temporaryC2Document_supportingEvidenceBundle'),
    supplements: supplementsFragment.supplements(0, 'temporaryC2Document_supplementsBundle'),
  },

  applicationTypePrefix: '#c2ApplicationType_type-',
  additionalApplicationTypePrefix: '#additionalApplicationType-',
  c2AdditionalOrdersRequested:'#temporaryC2Document_c2OrdersRequested-',

  selectAdditionalApplicationType(type) {
    I.click(this.additionalApplicationTypePrefix + type);
  },

  selectApplicationType(type) {
    I.click(this.applicationTypePrefix + type);
  },

  uploadC2Document(file) {
    I.attachFile(this.fields.uploadC2, file);
  },

  async uploadSupplement(documents) {
    await I.addAnotherElementToCollection('Supplements');
    I.fillField(this.fields.supplements.name, documents.name);
    I.fillField(this.fields.supplements.notes, documents.notes);
    I.attachFile(this.fields.supplements.document, documents.document);
  },

  async uploadSupportingDocument(documents) {
    await I.addAnotherElementToCollection('Supporting Documents');
    I.fillField(this.fields.supportingDocuments.name, documents.name);
    I.fillField(this.fields.supportingDocuments.notes, documents.notes);
    I.attachFile(this.fields.supportingDocuments.document, documents.document);
  },

  selectC2AdditionalOrdersRequested(ordersRequested) {
    I.click(this.c2AdditionalOrdersRequested + ordersRequested);
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
