function WebSocket(){
    if ("WebSocket" in window){
        alert("WebSocket is supported by your browser!");
        
        var ws = new WebSocket("wss://localhost:10020");
        
        ws.onopen = function(){
            //WebSocket is connected, send data using send()
            ws.send("Message to send");
            alert("Message id sent...");
        };
        
        ws.onclose = function(evt){
            var received_msg = evt.data;
            alert("Message is received...");
        };
        
        ws.onclose = function(){
            //web socket id closed.
            alert("Connection is closed...");
        };
    }else{
        //the browser doesn't support WebSocket
    }
}