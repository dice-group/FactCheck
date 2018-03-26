import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import * as $ from 'jquery';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})


export class AppComponent {
  title = 'FactCheck';
  btnText = 'Submit';
  subject = '';
  predicate = '';
  object = '';
  selection = '';
  isFile = false;
  file;
  fileName = 'testName';
  result = '';
  onClick() {
    console.log('on click called');
    let obj;
    if (this.selection === 'file' && this.validateFileInput()) {
      console.log('file= ' + this.file);
      obj = this.file;
    } else if (this.selection === 'text' && this.validateTextInput()) {
      obj = { 'subject': this.subject, 'predicate': this.predicate, 'object': this.object };
    } else {
      console.log('Please select file or text before sending');
      alert('Please select file or text before sending');
    }
    /* Use the JavaScript function JSON.stringify() to convert it into a string. */
    const myJSON = JSON.stringify(obj);
    /* Using the XMLHttpRequest to get data from the server: */
    const xmlhttp = new XMLHttpRequest();
    this.result = 'awaiting result';
    xmlhttp.onreadystatechange = function () {
      if (this.readyState === 4 && this.status === 200) {
        const myObj = JSON.parse(this.responseText);
        document.getElementById('result').innerHTML = myObj.result;
      }
    };
    xmlhttp.open('POST', 'vineet set path here', true);
    xmlhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xmlhttp.send(obj);





  }

  /*
    Sets the file when user selects a file to upload.
  */
  uploadFile(e) {
    this.file = e.target.files[0];
    this.fileName = this.file.name;
    console.log('uploadFile: ' + this.file);
    console.log('file name after: ' + this.fileName);
  }







  /*
    Resets the value of file selection when
    chooses text selection.
  */
  textSelection() {
    document.getElementById('fileInput').removeAttribute('type');
    document.getElementById('fileInput').setAttribute('type', 'file');

    this.selection = 'text';
    this.isFile = false;
    this.file = '';
    this.fileName = '';
  }

  /*
    Resets the value of subject, predicate and object text fields when
    file is selected.
  */
  fileSelection() {
    this.isFile = true;
    this.selection = 'file';
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
    if (this.isNumeric(this.predicate) || (!this.predicate.match(/[a-z]/i))) {
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
        // Read file contents
        const reader = new FileReader();
        reader.onload = function () {
          console.log('onLoad is called...');
          const text = reader.result;
          console.log(text);
          // var output = document.getElementById('output');
        };
        reader.readAsText(this.file);

        return true;
      } else {
        console.log('Input file is not valid, please select ttl File...! ');
        alert('Input file is not valid, please select ttl File...! ');
        return false;
      }
    } else {
      alert('No file is selected, Please select ttl File...! ');
      console.log('No file is selected, Please select ttl File...! ');
    }
  }
}
