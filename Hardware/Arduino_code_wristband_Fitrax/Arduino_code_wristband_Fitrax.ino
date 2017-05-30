/*
* international project, autumn 2017
* bluetooth low energy heart rate meter
 */
//INCLUDES--------------------------------------------------

//nrf
#include <nrf.h>
#include <BLEPeripheral.h>
//#include <BLEBondStore.h>

//filter
#include <Filters.h>


//DEFINES----------------------------------------------------------------
#define PULSE_PIN       A1
#define ADVER_NAME      "FitRax"

#define LED_R_PIN       6
#define LED_G_PIN       7
#define LED_B_PIN       4

#define BUTTON_PIN      2

//led bitmasks
#define LED_OFF         0
#define LED_RED         1
#define LED_GREEN       2
#define LED_BLUE        4

#define LED_YELLOW      LED_RED | LED_GREEN
#define LED_CYAN        LED_GREEN | LED_BLUE
#define LED_PURPLE      LED_BLUE | LED_RED
#define LED_WHITE       LED_RED | LED_GREEN | LED_BLUE

#define VBAT_MAX_IN_MV  3500

#define UPDATE_RATE     1000  //ms

//make these defines if possible?
float f_lowpass = 2.5f;     //2.5hz
float f_highpass = 1.0f;    //1.0hz

#define NOISE_AVG_LEN   200   
#define THRESHOLD_MUL   14.0f
#define PULSE_THRESHOLD 3.0f     // 
#define COOLDOWN_LEN    70.0f    //precentage
#define COOLDOWN_MAX    1000     //ms

//GLOBALS------------------------------------------------------------
BLEPeripheral blePeripheral;
BLEService heartRateService("180D");
BLEService batteryService("180F");
BLECharacteristic heartRateChar("2A37", BLERead | BLENotify, 2);
BLECharacteristic batteryChar("2A19", BLERead | BLENotify, 1);

FilterOnePole lowpassFilter(LOWPASS, f_lowpass);
FilterOnePole highpassFilter(HIGHPASS, f_highpass);

//last ble update
unsigned long lastUpdate = 0;

//pulsesensor related
float noise_avg[NOISE_AVG_LEN];
int noise_i = 0;

unsigned long t_lastbeat = 0;
int pulse_cooldown = 100;
unsigned char b_wait = 0;

float bpm_avg[51] /*= { 0.0f }*/;
byte bpm_i = 0;

float bpm;

void setup() {
  //rgb led setup, will turn on ports
  pinMode(LED_R_PIN, OUTPUT);
  pinMode(LED_G_PIN, OUTPUT);
  pinMode(LED_B_PIN, OUTPUT);

  //turn led off for start
  rgb_set(LED_OFF);

  pinMode(BUTTON_PIN, INPUT_PULLUP);
  
  //initalize serial
  Serial.begin(9600);

  //memset(bpm_avg, 0.0f, 50);

  //setup ble
  blePeripheral.setLocalName(ADVER_NAME);
  blePeripheral.setAdvertisedServiceUuid(heartRateService.uuid());
  blePeripheral.addAttribute(heartRateService); 
  blePeripheral.addAttribute(heartRateChar);
  blePeripheral.addAttribute(batteryService);
  blePeripheral.addAttribute(batteryChar);

  blePeripheral.setTxPower(4);

  blePeripheral.setAdvertisingInterval(100);
  blePeripheral.setConnectionInterval(0x0006, 0x0c80);
  blePeripheral.begin();

  Serial.println("Setup done");
}

//call this at 100hz. does adc sampling, filtering and pulse detection from pulse sensor
void update_hr() {

  //only for serial visualizations
  static float v_cooldown;
  static float v_trigger;

  //reset trigger visualization every sample
  v_trigger = 0.0f;

  //get sample from adc
  float raw_sample = analogRead(PULSE_PIN);
  
  //bandpass filter sample
  lowpassFilter.input(raw_sample);
  highpassFilter.input(lowpassFilter.output());
  float sample = highpassFilter.output();

  //running average to determine noise floor
  noise_avg[noise_i] = sample;
  noise_i++;
  
  if(noise_i >= NOISE_AVG_LEN) noise_i = 0; 

  float noise_floor = 0.0f;
  for(int i = 0; i < NOISE_AVG_LEN; i++) {
    noise_floor += noise_avg[i];
  }

  //average
  noise_floor = noise_floor / (float)NOISE_AVG_LEN;
  
  //raise noise floor with multiplier 
  noise_floor = abs(noise_floor) * THRESHOLD_MUL;

  //ignore samples below average noise floor
  if(noise_floor > sample) sample = 0.0f;

  //delta time between last pulse
  unsigned long t_delta = millis() - t_lastbeat;

  //reset b_wait when coming back to zero level
  if(sample == 0.0f && b_wait == 1) b_wait = 0;

  //reset cooldown visualization
  if(t_delta > pulse_cooldown) v_cooldown = 0.0f;
  
  //pulse detection
  if((sample - noise_floor) > PULSE_THRESHOLD && t_delta > pulse_cooldown && b_wait == 0) {

    //visualize
    v_trigger = sample;
    v_cooldown = sample;
    
    //wait for signal to come back down to zero
    b_wait = 1;

    //calculate bpm
    //bpm = (1.0f / (t_delta / 1000.0f)) * 60.0f;
    float tmp_bpm = (1.0f / (t_delta / 1000.0f)) * 60.0f;

   // if(tmp_bpm > (bpm * 1.20f) && bpm > 45.0f) {
   //   bpm_avg[bpm_i] = bpm;
   // } else {
      bpm_avg[bpm_i] = tmp_bpm;
 // }
      
    bpm_i++;

    if(bpm_i >= 50) bpm_i = 0; 

    float tmp;
    for(int i = 0; i < 50; i++) {
      tmp += bpm_avg[i];
    }

    bpm = tmp / 50.0f;
    
    //calculate cooldown
    pulse_cooldown = t_delta * (COOLDOWN_LEN / 100.0f);

    //clamp cooldown to maximum value
    if(pulse_cooldown > COOLDOWN_MAX) pulse_cooldown = COOLDOWN_MAX;

    //update last beat time
    t_lastbeat = millis();
  }

  Serial.print(sample);
  Serial.print(",");
  Serial.print(noise_floor);
  Serial.print(",");
  Serial.print(v_trigger);
  Serial.print(",");
  Serial.println(v_cooldown);
  
}

