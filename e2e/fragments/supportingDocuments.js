
module.exports = {

  supportingDocuments (index, document) {
    return {
      name: `#${document}_${index}_name`,
      notes: `#${document}_${index}_notes`,
      dateAndTime: `#${document}_${index}_dateTimeReceived`,
      document: `#${document}_${index}_document`,
    };
  },
};
