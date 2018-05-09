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
import {MatDialogModule} from '@angular/material';
import { DialogComponent } from './dialog/dialog.component';

@NgModule({
  declarations: [
    AppComponent,
    DialogComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    MatTabsModule,
    BrowserAnimationsModule,
    HttpModule,
    NgxSpinnerModule,
    MatTooltipModule,
    MatDialogModule,
    MatButtonModule
    // LocalStorageModule
  ],
  entryComponents: [DialogComponent],
  providers: [ListService],
  bootstrap: [AppComponent],
})
export class AppModule {}
