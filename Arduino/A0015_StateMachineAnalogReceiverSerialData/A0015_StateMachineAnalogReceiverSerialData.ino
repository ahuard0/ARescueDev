#include <SPI.h>

String inputString = "";      // a String to hold incoming data
bool stringComplete = false;  // whether the string is complete

bool broadcast = true;
String receiveData = "";
signed long r = 0;
bool initialized = false;
int count = 0;

String outputString = "";
int checksum = 0;

const int CS = 10;
const byte wiper_addr = 0x00;

enum State {  // Define the states for the finite state machine
  IDLE,
  MONITOR,
  RECEIVE_COMMANDS,
  TRANSMIT_DATA,
  DUMP_DATA
};

enum Pin {
  ANALOG_0,
  ANALOG_1,
  ANALOG_2,
  ANALOG_3,
  ANALOG_4,
  ANALOG_5
};

const uint8_t MAX_MONITOR_PINS = 6;
uint8_t numMonitorPins = 2;  // Set the initial number of monitored pins
//Pin monitorPins[MAX_MONITOR_PINS] = {ANALOG_0, ANALOG_1, ANALOG_2, ANALOG_3};
Pin monitorPins[MAX_MONITOR_PINS] = {ANALOG_0, ANALOG_1};
//Pin monitorPins[MAX_MONITOR_PINS] = {ANALOG_0};
Pin currentPin;

const int NUM_SAMPLES = 50;  // Number of samples to collect
int samples_A0[NUM_SAMPLES];
int samples_A1[NUM_SAMPLES];
int samples_A2[NUM_SAMPLES];
int samples_A3[NUM_SAMPLES];
int samples_A4[NUM_SAMPLES];
int samples_A5[NUM_SAMPLES];
int sampleCount = 0;  // Counter for the collected samples

const uint8_t AVG_FACTOR = 3;
int samples_raw[AVG_FACTOR];  // used for N-point sample averaging

State currentState = MONITOR;

void setup() {
  Serial.begin(115200);  // enable serial output
  inputString.reserve(200); // reserve 200 bytes for the inputString buffer

  for (int i = 2; i <= 7; i++) {  // Setup digital pins 2-7 as outputs
    pinMode(i, OUTPUT);
  }

  for (int i = 0; i < numMonitorPins; i++) {
    pinMode(monitorPins[i], INPUT);  // Set monitor pins as input
  }

  pinMode(8, OUTPUT);
  pinMode(9, OUTPUT);
  pinMode(CS, OUTPUT);
  SPI.begin();

  // Set the potentiometers to an initial value
  delay(100);
  setPotValue(0, 5);
  delay(100);
  setPotValue(1, 5);
  delay(100);
  setPotValue(2, 5);  // sets the channel 0 value -> hardware problem?  Solution may be to set the resistor selectors to the same value and set all potentiometers at the same time
  delay(100);
  setPotValue(3, 5);
}

