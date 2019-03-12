angular.module('overview', [])
    .controller('main', function($scope, $http, $location, $timeout) {

        $scope.openedSummary = [];
        $scope.openedDownload = [];

        $scope.checkSummaryList = function(id) {

            for (var i=$scope.openedSummary.length-1; i>=0; i--) {
                if ($scope.openedSummary[i] === id) {
                    return false;
                }
            }
            return true;
        };
        $scope.checkDownloadList = function(id) {

            for (var i=$scope.openedDownload.length-1; i>=0; i--) {
                if ($scope.openedDownload[i] === id) {
                    return false;
                }
            }
            return true;
        };

        $scope.getDownloadButtons = function (manga) {
            if (manga.chapterAmount <= 50) {
                manga.downloadButtons = []
            }
            var ret = [];
            for (i = 0; i<manga.chapterAmount/50; i++){
                var button = [];
                button.href = 'files?file_name=' + $scope.getEncodedString(manga.title) + '&index=' + (i+1);
                button.text = "Download Chapters " + (i*50+1) + " - " + (i*50+50>manga.chapterAmount ? manga.chapterAmount : i*50+50);
                ret.push(button);
            }
            manga.downloadButtons = ret;
        };

        $scope.refresh = function(){
            $http.get($location.$$absUrl + 'mangas').
            then(function(response) {
                $scope.data = response.data;
                console.log($scope.data.items);
                loop = false;
                $scope.data.items.forEach(function(item){
                   if(item.status !== '' && !item.error){
                       loop = true;
                   }
                    $scope.getDownloadButtons(item);
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
        $scope.toggleDownload = function(id){
            var download = $("#download-dropdown-" + id);
            if(download.hasClass("hide")){
                download.removeClass("hide");
                $scope.openedDownload.push(id);
            }else{
                download.addClass("hide");
                for (var i=$scope.openedDownload.length-1; i>=0; i--) {
                    if ($scope.openedDownload[i] === id) {
                        $scope.openedDownload.splice(i, 1);
                    }
                }
            }
        };


        $scope.refresh();
    });