#include <ESP8266HTTPClient.h>
#include <ESP8266WiFi.h>

const char* WIFI_SSID = "netis_2.4G";
const char* WIFI_PASSWORD = "SaTeH03357200";
const char* IOT_READING_URL = "http://192.168.1.102:8000/iot/readings";

const char* DEVICE_UID = "NODEMCU-BIN-001";
const char* DEVICE_API_KEY = "cwJgerlBTInQ2P62jBaXB-02KQyxwKux";

const unsigned long WIFI_CONNECT_TIMEOUT_MS = 20000;
const float TEMP_FILL_PCT_FALLBACK = 0.0f;

struct SensorPacket {
  bool isValid = false;
  bool hasFillPct = false;
  bool hasGps = false;
  float fillPct = 0.0f;
  float weightKg = 0.0f;
  float gpsLat = 0.0f;
  float gpsLon = 0.0f;
  int itemCount = 0;
  String rawLine = "";
};

void connectToWifi();
SensorPacket parseSensorLine(const String& line);
void postSensorReading(const SensorPacket& packet);
String buildPayload(const SensorPacket& packet);
bool isNaToken(const String& value);

void setup() {
  Serial.begin(115200);
  delay(1000);

  Serial.println();
  Serial.println("NodeMCU serial-to-IoT test starting...");
  Serial.println("Waiting for SENSOR lines from the Mega and posting them to the backend.");

  WiFi.mode(WIFI_STA);
  connectToWifi();
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Wi-Fi disconnected. Trying again...");
    connectToWifi();
    delay(1000);
    return;
  }

  if (Serial.available() > 0) {
    String incomingLine = Serial.readStringUntil('\n');
    incomingLine.trim();

    if (incomingLine.length() > 0) {
      Serial.print("Received from Mega: ");
      Serial.println(incomingLine);

      SensorPacket packet = parseSensorLine(incomingLine);
      if (packet.isValid) {
        postSensorReading(packet);
      }
    }
  }

  delay(100);
}

void connectToWifi() {
  Serial.print("Connecting to Wi-Fi: ");
  Serial.println(WIFI_SSID);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  unsigned long startAt = millis();
  while (WiFi.status() != WL_CONNECTED &&
         millis() - startAt < WIFI_CONNECT_TIMEOUT_MS) {
    delay(500);
    Serial.print(".");
  }

  Serial.println();

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("Wi-Fi connected.");
    Serial.print("NodeMCU IP: ");
    Serial.println(WiFi.localIP());
    return;
  }

  Serial.println("Wi-Fi connection failed.");
  Serial.print("Wi-Fi status code: ");
  Serial.println(WiFi.status());
}

SensorPacket parseSensorLine(const String& line) {
  SensorPacket packet;
  packet.rawLine = line;

  int firstComma = line.indexOf(',');
  if (firstComma < 0) {
    Serial.println("Serial line rejected: missing commas.");
    return packet;
  }

  String prefix = line.substring(0, firstComma);
  if (prefix != "SENSOR") {
    Serial.println("Serial line ignored: prefix is not SENSOR.");
    return packet;
  }

  String fields[5];
  int startIndex = firstComma + 1;

  for (int index = 0; index < 5; index++) {
    int nextComma = (index < 4) ? line.indexOf(',', startIndex) : -1;

    if (index < 4 && nextComma < 0) {
      Serial.println("Serial line rejected: not enough fields.");
      return packet;
    }

    fields[index] = (index < 4)
      ? line.substring(startIndex, nextComma)
      : line.substring(startIndex);
    fields[index].trim();

    startIndex = nextComma + 1;
  }

  if (isNaToken(fields[0])) {
    packet.hasFillPct = false;
    packet.fillPct = TEMP_FILL_PCT_FALLBACK;
    Serial.println("fill_pct is NA from Mega. Using temporary fallback 0.0 for backend ingestion.");
  } else {
    packet.hasFillPct = true;
    packet.fillPct = fields[0].toFloat();
  }

  if (isNaToken(fields[1])) {
    Serial.println("Serial line rejected: weight_kg is missing.");
    return packet;
  }
  packet.weightKg = fields[1].toFloat();

  bool gpsLatMissing = isNaToken(fields[2]);
  bool gpsLonMissing = isNaToken(fields[3]);
  if (gpsLatMissing != gpsLonMissing) {
    Serial.println("Serial line rejected: GPS must provide both latitude and longitude together.");
    return packet;
  }

  if (!gpsLatMissing) {
    packet.hasGps = true;
    packet.gpsLat = fields[2].toFloat();
    packet.gpsLon = fields[3].toFloat();
  }

  if (isNaToken(fields[4])) {
    packet.itemCount = 0;
  } else {
    packet.itemCount = fields[4].toInt();
  }

  packet.isValid = true;
  return packet;
}

void postSensorReading(const SensorPacket& packet) {
  WiFiClient client;
  HTTPClient http;

  Serial.print("Posting sensor reading to: ");
  Serial.println(IOT_READING_URL);

  if (!http.begin(client, IOT_READING_URL)) {
    Serial.println("Could not start HTTP request.");
    return;
  }

  http.setTimeout(5000);
  http.addHeader("Content-Type", "application/json");

  String payload = buildPayload(packet);
  Serial.print("Payload: ");
  Serial.println(payload);

  int httpCode = http.POST(payload);

  if (httpCode > 0) {
    Serial.print("HTTP status: ");
    Serial.println(httpCode);
    Serial.print("Response: ");
    Serial.println(http.getString());
  } else {
    Serial.print("HTTP request failed: ");
    Serial.println(http.errorToString(httpCode));
  }

  http.end();
}

String buildPayload(const SensorPacket& packet) {
  String payload = "{";
  payload += "\"device_uid\":\"";
  payload += DEVICE_UID;
  payload += "\",";
  payload += "\"api_key\":\"";
  payload += DEVICE_API_KEY;
  payload += "\",";
  payload += "\"fill_pct\":";
  payload += String(packet.fillPct, 1);
  payload += ",";
  payload += "\"weight_kg\":";
  payload += String(packet.weightKg, 2);

  if (packet.hasGps) {
    payload += ",";
    payload += "\"gps_lat\":";
    payload += String(packet.gpsLat, 6);
    payload += ",";
    payload += "\"gps_lon\":";
    payload += String(packet.gpsLon, 6);
  }

  payload += "}";

  return payload;
}

bool isNaToken(const String& value) {
  return value.length() == 0 || value == "NA" || value == "na";
}
