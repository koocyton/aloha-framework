"use strict";

let WebSocketService = function(url) {

    let protocol = /^https/.test(window.location.protocol) ? "wss\:\/\/" : "ws\:\/\/";
    this.ws = /^ws/.test(url) ? new WebSocket(url) : new WebSocket(protocol + window.location.host + url);

    this.onOpen = function(callOpen) {
        if (typeof callOpen==="function") { this.ws.onopen = callOpen; }
        return this;
    };

    this.onClose = function(callClose) {
        if (typeof callClose==="function") { this.ws.onclose = callClose; }
        return this;
    };

    this.onError = function(callError) {
        if (typeof callError==="function") { this.ws.onerror = callError; }
        return this;
    };

    this.onMessage = function(callMessage) {
        if (typeof callMessage==="function") { this.ws.onmessage = callMessage; }
        return this;
    };

    this.send = function(message) {
        this.ws.send(message);
    };

    this.close = function() {
        try { this.ws.close(); } catch(e) { ; }
    };
};

/** login app && login controller **/
angular.module('ngLoginApp', ['ui.bootstrap', 'ngCookies'])
    .controller('ngLoginController', ['$scope', '$http', '$cookies', function ($scope, $http, $cookies) {
        $scope.formData = {};
        $scope.submitForm = function() {
            $http.post('/oauth/api/login', $scope.formData)
                .then(function(response) {
                        if (response.status===200) {
                            let loginResponse = response.data.data;
                            let expireDate = new Date();
                            expireDate.setDate(expireDate.getDate() + loginResponse.expire);
                            $cookies.put("se_id", loginResponse.token,{'expire': expireDate});
                            window.top.location = "/manage/index.html";
                        }
                    },
                    function(response) {
                        console.log(response.data.message)
                    });
        };
        // 清除 cookie
        $cookies.remove("se_id");
        // 下滑菜单
        $scope.toggleDropdown = function($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.status.isopen = !$scope.status.isopen;
        };
        // 登录需要 app 认证
        $http.get('/manage/api/authentication').then(function(response) {
            if (response.status===200) {
                let authenticationResponse = response.data.data;
                $scope.formData.client = authenticationResponse.client;
                $scope.formData.time = authenticationResponse.time;
                $scope.formData.security = authenticationResponse.security;
                $scope.formData.data = {account:"", password:""};
            }
        });
    }]
);

/** logout **/
angular.module('ngLogoutApp', []).run(function() {
        let ws = new WebSocketService("/game");
        ws.onMessage(function (e) {
            console.log(e);
        });
        // setInterval(function(){
        //     console.log("ws.send(\"hello\")");
        //     ws.send("hello");
        // }, 1000);
    });

/** 高亮代码块 **/
let highlightBlock = function() {
    if (typeof window.hljs!=="undefined") {
        let elts = document.querySelectorAll("pre code");
        if (typeof elts !== "undefined" && elts.length>=1) {
            for (let ii=0; ii<elts.length; ii++) {
                hljs.highlightBlock(elts[ii]);
            }
        }
    }
};

/** 从 String[0] 开始 比对字符串相似度 **/
let stringMatchRank = function (url1, url2) {
    let rank = 0;
    for (let s = 0; s < url2.length; s++) {
        if (url2[s] !== url1[s]) {
            break;
        }
        rank++;
    }
    return rank;
};

/** 页面加载的事件，左侧菜单高亮 **/
let locationChangeSuccess = function ($rootScope, locationUrl) {
    let originalPath = (locationUrl.split("#!"))[1];
    if (typeof originalPath==="undefined") {
        return;
    }
    let selectedMenuItem = null;
    let selectedMenuItemParent = null;
    let matchMenuRank = 0;
    for (let i in $rootScope.mainMenuData) {
        for (let n in $rootScope.mainMenuData[i]["menus"]) {
            if (typeof $rootScope.mainMenuData[i]["menus"][n]["menus"] === "object") {
                for (let m in $rootScope.mainMenuData[i]["menus"][n]["menus"]) {
                    $rootScope.mainMenuData[i]["menus"][n]["menus"][m]["selected"] = false;
                    let rank = stringMatchRank(originalPath, $rootScope.mainMenuData[i]["menus"][n]["menus"][m]["href"]);
                    if (rank > matchMenuRank) {
                        matchMenuRank = rank;
                        selectedMenuItem = $rootScope.mainMenuData[i]["menus"][n]["menus"][m];
                        selectedMenuItemParent = $rootScope.mainMenuData[i]["menus"][n];
                    }
                }
            }
            else {
                $rootScope.mainMenuData[i]["menus"][n]["selected"] = false;
                let rank = stringMatchRank(originalPath, $rootScope.mainMenuData[i]["menus"][n]["href"]);
                if (rank > matchMenuRank) {
                    matchMenuRank = rank;
                    selectedMenuItem = $rootScope.mainMenuData[i]["menus"][n];
                    selectedMenuItemParent = $rootScope.mainMenuData[i];
                }
            }
        }
    }
    selectedMenuItem["selected"] = true;
    selectedMenuItemParent["open"] = true;
    highlightBlock(); // 高亮代码块
};

