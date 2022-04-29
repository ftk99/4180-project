# The Bluetooth Batmobile


**Georgia Institute Technology** 

***

**Team Members: Colin Chen,  Beaudly Leriche, Rafael Laury,  Andrew Cline, Ashaan Facey**
(from left to right)


![IMG-2144](https://user-images.githubusercontent.com/104608117/165966958-43f30ebd-b7cc-4b71-a8e5-63fe10a6f7ba.jpg)

***
**Before**


![IMG_6713](https://user-images.githubusercontent.com/104608117/165966545-0472f7cd-f2b0-4848-8e61-501e6719db3d.jpg)

***
**During**


![IMG-2136](https://user-images.githubusercontent.com/104608117/165965660-41322448-bfe4-4345-b522-8e767adf6f80.jpg)

***
**After**


![IMG-2137](https://user-images.githubusercontent.com/104608117/165965689-2e52beb2-8dbd-4239-b4d4-6e74854d94e1.jpg)

***

We had an old RC car that didn't really work well. the RC car was rebuilt using a mbed controller and other electronics with extra functionalities added such as Bluetooth control and a back-up sensor using sonar radars, audio output and LED strips.
An interface was created on the Adafruit Bluetooth app to control the servo motor and a Hbridge to control a DC Motor. the servo motor controlled the front wheels which turned the car and the DC motor controlled the back wheels that controlled the speed of the car. The sonar sensor worked  with the led strip to detect when an object was within a 300 mm range of the front or back of the car. 

Tasks Completed 
* Modify the Adafruit Bluetooth app to create controls for the car
* Read the phone’s accelerometer values to steer the car
* Add an H-Bridge to control the DC Motor
* Add a servo motor to control direction of the car
* Add sonar sensors to detect obstacles in front and behind the vehicle
* Add two LED strips to visualize the detection of the sonar sensors
* Add a speaker and Class D Audio Amp to play a horn sound

***

# System Diagram

![RC Car System Diagram](https://user-images.githubusercontent.com/104608117/165981328-82f3d639-f34f-40ad-9f55-aea9408a9540.png)

***

# Parts List
* mbed LPC1768
* WS2812  LED strip
* 2x HC-SR04 Sonar Sensor
* Android phone - Bluefruit LE Connect for iOS and Android
* DC Motor
* Servo motor
* old RC car
* Sparkfun Speaker 8ohm .5W 

*** 

# Schematic Diagram
![ECE4180_schematic](https://user-images.githubusercontent.com/104608117/165953341-4bc718de-97da-4a4d-a380-9056d4d8bae1.png)

***

# Future Works
* Successfully created a car we can control via Bluetooth
* Current motor driver is insufficient to power the DC motor
> * Use a more powerful H-Bridge
> * Use a more powerful battery
* Use speaker to beep with obstacle detection
* Rocket League
> * Add a solenoid or two to the bottom of the car
> > * Makes the car jump in the air
> * Add a turbo mode 

# Source code 
```markdown
Syntax highlighted code block


****************** put code here ****************
`#include "mbed.h"
#include "Servo.h"
#include "neopixel.h"
#include "ultrasonic.h"

// This must be an SPI MOSI pin.
#define DATA_PIN p5

//int value;
#define DATA_PIN2 p11

RawSerial  pc(USBTX, USBRX);
RawSerial  dev(p28,p27);
//Serial bluemod(p28,p27);
DigitalOut led1(LED1);
DigitalOut led2(LED2);
DigitalOut led3(LED3);
DigitalOut led4(LED4);

DigitalOut in1(p23);
DigitalOut in2(p24);
PwmOut en(p25);

Servo servo_pin(p22);

PwmOut speaker(p21);

void dev_recv()
{
    led1 = !led1;
    while(dev.readable()) {
        pc.putc(dev.getc());
    }
}

void pc_recv()
{
    led4 = !led4;
    while(pc.readable()) {
        dev.putc(pc.getc());
    }
}

void s_system(float get_dist)

{
    if (get_dist < 300.0 && get_dist >200.0) {
        speaker.period(1.0/100.0);
        speaker = 0.5;
        //wait(2);
    }
    else if ( get_dist < 200.0 && get_dist > 100.0) {
        speaker.period(1.0/500.0);
        speaker = 0.5;
        //wait(2);
    }
    else if (get_dist < 100.0 && get_dist > 25.0){
        speaker.period(1.0/800.0);
        speaker = 0.5;
        //wait(2);
    }
    else  if (get_dist > 300 || get_dist < 20){
        speaker = 0.0;
    }
}


void LED_SIG(int value)
{        
        // Create a temporary DigitalIn so we can configure the pull-down resistor.
        // (The mbed API doesn't provide any other way to do this.)
        // An alternative is to connect an external pull-down resistor.
        DigitalIn(DATA_PIN, PullDown);
        // The pixel array control class.
        neopixel::PixelArray array(DATA_PIN);
        // Declare array/buffer of type Pixel.
        uint16_t numPixels = 8;

        for ( int x = 0; x < 8; x++){
            pixels[x].red = value;
            pixels[x].green = value;
            pixels[x].blue = value;
        }
        array.update(pixels, numPixels);
}
void LED_SIG2(float get_distan)
{      
        // Create a temporary DigitalIn so we can configure the pull-down resistor.
        // (The mbed API doesn't provide any other way to do this.)
        // An alternative is to connect an external pull-down resistor.
        DigitalIn(DATA_PIN2, PullDown);
       
        // The pixel array control class.
        neopixel::PixelArray array(DATA_PIN2);
        //get_distance = 0.0;
       
        float get_distance = get_distan;
        // Declare array/buffer of type Pixel.
        uint16_t numPixels = 8;
        neopixel::Pixel pixels[numPixels];
        if (get_distance <= 300.0 && get_distance >= 200) {
            for ( int x = 0; x < 8; x++){
               int value = 255;
                pixels[x].red = value;
                pixels[x].green = value;
                pixels[x].blue = value;
            }
           
        } else if (get_distance <= 199.0 && get_distance >= 25) {
            for ( int x = 0; x < 8; x++){
                int value = 255;
                pixels[x].red = value;
                pixels[x].green = 0;
                pixels[x].blue = 0;
            }
        } else if (get_distance > 300){
              for ( int x = 0; x < 8; x++){
                    int value = 0;

                    pixels[x].red = value;
                    pixels[x].green = value;
                    pixels[x].blue = value;
            }  
        }
        array.update(pixels, numPixels);
}

void dist(int distance)
{
        //pc.printf("Hello \n");
        //pc.printf("Distance %d mm\r\n", distance);
        //put code here to execute when the distance has changed
        int val;
        //pc.printf(" distance 1 : %d" ,distance);
        if (distance < 300)
        {
            val = 255;
            //thread.start(LED_SIG, &val);
            LED_SIG(val);
            //s_system(distance);
            //wait(0.5);
        } else {
            val = 0;
            //thread.start(LED_SIG, &val);
            LED_SIG(val);
            //speaker = 0.0;
            //wait(0.5);
        }
        //pc.printf("........");
        //ThisThread::wait(5);
}

void dist2(int distance)
{
        //pc.printf("Hello \n");
        //pc.printf("Distance %d mm\r\n", distance);
        //put code here to execute when the distance has changed
        int val;
        //pc.printf(" distance 2 : %d" ,distance);
        if (distance < 300)
        {
            val = 255;
            //thread.start(LED_SIG, &val);
            //LED_SIG(val);
            LED_SIG2(distance);
            //s_system(distance);
            //wait(0.5);
        } else {
            val = 0;
            //thread.start(LED_SIG, &val);
            //LED_SIG(val);
            LED_SIG2(distance);
            //speaker = 0.0;
            //wait(0.5);
        }      
        //pc.printf("........");
        //ThisThread::wait(5);
}

ultrasonic mu(p6, p7, .1, 1, &dist);    //Set the trigger pin to D8 and the echo pin to D9
                                        //have updates every .1 seconds and a timeout after 1
                                        //second, and call dist when the distance changes
ultrasonic mu2(p12, p13, .1, 1, &dist2);


int main()
{
    pc.baud(9600);
    dev.baud(9600);

    //pc.attach(&pc_recv, Serial::RxIrq);
    //dev.attach(&dev_recv, Serial::RxIrq);
    bool readTag = true;
    int tag = -1;
    int value = 0;
    int checkVal = 0;
   
    // Motor pins initialization
    en = 0.0;
    in1 = 1;
    in2 = 0;
   
    // Control variables
    //int accelerate = 0;
    //int decelerate = 0;
    //float servo_cntrl = 0.0;
   
    float pwm_out = 0.5;
    servo_pin = 0.5;

    mu.startUpdates();//start measuring the distance
    mu2.startUpdates();
    LED_SIG(0.0);
    LED_SIG2(0.0);
   
    while(1) {
       
        mu.checkDistance();     //call checkDistance() as much as possible, as this is where
                                //the class checks if dist needs to be called.
        mu2.checkDistance();
             
        if (dev.readable()){
            // ! markes the beginnig of a package
            // read the tag
            // , seperates tag from
            // read numbers until not readable or we get a !
            char readChar = dev.getc();
            checkVal = readChar - '0';
            if ((checkVal < 0 || checkVal > 9) && (readChar != '!' && readChar != ',')){
                //pc.printf("Error: %c\n", readChar);
                //pc.printf("CheckVal: %d\n", checkVal);
                while(1) {
                    if (dev.readable()) {
                        readChar = dev.getc();
                        if (readChar == '!') {
                            break;
                        }
                    }
                }
            }
            if (readChar == '!'){
                readTag = true;
                // do something with the values here?
                //if (tag != 4 && tag != -1)
                    //pc.printf("Char: %c, Tag: %d, Value: %d\n",readChar,tag,value);
                if (tag == 1){
                    led2 = 1;
                    // Drive car forward
                    if (value == 1){
                        //pc.printf("Accelerating!");
                        en = 1.0;
                        in1 = 1;
                        in2 = 0;
                    } else {
                        //pc.printf("not Accelerating!");
                        en = 0.0;
                        in1 = 1;
                        in2 = 0;
                    }
                } else if (tag == 0) {
                    if (value == 1) {
                        //pc.printf("De Accelerating!");
                        en = 1.0;
                        in1 = 0;
                        in2 = 1;
                    } else {
                        //pc.printf("not De Accelerating!");
                        en = 0.0;
                        in1 = 0;
                        in2 = 1;
                    }
                } else if (tag == 4 && value != 0) {                        
                    pwm_out = (float) value / 180.0;
                    if (pwm_out > 1.0)
                        pwm_out = 1.0;
                    servo_pin = pwm_out;
                    //pc.printf("Value: %d, Value Float: %f\n", value, pwm_out);
                   
                } else if (tag == 6) {
                    if (value == 1) {
                        speaker.period(1.0/100.0);
                        speaker = 0.5;
                    } else {
                        speaker = 0.0;
                    }
                } else {
                    led2 = 0;
                }
                if (value > 150){
                    led3 = 1;
                }
                else{
                    led3 = 0;
                }
                // reset values
                tag = -1;
                value = 0;
            }
            else if (readChar == ','){
                // done reading tag
                readTag = false;
            }
            else if (readChar != '!' && readChar != ','){
                if (readTag){
                  // update the tag  
                  // probably only expect single digits
                  tag = readChar - '0';
                }
                else{
                    // update the value
                    // this could have multiple digits
                    //if (tag != 4)
                        //pc.printf("BEFORE: Tag: %d, Char: %c, Value: %d\n", tag, readChar, value);
                    value *= 10;
                    value += readChar-'0';
                    //if (tag != 4) {
                        //pc.printf("AFTER: Tag: %d, Char: %c, Value: %d\n", tag, readChar, value);
                    //}
                }
            }
           
        }    

    }
}    `

```


