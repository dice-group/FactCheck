import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

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
  onClick() {
    console.log('on click called');
    //  if(this.subject ==''){
    //    console.log("subject is null ");
    //    alert("subject is null");
    //   }else {
    //     console.log("subject is not null ");
    //    alert("subject is not null");
    //   }
    if (this.validateInput()) {
      // send data
    } else {
      // return false;
    }

  }

  uploadFile(e) {
    console.log('file name before: ' + this.fileName);
    console.log('file was: ' + this.file);
    this.file = e.target.files[0];
    this.fileName = this.file.name;
    console.log('uploadFile is called file is: ' + this.file);
    console.log('file name after: ' + this.fileName);
  }


  // Validates the user input
  validateInput() {
    console.log('selction= ' + this.selection);
    if (this.selection === 'file') {
      console.log('file= ' + this.file);
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


    } else if (this.selection === 'text') {
      console.log('text selection: ' + this.subject + '\n' + this.predicate + '\n' + this.object);
      if (this.isNumeric(this.predicate) || (!this.predicate.match(/[a-z]/i))) {
        console.log('Predicate should not have only numeric value');
        alert('Predicate should not have only numeric value');
        return false;
      }
      return true;
    } else {
      alert('Please select file or text before sending');
    }
  }
  textSelection() {

    document.getElementById('fileInput').removeAttribute('type');
    document.getElementById('fileInput').setAttribute('type', 'file');

    this.selection = 'text';
    this.isFile = false;
    this.file = '';
    this.fileName = '';
  }

  fileSelection() {
    this.isFile = true;
    this.selection = 'file';
    this.subject = '';
    this.predicate = '';
    this.object = '';

  }

  isNumeric(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
  }
}
