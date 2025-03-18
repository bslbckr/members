import { Injectable } from '@angular/core';

type CsvOptions<T> = {
  header: { [key in keyof T]: string },
  delimiter: ',' | ';' | '\t'
}

@Injectable({providedIn: "root"})
export class GenericCsvService {

  toCsv<T>(data: T[], opts: CsvOptions<T>):string {
    const headers = opts.header;
    const headerLine: string[] = [];
    const rows: string[] = [];
    
    for (const h in headers)
      headerLine.push(headers[h]);

    rows.push(headerLine.join(opts.delimiter));

    for (const d of data) {
      let dataRow: string[] = [];
      for (const h in headers) {
        if(d[h]) {

          dataRow.push(new String(d[h]).toString());
        } else {
          dataRow.push("");
        }
      }
      rows.push(dataRow.join(opts.delimiter));
    }

    return rows.join("\n");
  }

}