/** 配置 ui route **/
let uiRouteConfig = function($stateProvider, mainMenuData) {
    for (let i in mainMenuData) {
        for (let n in mainMenuData[i]["menus"]) {
            let menuItem = "";
            if (typeof mainMenuData[i]["menus"][n]["menus"] === "object") {
                for (let m in mainMenuData[i]["menus"][n]["menus"]) {
                    menuItem = mainMenuData[i]["menus"][n]["menus"][m];
                    $stateProvider.state(menuItem.ui_sref, {
                        url : menuItem.href,
                        params: (typeof menuItem.params==="undefined") ? {} : menuItem.params,
                        templateUrl: 'tpl'+menuItem.href+'.html'
                    });
                }
            }
            else {
                menuItem = mainMenuData[i]["menus"][n];
                $stateProvider.state(menuItem.ui_sref, {
                    url : menuItem.href,
                    params: (typeof menuItem.params==="undefined") ? {} : menuItem.params,
                    templateUrl: 'tpl'+menuItem.href+'.html'
                });
            }
        }
    }
};

/** app config **/
let appConfig = function($stateProvider) {
    uiRouteConfig($stateProvider, mainMenuData);
};

/** 获取 uri 对应的 toKey ，如果 uri 为空，返回第一个 toKey **/
let getUriKey = function(mainMenuData, uri) {
    for (let i in mainMenuData) {
        for (let n in mainMenuData[i]["menus"]) {
            if (typeof mainMenuData[i]["menus"][n]["menus"] === "object") {
                for (let m in mainMenuData[i]["menus"][n]["menus"]) {
                    // 如果 uri 为空，返回第一个 tokey
                    if (typeof uri === "undefined") {
                        return mainMenuData[i]["menus"][n]["menus"][m]["ui_sref"];
                    }
                    // 获取 uri 对应的 toKey
                    if (mainMenuData[i]["menus"][n]["menus"][m]["href"] === uri) {
                        return mainMenuData[i]["menus"][n]["menus"][m]["ui_sref"];
                    }
                }
            }
            else {
                // 如果 uri 为空，返回第一个 tokey
                if (typeof uri === "undefined") {
                    return mainMenuData[i]["menus"][n]["ui_sref"];
                }
                // 获取 uri 对应的 toKey
                if (mainMenuData[i]["menus"][n]["href"] === uri) {
                    return mainMenuData[i]["menus"][n]["ui_sref"];
                }
            }
        }
    }
};

/** app execute **/
let appRun = function ($rootScope, $state, $http) {
    // 检查登陆成功才加载其他
    checkLoginSuccess($rootScope, $state, $http, function($rootScope, $state, checkLoginData){
        // menu data
        $rootScope.mainMenuData = mainMenuData;
        // 左侧主菜单点击处理
        $rootScope.$on('$locationChangeSuccess', function (event, locationUrl){
            locationChangeSuccess($rootScope, locationUrl);
        });
        // manager
        $rootScope.currentManager = checkLoginData.data;
        // 打开界面
        setTimeout(function(){
            $state.go(getUriKey($rootScope.mainMenuData, location.href.split("#!")[1]));
            locationChangeSuccess($rootScope, location.href);
        }, 100);
        // 下滑菜单
        $rootScope.toggleDropdown = function($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $rootScope.status.isopen = !$rootScope.status.isopen;
        };
    });
};

/** check user login **/
let checkLoginSuccess = function($rootScope, $state, $http, success) {
    // 读取登录消息
    $http.get('/manage/api/manager')
        .then(function(response){
            if (response.status===200) {
                success($rootScope, $state, response.data);
            }
            else {
                window.top.location = "/manage/login.html";
            }
        });
};

/** 列表 分页 **/
let pageList = function ($scope, $http, $uri) {
    $scope.pageList = {currentPage:1};
    $scope.pageChanged = function() {
        $http.get($uri + "?page=" + $scope.pageList.currentPage).then(function (response) {
            if (response.status === 200) {
                $scope.pageList = response.data.data;
                document.getElementsByClassName("body-content-right")[0].scrollTop = 0;
            }
        });
    };
    $scope.pageChanged();
};

/** angular loading **/
angular.module('ngMainApp', ['ui.router', 'ui.bootstrap'])
    // app config
    .config(['$stateProvider', appConfig])
    // icon controller
    .controller('ngIconsController', ['$scope', '$http', function ($scope, $http) {
        // 加载主菜单
        $http.get('tpl/doc/icons_data.json').then(function(response){
            $scope.iconsData = response.data;
        });
    }])
    // user controller
    .controller('ngUserController', ['$scope', '$http', function ($scope, $http) {
        pageList($scope, $http, "/manage/api/users");
    }])
    // app controller
    .controller('ngAppController', ['$scope', '$http', function ($scope, $http) {
        pageList($scope, $http, "/manage/api/apps");
    }])
    // app execute
    .run(['$rootScope', '$state', '$http', appRun]);
