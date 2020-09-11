import {Injectable, NgZone} from '@angular/core';

export interface ConsentResult {
	consent: boolean;
	hasShownDialog: boolean;
}

let ngZone;

@Injectable()
export class Ump {

	constructor (
		private zone :NgZone
	) {
		ngZone = this.zone; // save to local variable because the "this" context gets lost in some callbacks of the class methods
	}

	verifyConsent( isAgeConsent :boolean, isDebug :boolean) :Promise<ConsentResult> {
		if (!window['Ump']) {
			console.warn('Ump plugin not present (verifyConsent)');
			return Promise.reject('Ump plugin not present');
		}

		return new Promise((resolve, reject) =>
			window['Ump'].verifyConsent(isAgeConsent, isDebug,
				(value: ConsentResult) => ngZone.run(() => resolve(value)),
				(value: string) => ngZone.run(() => reject(value))
			)
		);
	}

	forceForm() :Promise<ConsentResult> {
		if (!window['Ump']) {
			console.warn('Ump plugin not present (forceForm)');
			return Promise.reject('Ump plugin not present');
		}

		return new Promise((resolve, reject) =>
			window['Ump']. forceForm(
				(value: ConsentResult) => ngZone.run(() => resolve(value)),
				(value: string) => ngZone.run(() => reject(value))
			)
		);
	}
}