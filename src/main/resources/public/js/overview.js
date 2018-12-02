angular.module('overview', [])
    .controller('main', function($scope, $http, $location, $timeout) {

        $scope.openedSummary = [];

        $scope.checkList = function(id) {

            for (var i=$scope.openedSummary.length-1; i>=0; i--) {
                if ($scope.openedSummary[i] === id) {
                    return false;
                }
            }
            return true;
        };

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
        };

        $scope.toggleSummary = function(id){
            var summary = $("#summary-dropdown-" + id);
            if(summary.hasClass("hide")){
                summary.removeClass("hide");
                $scope.openedSummary.push(id);
            }else{
                summary.addClass("hide");
                for (var i=$scope.openedSummary.length-1; i>=0; i--) {
                    if ($scope.openedSummary[i] === id) {
                        $scope.openedSummary.splice(i, 1);
                    }
                }
            }
        };

        $scope.refresh();
    });