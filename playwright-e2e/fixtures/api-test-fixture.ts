import { test as base } from "@playwright/test";
import { RequestService } from "../apiServices/RequestService";
import { DocumentService } from "../apiServices/DocumentService";
import { ManageOrderTestService } from "../apiServices/ManageOrderTestService";

type ApiTestFixture = {
    callback: RequestService,
    documentService: DocumentService,
    manageOrderTestService: ManageOrderTestService
};

export const test = base.extend<ApiTestFixture>({
    callback: async ({request}, use) => {
        await use(new RequestService(request));
    },
    documentService: async ({callback}, use) => {
        await use(new DocumentService(callback));
    },
    manageOrderTestService: async ({callback, documentService}, use) => {
        await use(new ManageOrderTestService(callback, documentService));
    },
});