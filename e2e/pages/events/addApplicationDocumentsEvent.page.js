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

  async selectDocumentType(option) {
    I.click('Add new');
    const elementIndex = await this.getActiveElementIndex();
    I.selectOption(this.fields(elementIndex).documentType, option);
  },

  async uploadFile(file) {
    const elementIndex = await this.getActiveElementIndex();
    I.attachFile(this.fields(elementIndex).document, file);
  },

  async enterWhatIsIncludedInSWET(description) {
    const elementIndex = await this.getActiveElementIndex();
    I.fillField(this.fields(elementIndex).includedInSWET, description);
  },

  async enterDocumentName(name) {
    const elementIndex = await this.getActiveElementIndex();
    I.fillField(this.fields(elementIndex).documentName, name);
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
