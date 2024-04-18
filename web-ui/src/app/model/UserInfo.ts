export interface UserInfo {
    email: string,
    family_name: string,
    given_name: string,
    iss: string,
    name: string,
    preferred_username: string,
    resource_access: {
        members: {
            roles: string[]
        }
    }
}
