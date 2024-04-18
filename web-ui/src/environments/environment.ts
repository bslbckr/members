import { LogLevel } from "angular-auth-oidc-client";

export const environment = {
    auth: {
        authority: "https://login.goldfingers-potsdam.de/realms/guc",
        clientId: "members",
        logLevel: LogLevel.Warn
    }
};