void loop() {
  switch (currentState) {
    case IDLE:
      if (Serial.available() > 0)
        currentState = RECEIVE_COMMANDS;
      else
        delay(1000);
      break;
    case MONITOR:
      memset(samples_A0, 0, sizeof(samples_A0));  // reset to zero
      memset(samples_A1, 0, sizeof(samples_A1));  // reset to zero
      memset(samples_A2, 0, sizeof(samples_A2));  // reset to zero
      memset(samples_A3, 0, sizeof(samples_A3));  // reset to zero
      memset(samples_A4, 0, sizeof(samples_A4));  // reset to zero
      memset(samples_A5, 0, sizeof(samples_A5));  // reset to zero

      while (sampleCount < NUM_SAMPLES) {  // get samples
        for (int i = 0; i < numMonitorPins; i++) {
          currentPin = monitorPins[i];
          switch(currentPin) {  // sample only those pins requested
            case ANALOG_0:
              ADMUX = (ADMUX & 0xF0) | 0x00;  // Set the MUX bits to select channel 0
              for (int k = 0; k < AVG_FACTOR; k++) {
                delayMicroseconds(12);  // Stabilization delay of 12 microseconds
                samples_raw[k] = analogRead(A0);
              }
              samples_A0[sampleCount] = average(samples_raw, AVG_FACTOR);
              continue;
            case ANALOG_1:
              ADMUX = (ADMUX & 0xF0) | 0x01;  // Set the MUX bits to select channel 1
              for (int k = 0; k < AVG_FACTOR; k++) {
                delayMicroseconds(12);  // Stabilization delay of 12 microseconds
                samples_raw[k] = analogRead(A1);
              }
              samples_A1[sampleCount] = average(samples_raw, AVG_FACTOR);
              continue;
            case ANALOG_2:
              ADMUX = (ADMUX & 0xF0) | 0x02;  // Set the MUX bits to select channel 2
              for (int k = 0; k < AVG_FACTOR; k++) {
                delayMicroseconds(12);  // Stabilization delay of 12 microseconds
                samples_raw[k] = analogRead(A2);
              }
              samples_A2[sampleCount] = average(samples_raw, AVG_FACTOR);
              continue;
            case ANALOG_3:
              ADMUX = (ADMUX & 0xF0) | 0x03;  // Set the MUX bits to select channel 3
              for (int k = 0; k < AVG_FACTOR; k++) {
                delayMicroseconds(12);  // Stabilization delay of 12 microseconds
                samples_raw[k] = analogRead(A3);
              }
              samples_A3[sampleCount] = average(samples_raw, AVG_FACTOR);
              continue;
            case ANALOG_4:
              ADMUX = (ADMUX & 0xF0) | 0x04;  // Set the MUX bits to select channel 4
              for (int k = 0; k < AVG_FACTOR; k++) {
                delayMicroseconds(12);  // Stabilization delay of 12 microseconds
                samples_raw[k] = analogRead(A4);
              }
              samples_A4[sampleCount] = average(samples_raw, AVG_FACTOR);
              continue;
            case ANALOG_5:
              ADMUX = (ADMUX & 0xF0) | 0x05;  // Set the MUX bits to select channel 5
              for (int k = 0; k < AVG_FACTOR; k++) {
                delayMicroseconds(12);  // Stabilization delay of 12 microseconds
                samples_raw[k] = analogRead(A5);
              }
              samples_A5[sampleCount] = average(samples_raw, AVG_FACTOR);
              continue;
          }
        }
        sampleCount++;
      }

      currentState = TRANSMIT_DATA;  // Switch to the next state to send data
      //currentState = DUMP_DATA;  // Switch to the next state to send data
      break;  // exit state

    case TRANSMIT_DATA:
      for (int k = 0; k < NUM_SAMPLES; k++) {  // Send the collected data over the serial port
        outputString = "";
        count++;

        outputString += "#";
        outputString += "|";
        outputString += count;
        outputString += "|MON|";
        outputString += k;
        outputString += "|";

        for (int j = 0; j < numMonitorPins; j++) {  // Output List of Pins Monitored (e.g., "|0,3|")
          currentPin = monitorPins[j];
          switch(currentPin) {  // sample only those pins requested
            case ANALOG_0:
              outputString += "0";
              break;
            case ANALOG_1:
              outputString += "1";
              break;
            case ANALOG_2:
              outputString += "2";
              break;
            case ANALOG_3:
              outputString += "3";
              break;
            case ANALOG_4:
              outputString += "4";
              break;
            case ANALOG_5:
              outputString += "5";
              break;
          }
          if (j+1<numMonitorPins)  // add comma if not at the end of the list
            outputString += ",";
        }
        outputString += "|";

        for (int j = 0; j < numMonitorPins; j++) {  // Output Pin Data e.g., ("|123,573|")
          currentPin = monitorPins[j];
          switch(currentPin) {  // sample only those pins requested
            case ANALOG_0:
              outputString += samples_A0[k];
              break;
            case ANALOG_1:
              outputString += samples_A1[k];
              break;
            case ANALOG_2:
              outputString += samples_A2[k];
              break;
            case ANALOG_3:
              outputString += samples_A3[k];
              break;
            case ANALOG_4:
              outputString += samples_A4[k];
              break;
            case ANALOG_5:
              outputString += samples_A5[k];
              break;
          }
          if (j+1<numMonitorPins)  // add comma if not at the end of the list
            outputString += ",";
        }

        checksum = computeChecksum(outputString);  // checksum of the prior string (e.g., "|4312")
        
        outputString += "|";
        outputString += checksum;

        for (int j = 0; j < outputString.length(); j++) {  // output the data to the serial link
          Serial.print(outputString.charAt(j));
        }
        Serial.println("");  // end with a line break

      }

      sampleCount = 0;  // Reset the sample count and switch state
      currentState = RECEIVE_COMMANDS;
      break;  // exit state

    case RECEIVE_COMMANDS:
      while(Serial.available() > 0) {  // Data is available to read
        char inChar = (char)Serial.read();
        if (inChar != -1)
          inputString += inChar;
        if (inChar == '\n') {
          stringComplete = true;
          Serial.flush();
        }
      }

      if (stringComplete) {  // Called when a command is received
        stringComplete = false;  // Reset for receiving the next command
        processCommand(inputString);
        inputString = "";  // Reset the buffer
      }
      
      if (broadcast) {
          currentState = MONITOR;
      }
      break;
    case DUMP_DATA:
      for (int k = 0; k < NUM_SAMPLES; k++) {  // Send the collected data over the serial port
        Serial.println(samples_A0[k]);  // dump A0 samples, one per line
      }

      sampleCount = 0;  // Reset the sample count and switch back to the monitoring state
      currentState = MONITOR;
      break;  // exit state
    default:
      break;
  }
}

