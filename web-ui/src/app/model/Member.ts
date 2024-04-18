export interface Member {
    id: string,
    givenName: string;
    name: string;
    dayOfBirth: Date;
    entryDate: Date;
    gender: 'male' | 'female';
    exitDate: Date | null;
    street: string;
    zipCode: string;
    city: string;
    state: 'passiv' | 'jugendliche' | 'ermäßigt' | 'berufstätig';
    stateEffective: Date,
    dfvNumber: number;
    dse: boolean;
    dfvDiscount: boolean;
    email: string;
    emailList: boolean;
    mobile: string
}
