
module.exports = {

  supplements (index, document) {
    return {
      name: `#${document}_${index}_name`,
      secureAccommodationType: `#${document}_${index}_secureAccommodationType-`,
      notes: `#${document}_${index}_notes`,
      document: `#${document}_${index}_document`,
      acknowledge: `#${document}_${index}_documentAcknowledge-ACK_RELATED_TO_CASE`,
    };
  },
};
