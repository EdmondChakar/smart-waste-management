#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <qrcode.h>
#include <HX711_ADC.h>
#include <TinyGPSPlus.h>

#if defined(ESP8266) || defined(ESP32) || defined(AVR)
#include <EEPROM.h>
#endif

// -----------------------------------------------------------------------------
// IR SENSOR + BUZZER + OLED + QR  (same logic as your original sketch)
// -----------------------------------------------------------------------------
#define SENSOR 48
#define ACTION 50

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_ADDR 0x3C
#define QR_VERSION 2

const unsigned long qrDelay = 3000;
const unsigned long qrDisplayTime = 10000;

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);

int count = 0;
int lastSensorState = HIGH;

unsigned long clearStartTime = 0;
unsigned long qrStartTime = 0;

String qrValue = "";

enum Mode {
  COUNT_MODE,
  QR_MODE
};

Mode currentMode = COUNT_MODE;

// -----------------------------------------------------------------------------
// LOAD CELL + HX711
// forced pin change only: DOUT 48 -> 47 because IR already uses 48
// -----------------------------------------------------------------------------
const int HX711_dout = 47;
const int HX711_sck  = 46;

HX711_ADC LoadCell(HX711_dout, HX711_sck);

const int calVal_eepromAdress = 0;
unsigned long t = 0;
float calibrationFactor = 1.0;

// -----------------------------------------------------------------------------
// ULTRASONIC  (same distance formula, same 5-sample averaging idea)
// -----------------------------------------------------------------------------
const int trigPin = 10;
const int echoPin = 11;

bool ultrasonicCycleActive = false;
unsigned long ultrasonicLastStep = 0;
unsigned long ultrasonicLastCycleEnd = 0;
float ultrasonicSum = 0;
int ultrasonicValidCount = 0;
int ultrasonicSampleIndex = 0;

// -----------------------------------------------------------------------------
// GPS
// GPS TX -> Mega 19 (RX1)
// GPS RX -> Mega 18 (TX1)
// -----------------------------------------------------------------------------
TinyGPSPlus gps;

// -----------------------------------------------------------------------------
// OLED FUNCTIONS
// -----------------------------------------------------------------------------
void showCountScreen(const char* statusText) {
  display.clearDisplay();
  display.setTextColor(SSD1306_WHITE);
  display.setTextWrap(false);

  display.setTextSize(1);
  display.setCursor(0, 0);
  display.println("IR SENSOR COUNTER");

  display.setCursor(0, 16);
  display.print("Status: ");
  display.println(statusText);

  display.setCursor(0, 30);
  display.println("Count:");

  display.setTextSize(3);
  display.setCursor(0, 42);
  display.println(count);

  display.display();
}

void showQRCodeScreen(const char* text) {
  QRCode qrcode;
  uint8_t qrcodeData[qrcode_getBufferSize(QR_VERSION)];
  qrcode_initText(&qrcode, qrcodeData, QR_VERSION, 0, text);

  display.clearDisplay();
  display.setTextColor(SSD1306_WHITE);
  display.setTextWrap(false);
  display.setTextSize(1);

  const char* title = "Scan to claim points";

  int16_t x1, y1;
  uint16_t w, h;
  display.getTextBounds(title, 0, 0, &x1, &y1, &w, &h);

  int titleX = (SCREEN_WIDTH - w) / 2;
  display.setCursor(titleX, 0);
  display.print(title);

  const int qrOffsetDown = 0;
  const int gapBelowTitle = 0;
  const int bottomMargin = 0;

  int topMargin = h + gapBelowTitle + qrOffsetDown;
  int availableWidth = SCREEN_WIDTH;
  int availableHeight = SCREEN_HEIGHT - topMargin - bottomMargin;

  int scale = min(availableWidth / qrcode.size, availableHeight / qrcode.size);
  if (scale < 1) scale = 1;

  int qrWidth = qrcode.size * scale;
  int qrHeight = qrcode.size * scale;

  int shiftX = (SCREEN_WIDTH - qrWidth) / 2;
  int shiftY = topMargin + (availableHeight - qrHeight) / 2;

  for (uint8_t y = 0; y < qrcode.size; y++) {
    for (uint8_t x = 0; x < qrcode.size; x++) {
      if (qrcode_getModule(&qrcode, x, y)) {
        display.fillRect(
          shiftX + x * scale,
          shiftY + y * scale,
          scale,
          scale,
          SSD1306_WHITE
        );
      }
    }
  }

  display.display();
}

