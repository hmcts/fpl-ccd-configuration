export interface EnvironmentConfig {
 swanseaOrgPBA: string;
 privateSolicitorOrgPBA: string;
}

export const environments: Record<string, EnvironmentConfig> = {
    aat: {
    swanseaOrgPBA: 'PBA0076191',
    privateSolicitorOrgPBA: 'PBA0096432'
    },
    demo: {
    swanseaOrgPBA: 'PBA7183462',
    privateSolicitorOrgPBA: 'PBA0090842'
    },
};

export function getEnvironmentSpecificTestData(): EnvironmentConfig {
    const env = process.env.ENVIRONMENT || 'aat';
    const config = environments[env.toLowerCase()];

    if (!config) {
        throw new Error(
            `Environment "${env}" not found}`
        );
    }

    return config;
}
