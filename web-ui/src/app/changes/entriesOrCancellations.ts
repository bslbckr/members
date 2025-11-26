export interface EntriesOrCancellationsData {
  givenName: string;
  name: string;
  date: string;
}

export class EntriesOrCancellation {
  private readonly nameInternal: string;
  private readonly givenNameInternal: string;
  private readonly dateInternal: Date;
  constructor(data: EntriesOrCancellationsData) {
    this.nameInternal = data.name;
    this.givenNameInternal = data.givenName;
    this.dateInternal = new Date(data.date);
  }

  get name() { return this.nameInternal; }
  get givenName() { return this.givenNameInternal; }
  get date() { return this.dateInternal; }
}