// -----------------------------------------------------------------------------
// ULTRASONIC
// -----------------------------------------------------------------------------
float readDistanceCm() {
  unsigned long duration;

  digitalWrite(trigPin, LOW);
  delayMicroseconds(5);

  digitalWrite(trigPin, HIGH);
  delayMicroseconds(20);
  digitalWrite(trigPin, LOW);

  duration = pulseIn(echoPin, HIGH, 40000);

  if (duration == 0) {
    return -1;
  }

  return duration * 0.0343 / 2.0;
}

void updateUltrasonicTask() {
  // same logic idea as your original:
  // average 5 readings, then print result
  // made non-blocking so the whole system stays responsive

  if (!ultrasonicCycleActive) {
    if (ultrasonicLastCycleEnd == 0 || millis() - ultrasonicLastCycleEnd >= 200) {
      ultrasonicCycleActive = true;
      ultrasonicSum = 0;
      ultrasonicValidCount = 0;
      ultrasonicSampleIndex = 0;
      ultrasonicLastStep = 0;
    } else {
      return;
    }
  }

  if (ultrasonicLastStep == 0 || millis() - ultrasonicLastStep >= 30) {
    float d = readDistanceCm();

    if (d > 0) {
      ultrasonicSum += d;
      ultrasonicValidCount++;
    }

    ultrasonicSampleIndex++;
    ultrasonicLastStep = millis();

    if (ultrasonicSampleIndex >= 5) {
      if (ultrasonicValidCount == 0) {
        Serial.println("Ultrasonic: No valid echo");
      } else {
        float avg = ultrasonicSum / ultrasonicValidCount;
        Serial.print("Ultrasonic Average distance: ");
        Serial.print(avg);
        Serial.println(" cm");
      }

      ultrasonicCycleActive = false;
      ultrasonicLastCycleEnd = millis();
    }
  }
}

// -----------------------------------------------------------------------------
// GPS
// -----------------------------------------------------------------------------
void updateGPS() {
  while (Serial1.available() > 0) {
    gps.encode(Serial1.read());
  }

  static unsigned long lastGpsPrint = 0;

  if (millis() - lastGpsPrint >= 800) {
    lastGpsPrint = millis();

    if (gps.location.isValid()) {
      Serial.print("GPS Latitude: ");
      Serial.println(gps.location.lat(), 6);

      Serial.print("GPS Longitude: ");
      Serial.println(gps.location.lng(), 6);

      Serial.print("GPS Satellites: ");
      if (gps.satellites.isValid()) {
        Serial.println(gps.satellites.value());
      } else {
        Serial.println("N/A");
      }
    } else {
      Serial.println("GPS: No valid fix yet");
    }

    Serial.println("--------------------");
  }
}

