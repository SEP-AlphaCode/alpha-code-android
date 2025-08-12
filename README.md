# Alpha Code Android Application

## Installation on Robot Device

### Setup Instructions

1. **Configure API Endpoint**  
   Go to `local.properties` in the project root and change:
    ```code
    api.path=http://<your-device-IP-address>:8000/api/v1/
    api.websocket=ws://<your-device-IP-address>:8000/websocket/ws
    ```
   8000 is the default Python backend port (adjust if different)
2. **Rebuild Project**  
   In Android Studio, do the following
    - Build > Clean Project
    - Build > Compile All Sources

### To obtain your device's IP address
- Open Command Prompt and type in
  ```code 
  ipconfig
  ```
- Find `IPv4 Address` under `Wireless LAN adapter Wi-Fi`

### Guide on creating more environment variables
- Add to `local.properties` in the project root:
    ```code
    <key>=<value>
    ```
- Go to `build.gradle` (app level), and add
    ```code
    buildConfigField "<data type>", "KEY_NAME", "\"${getLocalProperty('<key name in file>', '<default value>')}\""
    ```
  You may exclude the double quotes (`\"`) if it isn't a string value
- Clean and build as above
- To verify, go to
    ```code
    app/build/generated/source/buildConfig/<variant>[main]/<package name>/BuildConfig.java
    ```
  There should be a field `<data type>` `<KEY_NAME>` = `value in local.properties`