/**
 *  Скрипт для страницы ожидания  ( wait.jsp )
 */
		var timer = 3;
		var left = timer;
		var cmd = "run";
		
		$(document).ready(function() {

			GetCnt(cmd);
			cmd = "get";
			var timerId = setTimeout(function tick() {
				left--;
				 $("#wait").text('Дайте подумать '+str_repeat('.',timer-left));
				GetCnt(cmd);

				if(left<0){
					$("#wait").text('Дайте подумать ');
					left =timer;
				}
				  timerId = setTimeout(tick, 1000);
				}, 3000);
		});
		
 
    var GetCnt = function(cmnd) {
    	var tmpvar = (new Date().getTime());
    	$.ajax({
    		url : '/nanogoogle/getcnt',
    		type: 'GET',
    		dataType: 'json',
    		contentType: 'application/json',
    	    mimeType: 'application/json',
    		data : ({
    			cmd: ''+cmnd, tmp:''+tmpvar
    		}),
    		success: function (data) {	
    			console.log('tmp='+tmpvar+'     count='+data.count);
    			 if(data.count<0){
    				 window.location ="http://"+window.location.host+ "/nanogoogle/";
    			 }else{
    				 $("#cnt").text('Оработано документов  :  '+data.count);
    			 }
    		},
    		  error: function(e) {
    			  $("#cnt").text(e.message);
    			  console.log(e.message);
    		  }
    	});

     }
    
    var BreakCnt = function() {
    	cmd = "break";
    	var tmpvar = (new Date().getTime());
    	$.ajax({
    		url : '/nanogoogle/getcnt',
    		type: 'GET',
    		dataType: 'json',
    		contentType: 'application/json',
    	    mimeType: 'application/json',
    		data : ({
    			cmd: 'break' , tmp:''+tmpvar
    		}),
    		success: function (data) {	
    			 $("#cnt").text('Оработано документов  :  '+data.count);
    			 if(data.count<0){
    				 window.location ="http://"+window.location.host+ "/nanogoogle/";
    			 }
    		}
    	});
     }

    function str_repeat ( input, multiplier ) {	
    	var buf = '';
    	for (i=0; i < multiplier; i++){ 	buf += input;   	}
    	return buf;
    }  