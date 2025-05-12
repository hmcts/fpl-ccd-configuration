import { RequestService, UserCredential } from "./RequestService";
import { systemUpdateUser } from "../settings/user-credentials";
import { expect } from "@playwright/test";
import { getDocument } from "pdfjs-dist/legacy/build/pdf.mjs";
import * as fs from 'fs';
import * as path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url); // get the resolved path to the file
const __dirname = path.dirname(__filename); // get the name of the directory

function normalizeWhitespace(text: string): string {
    return text.replace(/\s+/g, " ").trim();
}

export class DocumentService {
    readonly requestSvc : RequestService;
    constructor(requestSvc : RequestService) {
        this.requestSvc = requestSvc;
    }

    async downloadDocument(documentReference: any, user: UserCredential = systemUpdateUser) : Promise<Buffer> {
        let rsp = await this.requestSvc.sendRequest("testing-support/document", user, documentReference?.document_binary_url, "get");
        expect(rsp.ok()).toBeTruthy();

        return rsp.body();
    }

    async getPdfContent(pdfFile: Buffer, ignoreTexts?: string[]) : Promise<string> {
        const pdf = await getDocument(pdfFile.buffer.slice(pdfFile.byteOffset, pdfFile.byteOffset + pdfFile.byteLength)).promise;
        // const pdf = await getDocument(pdfFile).promise;
        let extractedText = "";

        for (let pageNum = 1; pageNum <= pdf.numPages; pageNum++) {
            const page = await pdf.getPage(pageNum);
            const textContent = await page.getTextContent();

            let extractedPage = "";
            let extractedLine = "";
            textContent.items.forEach((item: any) => {
                extractedLine += (item.str || "")
                if (item.hasEOL) {
                    extractedPage += normalizeWhitespace(extractedLine) + "\n";
                    extractedLine = "";
                }
            });
            if (extractedLine) {
                extractedPage += normalizeWhitespace(extractedLine) + "\n";
            }

            const paginationText = `${pageNum} of ${pdf.numPages}`;
            extractedPage = extractedPage.replace(new RegExp(`${paginationText}(?!.*${paginationText})`), "");
            extractedText += extractedPage;
        }

        if (ignoreTexts) {
            for (let text of ignoreTexts) {
                extractedText = extractedText.replace(text, "")
            }
        }

        return extractedText.trim();
    }

    readTextFile(filePath: string, encoding: string = 'utf-8') {
       return fs.readFileSync(path.resolve(__dirname, filePath), 'utf-8');
    }

    async expectPdfContentSame(documentReference: any,  expectedFile: string, placeholders?: {[index: string]: any}, ignoreTexts?: string[]) {
        const pdf = await this.downloadDocument(documentReference);

        let actualPdfContent = await this.getPdfContent(pdf, ignoreTexts);

        let expectedContent = this.readTextFile(`../files/apiTest/${expectedFile}`);
        if (placeholders) {
            for (const [key, value] of Object.entries(placeholders)) {
                expectedContent = expectedContent.replace(new RegExp("\\\$\\\{" + key + "\\\}", 'g'), value);
            }
        }

        const normalizedActual = normalizeWhitespace(actualPdfContent);
        const normalizedExpected = normalizeWhitespace(expectedContent);

        expect(normalizedActual).toBe(normalizedExpected)
    }
}