const { I } = inject();

module.exports = {

  documents: {
    standardDirections: locate('input').withAttr({id:'standardDirectionsDocument'}),
    otherDocument1: '#otherCourtAdminDocuments_0_document',
    otherDocument2: '#otherCourtAdminDocuments_1_document',
  },

  fields: {
    otherDocumentTitle1: '#otherCourtAdminDocuments_0_documentTitle',
    otherDocumentTitle2: '#otherCourtAdminDocuments_1_documentTitle',
  },

  uploadStandardDirections(file) {
    I.attachFile(this.documents.standardDirections, file);
  },

  uploadAdditionalDocuments(file) {
    I.click('Add new');
    I.fillField(this.fields.otherDocumentTitle1, 'Document 1');
    I.attachFile(this.documents.otherDocument1, file);
    I.click('Add new');
    I.fillField(this.fields.otherDocumentTitle2, 'Document 2');
    I.attachFile(this.documents.otherDocument2, file);
  },
};
