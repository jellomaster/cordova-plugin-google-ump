# cordova-plugin-google-ump

This plugin is an Ionic Cordova wrapper for the Google UMP SDK.
Google UMP (User Messaging Platform) SDK is used for asking users in the European Economic Area (EEA) for permission to display personalized ads or 

## News

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

- Android (Coming soon)
- iOS

## Methods

- ump.verifyConsent
- ump.forceForm

## consent.verifyConsent

Should be called on every app start. Checks and returns a consent status. If the user has not made any consent decision yet, it will display the appropriate consent form for EEA users.  For IOS 14+ users will be asked for the AppTrackingTransparency permission after.

### Params
```typescript
verifyConsent(isAgeConsent :boolean, isDebug :boolean) :Promise<ConsentResult>
```

- isAgeConsent: If set to true, on the dialog is shown an option to the user, where he can choose to buy an ad-free pro version.
- isDebug: If set to true, the device acts like it is in the EEA, even if it is not.


### Result
```typescript
interface ConsentResult {
	consent: boolean; // true if given consent / false other
 dialog
	hasShownDialog: boolean; // if false, user already made a decision earlier and there was no need to show the dialog
}
```


### Example (Ionic)
```typescript
this.ump.verifyConsent(true, false)
			.then((result) => {
				console.log(result);
			})
			.catch((error) => {
				console.log(error);
			});
```

### Example (Cordova)
```javascript
window['Ump'].verifyConsent(isAgeConsent, isDebug,
				function(result) {/* do something with the result */},
				function(error) {/* handle the error case */}
			);
```

## consent.forceForm

Method to change the made decision later. This option should be offered to the user in the settings, that he can change his decision at any time.
This method has no check if a user is really in the EEA or not, the dialog is shown to every user. Use method verifyConsent on load to see if the user needs to see a form or not

### Params
```typescript
forceForm() :Promise<ConsentResult>
```

### Result
```typescript
interface ConsentResult {
    consent: boolean; // true if given consent / false other
 dialog
    hasShownDialog: boolean; // in this case always true
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
window['Ump'].then(
				function(result) {/* do something with the result */},
				function(error) {/* handle the error case */}
			);
```

## iOS specific issues

When installing the plugin on iOS it will download the Google UMP SDK and install it as a CocoaPod. It will also create a yourprojectname.xcworkspace file. To build 
your project for iOS you will need to open the .xcworkspace file in XCode and build it there, the command line build does not work correctly with workspaces instead of
projects.
