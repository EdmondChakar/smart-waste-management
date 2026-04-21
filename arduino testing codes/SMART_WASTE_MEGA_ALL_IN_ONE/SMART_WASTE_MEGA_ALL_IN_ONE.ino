#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <TinyGPSPlus.h>
#include <HX711_ADC.h>
#include <qrcode.h>

#if defined(ESP8266) || defined(ESP32) || defined(AVR)
#include <EEPROM.h>
#endif

// Arduino Mega 2560 pin map
const int ULTRASONIC_TRIG_PIN = 10;
const int ULTRASONIC_ECHO_PIN = 11;
const int HX711_DOUT_PIN = 48;
const int HX711_SCK_PIN = 46;
const int IR_SENSOR_PIN = 49;  // moved from 48 to avoid conflict with HX711
const int BUZZER_PIN = 50;

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_ADDR 0x3C
#define QR_VERSION 2

const unsigned long qrDelay = 3000;
const unsigned long qrDisplayTime = 10000;

const byte ULTRASONIC_SAMPLES_PER_BATCH = 5;
const unsigned long ULTRASONIC_SAMPLE_DELAY = 80;
const unsigned long ULTRASONIC_BATCH_DELAY = 500;

const unsigned long GPS_PRINT_INTERVAL = 2000;
const unsigned long GPS_NO_DATA_WARNING_DELAY = 5000;
const unsigned long GPS_NO_DATA_WARNING_INTERVAL = 1000;

const unsigned long IR_POLL_INTERVAL = 50;
const unsigned long NODEMCU_SERIAL_SEND_INTERVAL = 5000;
const unsigned long NODEMCU_SERIAL_BAUD = 115200;

TinyGPSPlus gps;
HX711_ADC LoadCell(HX711_DOUT_PIN, HX711_SCK_PIN);
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);

const int calVal_eepromAdress = 0;

unsigned long loadCellPrintTimestamp = 0;
unsigned long gpsStartTime = 0;
unsigned long gpsLastPrint = 0;
unsigned long gpsLastNoDataWarning = 0;
unsigned long lastIRPoll = 0;
unsigned long lastNodeMcuSerialSend = 0;

float ultrasonicSum = 0.0;
byte ultrasonicValidCount = 0;
byte ultrasonicAttemptCount = 0;
bool ultrasonicBatchActive = false;
unsigned long ultrasonicLastSampleTime = 0;
unsigned long ultrasonicLastBatchDoneTime = 0;
float latestUltrasonicAverageCm = -1.0;
float latestLoadCellValue = 0.0;
bool latestLoadCellValueValid = false;

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

void feedGPSData();
void printGPSStatus();
void checkGPSNoDataWarning();
float readDistanceCm();
void handleUltrasonic();
void handleLoadCell();
void handleIRQrBuzzer();
void showCountScreen(const char *statusText);
void showQRCodeScreen(const char *text);
void calibrate();
void changeSavedCalFactor();
void sendNodeMcuSerialTestLine();

void setup() {
  Serial.begin(115200);
  Serial1.begin(9600);  // GPS on Mega pins 19 (RX1) and 18 (TX1)
  Serial2.begin(NODEMCU_SERIAL_BAUD);  // NodeMCU on Mega pins 17 (TX2) and 16 (RX2)

  pinMode(ULTRASONIC_TRIG_PIN, OUTPUT);
  pinMode(ULTRASONIC_ECHO_PIN, INPUT);

  pinMode(IR_SENSOR_PIN, INPUT_PULLUP);
  pinMode(BUZZER_PIN, OUTPUT);
  digitalWrite(BUZZER_PIN, LOW);

  Wire.begin();

  if (!display.begin(SSD1306_SWITCHCAPVCC, OLED_ADDR)) {
    Serial.println("OLED not found");
    while (true) {
    }
  }

  display.clearDisplay();
  display.display();

  Serial.println("GPS starting...");
  Serial.println("Waiting for data...");
  Serial.println();

  Serial.println("Starting...");

  LoadCell.begin();
  unsigned long stabilizingtime = 2000;
  boolean _tare = true;
  LoadCell.start(stabilizingtime, _tare);

  if (LoadCell.getTareTimeoutFlag() || LoadCell.getSignalTimeoutFlag()) {
    Serial.println("Timeout, check MCU>HX711 wiring and pin designations");
    while (true) {
    }
  } else {
    LoadCell.setCalFactor(1.0);
    Serial.println("Startup is complete");
  }

  while (!LoadCell.update()) {
    feedGPSData();
  }

  calibrate();

  clearStartTime = millis();
  ultrasonicLastBatchDoneTime = millis() - ULTRASONIC_BATCH_DELAY;
  gpsStartTime = millis();

  Serial.println("System started");
  showCountScreen("CLEAR");
}

