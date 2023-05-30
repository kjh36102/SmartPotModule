#ifndef __TASK_UPDATE_SOIL_DATA_H__
#define __TASK_UPDATE_SOIL_DATA_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include "stdio.h"
#include "TaskDBManager.h"

#define PIN_SOIL_DERE 13
#define PIN_SOIL_RO 32
#define PIN_SOIL_DI 33

//-------------------------------------------------------------
#define LOGKEY "TaskUpdateSoilData.h"
#include "Logger.h"
//-------------------------------------------------------------

SoilSensor soilSensor(PIN_SOIL_DERE, PIN_SOIL_RO, PIN_SOIL_DI);

char sql_buffer[150] = {
  '\0',
};

void tListenUpdateSoilData(void* taskParams) {

  for (;;) {
    float* received = soilSensor.read();

    sprintf(sql_buffer, "UPDATE soil_data SET hm=%.1f, tm=%.1f, ec=%.0f, ph=%.1f, n=%.0f, p=%.0f, k=%.0f, lt=0",
            received[0],  //hm
            received[1],  //tm
            received[2],  //ec
            received[3],  //ph
            received[4],  //n
            received[5],  //p
            received[6]   //k
            //여기에 광량 추가해야함
    );

    if (stateDBPrepared) executeSql(sql_buffer);

    vTaskDelay(5000);  //매 5초마다 실행
  }
}


//-------------------------------------------------------------
#endif  //__TASK_UPDATE_SOIL_DATA_H__