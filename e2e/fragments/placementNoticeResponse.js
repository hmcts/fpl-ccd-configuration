
module.exports = {

  placementResponses (index, document) {
    return {
      document: `#${document}_${index}_response`,
      description: `#${document}_${index}_responseDescription`,
      typeList: `#${document}_${index}_type`,
      typeOptions: ['Local authority', 'Cafcass', 'Respondent'],
    };
  },
};
