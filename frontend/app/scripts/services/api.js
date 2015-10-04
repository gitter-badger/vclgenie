angular.module('vclgenie').factory('Api', ['$http', function($http) {

 var service = {

   generate : function(hostnames, ordered_rules, global_rules, backends ) {
   	 var json = {}; 
   	 json.hostnames = hostnames ;
   	 json.ordered_rules = ordered_rules ; 
   	 json.global_rules = global_rules ; 
   	 json.backends = backends ; 
   	 // json.backends = []; 
   	 // json.backends.push({ "name" : "backend1", "host" : "www.myhost.com", "host_header" : "www.myhost.com"});

   	 return $http.post('/api/vcl',json); 
   }

 } 

 return service ; 

}]);