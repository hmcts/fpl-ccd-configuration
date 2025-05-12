import { RequestService, UserCredential } from "./RequestService";
import { swanseaOrgCAAUser } from "../settings/user-credentials";
import { DocumentService } from "./DocumentService";
import { expect, test } from "@playwright/test";
import { formatDateToString, formatCCDCaseNumber, formatDateToStringWithOrdinalSuffix, formatDateToStringAsDateTime } from "../utils/document-format-helper";

const EVENT = 'manage-orders';

export class ManageOrderTestService {
    private requestSvc: RequestService;
    private documentService: DocumentService;

    constructor(requestService: RequestService, documentService: DocumentService) {
        this.requestSvc = requestService;
        this.documentService = documentService;
    }

    async testManageOrderContentSame(caseDetailsBefore: any, orderType: string, user: UserCredential = swanseaOrgCAAUser) {
        let orderCaseDetailsJson = (await import(`../caseData/apiTest/manageOrder/${orderType}.json`, { assert: { type: "json" } })).default;
        let caseData = Object.assign({}, caseDetailsBefore.caseData, orderCaseDetailsJson.caseData);
        let orderDocumentReference : any;

        let caseDetailsAfter : any;
        
        let today = new Date();
        await test.step('call about-to-submit', async () => {
            caseData = Object.assign({}, caseData, {
                "manageOrdersType": caseData.manageOrdersType,
                "manageOrdersTitle": "Order title",
                "manageOrdersDirections": "Order details",
                "manageOrdersApprovalDate": today,
                "manageOrdersCareOrderIssuedDate": today,  // c32b
                "manageOrdersEndDateTime": today, // c23
                "manageOrdersApprovalDateTime": today, // c23
                "manageOrdersExclusionStartDate": today, // c23
                "manageOrdersOrderCreatedDate": today, // c29
                "manageOrdersSetDateEndDate": today, //c33
                "manageOrdersEndDateTypeWithEndOfProceedings": "END_OF_PROCEEDINGS", //c33
                "manageOrdersEndDateTypeWithMonth": "NUMBER_OF_MONTHS", //c35a
                "manageOrdersSetMonthsEndDate": 12, //c35a
                "manageOrdersCafcassOfficesEngland": "BOURNEMOUTH", //c47a
                "manageOrdersCafcassRegion": "ENGLAND",
                "manageOrdersPlacedUnderOrder": "CARE_ORDER",
                "manageOrdersActionsPermitted": [
                    "ENTRY"
                ]
            });

            let caseDetails = Object.assign({}, caseDetailsBefore, {caseData: caseData});
            caseDetailsAfter = await this.requestSvc.callAboutToSubmit(EVENT, user, caseDetails);
            let updatedCaseData = caseDetailsAfter?.caseData;
            expect(updatedCaseData).toBeDefined();

            let orderCollection = updatedCaseData?.orderCollection;
            expect(orderCollection).toBeDefined();
            expect(orderCollection.length).toBe(1);

            orderDocumentReference = orderCollection[0]?.value?.document;
            expect(orderDocumentReference).toBeDefined();
        });

        await test.step('verify document content', async () => {
            let oneYearLater = new Date(today);
            oneYearLater.setFullYear(today.getFullYear() + 1);
            
            await this.documentService.expectPdfContentSame(orderDocumentReference, `/manageOrder/${orderType}.txt`,
                {
                    "id": formatCCDCaseNumber(caseDetailsAfter.caseData.id),
                    "issueDate": formatDateToString(today),
                    "dateSuffix": formatDateToStringWithOrdinalSuffix(today),
                    "oneYearLaterTodaySuffix": formatDateToStringWithOrdinalSuffix(oneYearLater),
                    "dateTimeAt": formatDateToStringAsDateTime(today),
                    "dateTimeComma": formatDateToStringAsDateTime(today, ", "),
                });
        });
    }
}