// -----------------------------------------------------------------------------
// LOAD CELL  (same calibration flow as your original sketch)
// -----------------------------------------------------------------------------
void calibrate() {
  Serial.println("***");
  Serial.println("Start calibration:");
  Serial.println("Place the load cell on a level stable surface.");
  Serial.println("Remove any load applied to the load cell.");
  Serial.println("Send 't' from serial monitor to set the tare offset.");

  bool _resume = false;
  while (_resume == false) {
    LoadCell.update();
    updateGPS();
    updateUltrasonicTask();

    if (Serial.available() > 0) {
      char inByte = Serial.read();
      if (inByte == 't') LoadCell.tareNoDelay();
    }

    if (LoadCell.getTareStatus() == true) {
      Serial.println("Tare complete");
      _resume = true;
    }
  }

  Serial.println("Now, place your known mass on the loadcell.");
  Serial.println("Then send the weight of this mass (i.e. 100.0) from serial monitor.");

  float known_mass = 0;
  _resume = false;
  while (_resume == false) {
    LoadCell.update();
    updateGPS();
    updateUltrasonicTask();

    if (Serial.available() > 0) {
      known_mass = Serial.parseFloat();
      if (known_mass != 0) {
        Serial.print("Known mass is: ");
        Serial.println(known_mass);
        _resume = true;
      }
    }
  }

  LoadCell.refreshDataSet();
  float newCalibrationValue = LoadCell.getNewCalibration(known_mass);

  Serial.print("New calibration value has been set to: ");
  Serial.print(newCalibrationValue);
  Serial.println(", use this as calibration value (calFactor) in your project sketch.");
  Serial.print("Save this value to EEPROM adress ");
  Serial.print(calVal_eepromAdress);
  Serial.println("? y/n");

  _resume = false;
  while (_resume == false) {
    updateGPS();
    updateUltrasonicTask();

    if (Serial.available() > 0) {
      char inByte = Serial.read();

      if (inByte == 'y') {
#if defined(ESP8266) || defined(ESP32)
        EEPROM.begin(512);
#endif
        EEPROM.put(calVal_eepromAdress, newCalibrationValue);
#if defined(ESP8266) || defined(ESP32)
        EEPROM.commit();
#endif
        EEPROM.get(calVal_eepromAdress, newCalibrationValue);

        calibrationFactor = newCalibrationValue;
        LoadCell.setCalFactor(calibrationFactor);

        Serial.print("Value ");
        Serial.print(newCalibrationValue);
        Serial.print(" saved to EEPROM address: ");
        Serial.println(calVal_eepromAdress);
        _resume = true;
      } else if (inByte == 'n') {
        calibrationFactor = newCalibrationValue;
        LoadCell.setCalFactor(calibrationFactor);

        Serial.println("Value not saved to EEPROM");
        _resume = true;
      }
    }
  }

  Serial.println("End calibration");
  Serial.println("***");
  Serial.println("To re-calibrate, send 'r' from serial monitor.");
  Serial.println("For manual edit of the calibration value, send 'c' from serial monitor.");
  Serial.println("***");
}

void changeSavedCalFactor() {
  float oldCalibrationValue = LoadCell.getCalFactor();
  bool _resume = false;

  Serial.println("***");
  Serial.print("Current value is: ");
  Serial.println(oldCalibrationValue);
  Serial.println("Now, send the new value from serial monitor, i.e. 696.0");

  float newCalibrationValue;
  while (_resume == false) {
    updateGPS();
    updateUltrasonicTask();

    if (Serial.available() > 0) {
      newCalibrationValue = Serial.parseFloat();
      if (newCalibrationValue != 0) {
        Serial.print("New calibration value is: ");
        Serial.println(newCalibrationValue);
        LoadCell.setCalFactor(newCalibrationValue);
        calibrationFactor = newCalibrationValue;
        _resume = true;
      }
    }
  }

  _resume = false;
  Serial.print("Save this value to EEPROM adress ");
  Serial.print(calVal_eepromAdress);
  Serial.println("? y/n");

  while (_resume == false) {
    updateGPS();
    updateUltrasonicTask();

    if (Serial.available() > 0) {
      char inByte = Serial.read();
      if (inByte == 'y') {
#if defined(ESP8266) || defined(ESP32)
        EEPROM.begin(512);
#endif
        EEPROM.put(calVal_eepromAdress, newCalibrationValue);
#if defined(ESP8266) || defined(ESP32)
        EEPROM.commit();
#endif
        EEPROM.get(calVal_eepromAdress, newCalibrationValue);

        Serial.print("Value ");
        Serial.print(newCalibrationValue);
        Serial.print(" saved to EEPROM address: ");
        Serial.println(calVal_eepromAdress);
        _resume = true;
      } else if (inByte == 'n') {
        Serial.println("Value not saved to EEPROM");
        _resume = true;
      }
    }
  }

  Serial.println("End change calibration value");
  Serial.println("***");
}

