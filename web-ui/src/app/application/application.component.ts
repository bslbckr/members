import { STEPPER_GLOBAL_OPTIONS } from '@angular/cdk/stepper';
import { AsyncPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import {  MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatStepperIcon, MatStepperModule } from '@angular/material/stepper';
import { Eta } from 'eta/core';
import { first, map, Observable } from 'rxjs';

@Component({
  selector: 'application',
  imports: [ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSlideToggleModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatStepperModule,
    MatStepperIcon,
    AsyncPipe],
  providers: [
    { provide: STEPPER_GLOBAL_OPTIONS, useValue: {displayDefaultIndicatorType: false}}
  ],
  templateUrl: './application.component.html',
  styleUrl: './application.component.css'
})
export class ApplicationComponent implements OnInit {
  private readonly applicationTemplateUrl = "/assets/mitgliedsantrag.eta";
  private readonly http = inject(HttpClient);
  private readonly builder = inject(NonNullableFormBuilder);
  readonly form = this.builder.group({
    firstName: this.builder.control("", Validators.required),
    name: this.builder.control("", Validators.required),
    email: this.builder.control("", [Validators.email, Validators.required]),
    isChild: this.builder.control(false),
    parentFirstName: this.builder.control("", {validators: Validators.required}),
    parentName: new FormControl<string>("", {validators: Validators.required, nonNullable: true})
  });
  readonly invalid$ = this.form.statusChanges.pipe(takeUntilDestroyed(), map(s => s !== "VALID"));
  constructor() {
        this.form.get("isChild")?.valueChanges.pipe(takeUntilDestroyed()).subscribe(isChild => {
      if (isChild) {
        this.form.get("parentFirstName")?.enable();
        this.form.get("parentName")?.enable();
      } else {
        this.form.get("parentFirstName")?.disable();
        this.form.get("parentName")?.disable();
      }
    });

  }
  ngOnInit() {
    this.form.get("parentFirstName")?.disable();
    this.form.get("parentName")?.disable();
  }
  
  renderApplication() {


    this.renderHttp().subscribe(this.downloadBlob);
    
  }
  
  private renderHttp():Observable<Blob> {
    return this.loadTemplateHttp().pipe(
      map(this.etaRender.bind(this)),
      map(this.createBlob)
    );
  }
  private loadTemplateHttp():Observable<string> {
    return this.http.get(this.applicationTemplateUrl, {observe: "body", responseType: "text"}).pipe(first());
  }

  private etaRender(template: string):string {
    const eta = new Eta();
    
    return eta.renderString(template, this.form.value);
  }

  private createBlob(doc: string): Blob {
    return new Blob([doc], {type: "text/html"});
  }

  private downloadBlob(blob: Blob):void {
    const downloadUrl = URL.createObjectURL(blob);
    
    const anchor = document.createElement("a");
    anchor.href = downloadUrl;
    anchor.target = "_blank";
    //anchor.download = "Mitgliedsantrag-Goldfingers.html";

    anchor.click();
    //URL.revokeObjectURL(downloadUrl);
    
  }
}
