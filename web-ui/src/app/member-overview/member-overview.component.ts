import { AfterViewInit, Component, inject, OnInit, ViewChild } from '@angular/core';
import { MemberOverview, MemberOverviewService } from './member-overview.service';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { DatePipe } from '@angular/common';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';

@Component({
    selector: 'app-member-overview',
  imports: [MatTableModule, MatPaginatorModule, DatePipe, MatSortModule],
    templateUrl: './member-overview.component.html',
    styleUrl: './member-overview.component.css',
    providers: [MemberOverviewService]
})
export class MemberOverviewComponent implements OnInit, AfterViewInit{
  private readonly service = inject(MemberOverviewService);
  readonly displayedColumns = ["givenName", "name", "entryDate", "state", "stateEffective", "exitDate"];

  readonly datasource = new MatTableDataSource<MemberOverview>();
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  
  private rawData!: MemberOverview[];
  ngOnInit() {
    this.service.loadMemberOverview().subscribe(data => {
      this.datasource.data = data;
      this.rawData = data;
    });
  }

  ngAfterViewInit() {
    this.datasource.paginator = this.paginator;
    this.datasource.sort = this.sort;
  }

}
