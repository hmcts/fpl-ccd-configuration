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
        expect(caseDetails.caseData?.othersV2).toEqual([]);
    });

    test('Should extract confidential data', async ({ callback }) => {
        let caseDetails = {
            ...caseDetailsBefore,
            caseData: {
                ...caseDetailsBefore.caseData,
                othersV2: [
                    {
                        id: "c721b8c8-dcdd-4b77-a16f-841a47d83e8f",
                        value: {
                            name: null,
                            firstName: "John",
                            lastName: "Smith",
                            DOB: "2000-07-12",
                            gender: "Male",
                            genderIdentification: null,
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
                            telephone: "0123456789",
                            childInformation: null,
                            litigationIssues: "NO",
                            litigationIssuesDetails: null,
                            representedBy: [],
                            addressKnowV2: "Yes",
                            addressNotKnowReason: null,
                            whereaboutsUnknownDetails: null,
                            detailsHidden: null,
                            detailsHiddenReason: null,
                            hideAddress: "Yes",
                            hideTelephone: "Yes"
                        }
                    }
                ]
            }
        };

        let caseDetailsAfter = await callback.callAboutToSubmit(EVENT, swanseaOrgCAAUser, caseDetails);
        expect(caseDetailsAfter.caseData?.othersV2).toEqual([
            {
                id: "c721b8c8-dcdd-4b77-a16f-841a47d83e8f",
                value: {
                    name: null,
                    firstName: "John",
                    lastName: "Smith",
                    DOB: "2000-07-12",
                    gender: "Male",
                    genderIdentification: null,
                    birthPlace: "London",
                    address: null,
                    telephone: null,
                    childInformation: null,
                    litigationIssues: "NO",
                    litigationIssuesDetails: null,
                    addressKnowV2: null,
                    addressNotKnowReason: null,
                    whereaboutsUnknownDetails: null,
                    hideAddress: "Yes",
                    hideTelephone: "Yes",
                    detailsHidden: null,
                    detailsHiddenReason: null,
                    representedBy: []
                }
            }
        ]);

        expect(caseDetailsAfter.caseData.confidentialOthers).toBeDefined();
        expect(caseDetailsAfter.caseData.confidentialOthers.length).toEqual(1);
        expect(caseDetailsAfter.caseData.confidentialOthers[0]?.value).toEqual({
            name: null,
            firstName: "John",
            lastName: "Smith",
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
            telephone: "0123456789",
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
            addressNotKnowReason: null,
            whereaboutsUnknownDetails: null,
            hideAddress: "Yes",
            hideTelephone: "Yes"
        })
    });
});
