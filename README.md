# RaspiOpener

Open source project to remotely operate electronic door openers.
You need to connect a raspberry pi (zero?) with a relay to the normal button that you use.
The App to use with it is "DoorOpenerApp"

# Command syntax:

Store Key: k:<key>
Store Password: p:(<hash>);<nonce>
Change Password: c:(<oldHash>;<newHash>);<nonce>
Set new OTP: "s:(<otp>;<hash>);<nonce>
Use OTP: e:<otp>;<time>
Open: o:(<hash>;<time>);<nonce>
reset: r:(<hash>);<nonce>
