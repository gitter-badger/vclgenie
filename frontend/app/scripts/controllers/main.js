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
    $scope.ruleset.backends = []; 

    $scope.form = {} ; 
    $scope.form.conditions = data.conditions ; 
    $scope.form.actions = data.actions; 
    $scope.form.vcl_matchers = data.vcl_matchers ; 

    $scope.globalconditions = []; 

    $scope.vcl = {}; 


  }).error(function(error) { 
  });

 
  //------------------------
  // Hostname Functions
  //-------------------------
  $scope.addHost = function() {
  	$scope.ruleset.hostnames.push({"hostname" : $scope.ruleset.hostname});
    $scope.ruleset.hostname = undefined; 
  }

  $scope.removeHost = function(idx) {
  	$scope.ruleset.hostnames.splice(idx,1);
  }

  //-------------------------
  // Backend Functions
  //---------------------------
  $scope.addBackend = function() { 
    if ($scope.ruleset.backend_header == undefined) 
      $scope.ruleset.backend_header = $scope.ruleset.backend_host ; 

    $scope.ruleset.backends.push({"name" : $scope.ruleset.backend_name, "host" : $scope.ruleset.backend_host, "host_header" : $scope.ruleset.backend_header});

    $scope.ruleset.backend_name = undefined ; 
    $scope.ruleset.backend_host = undefined ; 
    $scope.ruleset.backend_header = undefined ; 

  }

  $scope.removeBackend = function(idx) { 
    $scope.ruleset.backends.splice(idx,1); 
  }

  //---------------------------------
  // View conditions
  //---------------------------------
  $scope.showNameCond = function(cond) {
    var c = getConditionByKey(cond); 

    if (c != undefined && c.condition_type == 'NameValCond') 
      return true ;
    else 
      return false; 
  }

  $scope.showValCond = function(cond) {
    var c = getConditionByKey(cond); 

    console.log("Checking condition:"); console.log(c);

    if (c != undefined && c.condition_type != 'BoolCond' ) 
      return true ;
    else 
      return false; 
  }

  $scope.showMatchCond = function(cond) {
    var c = getConditionByKey(cond); 

    if (c != undefined && c.condition_type != 'BoolCond') 
      return true ; 
    else 
      return false ; 
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
    }
  	else if (rule_type == 'ordered') {
  		$scope.currentOrderedAction = action ; 
    }
  }

  $scope.addAction = function(rule_type) {
    if (rule_type == 'global')
     $scope.globalrule.actions.push({}); 
    else if (rule_type == 'ordered')
      $scope.orderedrule.actions.push({});
  }


  //-------------------------
  // Rule Functions
  //-------------------------
  $scope.addGlobalRule = function() {
  	var rule = {} ;

  	rule.conditions = $scope.globalrule.conditions ; 
    rule.actions = $scope.globalrule.actions ; 
  	rule.match_type = $scope.globalrule.rule_match_type ; 

  	$scope.ruleset.global_rules.push(rule); 

    resetGlobalCondition(); 
  }

  $scope.addOrderedRule = function() {
    var rule = {};

    rule.conditions = $scope.orderedrule.conditions;
    rule.actions = $scope.orderedrule.actions; 
    rule.match_type = $scope.orderedrule.rule_match_type; 

    $scope.ruleset.ordered_rules.push(rule);

    resetOrderedCondition(); 
  }

  $scope.removeGlobalRule = function(idx) {
  	$scope.ruleset.global_rules.splice(idx,1); 
  }

  $scope.removeOrderedRule = function(idx) { 
    $scope.ruleset.ordered_rules.splice(idx,1); 
  }


  $scope.conditionToText = function(c) {
    var cond = getConditionByKey(c); 
    var str = '' ;

    str = str + cond.label ; 
    if (cond.condition_type == 'NameValCond')
      str = str + ' ' + cond.name ; 

    if (cond.condition_type == 'NameValCond' || cond.condition_type == 'ValCond')
      str = str + matcherToText(cond.matcher) + ' ' + cond.value ; 

    return str ; 
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


  //----------------------
  // Generate VCL 
  //----------------------
  function addIndexToRules(rules) {
    var withIndex = []; 

    for(var i =0 ; i < rules.length; i++) {
      withIndex[i] = rules[i] ; 
      withIndex[i].index = i ; 
    }
    return withIndex ; 
  }

  $scope.generateVcl = function() {
     $scope.vcl.errors = ''; 
     $scope.vcl.output = 'Loading VCL........'; 

     var ordered = addIndexToRules($scope.ruleset.ordered_rules);

     console.log(ordered);

  	 Api.generate($scope.ruleset.hostnames, ordered, $scope.ruleset.global_rules, $scope.ruleset.backends  ).then(
      function(response) {  $scope.vcl.output = response.data ; },
      function(error) { 
        $scope.vcl.errors = error.data.errors ; 
        $scope.vcl.output = 'Error generating VCL';
      }
     )
  }

  //------------------------------
  // Private Methods 
  //------------------------------
  function resetGlobalCondition() {
  	$scope.currentGlobalCondition = undefined ; 
    $scope.currentGlobalAction = undefined ; 
  	$scope.globalrule.conditions = []; 
    $scope.globalrule.actions = [];  
    $scope.globalrule.rule_match_type = undefined ; 	
  }

   function resetOrderedCondition() {
    $scope.currentOrderedCondition = undefined ; 
    $scope.currentOrderedAction = undefined ; 
    $scope.orderedrule.conditions = []; 
    $scope.orderedrule.actions = [];  
    $scope.orderedrule.rule_match_type = undefined ;   
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

   $scope.sortableOptions = {
    containment: '#sortable-container'
  };


}]);
