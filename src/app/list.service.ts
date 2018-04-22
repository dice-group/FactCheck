import { Injectable } from '@angular/core';
import { Item } from './item';
@Injectable()
export class ListService {
  subjects: Item[] = [];
  objects: Item[] = [];
  constructor() {
  }
  addSubject(item: Item) {
    this.subjects.push(item);
  }

  removeSubject(index: number) {
    console.log('deleteing item');
    this.subjects.splice(index, 1);
  }

  addObject(item: Item) {
    this.objects.push(item);
  }

  removeObject(index: number) {
    console.log('deleteing item');
    this.objects.splice(index, 1);
  }
}
