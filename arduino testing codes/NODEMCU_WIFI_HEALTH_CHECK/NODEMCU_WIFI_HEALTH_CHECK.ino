#include <ESP8266HTTPClient.h>
#include <ESP8266WiFi.h>

const char* WIFI_SSID = "netis_2.4G";
const char* WIFI_PASSWORD = "SaTeH03357200";
const char* HEALTH_URL = "http://192.168.1.102:8000/health";

const unsigned long WIFI_CONNECT_TIMEOUT_MS = 20000;
const unsigned long HEALTH_CHECK_INTERVAL_MS = 10000;

unsigned long lastHealthCheckAt = 0;

void connectToWifi();
void checkBackendHealth();

void setup() {
  Serial.begin(115200);
  delay(1000);

  Serial.println();
  Serial.println("NodeMCU Wi-Fi health check starting...");
  Serial.println("Update WIFI_SSID, WIFI_PASSWORD, and HEALTH_URL before upload.");

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

  if (millis() - lastHealthCheckAt >= HEALTH_CHECK_INTERVAL_MS) {
    lastHealthCheckAt = millis();
    checkBackendHealth();
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

void checkBackendHealth() {
  WiFiClient client;
  HTTPClient http;

  Serial.print("Calling backend health endpoint: ");
  Serial.println(HEALTH_URL);

  if (!http.begin(client, HEALTH_URL)) {
    Serial.println("Could not start HTTP request.");
    return;
  }

  http.setTimeout(5000);

  int httpCode = http.GET();

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
