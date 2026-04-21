const unsigned long MEGA_SERIAL_BAUD = 115200;

void setup() {
  Serial.begin(MEGA_SERIAL_BAUD);
  delay(1000);

  Serial.println();
  Serial.println("NodeMCU serial receive test starting...");
  Serial.println("Waiting for Mega Serial2 lines...");
}

void loop() {
  if (Serial.available() > 0) {
    String incomingLine = Serial.readStringUntil('\n');
    incomingLine.trim();

    if (incomingLine.length() > 0) {
      Serial.print("Received from Mega: ");
      Serial.println(incomingLine);
    }
  }
}
