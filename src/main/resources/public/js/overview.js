angular.module('overview', [])
    .controller('main', function($scope, $http, $location) {

        $scope.refresh = function(){
            $http.get($location.$$absUrl + 'mangas').
            then(function(response) {
                $scope.data = response.data;
                console.log($scope.data.items);
            });
        };

        $scope.post = function(input) {
            $http.post($location.$$absUrl + 'mangas', input.url);
            $scope.input = "";
        };

        $scope.refresh();
    });