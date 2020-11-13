cordova.define("cordova-plugin-google-ump.Ump", function(require, exports, module) {
	var exec = require('cordova/exec');
	
	var PLUGIN_NAME = 'Ump';
	
	var Ump = {
		verifyConsent: function(pubId, privacyUrl, isDebug, cb, cbError) {
			exec(cb, cbError, PLUGIN_NAME, 'verifyConsent', [pubId, privacyUrl, isDebug]);
		},
		forceForm: function(cb, cbError) {
			exec(cb, cbError, PLUGIN_NAME, 'forceForm');
		}
	};
	
	module.exports = Ump;

});