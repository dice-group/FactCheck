import { Injectable } from '@angular/core';
import { Item } from './item';
@Injectable()
export class ListService {
  subjects: Item[] = [];
  objects: Item[] = [];
  constructor() {
  }
  addSubject(item: Item) {
    if (!(this.subjects.filter(e => e.value === item.value).length > 0)) {
      this.subjects.push(item);
    }
  }

  removeSubject(index: number) {
    console.log('deleteing item');
    this.subjects.splice(index, 1);
  }

  addObject(item: Item) {
    if (!(this.objects.filter(e => e.value === item.value).length > 0)) {
      this.objects.push(item);
    }
  }

  removeObject(index: number) {
    console.log('deleteing item');
    this.objects.splice(index, 1);
  }

  resetEverthing() {
    this.subjects = [];
    this.objects = [];
    console.log('values has been reset successfully...!');
  }
}
