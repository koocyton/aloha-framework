"use strict";

let WebSocketService = function(url) {
    let protocol = /^https/.test(window.location.protocol) ? "wss\:\/\/" : "ws\:\/\/";
    this.ws = /^ws/.test(url) ? new WebSocket(url) : new WebSocket(protocol + window.location.host + url);

    this.onOpen = function(callOpen) {
        if (typeof callOpen==="function") {
            this.ws.onopen = callOpen;
        }
        return this;
    };

    this.onClose = function(callClose) {
        if (typeof callClose==="function") {
            this.ws.onclose = callClose;
        }
        return this;
    };

    this.onError = function(callError) {
        if (typeof callError==="function") {
            this.ws.onerror = callError;
        }
        return this;
    };

    this.onMessage = function(callMessage) {
        if (typeof callMessage==="function") {
            this.ws.onmessage = callMessage;
        }
        return this;
    };

    this.send = function(message) {
        this.ws.send(message);
    };

    this.close = function() {
        try {
            this.ws.close();
        }
        catch(e) {

        }
    };
};

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

let cellCtrl = function($scope, $http, $websocket) {
    let ws = new WebSocketService("/game");
    ws.onMessage(function (e) {
        console.log(e);
    });
    setInterval(function(){
        console.log("ws.send(\"hello\")");
        ws.send("hello");
    }, 1000);
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
