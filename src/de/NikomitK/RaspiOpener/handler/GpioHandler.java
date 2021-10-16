package de.NikomitK.RaspiOpener.handler;

import com.pi4j.io.gpio.*;

public class GpioHandler {

    final static com.pi4j.io.gpio.GpioController gpio = GpioFactory.getInstance();
    final static GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "MyRelais", PinState.LOW);

    // gets called from TCPServer, if the App sends a command to activate GPIO pin 25
    // maybe the pin number will be customizable in the future, but for now you just
    // have to change "RaspiPin.GPIO_25" to "RaspiPin.GPIO_XX"
    public static void activate(long time)  throws InterruptedException {
        // sets the previously specified pin from 0V to 3.3V and back
        pin.high();
        Thread.sleep(time);
        pin.low();
    }
}

