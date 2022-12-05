
module.exports = {

  supportingDocuments (index, document) {
    return {
      name: `#${document}_${index}_name`,
      notes: `#${document}_${index}_notes`,
      dateAndTime: `#${document}_${index}_dateTimeReceived`,
      document: `#${document}_${index}_document`,
      confidential: `#${document}_${index}_confidential-CONFIDENTIAL`,
      expertReportType: `#${document}_${index}_expertReportType`,
      type: {
        expert: `#${document}_${index}_type-EXPERT_REPORTS`,
        other: `#${document}_${index}_type-OTHER_REPORTS`,
      },
      translationRequirement: request =>  `#${document}_${index}_translationRequirements-${request}`,
      acknowledge: `#${document}_${index}_documentAcknowledge-ACK_RELATED_TO_CASE`,
    };
  },
};
