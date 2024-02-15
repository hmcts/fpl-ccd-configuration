import path from "path";

export interface testFiles {}

interface Config {
  [key: string]: testFiles | string;
}

const config: Config = {
  testPdfFile: path.resolve(
    __dirname,
    "../playwright-e2e/settings/test-docs/testPdf.pdf",
  ),
  testWordFile: path.resolve(
    __dirname,
    "../playwright-e2e/settings/test-docs/testWordDoc.docx",
  ),
};

export default config as {
  testPdfFile: string;
  testWordFile: string;
};
