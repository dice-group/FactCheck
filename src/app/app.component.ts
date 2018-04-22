import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import * as $ from 'jquery';
import { ListService } from './list.service';
import { Item } from './item';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})


export class AppComponent {
  // items: Item[] = [];
  // items = ['just', 'some',    'cool',    'tags'  ];
  constructor(public list: ListService) {
    const newItem = { value: 'Sample Subject ' };
    this.list.addSubject(newItem);
    this.list.addObject({ value: 'Sample Object' });
  }

  title = 'FactCheck';
  btnText = 'Submit';
  subject = '';
  predicate = '';
  object = '';
  isFile = false;
  file;
  fileName = 'testName';
  result = '';
  fileData: MSBaseReader;
  text = 'sample';
  isUri = false;

  onClick() {
    let obj;
    if (this.isFile) {
      if (this.validateFileInput()) {
        obj = { 'taskid': 1112, 'filedata': this.text };
      } else { return false; }
    } else {
      if (this.validateTextInput()) {
        obj = { 'taskid': 22323, 'filedata': 'text ' };
      } else { return false; }
    }

    // document.getElementById('result').innerHTML = 'Please wait while result is being displayed ';
    /* Use the JavaScript function JSON.stringify() to convert it into a string. */
    const myJSON = JSON.stringify(obj);
    /* Using the XMLHttpRequest to get data from the server: */
    const xmlhttp = new XMLHttpRequest();
    this.result = 'awaiting result';
    xmlhttp.onreadystatechange = function () {
      if (this.readyState === 4 && this.status === 200) {
        const myObj = JSON.parse(this.responseText);
        document.getElementById('result').innerHTML = 'Defecto Score is: ' + myObj.defactoScore;
      }
    };
    xmlhttp.open('POST', 'http://localhost:8080/api/execTask/', true);
    xmlhttp.setRequestHeader('Content-Type', 'application/json');
    xmlhttp.send(myJSON);
  }
  // addItem(item) {
  //   this.items.push(item);
  // }

  // deleteItem(index: number) {
  //   console.log('deleteing item number: ' + index);
  //   this.items.splice(index, 1);
  // }
  /*
    Sets the file when user selects a file to upload.
  */
  uploadFile(e) {
    this.file = e.target.files[0];
    this.fileName = this.file.name;
    // Read file contents
    const reader = new FileReader();
    reader.onload = x => {
      console.log('onLoad is called...');
      // const text = reader.result;
      this.text = reader.result;
    };
    reader.readAsText(this.file);
  }

  /*
    Resets the value of file selection when
    chooses text selection.
  */
  textSelection() {
    document.getElementById('fileInput').removeAttribute('type');
    document.getElementById('fileInput').setAttribute('type', 'file');
    this.isFile = false;
    this.file = '';
    this.fileName = '';
  }

  addSubject() {
    if (!this.isUri) {
      const temp = '<http://example.org/' + this.subject + '>';
      const subject = {
        // value: encodeURI(temp)
        value: temp
      };
      this.list.addSubject(subject);
    } else {
      const subject = {
        value: this.subject
      };
    }
    this.subject = '';
  }

  addObject() {
    if (!this.isUri) {
      const temp = '<http://example.org/' + this.object + '>';
      const object = {
        // value: encodeURI(this.object)
        value: temp
      };
      this.list.addObject(object);
    } else {
      const object = {
        value: this.object
      };
    }
    this.object = '';
  }
  /*
    Resets the value of subject, predicate and object text fields when
    file is selected.
  */
  fileSelection() {
    this.isFile = true;
    this.subject = '';
    this.predicate = '';
    this.object = '';
  }
  /*
    Returns true if given string contains only numbers, false otherwise.
  */
  isNumeric(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
  }

  /* validates text input */
  validateTextInput() {
    if ((this.subject === '') || (this.object === '') || (this.predicate === '')) {
      console.log('All field are required, please fill subject, predicate and object');
      alert('All field are required, please fill subject, predicate and object');
      return false;
    } else if (this.isNumeric(this.predicate) || (!this.predicate.match(/[a-z]/i))) {
      console.log('Predicate should not have only numeric value');
      alert('Predicate should not have only numeric value');
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
