/**
 * PhotoCrop is an addition to the Cordova Camera Library for Android and iOS with Cropping utility
 */

var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec'),
    PhotoCrop = require('./Camera');

var cameraExport = {};

// Tack on the Camera Constants to the base camera plugin.
for (var key in Camera) {
    cameraExport[key] = PhotoCrop[key];
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
cameraExport.getPicture = function(successCallback, errorCallback, options) {
    argscheck.checkArgs('fFO', 'PhotoCrop.getPicture', arguments);
    options = options || {};
    var getValue = argscheck.getValue;

    var fileUri = getValue(options.fileUri, '');
    var destinationType = getValue(options.destinationType, PhotoCrop.DestinationType.FILE_URI);

    var cropType = getValue(options.cropType, PhotoCrop.CropType.SQUARE);

    var args = [quality, destinationType, sourceType, targetWidth, targetHeight, encodingType,
                mediaType, allowEdit, correctOrientation, saveToPhotoAlbum, popoverOptions, cameraDirection, cropType];

    exec(successCallback, errorCallback, "PhotoCrop", "takePicture", args);
};

cameraExport.cleanup = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback, "PhotoCrop", "cleanup", []);
};

module.exports = cameraExport;
