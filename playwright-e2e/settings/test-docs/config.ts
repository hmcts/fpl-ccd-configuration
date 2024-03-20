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
  testTextFile: path.resolve(
    __dirname,
    "../test-docs/testTextFile.txt",
  ),
};

export default config as {
  testPdfFile: string;
  testWordFile: string;
  testTextFile: string;
};