void loop() {
  feedGPSData();
  handleLoadCell();
  handleIRQrBuzzer();
  handleUltrasonic();
  printGPSStatus();
  checkGPSNoDataWarning();
  sendNodeMcuSerialTestLine();
}

void feedGPSData() {
  while (Serial1.available() > 0) {
    gps.encode(Serial1.read());
  }
}

void printGPSStatus() {
  if (millis() - gpsLastPrint < GPS_PRINT_INTERVAL) {
    return;
  }

  gpsLastPrint = millis();

  Serial.println("========== GPS STATUS ==========");

  if (gps.location.isValid()) {
    Serial.println("Status: GPS FIXED");
  } else {
    Serial.println("Status: NO FIX");
  }

  Serial.print("Latitude: ");
  if (gps.location.isValid()) {
    Serial.println(gps.location.lat(), 6);
  } else {
    Serial.println("N/A");
  }

  Serial.print("Longitude: ");
  if (gps.location.isValid()) {
    Serial.println(gps.location.lng(), 6);
  } else {
    Serial.println("N/A");
  }

  Serial.print("Altitude (m): ");
  if (gps.altitude.isValid()) {
    Serial.println(gps.altitude.meters());
  } else {
    Serial.println("N/A");
  }

  Serial.print("Satellites: ");
  if (gps.satellites.isValid()) {
    Serial.println(gps.satellites.value());
  } else {
    Serial.println("N/A");
  }

  Serial.print("HDOP: ");
  if (gps.hdop.isValid()) {
    Serial.println(gps.hdop.hdop());
  } else {
    Serial.println("N/A");
  }

  Serial.print("Date: ");
  if (gps.date.isValid()) {
    Serial.print(gps.date.day());
    Serial.print("/");
    Serial.print(gps.date.month());
    Serial.print("/");
    Serial.println(gps.date.year());
  } else {
    Serial.println("N/A");
  }

  Serial.print("Time (UTC): ");
  if (gps.time.isValid()) {
    if (gps.time.hour() < 10) {
      Serial.print("0");
    }
    Serial.print(gps.time.hour());
    Serial.print(":");
    if (gps.time.minute() < 10) {
      Serial.print("0");
    }
    Serial.print(gps.time.minute());
    Serial.print(":");
    if (gps.time.second() < 10) {
      Serial.print("0");
    }
    Serial.println(gps.time.second());
  } else {
    Serial.println("N/A");
  }

  Serial.println("================================");
  Serial.println();
}

void checkGPSNoDataWarning() {
  if (millis() - gpsStartTime <= GPS_NO_DATA_WARNING_DELAY) {
    return;
  }

  if (gps.charsProcessed() >= 10) {
    return;
  }

  if (millis() - gpsLastNoDataWarning >= GPS_NO_DATA_WARNING_INTERVAL) {
    gpsLastNoDataWarning = millis();
    Serial.println("No GPS data received. Check wiring and baud rate.");
  }
}

