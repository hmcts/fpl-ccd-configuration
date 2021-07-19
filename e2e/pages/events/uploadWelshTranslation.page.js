const { I } = inject();

// Fields
const translations = {
  list: '#uploadTranslationsRelatedToDocument',
  upload: '#uploadTranslationsTranslatedDoc',
};

const selectItemToTranslate = item => {
  I.selectOption(translations.list, item);
};

const reviewItemToTranslate = fileName => {
  I.see('English version');
  I.see(fileName);
};

const uploadTranslatedItem = translatedItem => {
  I.attachFile(translations.upload, translatedItem);
};

module.exports = {
  selectItemToTranslate, reviewItemToTranslate, uploadTranslatedItem,
};
