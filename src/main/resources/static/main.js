import { Application } from './modules/application.js';
import { PhotoViewerController } from './modules/photo-viewer-controller.js';


window.onload = function() {

  let application = new Application();
  application.init();

  angular
    .module('photoLoaderApplication', [])
    .controller('PhotoViewerController', PhotoViewerController);

};