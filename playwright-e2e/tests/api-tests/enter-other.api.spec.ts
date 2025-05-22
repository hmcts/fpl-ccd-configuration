import { test } from "../../fixtures/api-test-fixture";
import { expect } from "@playwright/test";
import { swanseaOrgCAAUser } from "../../settings/user-credentials";
import caseDetailsJson from "../../caseData/apiTest/mandatoryOpenCase.json" assert { type: 'json' };

const EVENT = "enter-others";

test.describe('Enter other API test @apiTest', () => {
    let caseDetailsBefore : any;
    test.beforeAll(async ({ callback }) => {
        caseDetailsBefore = await callback.createCase(swanseaOrgCAAUser, "Enter other API test", caseDetailsJson);
    });

    test('Should initialise first other', async ({ callback }) => {
        let caseDetails = await callback.callAboutToStart(EVENT, swanseaOrgCAAUser, caseDetailsBefore);
        expect(caseDetails.caseData?.others).toEqual({
            firstOther: null,
            additionalOthers: []
        });
    });

    test('Should extract confidential data', async ({ callback }) => {
        let caseDetails = {
            ...caseDetailsBefore,
            caseData: {
                ...caseDetailsBefore.caseData, 
                others: {
                    firstOther: {
                        name: "John Smith",
                        DOB: "2000-07-12",
                        gender: "Male",
                        birthPlace: "London",
                        address: {
                            AddressLine1: "1st Avenue",
                            AddressLine2: "5 Saffron Central Square",
                            AddressLine3: "",
                            PostTown: "Croydon",
                            County: "",
                            PostCode: "CR0 2FT",
                            Country: "United Kingdom"
                        },
                        telephone: null,
                        childInformation: null,
                        litigationIssues: "NO",
                        detailsHidden: "Yes",
                        addressKnowV2: "Yes"
                    },
                    additionalOthers: []
                }
            }
        };
        
        let caseDetailsAfter = await callback.callAboutToSubmit(EVENT, swanseaOrgCAAUser, caseDetails);
        expect(caseDetailsAfter.caseData?.others).toEqual({
            firstOther: {
                name: "John Smith",
                gender: "Male",
                address: null,
                telephone: null,
                birthPlace: "London",
                childInformation: null,
                genderIdentification: null,
                litigationIssues: "NO",
                litigationIssuesDetails: null,
                detailsHidden: "Yes",
                addressKnowV2: null,
                detailsHiddenReason: null,
                representedBy: [],
                DOB: "2000-07-12",
                addressNotKnowReason: null
            },
            additionalOthers: []
        });

        expect(caseDetailsAfter.caseData.confidentialOthers).toBeDefined();
        expect(caseDetailsAfter.caseData.confidentialOthers.length).toEqual(1);
        expect(caseDetailsAfter.caseData.confidentialOthers[0]?.value).toEqual({
            name: "John Smith",
            gender: null,
            address: {
                AddressLine1: "1st Avenue",
                AddressLine2: "5 Saffron Central Square",
                AddressLine3: "",
                PostTown: "Croydon",
                County: "",
                PostCode: "CR0 2FT",
                Country: "United Kingdom"
            },
            telephone: null,
            birthPlace: null,
            childInformation: null,
            genderIdentification: null,
            litigationIssues: null,
            litigationIssuesDetails: null,
            detailsHidden: null,
            detailsHiddenReason: null,
            representedBy: [],
            DOB: null,
            addressKnowV2: "Yes",
            addressNotKnowReason: null
        })
    });
});