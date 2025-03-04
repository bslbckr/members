import { AfterViewInit, Component, inject, OnInit, ViewChild } from '@angular/core';
import { MemberOverview, MemberOverviewService } from './member-overview.service';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { DatePipe } from '@angular/common';

@Component({
    selector: 'app-member-overview',
    imports: [MatTableModule, MatPaginatorModule, DatePipe],
    templateUrl: './member-overview.component.html',
    styleUrl: './member-overview.component.css',
    providers: [MemberOverviewService]
})
export class MemberOverviewComponent implements OnInit, AfterViewInit{
  private readonly service = inject(MemberOverviewService);
  readonly displayedColumns = ["givenName", "name", "entryDate", "state", "stateEffective", "exitDate"];

  readonly datasource = new MatTableDataSource<MemberOverview>();
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  
  ngOnInit() {
    this.service.loadMemberOverview().subscribe(data => {
      this.datasource.data = data;
    });
  }

  ngAfterViewInit() {
    this.datasource.paginator = this.paginator;
  }
}
