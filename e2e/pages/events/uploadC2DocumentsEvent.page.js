const { I } = inject();
const money = require('../../helpers/money_helper');

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
    documentName: '#temporaryC2Document_manageDocumentBundle_0_name',
    documentNotes: '#temporaryC2Document_manageDocumentBundle_0_notes',
    documentDateAndTime: {
      day: 'temporaryC2Document_manageDocumentBundle_0_dateTimeReceived-day',
      month: 'temporaryC2Document_manageDocumentBundle_0_dateTimeReceived-month',
      year: 'temporaryC2Document_manageDocumentBundle_0_dateTimeReceived-year',
      hour: 'temporaryC2Document_manageDocumentBundle_0_dateTimeReceived-hour',
      minute: 'temporaryC2Document_manageDocumentBundle_0_dateTimeReceived-minute',
      second: 'temporaryC2Document_manageDocumentBundle_0_dateTimeReceived-second',
    },
    uploadC2SupportingDocument: '#temporaryC2Document_manageDocumentBundle_0_document',
  },
  applicationTypePrefix: '#c2ApplicationType_type-',

  selectApplicationType(type) {
    I.click(this.applicationTypePrefix + type);
  },

  uploadC2Document(file, description) {
    I.attachFile(this.fields.uploadC2, file);
    I.fillField(this.fields.description, description);
  },

  async uploadC2SupportingDocument(file) {

    await I.addAnotherElementToCollection();
    I.fillField(this.fields.documentName, 'C2 supporting document');
    I.fillField(this.fields.documentNotes, 'This is a note about supporting doc');
    I.fillField(this.fields.documentDateAndTime.day, '01');
    I.fillField(this.fields.documentDateAndTime.month, '01');
    I.fillField(this.fields.documentDateAndTime.year, '2020');
    I.fillField(this.fields.documentDateAndTime.second, '00');
    I.fillField(this.fields.documentDateAndTime.minute, '00');
    I.fillField(this.fields.documentDateAndTime.hour, '11');
    I.attachFile(this.fields.uploadC2SupportingDocument, file);
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
