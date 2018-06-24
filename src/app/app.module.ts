import { BrowserModule } from '@angular/platform-browser';
import { NgModule, Component } from '@angular/core';
import { AppComponent } from './app.component';
import { FormsModule } from '@angular/forms';
import { ListService } from './list.service';
import {MatTabsModule, MatButtonModule, MatCheckboxModule} from '@angular/material';
import {MatExpansionModule} from '@angular/material/expansion';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
// import {LocalStorageModule} from 'angular-local-storage.min.js';
import { NgxSpinnerModule } from 'ngx-spinner';
import {
  HttpModule
} from '@angular/http';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialogModule} from '@angular/material';
import { DialogComponent } from './dialog/dialog.component';
import { StatuscodesService } from './statuscodes.service';
import { AboutComponent } from './about/about.component';
import { FaqComponent } from './faq/faq.component';
import { routes } from './app.router';
import { HomeComponent } from './home/home.component';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';

@NgModule({
  declarations: [
    AppComponent,
    DialogComponent,
    AboutComponent,
    FaqComponent,
    HomeComponent,
    PageNotFoundComponent,
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
    MatButtonModule,
    MatExpansionModule,
    routes
    // LocalStorageModule
  ],
  entryComponents: [DialogComponent],
  providers: [ListService, StatuscodesService],
  bootstrap: [AppComponent],
})
export class AppModule {}
