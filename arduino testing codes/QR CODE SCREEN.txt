#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <qrcode.h>

// OLED display dimensions
#define OLED_WIDTH 128
#define OLED_HEIGHT 64

// OLED display object
Adafruit_SSD1306 display(OLED_WIDTH, OLED_HEIGHT, &Wire, -1);

void setup() {
  // Initialize the OLED display
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
  display.clearDisplay();

  // Display the QR code for "Marios Id"
  generateQRCode("GR8 that U R here:)");
}

void loop() {
  // Nothing to do here
}

void generateQRCode(const char* text) {
  // Create a QR code object
  QRCode qrcode;
  
  // Define the size of the QR code (1-40, higher means bigger size)
  uint8_t qrcodeData[qrcode_getBufferSize(3)];
  qrcode_initText(&qrcode, qrcodeData, 3, 0, text);

  // Clear the display
  display.clearDisplay();

  // Calculate the scale factor
  int scale = min(OLED_WIDTH / qrcode.size, OLED_HEIGHT / qrcode.size);
  
  // Calculate horizontal shift
  int shiftX = (OLED_WIDTH - qrcode.size*scale)/2;
  
  // Calculate horizontal shift
  int shiftY = (OLED_HEIGHT - qrcode.size*scale)/2;

  // Draw the QR code on the display
  for (uint8_t y = 0; y < qrcode.size; y++) {
    for (uint8_t x = 0; x < qrcode.size; x++) {
      if (qrcode_getModule(&qrcode, x, y)) {
        display.fillRect(shiftX+x * scale, shiftY + y*scale, scale, scale, WHITE);
      }
    }
  }

  // Update the display
  display.display();
}