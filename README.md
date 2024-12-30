1. Go to app-xpp -> kotlin+java -> telco -> BrilliantApi.java 
give proper url for variables url, xmppUrl and freeswitchUrl.

2. Go to app-xpp -> kotlin+java -> MainApplication.java
.setXMPP("103.209.42.15", "ej.hobenaki.com", 5222)
make change to the above line with proper values.

3. Go to app-xpp -> kotlin+java -> MainApplication.java
.setXMPP("103.209.42.15", "ej.hobenaki.com", 5222)
make changes to the above line with proper values.

4. Go to chat-sdk-core-ui -> kotlin+java -> api -> RegisteredUserService.java 
give proper url for variables ejabberdApiUrl and host.

5. Go to Janus -> kotlin+java -> Websocket.java
webSocket = new WebSocketClient(new URI("wss://36.255.68.143/"),httpHeaders)
give the proper websocket url here

6. Go to Janus -> res -> raw
add your janus server's .crt file here

7. Go to Janus-> res -> xml -> network_security_config.xml
add the import the recently added .crt file here (<certificates src="@raw/janus1"/>)
include your janus server's ip here (<domain includeSubdomains="true">36.255.68.143</domain>)

