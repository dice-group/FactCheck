import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
// import * as $ from 'jquery';
import { ListService } from './list.service';
import { Item } from './item';
import { MatTabChangeEvent } from '@angular/material';
import { String, StringBuilder } from 'typescript-string-operations';
import {
  Http,
  Response,
  RequestOptions,
  Headers,
  HttpModule
} from '@angular/http';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/catch';
import { NgxSpinnerService } from 'ngx-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { StatuscodesService } from './statuscodes.service';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { DialogComponent } from './dialog/dialog.component';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})


export class AppComponent {
  position = 'below';
  apiRoot: String = 'http://localhost:8080';
  results: any;
  loading: boolean;
  headers = new Headers({ 'Content-Type': 'application/json;charset=UTF-8' });
  options = new RequestOptions({ headers: this.headers });
  isURI = require('validate.io-uri');
  title = 'FactCheck';
  url = `${this.apiRoot}/api/execTask/`;
  // url = `${this.apiRoot}/factcheck-api-0.1.0/api/execTask/`;
  subject = '';
  predicate = '';
  object = '';
  objectURI = '';
  subjectURI = '';
  isFile = false;
  file;
  fileName = 'testName';
  defactoScore = '';
  fileData: string;
  text = 'sample';
  taskId = 1;
  loadingText = 'Loading...';
  boxTitle = '';
  boxMessage = '';
  noEvidence = '';
  N3 = require('n3');
  parser;
  /* Return value that is return from model dialog */
  retValue = false;

  /* Set it to true when calling this.openDialog() to
  show confirm buttons (yes/no),  false otherwise. */
  yesNo = false;

  constructor(public list: ListService,
    public http: Http,
    public spinner: NgxSpinnerService,
    public dialog: MatDialog,
    public map: StatuscodesService) {

      this.results = [];
    this.loading = false;

    const subUri = JSON.parse(localStorage.getItem('subjectURI'));
    this.subjectURI = subUri === null ? '' : subUri.subjectURI;

    const predicate = JSON.parse(localStorage.getItem('predicate'));
    this.predicate = predicate === null ? '' : predicate.predicate;

    const oUri = JSON.parse(localStorage.getItem('objectURI'));
    this.objectURI = oUri === null ? '' : oUri.objectURI;
    // this.testEnvironment();
    this.parser = new this.N3.Parser();
  }

  openDialog(): any {
    const promise = new Promise((resolve, reject) => {
      const dialogRef = this.dialog.open(DialogComponent, {
        disableClose: true,
        closeOnNavigation: false,
        // width: '350px',
        data: { title: this.boxTitle, message: this.boxMessage, yesNo: this.yesNo }
      }).afterClosed()
        .toPromise()
        .then(
          result => {
            this.retValue = result;
            this.yesNo = false;
            resolve();
          },
          msg => {
            reject(msg);
          }
        );
    });
    return promise;
  }

  submitData() {
    let obj;
    if (this.isFile) {
      if (!this.validateFileInput()) {
        return;
      } else { obj = { 'taskid': this.taskId, 'filedata': this.text }; }
    } else {
      if (!this.validateInput()) { return; } // return if validation fails
      const builder = new StringBuilder();
      builder.Append(this.createTtlFile());

      if ( !this.inputParseTest(builder.ToString()) ) {
        this.boxTitle = 'Error';
        this.openDialog();
        return false;
      }
      obj = { 'taskid': this.taskId, 'filedata': builder.ToString() };

    }
    const myJSON = JSON.stringify(obj);
    this.loading = true;
    this.spinner.show();
    this.loadingText = 'Loading...';
    this.spinner.show();
    this.sendToApi(myJSON)
      .then(() => {
        this.loading = false;
        this.spinner.hide();
      })
      .catch((e) => {
        this.spinner.hide();
          const code = this.map.get(e.status);
        if (code !== undefined) {
          this.loadingText = code;
          console.log(this.loadingText);
        } else {
          this.loadingText = e;
        }
      });
  }
  createTtlFile() {
    const builder = new StringBuilder();
    builder.Append(this.getPrefixes());
    builder.Append(this.createContents());
    return builder.ToString();
  }

  // Create rest of the contents of ttl
  createContents() {
    const builder = new StringBuilder();
    builder.Append(this.subjectURI + '\n');
    builder.Append('\tdbo:' + this.predicate + '\t' + this.objectURI + ' .\n\n');
    if (this.list.getObjectLabels().length > 0) {
      builder.Append(this.objectURI);
      const lables = String.Join(' , ', this.list.getObjectLabels());
      builder.Append('\trdfs:label\t' + this.list.getObjectLabels() + ' .\n\n');
    }
    if (this.list.getSubjectLabels().length > 0) {
      builder.Append(this.subjectURI + '\n');
      const lables = String.Join(' , ', this.list.getSubjectLabels());
      builder.Append('\trdfs:label\t' + this.list.getSubjectLabels() + ' .\n');
    }
    return builder.ToString();
  }

