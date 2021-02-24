import com.pi4j.io.gpio.*;

public class GpioController {

    final static com.pi4j.io.gpio.GpioController gpio = GpioFactory.getInstance();
    final static GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "MyLed", PinState.LOW);

    // gets called from TCPServer, if the App sends a command to activate GPIO pin 25
    public static void activate(long time)  throws InterruptedException {
        // sets the previously specified pin from 0V to 3.3V
        pin.high();
        Thread.sleep(time);
        pin.low();
        System.out.println("lmao");
    }
}

