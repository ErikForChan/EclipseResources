/******************************************************************************
 * @file     CSIDRV_UART.h
 * @brief    CSI Header File SHA Driver definitions
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

#ifndef __CSIDRV_SHA_H
#define __CSIDRV_SHA_H

#include "CSIDRV_Common.h"

#define CSKY_SHA_API_VERSION CSKY_DRIVER_VERSION_MAJOR_MINOR(1,0)  /* API version */


/****** SHA Control Codes *****/

#define CSKY_SHA_CONTROL_Pos                0
#define CSKY_SHA_CONTROL_Msk               (0xFFUL << CSKY_SHA_CONTROL_Pos)

/*----- SHA Control Codes: Mode -----*/
#define CSKY_SHA_MODE_1                    (0x01UL << CSKY_SHA_CONTROL_Pos)   /* /< SHA_1 mode */
#define CSKY_SHA_MODE_256                  (0x02UL << CSKY_SHA_CONTROL_Pos)   /* /< SHA_256 mode */
#define CSKY_SHA_MODE_224                  (0x03UL << CSKY_SHA_CONTROL_Pos)   /* /< SHA_224 mode */
#define CSKY_SHA_MODE_512                  (0x04UL << CSKY_SHA_CONTROL_Pos)   /* /< SHA_512 mode */
#define CSKY_SHA_MODE_384                  (0x05UL << CSKY_SHA_CONTROL_Pos)   /* /< SHA_384 mode */
#define CSKY_SHA_MODE_512_256              (0x06UL << CSKY_SHA_CONTROL_Pos)   /* /< SHA_512_256 mode */
#define CSKY_SHA_MODE_512_224              (0x07UL << CSKY_SHA_CONTROL_Pos)   /* /< SHA_512_224 mode */

/*----- SHA Control Codes: Endian Mode -----*/
#define CSKY_SHA_ENDIAN_MODE_Pos              8
#define CSKY_SHA_ENDIAN_MODE_Msk             (3UL << CSKY_SHA_ENDIAN_MODE_Pos)
#define CSKY_SHA_ENDIAN_MODE_BIG             (1UL << CSKY_SHA_ENDIAN_MODE_Pos)    /* /< Big Endian Mode */
#define CSKY_SHA_ENDIAN_MODE_LITTLE          (2UL << CSKY_SHA_ENDIAN_MODE_Pos)    /* /< Little Endian Mode */


/****** SHA specific error codes *****/
#define CSKY_SHA_ERROR_MODE                (CSKY_DRIVER_ERROR_SPECIFIC - 1)     /* /< Specified Mode not supported */
#define CSKY_SHA_ERROR_ENDIAN_MODE         (CSKY_DRIVER_ERROR_SPECIFIC - 2)     /* /< Specified Endian mode not supported */



typedef void (*CSKY_SHA_SignalEvent_t) (uint32_t event);  /* /< Pointer to \ref CSKY_SHA_SignalEvent : Signal SHA Event. */


/**
\brief SHA Status
*/
typedef struct _CSKY_SHA_STATUS {
  uint32_t cal_done         : 1;        /* /< calculate done flag */
  uint32_t busy             : 1;        /* /< calculate busy flag */
} CSKY_SHA_STATUS;

/****** SHA Event *****/
#define CSKY_SHA_EVENT_CALCULATE_COMPLETE       (1UL << 0)  /* /< calculate completed; */
#define CSKY_SHA_EVENT_RECEIVE_COMPLETE         (1UL << 1)  /* /< Receive completed */


/**
\brief SHA Device Driver Capabilities.
*/
typedef struct _CSKY_SHA_CAPABILITIES {
  uint32_t sha1               : 1;      /* /< supports sha1 mode */
  uint32_t sha224             : 1;      /* /< supports sha224 mode */
  uint32_t sha256             : 1;      /* /< supports sha256 mode */
  uint32_t sha384             : 1;      /* /< supports sha384 mode */
  uint32_t sha512             : 1;      /* /< supports sha512 mode */
  uint32_t sha512_224         : 1;      /* /< supports sha512_224 mode */
  uint32_t sha512_256         : 1;      /* /< supports sha512_256 mode */
  uint32_t endianmode         : 1;      /* /< supports endian mode control */
  uint32_t interruptmode      : 1;      /* /< supports interrupt mode */
} CSKY_SHA_CAPABILITIES;

/**
\brief Access structure of the SHA Driver
*/
typedef struct _CSKY_DRIVER_SHA {
  CSKY_DRIVER_VERSION     (*GetVersion)     (const struct _CSKY_DRIVER_SHA *instance);                                                 /* /< Pointer to \ref CSKY_SHA_GetVersion : Get driver version. */
  CSKY_SHA_CAPABILITIES   (*GetCapabilities)(const struct _CSKY_DRIVER_SHA *instance);                                                 /* /< Pointer to \ref CSKY_SHA_GetCapabilities : Get driver capabilities. */
  int32_t                 (*Initialize)     (const struct _CSKY_DRIVER_SHA *instance, CSKY_SHA_SignalEvent_t cb_event);                      /* /< Pointer to \ref CSKY_SHA_Initialize : Initialize SHA Interface. */
  int32_t                 (*Uninitialize)   (const struct _CSKY_DRIVER_SHA *instance);                                                 /* /< Pointer to \ref CSKY_SHA_Uninitialize : De-initialize SHA Interface. */
  int32_t                 (*PowerControl)   (const struct _CSKY_DRIVER_SHA *instance, CSKY_POWER_STATE state);                               /* /< Pointer to \ref CSKY_SHA_PowerControl : Control SHA Interface Power. */
  int32_t                 (*FillData)       (const struct _CSKY_DRIVER_SHA *instance, void *data, uint32_t length,  uint32_t total_length);  /* /< Pointer to \ref CSKY_SHA_FillData : FIll data to the register. */
  int32_t                 (*GetResult)      (const struct _CSKY_DRIVER_SHA *instance, void *data);                                           /* /< Pointer to \ref CSKY_SHA_GetResult : Get the result. */
  int32_t                 (*Control)        (const struct _CSKY_DRIVER_SHA *instance, uint32_t control, uint32_t arg);                       /* /< Pointer to \ref CSKY_SHA_Control : Control SHA Interface. */
  CSKY_SHA_STATUS         (*GetStatus)      (const struct _CSKY_DRIVER_SHA *instance);                                                 /* /< Pointer to \ref CSKY_SHA_GetStatus : Get Flash status. */
  void                    *priv;                                                                                                  /* /< Pointer to SHA's private data. */
}const CSKY_DRIVER_SHA;
#endif
