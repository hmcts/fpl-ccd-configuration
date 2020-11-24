const {I} = inject();

module.exports = {
  fields: function (index) {
    return {
      documentType: `#documents_${index}_documentType`,
      documents: {
        document: `#documents_${index}_document`,
      },
      includedInSWET: `#documents_${index}_includedInSWET`,
    };
  },

  async selectDocumentType(option) {
    I.click('Add new');
    const elementIndex = await this.getActiveElementIndex();
    I.selectOption(this.fields(elementIndex).documentType, option);
  },

  async uploadFile(file) {
    const elementIndex = await this.getActiveElementIndex();
    I.attachFile(this.fields(elementIndex).documents.document, file);
  },

  async enterWhatIsIncludedInSWET(description) {
    const elementIndex = await this.getActiveElementIndex();
    I.fillField(this.fields(elementIndex).includedInSWET, description);
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
