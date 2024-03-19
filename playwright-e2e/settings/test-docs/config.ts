import path from "path";

export interface testFiles {}

interface Config {
  [key: string]: testFiles | string;
}

const config: Config = {
  testPdfFile: path.resolve(
    __dirname,
    "../test-docs/testPdf.pdf",
  ),
  testWordFile: path.resolve(
    __dirname,
    "../test-docs/testWordDoc.docx",
  ),
};

export default config as {
  testPdfFile: string;
  testWordFile: string;
};
