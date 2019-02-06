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

let cellCtrl = function($scope, $http) {

};

let bubbleCtrl = function($scope, $http) {

};

let flagCtrl = function($scope, $http) {

};

let snakeCtrl = function($scope, $http) {

};

/** angular loading **/
angular.module('ngMainApp', ['ui.router', 'ui.bootstrap'])
    .config(['$stateProvider', appConfig])
    // game controller
    .controller('ngCellController', ['$scope', '$http', cellCtrl])
    .controller('ngBubbleController', ['$scope', '$http', bubbleCtrl])
    .controller('ngFlagController', ['$scope', '$http', flagCtrl])
    .controller('ngSnakeController', ['$scope', '$http', snakeCtrl])
    .run(['$rootScope', '$state', '$http', appRun]);