float readDistanceCm() {
  unsigned long duration;

  digitalWrite(ULTRASONIC_TRIG_PIN, LOW);
  delayMicroseconds(5);

  digitalWrite(ULTRASONIC_TRIG_PIN, HIGH);
  delayMicroseconds(20);
  digitalWrite(ULTRASONIC_TRIG_PIN, LOW);

  duration = pulseIn(ULTRASONIC_ECHO_PIN, HIGH, 40000);

  if (duration == 0) {
    return -1;
  }

  return duration * 0.0343 / 2.0;
}

void handleUltrasonic() {
  if (!ultrasonicBatchActive) {
    if (millis() - ultrasonicLastBatchDoneTime >= ULTRASONIC_BATCH_DELAY) {
      ultrasonicBatchActive = true;
      ultrasonicSum = 0.0;
      ultrasonicValidCount = 0;
      ultrasonicAttemptCount = 0;
      ultrasonicLastSampleTime = 0;
    }
    return;
  }

  if (ultrasonicAttemptCount > 0 && millis() - ultrasonicLastSampleTime < ULTRASONIC_SAMPLE_DELAY) {
    return;
  }

  float d = readDistanceCm();

  if (d > 0) {
    ultrasonicSum += d;
    ultrasonicValidCount++;
  }

  ultrasonicAttemptCount++;
  ultrasonicLastSampleTime = millis();

  if (ultrasonicAttemptCount < ULTRASONIC_SAMPLES_PER_BATCH) {
    return;
  }

  if (ultrasonicValidCount == 0) {
    latestUltrasonicAverageCm = -1.0;
    Serial.println("No valid echo");
  } else {
    float avg = ultrasonicSum / ultrasonicValidCount;
    latestUltrasonicAverageCm = avg;
    Serial.print("Average distance: ");
    Serial.print(avg);
    Serial.println(" cm");
  }

  ultrasonicBatchActive = false;
  ultrasonicLastBatchDoneTime = millis();
}

void handleLoadCell() {
  static boolean newDataReady = false;
  const int serialPrintInterval = 0;

  if (LoadCell.update()) {
    newDataReady = true;
  }

  if (newDataReady) {
    if (millis() > loadCellPrintTimestamp + serialPrintInterval) {
      float i = LoadCell.getData();
      latestLoadCellValue = i;
      latestLoadCellValueValid = true;
      Serial.print("Load_cell output val: ");
      Serial.println(i);
      newDataReady = false;
      loadCellPrintTimestamp = millis();
    }
  }

  if (Serial.available() > 0) {
    char inByte = Serial.read();
    if (inByte == 't') {
      LoadCell.tareNoDelay();
    } else if (inByte == 'r') {
      calibrate();
    } else if (inByte == 'c') {
      changeSavedCalFactor();
    }
  }

  if (LoadCell.getTareStatus() == true) {
    Serial.println("Tare complete");
  }
}

