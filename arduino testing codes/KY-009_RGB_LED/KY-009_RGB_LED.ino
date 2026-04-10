#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <qrcode.h>

#define SENSOR 48
#define ACTION 50

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_ADDR 0x3C

#define QR_VERSION 2

const unsigned long qrDelay = 3000;       // 15 seconds clear before QR
const unsigned long qrDisplayTime = 10000; // 30 seconds QR display

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

  // Draw title centered at the very top
  int titleX = (SCREEN_WIDTH - w) / 2;
  display.setCursor(titleX, 0);
  display.print(title);

  // No extra downward shift now
  const int qrOffsetDown = 0;
  const int gapBelowTitle = 0;
  const int bottomMargin = 0;

  int topMargin = h + gapBelowTitle + qrOffsetDown;
  int availableWidth = SCREEN_WIDTH;
  int availableHeight = SCREEN_HEIGHT - topMargin - bottomMargin;

  // Largest whole-number scale that fits
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

void setup() {
  Serial.begin(9600);

  pinMode(SENSOR, INPUT_PULLUP);
  pinMode(ACTION, OUTPUT);
  digitalWrite(ACTION, LOW);

  Wire.begin();

  if (!display.begin(SSD1306_SWITCHCAPVCC, OLED_ADDR)) {
    Serial.println("OLED not found");
    while (true);
  }

  display.clearDisplay();
  display.display();

  clearStartTime = millis();

  Serial.println("System started");
  showCountScreen("CLEAR");
}

void loop() {
  int currentSensorState = digitalRead(SENSOR);

  if (currentMode == COUNT_MODE) {
    if (currentSensorState == LOW) {
      digitalWrite(ACTION, HIGH);

      clearStartTime = 0;

      if (lastSensorState == HIGH) {
        count++;
        Serial.print("Obstacle detected. Counter = ");
        Serial.println(count);
        showCountScreen("DETECTED");
      }

    } else {
      digitalWrite(ACTION, LOW);

      if (lastSensorState == LOW) {
        clearStartTime = millis();
        Serial.println("=== All clear");
        showCountScreen("CLEAR");
      }

      if (clearStartTime == 0) {
        clearStartTime = millis();
      }
    }

    // After 15 seconds clear, show QR with the counted number
    if (count > 0 && currentSensorState == HIGH && clearStartTime > 0 &&
        millis() - clearStartTime >= qrDelay) {
      currentMode = QR_MODE;
      qrStartTime = millis();

      qrValue = String(count);   // QR now contains the counted number

      digitalWrite(ACTION, LOW);
      Serial.print("Showing QR with value: ");
      Serial.println(qrValue);

      showQRCodeScreen(qrValue.c_str());
    }

    lastSensorState = currentSensorState;
  }

  else if (currentMode == QR_MODE) {
    digitalWrite(ACTION, LOW);

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

  delay(50);
}