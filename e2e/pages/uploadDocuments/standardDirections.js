/* global locate */
const I = actor();

module.exports = {

  documents: {
    standardDirections: locate('input').withAttr({id:'standardDirections_standardDirections'}),
    otherDocument1: '#standardDirectionsOther_0_otherDocuments',
    otherDocument2: '#standardDirectionsOther_1_otherDocuments',
  },

  fields: {
    otherDocumentTitle1: '#standardDirectionsOther_0_otherDocumentsTitle',
    otherDocumentTitle2: '#standardDirectionsOther_1_otherDocumentsTitle',
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
