import { OnDestroy } from '@angular/core';
import { Ng4LoadingSpinnerService } from './ng4LoadingSpinner.service';
import { Subscription } from 'rxjs/Subscription';
/**
 * @description
 * @author Amit Mahida
 * @export
 * @class Ng4LoadingSpinnerComponent
 * @implements {OnDestroy}
 */
export declare class Ng4LoadingSpinnerComponent implements OnDestroy {
    private spinnerService;
    /**
     * @description Default loading spinner template
     * @type {string}
     * @memberof Ng4LoadingSpinnerComponent
     */
    _template: string;
    /**
     * @description Loading text
     * @type {string}
     * @memberof Ng4LoadingSpinnerComponent
     */
    _loadingText: string;
    /**
     * @description Defines threhold for not to diplay if time is less than 500ms
     * @type {number}
     * @memberof Ng4LoadingSpinnerComponent
     */
    _threshold: number;
    /**
     * @description Defines z-index property of the loading text
     * @type {number}
     * @memberof Ng4LoadingSpinnerComponent
     */
    _zIndex: number;
    /**
     * @description returns z-index for input text
     * @readonly
     * @type {number}
     * @memberof Ng4LoadingSpinnerComponent
     */
    /**
     * @description Sets z-index for input text
     * @memberof Ng4LoadingSpinnerComponent
     */
    zIndex: number;
    /**
     * @description Gives the current template
     * @readonly
     * @type {string}
     * @memberof Ng4LoadingSpinnerComponent
     */
    /**
     * @description Accepts custom template
     * @memberof Ng4LoadingSpinnerComponent
     */
    template: string;
    /**
     * @description Gives loading text
     * @readonly
     * @type {string}
     * @memberof Ng4LoadingSpinnerComponent
     */
    /**
     * @description Accepts loading text string
     * @memberof Ng4LoadingSpinnerComponent
     */
    loadingText: string;
    /**
     * @description
     * @readonly
     * @type {number}
     * @memberof Ng4LoadingSpinnerComponent
     */
    /**
     * @description Accepts external threshold
     * @memberof Ng4LoadingSpinnerComponent
     */
    threshold: number;
    /**
     * Subscription
     * @type {Subscription}
     * @memberof Ng4LoadingSpinnerComponent
     */
    subscription: Subscription;
    /**
     * @description Show/hide spinner
     * @memberof Ng4LoadingSpinnerComponent
     */
    showSpinner: boolean;
    /**
     * Constructor
     * @param {Ng4LoadingSpinnerService} spinnerService Spinner Service
     * @memberof Ng4LoadingSpinnerComponent
     */
    constructor(spinnerService: Ng4LoadingSpinnerService);
    /**
     * Destroy function
     * @memberof Ng4LoadingSpinnerComponent
     */
    ngOnDestroy(): void;
    /**
     * Create service subscription
     * @memberof Ng4LoadingSpinnerComponent
     */
    createServiceSubscription(): void;
}
