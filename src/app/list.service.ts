import { Injectable } from '@angular/core';
import { String, StringBuilder } from 'typescript-string-operations';
import { extend } from 'webdriver-js-extender';

@Injectable()
export class ListService {
  private subjects = [];
  private objects = [];
  constructor() {
    const localStorageItem = JSON.parse(localStorage.getItem('oLabels'));
    this.objects = localStorageItem === null ? [] : localStorageItem.oLabels;

    const localStorageItem2 = JSON.parse(localStorage.getItem('sLabels'));
    this.subjects = localStorageItem2 === null ? [] : localStorageItem2.sLabels;
    console.log('subjects ' + this.subjects);
    console.log('objects ' + this.objects);
  }
  addSubject(item: string) {
    if (this.subjects.indexOf('"' + item + '"') === -1) {
      this.subjects.push('"' + item + '"');
      this.setLocalStorageSLabels(this.subjects);
    }
  }

  removeSubject(index: number) {
    this.subjects.splice(index, 1);
    this.setLocalStorageSLabels(this.subjects);
  }

  addObject(item: string) {
    if (this.objects.indexOf('"' + item + '"') === -1) {
      this.objects.push('"' + item + '"');
      this.setLocalStorageOLabels(this.objects);
    }
  }

  removeObject(index: number) {
    this.objects.splice(index, 1);
    this.setLocalStorageOLabels(this.objects);
  }

  resetEverthing() {
    this.subjects = [];
    this.objects = [];
    this.setLocalStorageOLabels(this.objects);
    this.setLocalStorageSLabels(this.subjects);
  }

  getSubjectLabels() {
    // localStorage.getItem('sLabels') === null ? [] : localStorage.getItem('sLabels');
    return  String.Join(' , ', this.subjects);
  }

  getObjectLabels() {
    // const localStorageItem = JSON.parse(localStorage.getItem('oLabels'));
    // return localStorageItem === null ? [] : localStorageItem.oLabels;
    return  String.Join(' , ', this.objects);
  }

  hasSubjects() {
    return this.subjects.length > 0;
  }

  hasOjbects() {
    return this.objects.length > 0;
  }

  private setLocalStorageOLabels(oLabels: string[]): void {
    localStorage.setItem('oLabels', JSON.stringify({oLabels: oLabels}));
  }

  private setLocalStorageSLabels(sLabels: string[]): void {
    localStorage.setItem('sLabels', JSON.stringify({sLabels: sLabels}));
  }
}
