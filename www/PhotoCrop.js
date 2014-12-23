/**
 * PhotoCrop is an addition to the Cordova Camera Library for Android and iOS with Cropping utility
 */

var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');

var pluginExport = {};

var constants = {
    DestinationType: {
        DATA_URL: 0, // Return base64 encoded string
        FILE_URI: 1, // Return file uri (content://media/external/images/media/2 for Android)
        NATIVE_URI: 2 // Return native uri (eg. asset-library://... for iOS)
    },
    CropType: {
        SQUARE: 0,
        SIZE: 1,
        ASPECT: 2
    }
}

// Tack on the Camera Constants to the base camera plugin.
for (var key in constants) {
    pluginExport[key] = constants[key];
}

/**
 * Gets a picture from source defined by "options.sourceType", and returns the
 * image as defined by the "options.destinationType" option.

 * The defaults are sourceType=CAMERA and destinationType=FILE_URI.
 *
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @param {Object} options
 */
pluginExport.cropPicture = function (successCallback, errorCallback, options) {
    argscheck.checkArgs('fFO', 'navigator.PhotoCrop.cropPicture', arguments);
    options = options || {};
    var getValue = argscheck.getValue;

    var fileUri = getValue(options.fileUri, '');
    var destinationType = getValue(options.destinationType, navigator.PhotoCrop.DestinationType.FILE_URI);

    var targetWidth = getValue(options.targetWidth || options.targetX, 0);
    var targetHeight = getValue(options.targetHeight || options.targetY, 0);

    var cropType = getValue(options.cropType, navigator.PhotoCrop.CropType.SQUARE);

    var args = [fileUri, destinationType, cropType, targetWidth, targetHeight];

    exec(successCallback, errorCallback, "PhotoCrop", "cropPicture", args);
};

module.exports = pluginExport;