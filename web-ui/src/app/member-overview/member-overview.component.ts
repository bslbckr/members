import { AfterViewInit, Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { MemberOverview, MemberOverviewService } from './member-overview.service';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { DatePipe } from '@angular/common';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { debounceTime, distinctUntilChanged, fromEvent, tap } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatToolbar } from '@angular/material/toolbar';
import { MatFormField, MatInput, MatLabel, MatSuffix } from '@angular/material/input';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';


@Component({
    selector: 'app-member-overview',
  imports: [MatTableModule, MatPaginatorModule, DatePipe, MatSortModule, MatCardModule, MatToolbar, MatInput, MatFormField, MatLabel, MatSuffix, MatIconButton, MatIcon],
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
  @ViewChild("filterInput") filterInput!: ElementRef;
  
  ngOnInit() {
    this.service.loadMemberOverview().subscribe(data => {
      this.datasource.data = data;
    });
  }

  ngAfterViewInit() {
    this.datasource.paginator = this.paginator;
    this.datasource.sort = this.sort;

    fromEvent(this.filterInput.nativeElement, "input", {once: false})
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(async (e) => {
          const filter = (this.filterInput.nativeElement as HTMLInputElement).value;
          this.datasource.filter = filter;
        }))
      .subscribe();
  }

  filterReset(): void {
    this.filterInput.nativeElement.value = "";
    this.filterInput.nativeElement.dispatchEvent(new Event("input", {
      bubbles: true,
      cancelable: true
    }));
  }

}
