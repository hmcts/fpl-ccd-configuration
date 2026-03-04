import { users, services } from '../settings/token-config';


export class TokenManager {
    private static accessTokens: Map<string, string> = new Map();
    private static s2sTokens: Map<string, string> = new Map();

    static setAccessToken(email: string, token: string) {
        this.accessTokens.set(email, token);
    }

    static getAccessToken(email: string): string {
        const token = this.accessTokens.get(email);
        if (!token) {
            throw new Error(`Access token not found for email: ${email}`);
        }
        return token;
    }

    static setS2SToken(serviceName: string, token: string) {
        if (serviceName && token) {
            this.s2sTokens.set(serviceName, token);
        }
    }

    static getS2SToken(serviceName: string): string  {
        const S2Stoken = this.s2sTokens.get(serviceName);
        if (!S2Stoken) {
            throw new Error(`S2S token not found for service: ${serviceName}`);
        }
        return S2Stoken;
    }


}

// TokenManager usage can now use users and services from config
// Example:
// users.forEach(user => TokenManager.setAccessToken(user, 'token'));
// services.forEach(service => TokenManager.setS2SToken(service, 'token'));
