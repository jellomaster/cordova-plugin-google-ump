# cordova-plugin-google-ump

This plugin is an Ionic Cordova wrapper for the Google UMP SDK.
Google UMP (User Messaging Platform) SDK is used for asking users in the European Economic Area (EEA) for permission to display personalized ads or 

## News

**Version 2.1.0 / 2023-10-05:**
- add reset function
- android support
- support for Google User Messagint Platform 2.1.0

**Version 1.0.0 / 2020-09-11:**
Initial plugin release.

## Installation

```cmd
ionic cordova plugin add cordova-plugin-google-ump
```

## Ionic Include
Include the plugin in your app.module.ts

```typescript
...

import {Consent} from '../../plugins/cordova-plugin-google-ump/www/consent-typescript-wrapper';

...

@NgModule({
  ...

  providers: [
    ...
    Ump
    ...
  ]
  ...
})
export class AppModule { }
})

```

## Funding Choices Settings
In order to use Google UMP SDK, you need to link your Admob account to Funding Choices (https://fundingchoices.google.com) and create EU Consent & IDFA messages for your app

## Supported Platforms

- iOS
- Android

## Methods

- ump.verifyConsent
- ump.forceForm

## consent.verifyConsent

Should be called on every app start. Checks and returns a consent status. If the user has not made any consent decision yet, it will display the appropriate consent form for EEA users.  For IOS 14+ users will be asked for the AppTrackingTransparency permission after.

### Params
```typescript
verifyConsent(isAgeConsent :boolean, isDebug :boolean, testDeviceHashId: string) :Promise<ConsentResult>
```

- isAgeConsent: True means the users are under the age of consent. False means users are not under age.
- isDebug: If set to true, the device acts like it is in the EEA, even if it is not.
- testDeviceHashId: Device ID for testing purposes

### Result
```typescript
interface ConsentResult {
	consent: boolean; // true if given consent / false other dialog
	hasShownDialog: boolean; // if false, user already made a decision earlier and there was no need to show the dialog
	formAvailable: boolean; // whether a consent form is available to the user
}
```


### Example (Ionic)
```typescript
this.ump.verifyConsent(true, false, "")
			.then((result) => {
				console.log(result);
			})
			.catch((error) => {
				console.log(error);
			});
```

### Example (Cordova)
```javascript
window['Ump'].verifyConsent(isAgeConsent, isDebug, "",
				function(result) {/* do something with the result */},
				function(error) {/* handle the error case */}
			);
```

## consent.forceForm

Method to change the consent decision later. This option should be offered to the user in the settings section of the app.
This method doesn't check if the user is in EEA or not, the dialog is shown to every user. Use method verifyConsent on load to see if the user needs to see a form or not.

### Params
```typescript
forceForm() :Promise<ConsentResult>
```

### Result
```typescript
interface ConsentResult {
	consent: boolean; // true if given consent / false other dialog
	hasShownDialog: boolean; // if false, user already made a decision earlier and there was no need to show the dialog
	formAvailable: boolean; // whether a consent form is available to the user
}
}

```


### Example (Ionic)
```typescript
this.ump.forceForm()
			.then((result) => {
				console.log(result);
			})
			.catch((error) => {
				console.log(error);
			});
```

### Example (Cordova)
```javascript
window['Ump'].forceForm(
				function(result) {/* do something with the result */},
				function(error) {/* handle the error case */}
			);
```



## consent.reset

Reset the User's consent choices

### Params
```typescript
reset()
```

### Example (Ionic)
```typescript
this.ump.reset();
```

### Example (Cordova)
```javascript
window['Ump'].reset();
```


## iOS specific issues

When installing the plugin on iOS it will download the Google UMP SDK and install it as a CocoaPod. It will also create a yourprojectname.xcworkspace file. To build 
your project for iOS you will need to open the .xcworkspace file in XCode and build it there, the command line build does not work correctly with workspaces instead of
projects.

## Thank you

Credit for the Android work goes to: Jim Trainor (@jptrainor)
