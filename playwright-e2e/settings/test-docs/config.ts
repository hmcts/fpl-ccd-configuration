import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

interface testFiles {}

interface Config {
  [key: string]: testFiles | string;
}

const config: Config = {
  testPdfFile: join(
    __dirname,
    "../test-docs/testPdf.pdf",
  ),
  testPdfFile2: join(
    __dirname,
    "../test-docs/testPdf2.pdf",
  ),
  testPdfFile3: join(
    __dirname,
    "../test-docs/testPdf3.pdf",
  ),
  testPdfFile4: join(
    __dirname,
    "../test-docs/testPdf4.pdf",
  ),
  testWordFile: join(
    __dirname,
    "../test-docs/testWordDoc.docx",
  ),
  testTextFile: join(
    __dirname,
    "../test-docs/testTextFile.txt",
  ),
};

export default config as {
  testPdfFile: string;
  testPdfFile2: string;
  testPdfFile3: string;
  testPdfFile4: string;
  testWordFile: string;
  testTextFile: string;
};
