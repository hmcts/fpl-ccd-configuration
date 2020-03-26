const { I } = inject();

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
    fileReference: '#temporaryC2Document_fileReference',
  },
  applicationTypePrefix: '#c2ApplicationType_type-',

  selectApplicationType(type) {
    I.click(this.applicationTypePrefix + type);
  },

  uploadC2Document(file, description) {
    I.attachFile(this.fields.uploadC2, file);
    I.fillField(this.fields.description, description);
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
    I.fillField(this.fields.fileReference, payment.fileReference);
  },
};
