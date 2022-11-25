import AdSupport
import UserMessagingPlatform

@objc(Ump)
class Ump : CDVPlugin {
    
    override func pluginInitialize() {
        super.pluginInitialize()
    }
    
    @objc(verifyConsent:)
    func verifyConsent(command: CDVInvokedUrlCommand) {
        let isAgeConsent = command.arguments[0] as? Bool ?? false
        let isDebug = command.arguments[1] as? Bool ?? false
        
        print("isAgeConsent: \(isAgeConsent)")
        print("isDebug: \(isDebug)")


        let debugSettings = UMPDebugSettings()
        let parameters = UMPRequestParameters()
        
        if isDebug {
            let deviceId = ASIdentifierManager.shared().advertisingIdentifier.uuidString
            print("DEBUG, adding identifier \(deviceId) to debug ids")
            debugSettings.testDeviceIdentifiers = [deviceId]
            debugSettings.geography = .EEA
        }
        
        parameters.debugSettings = debugSettings
        
        if isAgeConsent {
            parameters.tagForUnderAgeOfConsent = true
        } else {
            parameters.tagForUnderAgeOfConsent = false
        }

        UMPConsentInformation.sharedInstance.requestConsentInfoUpdate(with: parameters, completionHandler: {(error) in
            if let error = error as NSError? {
                // deal with error
                print("Error verifyConsent: \(error.localizedDescription)")
                self.endError(error: error.localizedDescription, command: command)
            } else { // now check for available form and load it
             
              let formStatus = UMPConsentInformation.sharedInstance.formStatus
              if formStatus == .available {
                self.loadForm(command: command, forceForm: false)
              } else if formStatus == .unavailable {
                // just in case no consent form is required
                print("Consent form not required")
                let jsonResult = ["consent" : true, "hasShownDialog" : false, "formAvailable": false] as [AnyHashable : Any]
                self.endSuccess(jsonResult: jsonResult, command: command)
              }
            }
        })
    }
    
    func endError(error: String, command: CDVInvokedUrlCommand) {
        self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs:error), callbackId: command.callbackId)
    }
    
    func endSuccess(jsonResult : [AnyHashable : Any], command: CDVInvokedUrlCommand) {
        self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: jsonResult), callbackId: command.callbackId)
    }
        
    func loadForm(command: CDVInvokedUrlCommand, forceForm:Bool) {
        UMPConsentForm.load(completionHandler: { (form, loadError) in
            if let error = loadError as NSError? {
                // deal with error loading form
                print("Error verifyConsent: \(error.localizedDescription)")
                self.endError(error: error.localizedDescription, command: command)
            } else {
                if forceForm == true
                {
                    // force display the form
                    // usable for settings section
                    form?.present(from: self.viewController, completionHandler: {(error) in
                        if UMPConsentInformation.sharedInstance.consentStatus == .obtained {
                            // user gave consent
                            // OK to serve ads
                            print("User got the form and gave consent")
                            let jsonResult = ["consent" : true, "hasShownDialog" : true, "formAvailable": true] as [AnyHashable : Any]
                            self.endSuccess(jsonResult: jsonResult, command: command)
                        } else {
                            print("User got the form and didn't give consent")
                            let jsonResult = ["consent" : false, "hasShownDialog" : true, "formAvailable": true] as [AnyHashable : Any]
                            self.endSuccess(jsonResult: jsonResult, command: command)
                        }
                    })
                    
                } else
                {
                    // If user didn't get a consent form before, display it
                    if UMPConsentInformation.sharedInstance.consentStatus == .required {
                        form?.present(from: self.viewController, completionHandler: {(error) in
                            if UMPConsentInformation.sharedInstance.consentStatus == .obtained {
                                // user gave consent
                                // OK to serve ads
                                print("User got the form and gave consent")
                                let jsonResult = ["consent" : true, "hasShownDialog" : true, "formAvailable": true] as [AnyHashable : Any]
                                self.endSuccess(jsonResult: jsonResult, command: command)
                            } else {
                                print("User got the form and didn't give consent")
                                let jsonResult = ["consent" : false, "hasShownDialog" : true, "formAvailable": true] as [AnyHashable : Any]
                                self.endSuccess(jsonResult: jsonResult, command: command)
                            }
                        })
                    } else if UMPConsentInformation.sharedInstance.consentStatus == .obtained {
                        // if user received the consent form before and gave consent
                        // OK to serve ads
                        print("User gave consent before")
                        let jsonResult = ["consent" : true, "hasShownDialog" : false, "formAvailable": true] as [AnyHashable : Any]
                        self.endSuccess(jsonResult: jsonResult, command: command)
                    }
                }
            }
        })
    }
    
    @objc(forceForm:)
    func forceForm(command: CDVInvokedUrlCommand) {
        
        self.loadForm(command: command, forceForm: true)
    }

    @objc(reset:)
    func reset(command: CDVInvokedUrlCommand) {
    	UMPConsentInformation.sharedInstance.reset()
	self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
    }
}
