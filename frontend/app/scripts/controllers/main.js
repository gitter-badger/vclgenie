'use strict';

angular.module('vclgenie').controller('MainCtrl', ['$scope','$http','Api', function($scope,$http, Api) {
 
  $http.get('/api/config').success(function(data) {

    //FOR EACH RULE 
    $scope.globalrule = {} ; 
    $scope.globalrule.conditions = [] ; 
    $scope.globalrule.actions = [] ; 
    //$scope.globalrule.rule_match_type = undefined ; 
 
    //FOR EACH RULE 
    $scope.orderedrule = {} ;     
    $scope.orderedrule.conditions = []; 
    $scope.orderedrule.actions = []; 
    $scope.orderedrule.match_type = undefined; 

    //HOLDS THE JSON TO SUBMIT 
    $scope.ruleset = {}; 
    $scope.ruleset.global_rules = [] ; 
    $scope.ruleset.ordered_rules = []; 
    $scope.ruleset.hostnames = []; 

    $scope.form = {} ; 
    $scope.form.conditions = data.conditions ; 
    $scope.form.actions = data.actions; 
    $scope.form.vcl_matchers = data.vcl_matchers ; 

    $scope.globalconditions = []; 


  }).error(function(error) { 
  });

 
  $scope.addHost = function() {
  	$scope.ruleset.hostnames.push({"hostname" : $scope.ruleset.hostname});
  }

  $scope.removeHost = function(idx) {
  	$scope.ruleset.hostnames.splice(idx,1);
  }

  $scope.isNameValCond = function(cond) {
    var c = getConditionByKey(cond); 

    if (c != undefined && c.condition_type == 'NameValCond') 
      return true ;
    else 
      return false; 
  }

  $scope.isNameValAction = function(cond) {
    var c = getActionByKey(cond); 

    if (c != undefined && c.action_type == 'NameValAction') 
      return true ;
    else 
      return false; 
  }

  $scope.isValAction = function(cond) {
    var c = getActionByKey(cond); 

    if (c != undefined && (c.action_type == 'ValAction' || c.action_type == 'NameValAction' || c.action_type == 'Units') ) 
      return true ;
    else 
      return false; 
  }

  $scope.selectCondition = function(key,rule_type) {
  	var cond = getConditionByKey(key);

  	if (rule_type == 'global')
  	  $scope.currentGlobalCondition = cond ; 
  	else if (rule_type == 'ordered') 
  	  $scope.currentOrderedCondition = cond; 
  }

  $scope.addCondition = function(rule_type) {
    if (rule_type == 'global')
  	 $scope.globalrule.conditions.push({}); 
    else if (rule_type == 'ordered')
      $scope.orderedrule.conditions.push({});
  }

  $scope.selectAction = function(key,idx,rule_type) {
  	var action = getActionByKey(key); 

  	if (rule_type == 'global') {
  	   $scope.currentGlobalAction = action ; 
      // if (action.action_type == 'Bool')  $scope.globalrule.actions[idx].value = "1" ; 
    }
  	else if (rule_type == 'ordered') {
  		$scope.currentOrderedAction = action ; 
     // if (action.action_type == 'Bool') $scope.orderedrule.actions[idx].value = "1"; 
    }
  }

  $scope.addAction = function(rule_type) {
    if (rule_type == 'global')
     $scope.globalrule.actions.push({}); 
    else if (rule_type == 'ordered')
      $scope.orderedrule.actions.push({});
  }

  $scope.addGlobalRule = function() {
  	var rule = {} ;
  	console.log("RULE MATCH TYPE : " ); console.log($scope.globalrule.rule_match_type); 

  	rule.conditions = $scope.globalrule.conditions ; 
    rule.actions = $scope.globalrule.actions ; 
  	rule.match_type = $scope.globalrule.rule_match_type ; 

  	$scope.ruleset.global_rules.push(rule); 
    resetGlobalCondition(); 
    console.log("GLOBAL RULES:"); console.log($scope.ruleset.global_rules); 
  }

  $scope.removeGlobalRule = function(idx) {
  	$scope.ruleset.global_rules.splice(idx,1); 
  }


  $scope.conditionToText = function(c) {
    var cond = getConditionByKey(c); 
    return cond.label; 
  }

  $scope.matcherToText = function(matcher) {
    var m = getMatcherByKey(matcher); 
    return m.label; 
  }

  $scope.actionToText = function(a) {
    var action = getActionByKey(a); 
    return action.label; 
  }

  $scope.showUnits = function(action) {
    var a = getActionByKey(action);
    if (a.action_type == 'Units') {
      return true; 
    }
    else 
      return false ; 
  }

  $scope.valueClass = function(action) {
    var a = getActionByKey(action);
    if (a.action_type == 'Units')
      return 'col-sm-8'; 
    else
      return 'col-sm-10';
  }

  $scope.generateVcl = function() {
  	 Api.generate($scope.ruleset.hostnames, [], $scope.ruleset.global_rules ).then(
      function(response) {  $scope.vcl = response.data ; },
      function(error) { $scope.errors = error.data ; }
     )
  }

  function resetGlobalCondition() {
  	$scope.currentGlobalCondition = undefined ; 
    $scope.currentGlobalAction = undefined ; 
  	$scope.globalrule.conditions = []; 
    $scope.globalrule.actions = [];  
    $scope.globalrule.rule_match_type = undefined ; 	
  }

  function getConditionByKey(key) {
  	for (var i = 0; i < $scope.form.conditions.length; i++) {
  		if ($scope.form.conditions[i].key == key) return $scope.form.conditions[i];   		
	}
	return undefined; 
  }

  function getActionByKey(key) {
  	for (var i = 0; i < $scope.form.actions.length; i++) {
  		if ($scope.form.actions[i].key == key) return $scope.form.actions[i];   		
	}
	return undefined; 
  }

  function getMatcherByKey(key) {
    for (var i = 0; i < $scope.form.vcl_matchers.length; i++) {
      if ($scope.form.vcl_matchers[i].key == key) return $scope.form.vcl_matchers[i];       
  	}
    return undefined; 
  }

}]);
