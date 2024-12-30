1. Go to app-xmpp -> kotlin+java -> telco -> BrilliantApi.java 
give proper url for variables url, xmppUrl and freeswitchUrl.
var url = "https://server_ip/test/api/VendorOTP/SendOTP"
var xmppURL = "http://server_ip::port/api/register"
var freeswitchURL = "http://server_ip:port"

3. Go to app-xmpp -> kotlin+java -> MainApplication.java
.setXMPP("hostAddress", "domain", port)
make change to the above line with proper values.

4. Go to chat-sdk-core-ui -> kotlin+java -> api -> RegisteredUserService.java
private static final String ejabberdApiUrl = "http://server_ip:port/api";
private static final String host = "host";
give proper url for variables ejabberdApiUrl and host.

5. Go to Janus -> kotlin+java -> Websocket.java
webSocket = new WebSocketClient(new URI("janus websocket url/"),httpHeaders)
give the proper websocket url here

6. Go to Janus -> res -> raw
add your janus server's .crt file here

7. Go to Janus-> res -> xml -> network_security_config.xml
add the import the recently added .crt file here (certificates src="@raw/janus certificate"/) dont include .crt extention here
include your janus server's ip here (<domain includeSubdomains="true">janus server domain</domain>)

