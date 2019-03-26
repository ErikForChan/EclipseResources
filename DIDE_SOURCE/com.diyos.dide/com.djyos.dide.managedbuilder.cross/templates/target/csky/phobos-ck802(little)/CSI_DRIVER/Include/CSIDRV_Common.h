/******************************************************************************
 * @file     CSIDRV_UART.h
 * @brief    CSI Header File common Driver definitions
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

#ifndef __CSIDRV_COMMON_H
#define __CSIDRV_COMMON_H

#include <stddef.h>
#include <stdint.h>
#include <stdbool.h>

#define CSKY_DRIVER_VERSION_MAJOR_MINOR(major,minor) (((major) << 8) | (minor))

/**
\brief Driver Version
*/
typedef struct _CSKY_DRIVER_VERSION {
  uint16_t api;                         /* /< API version */
  uint16_t drv;                         /* /< Driver version */
} CSKY_DRIVER_VERSION;

/* General return codes */
#define CSKY_DRIVER_OK                 0 /* /< Operation succeeded */
#define CSKY_DRIVER_ERROR             -1 /* /< Unspecified error */
#define CSKY_DRIVER_ERROR_BUSY        -2 /* /< Driver is busy */
#define CSKY_DRIVER_ERROR_TIMEOUT     -3 /* /< Timeout occurred */
#define CSKY_DRIVER_ERROR_UNSUPPORTED -4 /* /< Operation not supported */
#define CSKY_DRIVER_ERROR_PARAMETER   -5 /* /< Parameter error */
#define CSKY_DRIVER_ERROR_SPECIFIC    -6 /* /< Start of driver specific errors */

/**
\brief General power states
*/
typedef enum _CSKY_POWER_STATE {
  CSKY_POWER_OFF,                        /* /< Power off: no operation possible */
  CSKY_POWER_LOW,                        /* /< Low Power mode: retain state, detect and signal wake-up events */
  CSKY_POWER_FULL                        /* /< Power on: full operation at maximum performance */
} CSKY_POWER_STATE;

#endif /* __CSIDRV_COMMON_H */