void updateLoadCellTask() {
  static bool newDataReady = false;
  const int serialPrintInterval = 120;

  if (LoadCell.update()) newDataReady = true;

  if (newDataReady) {
    if (millis() > t + serialPrintInterval) {
      float i = LoadCell.getData();
      Serial.print("Load_cell output val: ");
      Serial.println(i);
      newDataReady = false;
      t = millis();
    }
  }

  if (Serial.available() > 0) {
    char inByte = Serial.read();
    if (inByte == 't') LoadCell.tareNoDelay();
    else if (inByte == 'r') calibrate();
    else if (inByte == 'c') changeSavedCalFactor();
  }

  if (LoadCell.getTareStatus() == true) {
    Serial.println("Tare complete");
  }
}

// -----------------------------------------------------------------------------
// SETUP
// -----------------------------------------------------------------------------
void setup() {
  Serial.begin(57600);
  Serial1.begin(9600);

  pinMode(SENSOR, INPUT_PULLUP);
  pinMode(ACTION, OUTPUT);
  digitalWrite(ACTION, LOW);   // same startup logic as your IR sketch

  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);

  Wire.begin();

  if (!display.begin(SSD1306_SWITCHCAPVCC, OLED_ADDR)) {
    Serial.println("OLED not found");
    while (true);
  }

  display.clearDisplay();
  display.display();

  Serial.println();
  Serial.println("Starting...");

  LoadCell.begin();
  unsigned long stabilizingtime = 1000;
  bool _tare = true;
  LoadCell.start(stabilizingtime, _tare);

  if (LoadCell.getTareTimeoutFlag() || LoadCell.getSignalTimeoutFlag()) {
    Serial.println("Timeout, check MCU>HX711 wiring and pin designations");
    while (1);
  } else {
    LoadCell.setCalFactor(calibrationFactor);
    Serial.println("Startup is complete");
  }

  while (!LoadCell.update()) {
    updateGPS();
    updateUltrasonicTask();
  }

  // same startup behavior idea as your load-cell sketch:
  // calibrate first through Serial Monitor
  calibrate();

  clearStartTime = millis();

  Serial.println("System started");
  showCountScreen("CLEAR");
}

// -----------------------------------------------------------------------------
// LOOP
// -----------------------------------------------------------------------------
void loop() {
  updateGPS();
  updateLoadCellTask();
  updateUltrasonicTask();

  int currentSensorState = digitalRead(SENSOR);

  if (currentMode == COUNT_MODE) {
    if (currentSensorState == LOW) {
      digitalWrite(ACTION, HIGH);   // same as your original IR sketch

      clearStartTime = 0;

      if (lastSensorState == HIGH) {
        count++;
        Serial.print("Obstacle detected. Counter = ");
        Serial.println(count);
        showCountScreen("DETECTED");
      }

    } else {
      digitalWrite(ACTION, LOW);    // same as your original IR sketch

      if (lastSensorState == LOW) {
        clearStartTime = millis();
        Serial.println("=== All clear");
        showCountScreen("CLEAR");
      }

      if (clearStartTime == 0) {
        clearStartTime = millis();
      }
    }

    if (count > 0 && currentSensorState == HIGH && clearStartTime > 0 &&
        millis() - clearStartTime >= qrDelay) {
      currentMode = QR_MODE;
      qrStartTime = millis();

      qrValue = String(count);

      digitalWrite(ACTION, LOW);    // same as your original IR sketch
      Serial.print("Showing QR with value: ");
      Serial.println(qrValue);

      showQRCodeScreen(qrValue.c_str());
    }

    lastSensorState = currentSensorState;
  }

  else if (currentMode == QR_MODE) {
    digitalWrite(ACTION, LOW);      // same as your original IR sketch

    if (millis() - qrStartTime >= qrDisplayTime) {
      count = 0;
      qrValue = "";
      currentMode = COUNT_MODE;

      currentSensorState = digitalRead(SENSOR);
      lastSensorState = currentSensorState;

      if (currentSensorState == HIGH) {
        clearStartTime = millis();
        showCountScreen("CLEAR");
      } else {
        clearStartTime = 0;
        showCountScreen("DETECTED");
      }

      Serial.println("QR finished. Counter reset to 0");
    }
  }

  delay(5);
}