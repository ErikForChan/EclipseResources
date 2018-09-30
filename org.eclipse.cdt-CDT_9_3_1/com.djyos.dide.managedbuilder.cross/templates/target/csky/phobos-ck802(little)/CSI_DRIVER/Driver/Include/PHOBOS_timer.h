/******************************************************************************
 * @file     PHOBOS_timer.h
 * @brief    PHOBOS Header File for timer Driver definitions
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
#ifndef __PHOBOS_TIMER_H_
#define __PHOBOS_TIMER_H_

#include "CSIDRV_timer.h"

typedef CSKY_TIMER_TypeDef  CKStruct_TIMER;
typedef CSKY_TIMER_TypeDef* PCKPStruct_TIMER;

typedef CSKY_TIMER_Control_TypeDef CKStruct_TIMER_CON;
typedef CSKY_TIMER_Control_TypeDef* PCKStruct_TIMER_CON;

/* 
 * Define the information of TIMER
 */ 
typedef struct {
	CSKY_TIMER_STATUS *timer_status ;   /* the timer status */
	uint32_t id;                       /* the number of timer */
	PCKPStruct_TIMER  addr;            /* the base-address of timer */
	uint32_t irq;                      /* the interrupt number of timer */
	bool      bopened;                 /* indicate whether have been opened or not */
	uint32_t timeout;                  /* the set time (us) */
	CSKY_TIMER_SignalEvent_t cb_event;
} TIMER_RESOURE;

/*
 *  define the bits for TxControl
 */
#define CK_TIMER_TXCONTROL_ENABLE      (1UL << 0)
#define CK_TIMER_TXCONTROL_MODE        (1UL << 1)
#define CK_TIMER_TXCONTROL_INTMASK     (1UL << 2)

#endif /* PHOBOS_TIMER_H_ */
