const { I } = inject();

module.exports = {

  fields: {
    standardDirections: locate('input').withAttr({id:'standardDirectionsDocument'}),
    otherDocument: {
      document: function (index) {
        return `#limitedCourtAdminDocuments_${index}_document`;
      },
      title: function (index) {
        return `#limitedCourtAdminDocuments_${index}_documentTitle`;
      },
    },
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
