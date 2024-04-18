import { LogLevel } from "angular-auth-oidc-client";

export const environment = {
    auth: {
        authority: "http://localhost:8081/realms/guc",
        clientId: "members",
        logLevel: LogLevel.Debug
    }
};
