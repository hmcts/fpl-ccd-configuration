const { I } = inject();
const money = require('../../helpers/money_helper');
const c2SupportingDocuments = require('../../fixtures/c2SupportingDocuments.js');
const supportingDocumentsFragment = require('../../fragments/supportingDocuments.js');
module.exports = {
  fields: {
    uploadC2: '#temporaryC2Document_document',
    description: '#temporaryC2Document_description',
    usePbaPayment: {
      yes: '#temporaryC2Document_usePbaPayment_Yes',
      no: '#temporaryC2Document_usePbaPayment_No',
    },
    pbaNumber: '#temporaryC2Document_pbaNumber',
    clientCode: '#temporaryC2Document_clientCode',
    customerReference: '#temporaryC2Document_fileReference',
    supportingDocuments: supportingDocumentsFragment.supportingDocuments(0, 'temporaryC2Document_supportingEvidenceBundle'),
  },

  applicationTypePrefix: '#c2ApplicationType_type-',

  selectApplicationType(type) {
    I.click(this.applicationTypePrefix + type);
  },

  uploadC2Document(file, description) {
    I.attachFile(this.fields.uploadC2, file);
    I.fillField(this.fields.description, description);
  },

  async uploadC2SupportingDocument() {

    await I.addAnotherElementToCollection();
    I.fillField(this.fields.supportingDocuments.name, c2SupportingDocuments.name);
    I.fillField(this.fields.supportingDocuments.notes, c2SupportingDocuments.notes);
    I.fillDateAndTime(c2SupportingDocuments.date, this.fields.supportingDocuments.dateAndTime);
    I.attachFile(this.fields.supportingDocuments.document, c2SupportingDocuments.document);
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
