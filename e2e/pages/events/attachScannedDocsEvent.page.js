const {I} = inject();

module.exports = {

  fields: {
    scannedDoc: {
      type: '#scannedDocuments_0_type',
      subtype: '#scannedDocuments_0_subtype',
      url: '#scannedDocuments_0_url',
      controlNumber: '#scannedDocuments_0_controlNumber',
      fileName: '#scannedDocuments_0_fileName',
      scannedDate: '#scannedDate',
      deliveryDate: '#deliveryDate',
      exceptionRecordReference: '#scannedDocuments_0_exceptionRecordReference',
    },
  },

  async enterScannedDocument(scannedDocument, file) {
    await I.runAccessibilityTest();
    I.click('Add new');
    await I.runAccessibilityTest();
    I.selectOption(this.fields.scannedDoc.type, scannedDocument.type);
    I.attachFile(this.fields.scannedDoc.url, file);
    I.wait(1); //TODO investigate, without this next instruction does not type entire text and goes to next line (flaky)
    I.fillField(this.fields.scannedDoc.subtype, scannedDocument.subtype);
    I.fillField(this.fields.scannedDoc.controlNumber, scannedDocument.controlNumber);
    I.fillField(this.fields.scannedDoc.fileName, scannedDocument.fileName);
    await I.fillDateAndTime(scannedDocument.scannedDate, this.fields.scannedDoc.scannedDate);
    await I.fillDateAndTime(scannedDocument.deliveryDate, this.fields.scannedDoc.deliveryDate);
    I.fillField(this.fields.scannedDoc.exceptionRecordReference, scannedDocument.exceptionRecordReference);
  },
};
