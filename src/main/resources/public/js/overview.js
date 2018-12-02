angular.module('overview', [])
    .controller('main', function($scope, $http, $location, $timeout) {

        $scope.refresh = function(){
            $http.get($location.$$absUrl + 'mangas').
            then(function(response) {
                $scope.data = response.data;
                console.log($scope.data.items);
                loop = false;
                $scope.data.items.forEach(function(item){
                   if(item.status !== ''){
                       loop = true;
                   }
                });
                if(loop){
                    $timeout(function() { $scope.refresh();}, 1000);
                }
            });
        };

        $scope.post = function(input) {
            $http.post($location.$$absUrl + 'mangas', input.url);
            $scope.input = "";
            $timeout(function() { $scope.refresh();}, 2000);
        };

        $scope.getEncodedString= function (string) {
            return encodeURI(string);
        }

        $scope.refresh();
    });