#include "mbed.h"
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
        neopixel::Pixel pixels[numPixels];
        // Use buffer method of updating pixels.
       // pixels[0].red = value;
//        pixels[0].green = 0;
//        pixels[0].blue = 0;
//        pixels[1].red = value;
//        pixels[1].green = value;
//        pixels[1].blue = 0;
//        pixels[2].red = 0;
//        pixels[2].green = value;
//        pixels[2].blue = 0;
//        pixels[3].red = 0;
//        pixels[3].green = 0;
       // pixels[3].blue = value;
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
        //sleep();
        
        // Update motor
        /*
        if (accelerate == 1) {
            //pc.printf("Accelerating!");
            en = 1.0;
            in1 = 1;
            in2 = 0;
        } else if (decelerate == 1) {
            //pc.printf("Decelerating!");
            en = 1.0;
            in1 = 0;
            in2 = 1;
        } else if (accelerate == 0) {
            //pc.printf("Not accelerating!");
            en = 0.0;
            in1 = 1;
            in2 = 0;
        } else if (decelerate == 0) {
            //c.printf("Not decelerate!");
            en = 0.0;
            in1 = 0;
            in2 = 1;
        }
        */
        
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
        //if (bluemod.readable()){
//            char tag = bluemod.getc();
//            char value = bluemod.getc();
//            led2 = tag;
//            led3 = value;
//            //if (tag == '0'){
////                if (value == '1'){
////                    led2 = 1;
////                }
////                else if (value == '0'){
////                    led2 = 0;
////                }
////            }
////            else if (tag == '1'){
////                if (value == '1'){
////                    led3 = 1;
////                }
////                else if (value == '0'){
////                    led3 = 0;
////                }
////            }
//            
//            //if (bluemod.getc()=='!') {
////                if (bluemod.getc()=='C') { //color data packet
////                    bred = bluemod.getc(); // RGB color values
////                    bgreen = bluemod.getc();
////                    bblue = bluemod.getc();
////                    if (bluemod.getc()==char(~('!' + 'C' + bred + bgreen + bblue))) { //checksum OK?
////                        //red = bred/255.0; //send new color to RGB LED PWM outputs
////                        //green = bgreen/255.0;
////                        //blue = bblue/255.0;
////                        color_mutex.lock();
////                        r=bred/255.0;
////                        g=bgreen/255.0;
////                        b=bblue/255.0;
////                        myRGBled.write(r,g,b);
////                        color_mutex.unlock();
////                        wait(0.005);
////                        
////                    }
////                }
//                
//            }
//        }
    }
}    

