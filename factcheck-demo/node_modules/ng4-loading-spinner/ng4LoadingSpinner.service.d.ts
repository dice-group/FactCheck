import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable } from 'rxjs/Observable';
/**
 * Injectable service
 * @export
 * @class Ng4LoadingSpinnerService
 */
export declare class Ng4LoadingSpinnerService {
    /**
     * @description spinners BehaviorSubject
     * @type {BehaviorSubject<any>}
     * @memberof Ng4LoadingSpinnerService
     */
    spinnerSubject: BehaviorSubject<any>;
    /**
     * Creates an instance of Ng4LoadingSpinnerService.
     * @memberof Ng4LoadingSpinnerService
     */
    constructor();
    /**
     * To show spinner
     * @memberof Ng4LoadingSpinnerService
     */
    show(): void;
    /**
     * To hide spinner
     * @memberof Ng4LoadingSpinnerService
     */
    hide(): void;
    getMessage(): Observable<any>;
}