void handleIRQrBuzzer() {
  if (millis() - lastIRPoll < IR_POLL_INTERVAL) {
    return;
  }

  lastIRPoll = millis();

  int currentSensorState = digitalRead(IR_SENSOR_PIN);

  if (currentMode == COUNT_MODE) {
    if (currentSensorState == LOW) {
      digitalWrite(BUZZER_PIN, HIGH);
      clearStartTime = 0;

      if (lastSensorState == HIGH) {
        count++;
        Serial.print("Obstacle detected. Counter = ");
        Serial.println(count);
        showCountScreen("DETECTED");
      }
    } else {
      digitalWrite(BUZZER_PIN, LOW);

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

      digitalWrite(BUZZER_PIN, LOW);
      Serial.print("Showing QR with value: ");
      Serial.println(qrValue);

      showQRCodeScreen(qrValue.c_str());
    }

    lastSensorState = currentSensorState;
  } else if (currentMode == QR_MODE) {
    digitalWrite(BUZZER_PIN, LOW);

    if (millis() - qrStartTime >= qrDisplayTime) {
      count = 0;
      qrValue = "";
      currentMode = COUNT_MODE;

      currentSensorState = digitalRead(IR_SENSOR_PIN);
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
}

void showCountScreen(const char *statusText) {
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

void showQRCodeScreen(const char *text) {
  QRCode qrcode;
  uint8_t qrcodeData[qrcode_getBufferSize(QR_VERSION)];
  qrcode_initText(&qrcode, qrcodeData, QR_VERSION, 0, text);

  display.clearDisplay();
  display.setTextColor(SSD1306_WHITE);
  display.setTextWrap(false);
  display.setTextSize(1);

  const char *title = "Scan to claim points";

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
  if (scale < 1) {
    scale = 1;
  }

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

void calibrate() {
  Serial.println("***");
  Serial.println("Start calibration:");
  Serial.println("Place the load cell an a level stable surface.");
  Serial.println("Remove any load applied to the load cell.");
  Serial.println("Send 't' from serial monitor to set the tare offset.");

  boolean _resume = false;
  while (_resume == false) {
    LoadCell.update();
    feedGPSData();

    if (Serial.available() > 0) {
      char inByte = Serial.read();
      if (inByte == 't') {
        LoadCell.tareNoDelay();
      }
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
    feedGPSData();

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
    feedGPSData();

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

  Serial.println("End calibration");
  Serial.println("***");
  Serial.println("To re-calibrate, send 'r' from serial monitor.");
  Serial.println("For manual edit of the calibration value, send 'c' from serial monitor.");
  Serial.println("***");
}

void changeSavedCalFactor() {
  float oldCalibrationValue = LoadCell.getCalFactor();
  boolean _resume = false;

  Serial.println("***");
  Serial.print("Current value is: ");
  Serial.println(oldCalibrationValue);
  Serial.println("Now, send the new value from serial monitor, i.e. 696.0");

  float newCalibrationValue;
  while (_resume == false) {
    feedGPSData();

    if (Serial.available() > 0) {
      newCalibrationValue = Serial.parseFloat();
      if (newCalibrationValue != 0) {
        Serial.print("New calibration value is: ");
        Serial.println(newCalibrationValue);
        LoadCell.setCalFactor(newCalibrationValue);
        _resume = true;
      }
    }
  }

  _resume = false;
  Serial.print("Save this value to EEPROM adress ");
  Serial.print(calVal_eepromAdress);
  Serial.println("? y/n");

  while (_resume == false) {
    feedGPSData();

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

void sendNodeMcuSerialTestLine() {
  if (millis() - lastNodeMcuSerialSend < NODEMCU_SERIAL_SEND_INTERVAL) {
    return;
  }

  lastNodeMcuSerialSend = millis();

  // Temporary serial-test contract:
  // SENSOR,fill_pct,weight_kg,gps_lat,gps_lon,item_count
  //
  // fill_pct is currently sent as NA on purpose because the Mega sketch does
  // not yet have a validated distance-to-fill calibration formula.
  Serial2.print("SENSOR,");
  Serial2.print("NA,");

  if (latestLoadCellValueValid) {
    Serial2.print(latestLoadCellValue, 2);
  } else {
    Serial2.print("NA");
  }
  Serial2.print(",");

  if (gps.location.isValid()) {
    Serial2.print(gps.location.lat(), 6);
  } else {
    Serial2.print("NA");
  }
  Serial2.print(",");

  if (gps.location.isValid()) {
    Serial2.print(gps.location.lng(), 6);
  } else {
    Serial2.print("NA");
  }
  Serial2.print(",");
  Serial2.println(count);

  Serial.print("Sent to NodeMCU: SENSOR,NA,");
  if (latestLoadCellValueValid) {
    Serial.print(latestLoadCellValue, 2);
  } else {
    Serial.print("NA");
  }
  Serial.print(",");
  if (gps.location.isValid()) {
    Serial.print(gps.location.lat(), 6);
  } else {
    Serial.print("NA");
  }
  Serial.print(",");
  if (gps.location.isValid()) {
    Serial.print(gps.location.lng(), 6);
  } else {
    Serial.print("NA");
  }
  Serial.print(",");
  Serial.println(count);
}
