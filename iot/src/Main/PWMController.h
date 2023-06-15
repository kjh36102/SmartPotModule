#ifndef __PWM_CONTROLLER_H__
#define __PWM_CONTROLLER_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include <driver/ledc.h>

//-------------------------------------------------------------
#define LOGKEY "PWMController.h"
#include "Logger.h"
//-------------------------------------------------------------

class PWMController {
private:
  int channel;
  int pin;
  double frequency;
  int resolution;
  int maxDuty;
  int minDuty;
  int dutyCycle;
  bool operState = false;

public:
  PWMController(int channel, int pin, double frequency, int maxDuty, int minDuty, int resolution = LEDC_TIMER_10_BIT)
    : channel(channel), pin(pin), frequency(frequency), maxDuty(maxDuty), minDuty(minDuty), resolution(resolution), dutyCycle(minDuty) {

    ledcSetup(channel, frequency, resolution);
    ledcAttachPin(pin, channel);
    setDutyCycle(0);
  }

  PWMController() {}

  int getChannel() {
    return channel;
  }

  int getMaxDuty() {
    return maxDuty;
  }

  int getMinDuty() {
    return minDuty;
  }

  int getDutyCycle() {
    return dutyCycle;
  }

  void setFrequency(double frequency) {
    this->frequency = frequency;
    ledcWriteTone(this->channel, this->frequency);
    LOGF("PWM 채널 %d 주파수 설정 %.2f\n", this->channel, this->frequency);
  }

  void setDutyCycle(int dutyCycle) {
    // if (dutyCycle < minDuty || dutyCycle > maxDuty) {
    //   return;  // 주어진 듀티 사이클이 유효 범위를 벗어나면 무시합니다.
    // }

    int previousDuty = this->dutyCycle;
    this->dutyCycle = dutyCycle;

    if (operState) ledcWrite(this->channel, this->dutyCycle);

    LOGF("PWM 채널 %d 듀티사이클 설정 %d\n", this->channel, this->dutyCycle);
  }

  void setPercentDuty(int percent) {
    setDutyCycle((maxDuty - minDuty) * (100 - percent) / 100 + minDuty);
  }

  void start() {
    ledcWrite(this->channel, this->dutyCycle);
    operState = true;
    LOGF("PWM 채널 %d 시작됨, 현재 듀티사이클 %d\n", this->channel, this->dutyCycle);
  }

  void stop() {
    ledcWrite(channel, 0);
    operState = false;
    LOGF("PWM 채널 %d 중지됨\n", this->channel);
  }
};


//-------------------------------------------------------------
#endif  //__PWM_CONTROLLER_H__
