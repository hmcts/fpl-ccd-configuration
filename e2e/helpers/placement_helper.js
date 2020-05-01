const config = require('../config.js');

const assertCafcassCannotSeePlacementOrder = async (I, caseViewPage, caseId) => {
  await I.navigateToCaseDetailsAs(config.cafcassUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.placement);
  I.dontSee('Placement order');
};

module.exports = {
  assertCafcassCannotSeePlacementOrder,
};
