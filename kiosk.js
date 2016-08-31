
var exec = require('cordova/exec');

var KioskPlugin = {
    
    exitKiosk: function () {
        exec(null, null, "KioskPlugin", "exitKiosk", []);
    },
    
    killApp: function () {
        exec(null, null, "KioskPlugin", "killApp", []);
    },
    
    isInKiosk: function (callback) {
        exec(function(out){
            callback(out=="true");
        }, function(error){
            alert("KioskPlugin.isInKiosk failed: "+error);
        }, "KioskPlugin", "isInKiosk", []);
    },

    initKiosk: function (callback) {
        exec(function(out){
            callback(out=="true");
        }, function(error){
            alert("KioskPlugin.initKiosk failed: "+error);
        }, "KioskPlugin", "initKiosk", []);
    }
    
}

module.exports = KioskPlugin;

