angular.module('overview', [])
    .controller('list', function($scope, $http) {
        $http.get('http://localhost:8080/mangas').
        then(function(response) {
            $scope.greeting = response.data.items[0];
            console.log(response.data.items);
        });
    });