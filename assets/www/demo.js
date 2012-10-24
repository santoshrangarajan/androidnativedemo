var methodStack = {},
    callbackId = 0,
    port;
   firsttime = true;

var callback = {
		startXhr : function() {
			xmlhttp = new XMLHttpRequest();
			xmlhttp.onreadystatechange = function() {
				console.log("xmlhttp on state changed" + xmlhttp.readyState);
				if (!xmlhttp) {
					console.log("Not xmlHttp");
					return;
				}
				if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
					console.log("xmlhttp status: " + xmlhttp.status);
					var msg = decodeURIComponent(xmlhttp.responseText);
					if(msg!='undefined'){
					 console.log("msg = "+msg);
					 eval(msg);
					}
					
				}
			}
			xmlhttp.open("GET", "http://127.0.0.1:" + port, true);
			xmlhttp.send();
		},

		onSuccess : function(callbackId, args) {
			methodStack[callbackId].success(args);
		},

		onError : function(callbackId, args) {
			methodStack[callbackId].fail(args);
		}
};


var demo = {
		callnative : function(successCallBackMethod, failureCallBackMethod, params) {

			if (firsttime) {
				port = prompt("get_port");
				firsttime = false;
			}
			callback.startXhr();
			callbackId++;
			methodStack[callbackId] = {
					success : successCallBackMethod,
					fail : failureCallBackMethod
			};
			result = prompt(params, params + ":" + callbackId);
			//console.log("Result= " + result);
			if (result != "") {
				eval(result);
			}
		}
};