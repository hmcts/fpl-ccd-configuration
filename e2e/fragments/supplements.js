
module.exports = {

  supplements (index, document) {
    return {
      name: `#${document}_${index}_name`,
      notes: `#${document}_${index}_notes`,
      document: `#${document}_${index}_document`,
    };
  },
};
