const {I} = inject();

const section1 = {
  allChildren: {
    group: '#orderAppliesToAllChildren',
    options: {
      all: 'Yes',
      select: 'No',
    },
  },
  childSelector: {
    selector: index => `#childSelector_option${index}-SELECTED`,
  },
};

const section2 = {
  date: '#finalDecisionDate',
  child0: '#childFinalDecisionDetails00_finalDecisionReason',
  child1: '#childFinalDecisionDetails01_finalDecisionReason',
  child2: '#childFinalDecisionDetails02_finalDecisionReason',
  child3: '#childFinalDecisionDetails03_finalDecisionReason',
  reason: {
    withdrawn: 'WITHDRAWN',
    refusal: 'REFUSAL',
    noOrder: 'NO_ORDER',
    finalOrder: 'FINAL_ORDER',
  },
};

const selectChildren = async (option, indexes = []) => {
  I.click(`${section1.allChildren.group}_${option}`);

  if (option === section1.allChildren.options.select) {
    indexes.forEach((selectorIndex) => {
      I.checkOption(section1.childSelector.selector(selectorIndex));
    });
  }
};

const addDate = async (date) => {
  await I.fillDate(date, section2.date);
};

const selectReason = (reasonOption, indexes = []) => {
  indexes.forEach((selectorIndex) => {
    const child = eval('section2.child' + selectorIndex);
    I.click(`${child}-${reasonOption}`);
  });
};

module.exports = {
  section1, section2, selectChildren, selectReason, addDate,
};
