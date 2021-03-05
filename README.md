# RaspiOpener

Open source project to remotely operate electronic door openers.
You need to connect a raspberry pi (zero?) with a relay to the normal button that you use.
The App to use with it is "DoorOpenerApp"

# Command syntax:

Store Key: k:'key' <br/>
Store Password: p:('hash');'nonce' <br/>
Change Password: c:('oldHash';'newHash');'nonce' <br/>
Set new OTP: "s:('otp';'hash');'nonce' <br/>
Use OTP: e:'otp';'time' <br/>
Open: o:('hash';'time');'nonce' <br/>
Reset: r:('hash');'nonce' <br/>
