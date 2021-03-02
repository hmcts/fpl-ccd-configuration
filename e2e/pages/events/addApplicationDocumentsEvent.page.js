const {I} = inject();

module.exports = {
  fields: function (index) {
    return {
      documentType: `#applicationDocuments_${index}_documentType`,
      document: `#applicationDocuments_${index}_document`,
      includedInSWET: `#applicationDocuments_${index}_includedInSWET`,
      documentName: `#applicationDocuments_${index}_documentName`,
    };
  },

  async addApplicationDocument(option, file, name, description) {

    await I.addAnotherElementToCollection('Documents');
    const index = await I.getActiveElementIndex();

    this.selectDocumentType(option, index);
    await I.runAccessibilityTest();
    this.uploadFile(file, index);

    if (name) {
      this.enterDocumentName(name, index);
    }

    if (description) {
      this.enterWhatIsIncludedInSWET(description, index);
    }
  },

  selectDocumentType(option, index) {
    I.selectOption(this.fields(index).documentType, option);
  },

  uploadFile(file, index) {
    I.attachFile(this.fields(index).document, file);
  },

  enterWhatIsIncludedInSWET(description, index) {
    I.fillField(this.fields(index).includedInSWET, description);
  },

  enterDocumentName(name, index) {
    I.fillField(this.fields(index).documentName, name);
  },
};
