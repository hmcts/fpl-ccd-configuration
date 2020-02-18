const {I} = inject();

module.exports = {

  fields: {
    scannedDoc: {
      type: '#scannedDocuments_0_type',
      subtype: '#scannedDocuments_0_subtype',
      url: '#scannedDocuments_0_url',
      controlNumber: '#scannedDocuments_0_controlNumber',
      fileName: '#scannedDocuments_0_fileName',
      scannedDate: {
        second: '#scannedDocuments_0_scannedDate-second',
        minute: '#scannedDocuments_0_scannedDate-minute',
        hour: '#scannedDocuments_0_scannedDate-hour',
        day: '#scannedDocuments_0_scannedDate-day',
        month: '#scannedDocuments_0_scannedDate-month',
        year: '#scannedDocuments_0_scannedDate-year',
      },
      deliveryDate: {
        second: '#scannedDocuments_0_deliveryDate-second',
        minute: '#scannedDocuments_0_deliveryDate-minute',
        hour: '#scannedDocuments_0_deliveryDate-hour',
        day: '#scannedDocuments_0_deliveryDate-day',
        month: '#scannedDocuments_0_deliveryDate-month',
        year: '#scannedDocuments_0_deliveryDate-year',
      },
      exceptionRecordReference: '#scannedDocuments_0_exceptionRecordReference',
    },
  },

  enterScannedDocument(scannedDocument, file) {
    I.click('Add new');
    I.selectOption(this.fields.scannedDoc.type, scannedDocument.type);
    I.fillField(this.fields.scannedDoc.subtype, scannedDocument.subtype);
    I.attachFile(this.fields.scannedDoc.url, file);
    I.fillField(this.fields.scannedDoc.controlNumber, scannedDocument.controlNumber);
    I.fillField(this.fields.scannedDoc.fileName, scannedDocument.fileName);
    I.fillField(this.fields.scannedDoc.scannedDate.second, scannedDocument.scannedDate.second);
    I.fillField(this.fields.scannedDoc.scannedDate.minute, scannedDocument.scannedDate.minute);
    I.fillField(this.fields.scannedDoc.scannedDate.hour, scannedDocument.scannedDate.hour);
    I.fillField(this.fields.scannedDoc.scannedDate.day, scannedDocument.scannedDate.day);
    I.fillField(this.fields.scannedDoc.scannedDate.month, scannedDocument.scannedDate.month);
    I.fillField(this.fields.scannedDoc.scannedDate.year, scannedDocument.scannedDate.year);
    I.fillField(this.fields.scannedDoc.deliveryDate.second, scannedDocument.deliveryDate.second);
    I.fillField(this.fields.scannedDoc.deliveryDate.minute, scannedDocument.deliveryDate.minute);
    I.fillField(this.fields.scannedDoc.deliveryDate.hour, scannedDocument.deliveryDate.hour);
    I.fillField(this.fields.scannedDoc.deliveryDate.day, scannedDocument.deliveryDate.day);
    I.fillField(this.fields.scannedDoc.deliveryDate.month, scannedDocument.deliveryDate.month);
    I.fillField(this.fields.scannedDoc.deliveryDate.year, scannedDocument.deliveryDate.year);
    I.fillField(this.fields.scannedDoc.exceptionRecordReference, scannedDocument.exceptionRecordReference);
  },
};