  // Create prefixes
  getPrefixes() {
    return new StringBuilder(
      '@prefix fbase: <http://rdf.freebase.com/ns> .\n' +
      '@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n' +
      '@prefix dbo:   <http://dbpedia.org/ontology/> .\n' +
      '@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n' +
      '@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n' +
      '@prefix skos:  <http://www.w3.org/2004/02/skos/core#> .\n\n'
    ).ToString();
  }
  validateInput() {
    if (this.predicate === '') {
      this.boxMessage = 'No Predicate is selected, please select atleast one predicate from the list';
      this.boxTitle = 'Error';
      this.openDialog();
      return false;
    }
    if (!this.list.hasSubjects()) {
      this.boxMessage = 'No Label for subject is entered, please enter atleast one subject label.';
      this.boxTitle = 'Error';
      this.openDialog();
      return false;
    }
    if (!this.list.hasObjects()) {
      this.boxMessage = 'No Label for object is entered, please enter atleast one object label.';
      this.boxTitle = 'Error';
      this.openDialog();
      return false;
    }
    if (this.subjectURI === '') {
      this.subjectURI = '<https://www.example.com/subject>';
    }
    if (this.objectURI === '') {
      this.objectURI = '<https://www.example.com/object>';
    }
    return true;
  }

  multipleLables(input: string) {
    return input.lastIndexOf(',') !== -1;
  }

  clearResults() {
    this.defactoScore = '';
    this.results = [];
    this.noEvidence = '';
    this.loadingText = '';
  }
  sendToApi(myJSON: string) {
    this.clearResults();
    const promise = new Promise((resolve, reject) => {
      this.http.post(this.url, myJSON, this.options)
        .toPromise()
        .then(
          res => {
            try {
              this.defactoScore = res.json().defactoScore;
              this.results = res.json().complexProofs;
              if (this.results.length === 0 ) {
                this.defactoScore = '';
                this.noEvidence = 'No Evedences where found.';
              }
              this.taskId++;
              this.loading = false;
              resolve();
            } catch (e) {
              console.log('Exception: ' + e);
              this.loading = false;
              this.spinner.hide();
            }
          },
          msg => {
            reject(msg);
          }
        );
    });
    return promise;

  }
  /*
    Sets the file when user selects a file to upload.
  */
  uploadFile(e) {
    this.file = e.target.files[0];
    this.fileName = this.file.name;
    // Read file contents
    const reader = new FileReader();
    reader.onload = x => {
      this.text = reader.result;
    };
    reader.readAsText(this.file);
  }

  onTabChange(event: MatTabChangeEvent) {
    this.isFile = event.tab.textLabel === 'FILE';
  }

  addSubject() {
    if (this.validateTextInput(this.subject)) {
      if (this.isURI(this.subject)) {
        if (this.subjectURI === '') {
          this.subjectURI = '<' + this.subject + '>';
          this.storeSUri();
          this.subject = '';
          return;
        } else {
          const temp = '<' + this.subject + '>';
          ((this.subjectURI !== temp) ? this.replaceSubURI() : this.replaceSubLabels());
        }
      } else if (this.multipleLables(this.subject)) {
        const array = this.subject.split(',')
          .filter(function (n) { return n !== undefined && n.trim() !== ''; });
        array.forEach(element => {
          this.list.addSubject(element);
          this.subject = '';
        });
      } else {
        this.list.addSubject(this.subject);
        this.subject = '';
      }
    }
  }
  replaceSubLabels() {
    const temp = '"' + this.subject + '"';
    if (this.list.getSubjectLabels().indexOf(temp) === -1) {
      this.boxTitle = 'Confirm';
      this.boxMessage = 'Do you want add ' + this.subject + ' as label? ';
      this.yesNo = true;
      this.openDialog().then(() => {
        if (!this.retValue) {
          this.subject = '';
          return;
        } else {
          this.list.addSubject(this.subject);
          this.subject = '';
        }
      }).catch((e) => {
        console.log('error: ' + e);
      });
    } else {
      this.subject = '';
    }
  }
  replaceSubURI() {
    this.boxTitle = 'Confirm';
    this.boxMessage = 'Current URI will be relaced with the new URI. Are you sure ' +
      'you want to replace current URI? ';
    this.yesNo = true;
    this.openDialog().then(() => {
      if (this.retValue) {
        this.subjectURI = '<' + this.subject + '>';
        this.storeSUri();
        this.subject = '';
        return;
      } else {
        this.replaceSubLabels();
      }
    }).catch((e) => {
      console.log('error: ' + e);
    });

  }

  storeSUri(): void {
    localStorage.setItem('subjectURI', JSON.stringify({ subjectURI: this.subjectURI }));
  }

  storePredicate(): void {
    localStorage.setItem('predicate', JSON.stringify({ predicate: this.predicate }));
  }
  storeOUri(): void {
    localStorage.setItem('objectURI', JSON.stringify({ objectURI: this.objectURI }));
  }
  removeObjectURI() {
    this.objectURI = '';
    this.storeOUri();
  }
  removeSubjectURI() {
    this.subjectURI = '';
    this.storeSUri();
  }
  addObject() {
    if (this.validateTextInput(this.object)) {
      if (this.isURI(this.object)) {
        if (this.objectURI === '') {
          this.objectURI = '<' + this.object + '>';
          this.storeOUri();
          this.object = '';
          return;
        } else {
          const temp = '<' + this.object + '>';
          ((this.objectURI !== temp) ? this.replaceObjURI() : this.replaceObjLabels());
        }
      } else if (this.multipleLables(this.object)) {
        const array = this.object.split(',')
          .filter(function (n) { return n !== undefined && n.trim() !== ''; });
        array.forEach(element => {
          this.list.addObject(element);
          this.object = '';
        });
      } else {
        this.list.addObject(this.object);
        this.object = '';
      }
    }
  }

