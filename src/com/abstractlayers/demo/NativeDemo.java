package com.abstractlayers.demo;


import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class NativeDemo extends Activity {

	TrimmedDownCallbackServer trimmedServer;
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	trimmedServer = new TrimmedDownCallbackServer();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/www/index.html");
        
        webView.setWebChromeClient(new WebChromeClient(){
        	public boolean onJsPrompt(WebView view,
        			                  String url, 
        			                  String message,
        			                  String defaultValue,
        			                  JsPromptResult result){
	           if(message.trim().equals("get_port")){   
	        	    result.confirm(generateResponseForPort());
	           } else { 
	        	    result.confirm(genrateResponseForClick( message, defaultValue));
	           }
        	   return true;
        	}
        });
    }
    
    private String generateResponseForPort(){
        String response = trimmedServer.getPort()+"";
  	    return response;
    }
    
    private String genrateResponseForClick(String message, String defaultValue){
    	String response = "";
    	String androidResult = "";
    	String [] array =defaultValue.split(":");
		String respChannel = array[0];
        String params = array[1];
        String callbackId = array[2];
         ///// do some action and generate result .
        if(respChannel.equals("Callbackserver")) {
     	     androidResult = "Server -"+message;
	         String msgForQueue = "callback.onSuccess('"+callbackId+"','"+androidResult+"');";
             trimmedServer.addMessagetoQueue(msgForQueue);
        } else {
     	   ///// send response via JS confirm message
     	       androidResult = "JS Prompt-"+message;
		       response = "callback.onSuccess('"+callbackId+"','"+androidResult+"');";
        }
        return response;
    }
}