void processCommand(const String& cmd) {
  cmd.trim(); // remove any \r \n whitespace at the end of the String
  // Check for Header
  if(cmd.charAt(0)=='$' && cmd.charAt(1)=='|') {  // Header Originated at Host ($)
    receiveData = cmd.substring(2, (int)cmd.length());
    if (receiveData.compareTo("_BROADCAST_ON")==0) {
      broadcast = true;
      currentState = MONITOR;
    }
    else if (receiveData.compareTo("_BROADCAST_OFF")==0) {
      broadcast = false;
      currentState = RECEIVE_COMMANDS;
    }
    else if (receiveData.compareTo("_IDLE")==0) {
      currentState = IDLE;
    }
    else if (receiveData.compareTo("_SINGLE_READ")==0) {
      currentState = MONITOR;
    }
    else if (receiveData.startsWith("_CHAN")) {  // Example: "$|_CHAN|0,1" for (Ch0, Ch1, Ch2, ...)
      int delimiterIndex = receiveData.indexOf("|");
      if (delimiterIndex != -1) {
        int startValueIndex = delimiterIndex + 1;
        int endValueIndex = receiveData.length();

        // Extract the channel values substring
        String channelsString = receiveData.substring(startValueIndex, endValueIndex);
        
        // Split the channel values by comma delimiter
        String channelValues[10];  // Assuming a maximum of 10 channels
        int channelCount = 0;

        char charArray[channelsString.length() + 1];
        channelsString.toCharArray(charArray, sizeof(charArray));

        char* token = strtok(charArray, ",");
        while (token != NULL && channelCount < 10) {
          channelValues[channelCount] = String(token);
          token = strtok(NULL, ",");
          channelCount++;
        }

        numMonitorPins = channelCount;  // set the monitor pin count

        for (int i = 0; i < channelCount; i++) {  // Process each channel value
          int channel = channelValues[i].toInt();
          if (channel >= 0 && channel < MAX_MONITOR_PINS) {
            switch(channel) {
              case 0: 
                monitorPins[i] = ANALOG_0;
                break;
              case 1: 
                monitorPins[i] = ANALOG_1;
                break;
              case 2: 
                monitorPins[i] = ANALOG_2;
                break;
              case 3: 
                monitorPins[i] = ANALOG_3;
                break;
              case 4: 
                monitorPins[i] = ANALOG_4;
                break;
              case 5: 
                monitorPins[i] = ANALOG_5;
                break;
            }
          }
        }
      }
    }
    else if (receiveData.startsWith("_POT")) {  // Example: "$|_POT|0,127" for (Channel, Value)
      int delimiterIndex = receiveData.indexOf("|");
      if (delimiterIndex != -1) {
        int startChannelIndex = delimiterIndex + 1;

        int endChannelIndex = receiveData.indexOf(",", startChannelIndex); // Find the comma after the channel
        if (endChannelIndex == -1) { return; }
        String channelString = receiveData.substring(startChannelIndex, endChannelIndex);  // Extract the channel value substring
        int channel = channelString.toInt(); // Convert the channel string to an integer

        int startValueIndex = endChannelIndex + 1;
        int endValueIndex = receiveData.length();
        String valueString = receiveData.substring(startValueIndex, endValueIndex);  // Extract the value substring
        int value = valueString.toInt(); // Convert the value string to an integer

        setPotValue(channel, value);
      }
    }
    else if (receiveData.compareTo("_D0_ON")==0) {
      digitalWrite(0, HIGH);
    }
    else if (receiveData.compareTo("_D0_OFF")==0) {
      digitalWrite(0, LOW);
    }
    else if (receiveData.compareTo("_D1_ON")==0) {
      digitalWrite(1, HIGH);
    }
    else if (receiveData.compareTo("_D1_OFF")==0) {
      digitalWrite(1, LOW);
    }
    else if (receiveData.compareTo("_D2_ON")==0) {
      digitalWrite(2, HIGH);
    }
    else if (receiveData.compareTo("_D2_OFF")==0) {
      digitalWrite(2, LOW);
    }
    else if (receiveData.compareTo("_D3_ON")==0) {
      digitalWrite(3, HIGH);
    }
    else if (receiveData.compareTo("_D3_OFF")==0) {
      digitalWrite(3, LOW);
    }
    else if (receiveData.compareTo("_D4_ON")==0) {
      digitalWrite(4, HIGH);
    }
    else if (receiveData.compareTo("_D4_OFF")==0) {
      digitalWrite(4, LOW);
    }
    else if (receiveData.compareTo("_D5_ON")==0) {
      digitalWrite(5, HIGH);
    }
    else if (receiveData.compareTo("_D5_OFF")==0) {
      digitalWrite(5, LOW);
    }
    else if (receiveData.compareTo("_D6_ON")==0) {
      digitalWrite(6, HIGH);
    }
    else if (receiveData.compareTo("_D6_OFF")==0) {
      digitalWrite(6, LOW);
    }
    else if (receiveData.compareTo("_D7_ON")==0) {
      digitalWrite(7, HIGH);
    }
    else if (receiveData.compareTo("_D7_OFF")==0) {
      digitalWrite(7, LOW);
    }
    else if (receiveData.compareTo("_D8_ON")==0) {
      digitalWrite(8, HIGH);
    }
    else if (receiveData.compareTo("_D8_OFF")==0) {
      digitalWrite(8, LOW);
    }
    else if (receiveData.compareTo("_D9_ON")==0) {
      digitalWrite(9, HIGH);
    }
    else if (receiveData.compareTo("_D9_OFF")==0) {
      digitalWrite(9, LOW);
    }
    else if (receiveData.compareTo("_D10_ON")==0) {
      digitalWrite(10, HIGH);
    }
    else if (receiveData.compareTo("_D10_OFF")==0) {
      digitalWrite(10, LOW);
    }
    else if (receiveData.compareTo("_D11_ON")==0) {
      digitalWrite(11, HIGH);
    }
    else if (receiveData.compareTo("_D11_OFF")==0) {
      digitalWrite(11, LOW);
    }
    else if (receiveData.compareTo("_D12_ON")==0) {
      digitalWrite(12, HIGH);
    }
    else if (receiveData.compareTo("_D12_OFF")==0) {
      digitalWrite(12, LOW);
    }
    else if (receiveData.compareTo("_D13_ON")==0) {
      digitalWrite(13, HIGH);
    }
    else if (receiveData.compareTo("_D13_OFF")==0) {
      digitalWrite(13, LOW);
    }
    else if (receiveData.compareTo("_STATUS")==0) {
      Serial.print("#|DIG|D3:");
      Serial.print(digitalRead(3));
      Serial.print("|D4:");
      Serial.print(digitalRead(4));
      Serial.print("|D5:");
      Serial.print(digitalRead(5));
      Serial.print("|D6:");
      Serial.print(digitalRead(6));
      Serial.print("|D7:");
      Serial.println(digitalRead(7));
      return;
    }
    else {
      Serial.print("#|Command not recognized: ");
      Serial.println(cmd);  // Echo back to the serial bus
      return;
    }
    Serial.println("#|$|" + receiveData);
  } else {
    Serial.print("#|Command not recognized: ");
    Serial.println(cmd);  // Echo back to the serial bus
  }
}

