const { I } = inject();
const money = require('../../helpers/money_helper');
const c2SupportingDocuments = require('../../fixtures/testData/c2SupportingDocuments.js');

module.exports = {
  fields: {
    uploadC2: '#temporaryC2Document_document',
    description: '#temporaryC2Document_description',
    usePbaPayment: {
      yes: '#temporaryC2Document_usePbaPayment-Yes',
      no: '#temporaryC2Document_usePbaPayment-No',
    },
    pbaNumber: '#temporaryC2Document_pbaNumber',
    clientCode: '#temporaryC2Document_clientCode',
    customerReference: '#temporaryC2Document_fileReference',
    supportingDocuments: {
      name: '#temporaryC2Document_manageDocumentBundle_0_name',
      notes: '#temporaryC2Document_manageDocumentBundle_0_notes',
      dateAndTime: {
        day: '#temporaryC2Document_manageDocumentBundle_0_dateTimeReceived-day',
        month: '#temporaryC2Document_manageDocumentBundle_0_dateTimeReceived-month',
        year: '#temporaryC2Document_manageDocumentBundle_0_dateTimeReceived-year',
        hour: '#temporaryC2Document_manageDocumentBundle_0_dateTimeReceived-hour',
        minute: '#temporaryC2Document_manageDocumentBundle_0_dateTimeReceived-minute',
        second: '#temporaryC2Document_manageDocumentBundle_0_dateTimeReceived-second',
      },
      document: '#temporaryC2Document_manageDocumentBundle_0_document',
    },
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
    I.fillField(this.fields.supportingDocuments.dateAndTime.day, c2SupportingDocuments.dateAndTime.day);
    I.fillField(this.fields.supportingDocuments.dateAndTime.month, c2SupportingDocuments.dateAndTime.month);
    I.fillField(this.fields.supportingDocuments.dateAndTime.year, c2SupportingDocuments.dateAndTime.year);
    I.fillField(this.fields.supportingDocuments.dateAndTime.second, c2SupportingDocuments.dateAndTime.second);
    I.fillField(this.fields.supportingDocuments.dateAndTime.minute, c2SupportingDocuments.dateAndTime.minute);
    I.fillField(this.fields.supportingDocuments.dateAndTime.hour, c2SupportingDocuments.dateAndTime.hour);
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
    return money.parse(await I.grabTextFrom('ccd-read-money-gbp-field'));
  },
};
