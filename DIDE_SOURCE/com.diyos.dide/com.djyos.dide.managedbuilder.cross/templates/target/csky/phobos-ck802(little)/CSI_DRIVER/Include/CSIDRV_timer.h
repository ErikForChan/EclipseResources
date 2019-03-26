/******************************************************************************
 * @file     CSIDRV_timer.h
 * @brief    CSI Header File timer Driver definitions
 * @version  V1.0
 * @date     20. July 2016
 ******************************************************************************/
/* ---------------------------------------------------------------------------
 * Copyright (C) 2016 CSKY Limited. All rights reserved.
 *
 * Redistribution and use of this software in source and binary forms,
 * with or without modification, are permitted provided that the following
 * conditions are met:
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of CSKY Ltd. nor the names of CSKY's contributors may
 *     be used to endorse or promote products derived from this software without
 *     specific prior written permission of CSKY Ltd.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 * -------------------------------------------------------------------------- */
#ifndef CSIDRV_TIMER_H_
#define CSIDRV_TIMER_H_

#include "CSIDRV_Common.h"

#define CSKY_TIMER_API_VERSION CSKY_DRIVER_VERSION_MAJOR_MINOR(1,00)  /* API version */

/****** Timer Control Codes *****/
#define CSKY_TIMER_MODE_SELECT                  (0x01)
#define CSKY_TIMER_CLEAR_IRQFLAG                (0x02)

/*
 * enumeration the mode select
 */
typedef enum{
	TIMER_MODE_FREE,
	TIME_MODE_USER,
}CKEnum_Timer_Modeselect;

/**
\brief TIMER Status
*/
typedef struct _CSKY_TIMER_STATUS {
  uint32_t busy             : 1;        /* /< Busy flag */
  uint32_t mode             : 1;        /* /< Mode: 0=free-running, 1=user-defined */
} CSKY_TIMER_STATUS;


/****** TIMER Event *****/
#define CSKY_TIMER_EVENT_DONE             (1UL << 0)  /* /< time interrupt finished */
#define CSKY_TIMER_EVENT_ERROR_TIMEOUT    (1UL << 1)  /* /< time operation error for timeout */

typedef void (*CSKY_TIMER_SignalEvent_t) (uint32_t event);  /* /< Pointer to \ref CSKY_TIMER_SignalEvent : Signal TIMER Event. */

/**
\brief TIMER Driver Capabilities.
*/
typedef struct _CSKY_TIMER_CAPABILITIES {
  uint32_t timer_mode_select : 1;          /* /< supports for two operation modes: free-running and user-defined count */
} CSKY_TIMER_CAPABILITIES;

/**
\brief Access structure of the TIMER Driver.
*/

typedef struct _CSKY_DRIVER_TIMER {
  CSKY_DRIVER_VERSION       (*GetVersion)     (const struct _CSKY_DRIVER_TIMER *instance);                                                                /* /< Pointer to \ref CSKY_TIMER_GetVersion : Get driver version. */
  CSKY_TIMER_CAPABILITIES   (*GetCapabilities)(const struct _CSKY_DRIVER_TIMER *instance);                                                                /* /< Pointer to \ref CSKY_TIMER_GetCapabilities : Get driver capabilities. */
  int32_t                   (*Initialize)     (const struct _CSKY_DRIVER_TIMER *instance, CSKY_TIMER_SignalEvent_t cb_event);                             /* /< Pointer to \ref CSKY_TIMER_Initialize : Initialize TIMER Interface. */
  int32_t                   (*Uninitialize)   (const struct _CSKY_DRIVER_TIMER *instance);                                                                /* /< Pointer to \ref CSKY_TIMER_Uninitialize : De-initialize TIMER Interface. */
  int32_t                   (*PowerControl)   (const struct _CSKY_DRIVER_TIMER *instance, CSKY_POWER_STATE state);                                        /* /< Pointer to \ref CSKY_TIMER_PowerControl : Control TIMER Interface Power. */
  int32_t                   (*Open)           (const struct _CSKY_DRIVER_TIMER *instance, bool int_mode, uint32_t priority, bool bfast);                  /* /< Pointer to \ref CSKY_TIMER_Open : Open the timer, register the interrupt */
  int32_t                   (*Close)          (const struct _CSKY_DRIVER_TIMER *instance);                                                                /* /< Pointer to \ref CSKY_TIMER_Close : Close the timer, free interrupt */
  int32_t                   (*Start)          (const struct _CSKY_DRIVER_TIMER *instance, uint32_t timeout, uint32_t apbfreq);                            /* /< Pointer to \ref CSKY_TIMER_Start : Start the corresponding timer */
  int32_t                   (*Stop)           (const struct _CSKY_DRIVER_TIMER *instance);                                                                /* /< Pointer to \ref CSKY_TIMER_Stop : Stop a designated timer */
  int32_t                   (*GetCurrentValue)(const struct _CSKY_DRIVER_TIMER *instance);                                                                /* /< Pointer to \ref CSKY_TIMER_GetCurrentValue : read the current value of the timer */
  int32_t                   (*Control)        (const struct _CSKY_DRIVER_TIMER *instance, uint32_t control, uint32_t arg);                                /* /< Pointer to \ref CSKY_TIMER_Control : Control TIMER Interface. */
  CSKY_TIMER_STATUS         (*GetStatus)      (const struct _CSKY_DRIVER_TIMER *instance);                                                                /* /< Pointer to \ref CSKY_TIMER_GetStatus : Get TIMER status. */
  void                      *priv;                                                                                                                        /* /< Pointer to TIMER's private data. */
}const CSKY_DRIVER_TIMER;

#endif /* CSIDRV_TIMER_H_ */
