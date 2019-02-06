"use strict";

/** app config **/
let appConfig = function($stateProvider) {
    for (let i in gameList) {
        let game = gameList[i];
        $stateProvider.state(game.ui_sref, {
            url : game.ui_sref,
            templateUrl: 'tpl/'+game.ui_sref+'.html'
        });
    }
};

/** app execute **/
let appRun = function ($rootScope, $state, $http) {
    // menu data
    $rootScope.gameList = gameList;
    // 打开界面
    setTimeout(function(){$state.go("cell")}, 100);
    // 下滑菜单
    $rootScope.toggleDropdown = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $rootScope.status.isopen = !$rootScope.status.isopen;
    };
};

let cellCtrlSocket = function($websocket) {
    var dataStream = $websocket('ws://127.0.0.1:8081/game');
    var collection = [];
    dataStream.onMessage(function(message) {
        collection.push(JSON.parse(message.data));
    });
    setInterval(function(){
        dataStream.send(JSON.stringify({ action: 'get' }));
    }, 1000);
    return {
        collection: collection,
        get: function() {
            dataStream.send(JSON.stringify({ action: 'get' }));
        }
    };
};

let cellCtrl = function($scope, $http, $websocket) {
    $scope.cellSocketData = cellCtrlSocket($websocket);
    console.log($scope.cellSocketData);
};

let bubbleCtrl = function($scope, $http) {

};

let flagCtrl = function($scope, $http) {

};

let snakeCtrl = function($scope, $http) {

};

/** angular loading **/
angular.module('ngMainApp', ['ui.router', 'ui.bootstrap', 'ngWebSocket'])
    .config(['$stateProvider', appConfig])
    // game controller
    .controller('ngCellController', ['$scope', '$http', '$websocket', cellCtrl])
    .controller('ngBubbleController', ['$scope', '$http', bubbleCtrl])
    .controller('ngFlagController', ['$scope', '$http', flagCtrl])
    .controller('ngSnakeController', ['$scope', '$http', snakeCtrl])
    .run(['$rootScope', '$state', '$http', appRun]);