int computeChecksum(String str) {
  int sum = 0;
  
  for (int i = 0; i < str.length(); i++) {
    sum += str.charAt(i);
  }
  
  return sum;
}

int average(int arr[], int length) {
  int sum = 0;
  for (int i = 0; i < length; i++) {
    sum += arr[i];  // Add each element to the sum
  }
  return sum / length;  // Calculate and return the average
}

void setPotValue(int channel, int value) {  // use channels 0-3 and values 0-127
  if (value > 127) {
    Serial.print("#|Value cannot exceed 127. Value provided: ");
    Serial.println(channel);
    return;
  }
  switch(channel) {
    case 0:
      digitalWrite(9, LOW);
      digitalWrite(8, LOW);
      break;
    case 1:
      digitalWrite(9, LOW);
      digitalWrite(8, HIGH);
      break;
    case 2:
      digitalWrite(9, HIGH);
      digitalWrite(8, LOW);
      break;
    case 3:
      digitalWrite(9, HIGH);
      digitalWrite(8, HIGH);
      break;
    default:
      Serial.print("#|Channel not valid: ");
      Serial.print(channel);
      return;
  }
  digitalWrite(CS, LOW);
  delay(10);  // allow time to stabilize
  SPI.transfer(wiper_addr);
  SPI.transfer(value);
  digitalWrite(CS, HIGH);

  Serial.print("#|Channel ");
  Serial.print(channel);
  Serial.print(" potentiometer set to: ");
  Serial.println(value);
}
