#ifndef SERIAL_COMMANDER_H
#define SERIAL_COMMANDER_H

//-------------------------------------------------------------

#include <Arduino.h>
#include <map>
#include <functional>

//-------------------------------------------------------------
#define LOGKEY "SerialCommander.h"
#include "Logger.h"
//-------------------------------------------------------------

class SerialCommander {
private:
  std::map<String, std::function<void(String)>> callbacks;

  // Private constructor for singleton
  SerialCommander() {}

public:
  // Delete copy constructor and assignment operator
  SerialCommander(const SerialCommander&) = delete;
  SerialCommander& operator=(const SerialCommander&) = delete;

  // Public method to access the singleton instance
  static SerialCommander& getInstance() {
    static SerialCommander instance;
    return instance;
  }

  void registerCallback(String command, std::function<void(String)> func) {
    if (callbacks.find(command) == callbacks.end()) {
      callbacks[command] = std::move(func);
    } else {
      LOGLN("Callback already exists for this command!");
    }
  }

  static void taskFunction(void* taskParam) {
    SerialCommander::getInstance().run();
  }

  void run() {
    for (;;) {
      if (Serial.available()) {
        String input = Serial.readStringUntil('\n');
        int dividerIndex = input.indexOf("://");

        if (dividerIndex == -1) continue;  // Invalid command

        String command = input.substring(0, dividerIndex);
        String argument = input.substring(dividerIndex + 3);  // get argument after '://'

        auto it = callbacks.find(command);
        if (it != callbacks.end()) {  // command found
          it->second(argument);       // Execute the callback with the argument
        } else {
          LOGLN("Command not found!");
        }
      }

      vTaskDelay(250);
    }
  }
};

#endif  //SERIAL_COMMANDER_H
