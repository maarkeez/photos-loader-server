export function PhotoViewerController($http) {

  var self = this;
  self.photos = []

  $http.get('/api/photos').then(function(response) {
   self.photos = response.data;
  });

};