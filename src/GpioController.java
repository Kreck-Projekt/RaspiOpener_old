import com.pi4j.io.gpio.*;

public class GpioController {

    // gets called from TCPServer, if the App sends a command to activate GPIO pin 25
    public static void activate(long time)  throws InterruptedException {

        final com.pi4j.io.gpio.GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "MyLed", PinState.LOW);

        // sets the previously specified pin from 0V to 3.3V
        pin.high();
        System.out.println("pin on");
        Thread.sleep(2000);
        pin.low();
        System.out.println("pin off");

    }
}

