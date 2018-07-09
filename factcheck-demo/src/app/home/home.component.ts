import { Component, OnInit } from '@angular/core';
import { ElementRef, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ListService } from '../list.service';
import { Item } from '../item';
// import * as $ from 'jquery';
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
import { StatuscodesService } from '../statuscodes.service';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { DialogComponent } from '../dialog/dialog.component';
import { saveAs } from 'file-saver/FileSaver';
import * as jsPDF from 'jspdf';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})

/**
 * Main Component class
 */
export class HomeComponent {

  @ViewChild('resultcontent') resultcontent: ElementRef;
  position = 'below';
  apiRoot: String = 'http://131.234.28.204:8080';
  querySubject = '';
  queryPredicate = '';
  queryObject = '';
  predicates = {
    'award': 'recieved award', 'birth': 'birth place',
    'death': 'death place', 'foundationPlace': 'foundation place', 'leader': 'office',
    'nbateam': 'played team', 'publicationDate': 'publication', 'spouse': 'marriaged to',
    'starring': 'is starring', 'subsidiary': 'acquisition'
  };
  evidences: any;
  loading: boolean;
  headers = new Headers({ 'Content-Type': 'application/json;charset=UTF-8' });
  options = new RequestOptions({ headers: this.headers });
  isURI = require('validate.io-uri');
  title = 'FactCheck';
  url = `${this.apiRoot}/factcheck-api/api/execTask/`;
  // url = `${this.apiRoot}/factcheck-api/api/execTask/`;
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
  taskId = '1';
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

