import { BrowserModule } from '@angular/platform-browser';
import { NgModule, Component } from '@angular/core';
import { AppComponent } from './app.component';
import { FormsModule } from '@angular/forms';
import { ListService } from './list.service';
import {MatTabsModule, MatButtonModule, MatCheckboxModule} from '@angular/material';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
// import {LocalStorageModule} from 'angular-local-storage.min.js';
import { NgxSpinnerModule } from 'ngx-spinner';
import {
  HttpModule
} from '@angular/http';
import {MatTooltipModule} from '@angular/material/tooltip';

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    MatTabsModule,
    BrowserAnimationsModule,
    HttpModule,
    NgxSpinnerModule,
    MatTooltipModule
    // LocalStorageModule
  ],
  providers: [ListService],
  bootstrap: [AppComponent],
})
export class AppModule {}
