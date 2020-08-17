const {I} = inject();

module.exports = {

  fields: {
    documentAction: '#manageDocumentsAction',
    documentList: '#courtDocumentList',
    replacementDocument: {
      title: '#editedCourtDocument_documentTitle',
      document: '#editedCourtDocument_document',
    },
    standardDirections: locate('input').withAttr({id: 'standardDirectionsDocument'}),
    otherDocument: {
      document: function (index) {
        return `#newCourtDocuments_${index}_document`;
      },
      title: function (index) {
        return `#newCourtDocuments_${index}_documentTitle`;
      },
    },
  },

  selectEventOption(option) {
    within(this.fields.documentAction, () => {
      I.click(locate('label').withText(option));
    });
  },

  selectDocument(index) {
    I.waitForElement(this.fields.documentList);
    I.selectOption(this.fields.documentList, `Document ${index}`);
  },

  replaceDocument(file) {
    I.fillField(this.fields.replacementDocument.title, 'Replacement Document');
    I.attachFile(this.fields.replacementDocument.document, file);
  },

  uploadStandardDirections(file) {
    I.attachFile(this.fields.standardDirections, file);
  },

  uploadAdditionalDocuments(file, num) {
    for (let i = 0; i < num; i++) {
      I.click('Add new');
      I.fillField(this.fields.otherDocument.title(i), `Document ${i + 1}`);
      I.attachFile(this.fields.otherDocument.document(i), file);
    }
  },
};
