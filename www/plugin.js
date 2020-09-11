
var exec = require('cordova/exec');

var PLUGIN_NAME = 'Ump';

var Ump = {
	verifyConsent: function(isAgeConsent, isDebug, cb, cbError) {
		exec(cb, cbError, PLUGIN_NAME, 'verifyConsent', [isAgeConsent, isDebug]);
	},
	forceForm: function(cb, cbError) {
		exec(cb, cbError, PLUGIN_NAME, 'forceForm');
	}
};

module.exports = Ump;
