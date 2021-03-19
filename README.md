# RaspiOpener

Open source project to remotely operate electronic door openers.
You need to connect a raspberry pi (zero?) with a relay to the normal button that you use.
The App to use with it is "DoorOpenerApp"

# Installation Guide:
 install "RaspiOpener"  "sudo java -jar AliveKeeper.jar" in new screen

- flash sd card with RaspberryOS Lite (https://www.raspberrypi.org/software/operating-systems/)
- copy your wpa-supplicant.conf and the ssh file on the boot partition
  Thats how the wpa-supplicant.conf file could look:
  ```sh
  country=[insert your country code here without the square brackets]
  update_config=1
  ctrl_interface=/var/run/wpa_supplicant

  network={
  ssid="MyTestNetwork"
  psk="Password"
  }
  ```
- install all required:
  ```sh
  sudo apt-get install openjdk-8-jdk -y & sudo apt-get install wiringpi -y & sudo apt-get install screen -y & sudo apt-get install git -y
  ```
- install the two programms
  ```sh
  git clone https://github.com/Kreck-Projekt/AliveKeeper.git 
  git clone https://github.com/Kreck-Projekt/RaspiOpener.git
  ```   
- make the installed files executable
  ```sh
  sudo chmod -755 AliveKeeper.jar
  sudo chmod -755 RaspiOpener.jar
  ```  
- create a new screen and execute the AliveKeeper.jar
  ```sh
  screen -S RaspiOpener
  sudo java -jar AliveKeeper.jar
  ```  
  To exit hit Ctrl + A and then d

- now go through the init process in the app.
- If the initializing was succesful you should now open port 5000 in your network.
- After that change the IP-Addres in the app
  You can look up your public ip here:
  https://whatismyipaddress.com
  
  !Warning! After the reboot you must redo the last step 


# Command syntax:

Store Key: k:'key' <br/>
Store Password: p:('hash');'nonce' <br/>
Change Password: c:('oldHash';'newHash');'nonce' <br/>
Set new OTP: "s:('otp';'hash');'nonce' <br/>
Use OTP: e:'otp';'time' <br/>
Open: o:('hash';'time');'nonce' <br/>
Reset: r:('hash');'nonce' <br/>
