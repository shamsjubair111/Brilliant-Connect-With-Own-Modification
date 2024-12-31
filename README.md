1. Go to app-xmpp -> kotlin+java -> telco -> BrilliantApi.java 
give proper url for variables url, xmppUrl and freeswitchUrl.
var url = "https://server_ip/test/api/VendorOTP/SendOTP"
var xmppURL = "http://server_ip:port/api/register"
var freeswitchURL = "http://server_ip:port"

2. Go to app-xmpp -> kotlin+java -> MainApplication.java
.setXMPP("hostAddress", "domain", port)
make change to the above line with proper values.

3. Go to chat-sdk-core-ui -> kotlin+java -> api -> RegisteredUserService.java
private static final String ejabberdApiUrl = "http://server_ip:port/api";
private static final String host = "host";
give proper url for variables ejabberdApiUrl and host.

4. Go to Janus -> kotlin+java -> Websocket.java
webSocket = new WebSocketClient(new URI("janus websocket url/"),httpHeaders)
give the proper websocket url here

5. Go to Janus -> res -> raw
add your janus server's .crt file here

6. Go to Janus-> res -> xml -> network_security_config.xml
add the import the recently added .crt file here (certificates src="@raw/janus certificate"/) dont include .crt extention here
include your janus server's ip here (<domain includeSubdomains="true">janus server domain</domain>)

7.  iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").setUsername("83eebabf8b4cce9d5dbcb649").setPassword("2D7JvfkOQtBdYW3R").createIceServer());
keep the above stun server as the default stun server.


