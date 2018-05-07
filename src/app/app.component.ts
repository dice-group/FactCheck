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
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})


export class AppComponent {
  position = 'below';
  private apiRoot: String = 'http://localhost:8080';
  results: Object[];
  private loading: boolean;
  private headers = new Headers({ 'Content-Type': 'application/json;charset=UTF-8' });
  private options = new RequestOptions({ headers: this.headers });
  private isURI = require('validate.io-uri');
  private title = 'FactCheck';
  private url = `${this.apiRoot}/api/execTask/`;
  private subject = '';
  private predicate = '';
  private object = '';
  private objectURI = '';
  private subjectURI = '';
  private isFile = false;
  private file;
  private fileName = 'testName';
  private result = '';
  private fileData: string;
  private text = 'sample';
  private taskId = 1;
  private loadingText = 'Loading...';

  constructor(public list: ListService, private http: Http, private spinner: NgxSpinnerService) {
    this.results = [];
    this.loading = false;
    const subUri = JSON.parse(localStorage.getItem('subjectURI'));
    this.subjectURI = subUri === null ? '' : subUri.subjectURI;
    const oUri = JSON.parse(localStorage.getItem('objectURI'));
    this.objectURI = oUri === null ? '' : oUri.objectURI;
  }
  submitData() {
    let obj;
    if (this.isFile) {
      if (this.validateFileInput()) {
        obj = { 'taskid': this.taskId, 'filedata': this.text };
      } else { return false; }
    } else {
      if (!this.validate()) { return; } // return if validation fails
      const builder = new StringBuilder();
      builder.Append(this.createTtlFile());
      obj = { 'taskid': this.taskId, 'filedata': builder.ToString() };
      console.log(builder.ToString());
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
        this.loadingText = 'error' + e;
        this.spinner.hide();
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
      console.log('object labels: ' + lables);
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
  validate() {
    if (this.predicate === '') {
      console.log('No Predicate is selected, please select atleast one predicate from the list');
      alert('No Predicate is selected, please select atleast one predicate from the list');
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

  multipleURis(input: string) {
    return input.lastIndexOf(',,') !== -1;
  }

  sendToApi(myJSON: string) {
    this.result = '';
    const promise = new Promise((resolve, reject) => {
      this.http.post(this.url, myJSON, this.options)
        .toPromise()
        .then(
          res => {
            console.log(res.json());
            this.result = res.json().defactoScore;
            // this.results = res.json().results;
            this.taskId++;
            this.loading = false;
            resolve();
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
      // console.log(this.text);
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
          if (confirm('Do you want to replace current URI? ')) {
            this.subjectURI = '<' + this.subject + '>';
            this.storeSUri();
            this.subject = '';
            return;
          } else {
            if (!confirm('Do you want add ' + this.subject + ' as label? ')) {
              this.subject = '';
              return;
            }
          }
        }
      } else if (this.multipleURis(this.subject)) {
        const array = this.subject.split(',,')
        .filter(function (n) { return n !== undefined && n.trim() !== ''; });
        array.forEach(element => {
          this.list.addSubject(element);
        });
      } else {
        this.list.addSubject(this.subject);
      }
      this.subject = '';
    }
  }

  storeSUri(): void {
    localStorage.setItem('subjectURI', JSON.stringify({subjectURI: this.subjectURI}));
  }

  storeOUri(): void {
    localStorage.setItem('objectURI', JSON.stringify({objectURI: this.objectURI}));
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
          if (confirm('Do you want to replace current URI? ')) {
            this.objectURI = '<' + this.object + '>';
            this.storeOUri();
            this.object = '';
            return;
          } else {
            if (!confirm('Do you want add ' + this.object + ' as label? ')) {
              this.object = '';
              return;
            }
          }
        }
      } else if (this.multipleURis(this.object)) {
        const array = this.object.split(',,')
        .filter(function (n) { return n !== undefined && n.trim() !== ''; });
        array.forEach(element => {
          this.list.addObject(element);
        });
      } else {
        this.list.addObject(this.object);
      }
      this.object = '';
    }
  }

  resetEverthing() {
    if (this.isFile) {
      document.getElementById('fileInput').removeAttribute('type');
      document.getElementById('fileInput').setAttribute('type', 'file');
      this.file = '';
      this.fileName = '';
    } else {
      if (confirm('Please confirm to clear everything? ')) {
        this.subject = '';
        this.predicate = '';
        this.object = '';
        this.subjectURI = '';
        this.objectURI = '';
        this.list.resetEverthing();
      }
    }
  }
  /*
    Returns true if given string contains only numbers, false otherwise.
  */
  isNumeric(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
  }

  /* validates text input */
  validateTextInput(input) {
    if (input === '') {
      alert('input is empty');
      return false;
    } else if (this.isNumeric(input) || (!input.match(/[a-z]/i))) {
      console.log('Invalid input value');
      alert('Invalid input');
      return false;
    }
    return true;
  }

  /* validates file input */
  validateFileInput() {
    if (this.file !== undefined && this.file != null && this.file !== '') {
      if (this.file.name.endsWith('.ttl')) {
        return true;
      } else {
        console.log('Input file is not valid, please select ttl File...! ');
        alert('Input file is not valid, please select ttl File...! ');
        return false;
      }
    } else {
      alert('No file is selected, Please select ttl File...! ');
      console.log('No file is selected, Please select ttl File...! ');
      return false;
    }
  }
}
