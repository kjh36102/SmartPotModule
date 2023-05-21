#ifndef __MULTITASK_RTOS_H__
#define __MULTITASK_RTOS_H__

//-------------------------------------------------------------

//RTOS 라이브러리 가져오기
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"

#define PRINT_STACK_USAGE false
#define printStack if(PRINT_STACK_USAGE) printStack

//-------------------------------------------------------------
#define LOGKEY "MultitaskRTOS.h"
#include "Logger.h"
//-------------------------------------------------------------

/**
새로운 태스크를 생성하고 실행하는 함수

[예시 코드]
void myTask(void* taskParams) {
    // 실행될 코드
}
createAndRunTask(myTask, "MyTask"); //태스크 등록 및 실행
*/
void createAndRunTask(TaskFunction_t pvTaskCode, const char* const pcName, uint16_t stackDepth = 2000, void* taskParams = NULL, UBaseType_t priority = 1) {
    TaskHandle_t xHandle = NULL;
    BaseType_t xReturned;

    xReturned = xTaskCreate(pvTaskCode, pcName, stackDepth, taskParams, priority, &xHandle);

    if(xReturned != pdPASS) { // 태스크 생성에 실패하면
        LOGLN("Failed to create task: " + String(pcName));
    }else{
        LOGLN("New Task created: " + String(pcName));
    }
}

void deleteTask(TaskHandle_t task) {
  char* taskName = pcTaskGetTaskName(task);
  LOGLN(String(taskName) + " task is about to be deleted...");
  vTaskDelete(task);
}

volatile UBaseType_t STACK_USAGE = 0;

void printStackUsage(){
  STACK_USAGE = uxTaskGetStackHighWaterMark(xTaskGetCurrentTaskHandle());
  LOGF("\n\tSTACK_USAGE: %d\n", STACK_USAGE);
}

//스택 오버플로 감지기능 ON
#define configCheck_FOR_STACK_OVERFLOW 2

// 스택 오버플로우를 감지하면 호출되는 함수
extern "C" void vApplicationStackOverflowHook(TaskHandle_t xTask, char *pcTaskName) {
  LOGLN("Stack overflow detected!");
  LOG("\tTask name: ");
  LOGLN(pcTaskName);

  // 여기서 추가로 오류 처리를 수행할 수 있습니다.
  // 예를 들어, 시스템을 재시작하거나 오류 로그를 저장할 수 있습니다.

  for(;;); // 오류를 루프에서 알립니다.
}


//-------------------------------------------------------------
#endif //__MULTITASK_RTOS_H__