  /**
   * Default constructor.
   * @param list ListService
   * @param http Http
   * @param spinner NgxSpinnerService
   * @param dialog MatDialog
   * @param map StatuscodesService
   */
  constructor(public list: ListService,
    public http: Http,
    public spinner: NgxSpinnerService,
    public dialog: MatDialog,
    public map: StatuscodesService) {

    this.evidences = [];
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

  /**
   * Opens dialog box with appropriate message.
   */
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

  /**
   * Called when user clicks on submit button.
   */
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

      if (!this.inputParseTest(builder.ToString())) {
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
        console.log('then');
        this.loading = false;
        this.spinner.hide();
      })
      .catch((e) => {
        console.log('catch');
        this.spinner.hide();
        const code = this.map.get(e.status);
        if (code !== undefined) {
          this.loadingText = code;
          console.log(this.loadingText);
        } else {
          this.loadingText = e;
        }
      });
    console.log('below');
  }

  /**
   * Creates ttl file from text input.
   */
  createTtlFile() {
    const builder = new StringBuilder();
    builder.Append(this.getPrefixes());
    builder.Append(this.createContents());
    return builder.ToString();
  }

  /**
   * Create rest of the contents of ttl.
   */
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

  /**
   * Creates prefixes to generate tll file.
   */
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

  /**
   * Validates input for subject, object and predicate.
   */
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
      this.storeSUri();
    }
    if (this.objectURI === '') {
      this.objectURI = '<https://www.example.com/object>';
      this.storeOUri();
    }
    return true;
  }

  /**
   * Checks if input contains multiple labels for subject or object.
   * @param input string
   */
  multipleLables(input: string) {
    return input.lastIndexOf(',') !== -1;
  }

  /**
   * Clears previous resutls.
   */
  clearResults() {
    this.defactoScore = '';
    this.evidences = [];
    this.noEvidence = '';
    this.loadingText = '';
    this.querySubject = this.queryPredicate = this.queryObject = '';
    this.storeSUri();
    this.storeOUri();
  }

  /**
   * Sends request to back-end
   * @param myJSON string
   */
  sendToApi(myJSON: string) {
    this.clearResults();
    const promise = new Promise((resolve, reject) => {
      console.log('api url: ' + this.url);
      this.http.post(this.url, myJSON, this.options)
        .toPromise()
        .then(
          res => {
            try {
              console.log('try begin');
              this.defactoScore = res.json().defactoScore;
              this.querySubject = res.json().subject;
              this.queryObject = res.json().object;
              this.queryPredicate = res.json().predicate;
              this.evidences = res.json().complexProofs == null ? [] : res.json().complexProofs;
              if (this.evidences.length === 0) {
                this.noEvidence = 'No Evedences where found.';
              }
              // this.taskId++;
              this.loading = false;
              resolve();
              console.log('try end');
            } catch (e) {
              console.log('Exception: ' + e);
              this.loading = false;
              this.spinner.hide();
            }
          },
          msg => {
            console.log('reject');
            reject(msg);
          }
        );
    });
    return promise;

  }

  /**
   * Sets the file when user selects a file to upload.
   * @param e Event
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
  /**
   * Called when user changes the tab.
   * @param event MatTabChangeEvent
   */
  onTabChange(event: MatTabChangeEvent) {
    this.isFile = event.tab.textLabel === 'FILE';
  }

  /**
   * Adds subject as URI or label if uri exists.
   */
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

  /**
   * Replaces subject labels.
   */
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

  /**
   * Replaces subject uri.
   */
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

  /**
   * Stores subject uri in local storage
   */
  storeSUri(): void {
    localStorage.setItem('subjectURI', JSON.stringify({ subjectURI: this.subjectURI }));
  }

  /**
   * Stores predicate in local storage
   */
  storePredicate(): void {
    localStorage.setItem('predicate', JSON.stringify({ predicate: this.predicate }));
  }

  /**
   * Stores object uri in local storage
   */
  storeOUri(): void {
    localStorage.setItem('objectURI', JSON.stringify({ objectURI: this.objectURI }));
  }

  /**
   * Removes object uri from local storage
   */
  removeObjectURI() {
    this.objectURI = '';
    this.storeOUri();
  }

  /**
   * Removes subject uri from local storage
   */
  removeSubjectURI() {
    this.subjectURI = '';
    this.storeSUri();
  }

  /**
   * Adds object uri, if uri exists it adds as label.
   */
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

  /**
   * Replaces old object resource URI with new URI
   */
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

  /**
   * Adds uri as object label.
   */
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
        } else if (!this.inputParseTest(this.text)) {
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

  /**
   * Parses ttl file, throws exception if file is not correct.
   * @param text string
   */
  inputParseTest(text) {
    console.log(text);
    try {
      this.parser.parse(text);
      this.parser.parse(text, console.log);
      return true;
    } catch (e) {
      this.boxMessage = e + '\nPlease see the console for details';
      console.log(e);
      return false;
    }
  }

  /**
   * Creates and saves ttl file from text input.
   */
  createTTLFile() {
    const builder = new StringBuilder();
    builder.Append(this.createTtlFile());
    if (!this.inputParseTest(builder.ToString())) {
      this.boxTitle = 'Error';
      this.openDialog();
      return false;
    } else {
      const filename = 'file.ttl';
      const blob = new Blob([builder.ToString()], { type: 'text/plain' });
      saveAs(blob, filename);
    }
  }
  /**
   * Saves results as pdf file
   */
  saveResultsAsPdf() {
    const content = this.resultcontent.nativeElement;
    // const str = content.innerHTML;
    // const newstr = $(str).find('img').remove().end();
    // $('#hidden').append(newstr);
    // const elem = document.getElementById('hidden');
    const doc = new jsPDF();
    const specialElementHandlers = {
      '#editor': function (element, renderer) {
        return true;
      }
    };
    // doc.fromHTML(elem, 15, 15, {
    doc.fromHTML(content.innerHTML, 15, 15, {
      'width': 190,
      'elementHandlers': specialElementHandlers
    }, function (bla) {
      doc.save('file.pdf');
    });

  }
  /**
   * Temporary function to test
   * should be removed when development is finished.
   */
  testEnvironment() {
    this.querySubject = 'Einstein';
    this.queryPredicate = this.predicates.award;
    this.queryObject = 'Nobel Price';
    this.defactoScore = '0.156748798798798';
    this.evidences = [
      {
        website: 'http://www.google.com/test/data/dateofbirth/etc/this.thml',
        proofPhrase: 'This is sample proof phrase 1 . checking wraping , spacing , margin etc',
      },
      {
        website: 'http://www.google.com/test/data/dateofbirth/etc/this.thml',
        proofPhrase: 'Red'
      }
    ];
  }


}