  replaceObjURI() {
    this.boxTitle = 'Confirm';
    this.boxMessage = 'Current URI will be relaced with the new URI. Are you sure ' +
      'you want to replace current URI? ';
    this.yesNo = true;
    this.openDialog().then(() => {
      if (this.retValue) {
        this.objectURI = '<' + this.object + '>';
        this.storeOUri();
        this.object = '';
        return;
      } else {
        this.replaceObjLabels();
      }
    }).catch((e) => {
      console.log('error: ' + e);
    });
  }

  replaceObjLabels() {
    const temp = '"' + this.object + '"';
    if (this.list.getObjectLabels().indexOf(temp) === -1) {
      this.boxTitle = 'Confirm';
      this.boxMessage = 'Do you want add ' + this.object + ' as label? ';
      this.yesNo = true;
      this.openDialog().then(() => {
        if (!this.retValue) {
          this.object = '';
          return;
        } else {
          this.list.addObject(this.object);
          this.object = '';
        }
      }).catch((e) => {
        console.log('error: ' + e);
      });
    } else {
      this.object = '';
    }
  }
  /**
   * Resets all the variables value to default.
   */
  resetEverthing() {
    if (this.isFile) {
      document.getElementById('fileInput').removeAttribute('type');
      document.getElementById('fileInput').setAttribute('type', 'file');
      this.file = '';
      this.fileName = '';
      this.clearResults();
    } else {

      this.boxTitle = 'Confirm';
      this.boxMessage = 'Everything will be reset. Are you sure? ';
      this.yesNo = true;
      this.openDialog().then(() => {
        if (this.retValue) {
          this.subject = '';
          this.predicate = '';
          this.storePredicate();
          this.object = '';
          this.subjectURI = '';
          this.objectURI = '';
          this.list.resetEverthing();
          this.clearResults();
        }
      }).catch((e) => {
        console.log('error: ' + e);
      });
    }
  }


  /**
  * Returns true if given string contains only numbers, false otherwise.
  */
  isNumeric(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
  }

  /**
   *  Validates text input
   */
  validateTextInput(input) {
    if (input === '') {
      this.boxMessage = 'Input is empty..!';
      this.boxTitle = 'Error';
      this.openDialog();
      return false;
    } else if (this.isNumeric(input) || (!input.match(/[a-z]/i))) {
      this.boxMessage = 'Invalid input value..!';
      this.boxTitle = 'Error';
      this.openDialog();
      return false;
    }
    return true;
  }

  /**
   *  Validates file input
   */
  validateFileInput() {
    if (this.file !== undefined && this.file != null && this.file !== '') {
      if (this.file.name.endsWith('.ttl')) {
        if (this.text.length === 0) {
          this.boxMessage = 'File is empty...!';
          this.boxTitle = 'Error';
          this.openDialog();
          return false;
        } else if ( !this.inputParseTest(this.text) ) {
          this.boxTitle = 'Error';
          this.openDialog();
          return false;
        }
        return true;
      } else {
        this.boxMessage = 'Input file is not valid, please select ttl File...!';
        this.boxTitle = 'Error';
        this.openDialog();
        return false;
      }
    } else {
      this.boxMessage = 'No file is selected, Please select ttl File...! ';
      this.boxTitle = 'Error';
      this.openDialog();
      return false;
    }
  }

  inputParseTest(text) {
    console.log(text);
    try {
      this.parser.parse(text);
      this.parser.parse(text, console.log);
      // this.parser.parse(text, console.log);
      // this.parser.parse(
      //   `PREFIX c: <http://example.org/cartoons#>
      //    c:Tom a c:Cat.
      //    c:Jerry a c:Mouse;
      //            c:smarterThan c:Tom.`,
      //   (error, quad, prefixes) => {
      //     if (quad) {
      //       console.log(quad);
      //     } else {
      //       console.log('# Thats all, folks!', prefixes);
      //     }
      //   });
      return true;
    } catch (e) {
      this.boxMessage = e + '\nPlease see the console for details';
      console.log(e);
      return false;
    }
  }

  /**
   * Temporary function to test
   * should be removed when development is finished.
   */
  testEnvironment() {
    this.defactoScore = '0.156748798798798';
    this.results = [
      {
        website: 'http://www.google.com/test/data/dateofbirth/etc/this.thml',
        proofPhrase: 'http://www.google.com/test/data/dateofbirth/etc/this.thml',
      },
      {
        website: 'http://www.google.com/test/data/dateofbirth/etc/this.thml',
        proofPhrase: 'Red'
      }
    ];
  }
}
