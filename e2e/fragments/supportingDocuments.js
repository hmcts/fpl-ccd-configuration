
module.exports = {

  supportingDocuments (index, document) {
    return {
      name: `#${document}_supportingEvidenceBundle_${index}_name`,
      notes: `#${document}_supportingEvidenceBundle_${index}_notes`,
      dateAndTime: `#${document}_supportingEvidenceBundle_${index}_dateTimeReceived`,
      document: `#${document}_supportingEvidenceBundle_${index}_document`,
    };
  },
};