//copypasted from nrf51288 forums, will return battery precentage VBAT_MAX_IN_MV must be correct
uint8_t battery_level_get(void) {
    // Configure ADC
    NRF_ADC->CONFIG     = (ADC_CONFIG_RES_8bit                        << ADC_CONFIG_RES_Pos)     |
                          (ADC_CONFIG_INPSEL_SupplyOneThirdPrescaling << ADC_CONFIG_INPSEL_Pos)  |
                          (ADC_CONFIG_REFSEL_VBG                      << ADC_CONFIG_REFSEL_Pos)  |
                          (ADC_CONFIG_PSEL_Disabled                   << ADC_CONFIG_PSEL_Pos)    |
                          (ADC_CONFIG_EXTREFSEL_None                  << ADC_CONFIG_EXTREFSEL_Pos);
    NRF_ADC->EVENTS_END = 0;
    NRF_ADC->ENABLE     = ADC_ENABLE_ENABLE_Enabled;

    NRF_ADC->EVENTS_END  = 0;    // Stop any running conversions.
    NRF_ADC->TASKS_START = 1;
    
    while (!NRF_ADC->EVENTS_END)
    {
    }
    
    uint16_t vbg_in_mv = 1200;
    uint8_t adc_max = 255;
    uint16_t vbat_current_in_mv = (NRF_ADC->RESULT * 3 * vbg_in_mv) / adc_max;
    
    NRF_ADC->EVENTS_END     = 0;
    NRF_ADC->TASKS_STOP     = 1;
    
    return (uint8_t) ((vbat_current_in_mv * 100) / VBAT_MAX_IN_MV);
}

void rgb_set(unsigned char flags) {
  digitalWrite(LED_R_PIN, HIGH);
  digitalWrite(LED_G_PIN, HIGH);
  digitalWrite(LED_B_PIN, HIGH);
  
  if((flags & LED_RED) == LED_RED) {
    digitalWrite(LED_R_PIN, LOW);
  }
  if((flags & LED_GREEN) == LED_GREEN) {
    digitalWrite(LED_G_PIN, LOW);
  }
  if((flags & LED_BLUE) == LED_BLUE) {
    digitalWrite(LED_B_PIN, LOW);
  }
}

//callback for central connection
void ble_connected() {
  /*
  rgb_set(LED_GREEN);
  Serial.println("Connected");
  delay(300);
  rgb_set(LED_OFF);
  */
}

//called while connected to central
void ble_loop() {


  //update hr at 100hz
  update_hr();
  delay(10);
  
  if(millis() - lastUpdate >= UPDATE_RATE) {
    rgb_set(LED_BLUE);
    lastUpdate = millis();

    unsigned char out = (unsigned char)bpm;
    if(out == 0) out = 1;

    out = 123;
    
    const unsigned char heartRateCharArray[2] = { 0, (unsigned char)out };
    heartRateChar.setValue(heartRateCharArray, 2);
    uint8_t battery = battery_level_get();
    batteryChar.setValue(&battery, 1);
/*
    Serial.print("Update: HR: ");
    Serial.print(123);
    Serial.print(" Battery: ");
    Serial.println(battery);
    */
  }

  rgb_set(LED_OFF);
  
}

//callback for central disconnect
void ble_disconnected() {
  Serial.println("Disconnected");
}

//called while not connected to central
void idle_loop() {
  update_hr();
  delay(10);
  
//test button
/*
if(!digitalRead(BUTTON_PIN)) {
    rgb_set(LED_CYAN);
    delay(100);
  } else {
    rgb_set(LED_OFF);
  }
  */
}

//main loop, do everything in callbacks not here
void loop() {
 // blePeripheral.poll();

 // ble_loop();
  
  BLECentral central = blePeripheral.central();


  if(central) {
    ble_connected();

    while(central.connected()) {
      ble_loop();
    }

    ble_disconnected();
  }
  
  idle_loop();
  
}
