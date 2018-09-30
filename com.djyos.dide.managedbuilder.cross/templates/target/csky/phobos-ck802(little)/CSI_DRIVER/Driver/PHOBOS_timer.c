/******************************************************************************
 * @file     PHOBOS_timer.c
 * @brief    CSI Source File for timer Driver
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
#include "PHOBOS.h"
#include "PHOBOS_timer.h"
#include "CSIDRV_timer.h"

#define CSKY_TIMER_DRV_VERSION    CSKY_DRIVER_VERSION_MAJOR_MINOR(1, 0) /* driver version */

/* Driver Version */
static const CSKY_DRIVER_VERSION DriverVersion = {
    CSKY_TIMER_API_VERSION,
    CSKY_TIMER_DRV_VERSION
};

/* Driver Capabilities */
static const CSKY_TIMER_CAPABILITIES DriverCapabilities = {
    1  /* supports for two operation modes: free-running and user-defined count*/
};

static CSKY_TIMER_STATUS TIMER0_Status ={
	0,                     /* /< Busy flag */
	0                      /* /< Mode: 0=free-running, 1=user-defined */
};
static TIMER_RESOURE TIMER0_Resoure = {
	.timer_status = &TIMER0_Status,
	.id           = 0,
	.addr         = (PCKPStruct_TIMER)CSKY_TIMERA0_BASE,
	.irq          = TIM0_IRQn,
	.bopened      = false,
	.timeout      = 0,
	.cb_event     = NULL
};
static CSKY_TIMER_STATUS TIMER1_Status ={
	0,                     /* /< Busy flag */
	0                      /* /< Mode: 0=free-running, 1=user-defined */
};
static TIMER_RESOURE TIMER1_Resoure = {
	.timer_status = &TIMER1_Status,
	.id           = 1,
	.addr         = (PCKPStruct_TIMER)CSKY_TIMERA1_BASE,
	.irq          = TIM1_IRQn,
	.bopened      = false,
	.timeout      = 0,
	.cb_event     = NULL
};

/*
 * Make all the timers in the idle state;
 * this function should be called before
 * INTC module working;
 */

void CK_Deactive_TimerModule(TIMER_RESOURE *ptimer)
{
	/* stop the corresponding timer */
	ptimer->addr->TxControl &= ~CK_TIMER_TXCONTROL_ENABLE;
	/* Disable interrupt. */
	ptimer->addr->TxControl |= CK_TIMER_TXCONTROL_INTMASK;
}

/*
 * open the timer, register the interrupt.
 *
 * ptimer: struct of TIMER_RESOURE
 * handler: the interrupt service function of the corresponding timer;
 * bfast: indicate whether the fast interrupt ornot;
 * bopened: indicate the state whether be opened ornot
 */
int32_t CK_Timer_Open (TIMER_RESOURE *ptimer, bool int_mode, uint16_t priority, bool bfast)
{

   if(ptimer->bopened)
   {
	return false;
   }
   /* initialize irq handler */
   if (int_mode == true)
   {
	 NVIC_EnableSIRQ(ptimer->irq);
	 NVIC_EnableIRQ(ptimer->irq);
	 NVIC_SetPriority(ptimer->irq, priority%4);
     /* register timer isr */
   }
   ptimer->bopened = true;
   /* Enable Timer interrupt. */
   ptimer->addr->TxControl &= ~(CK_TIMER_TXCONTROL_INTMASK);
  return true;
}

/*
 * close the timer, free interrupt.
 *
 * ptimer: struct of TIMER_RESOURE
 * bopened: indicate the state whether be opened ornot
 */
int32_t CK_Timer_Close(TIMER_RESOURE *ptimer)
{
    if(!(ptimer->bopened))
    {
	return false;
    }

   /* stop the corresponding timer */
   ptimer->addr->TxControl &= ~CK_TIMER_TXCONTROL_ENABLE;
   /* Disable interrupt. */
   ptimer->addr->TxControl |= CK_TIMER_TXCONTROL_INTMASK;
   /*clear the backcall function*/
   NVIC_DisableIRQ(ptimer->irq);
   ptimer->bopened = false;
   return true;
}



/*
 * start the corresponding timer
 *
 * ptimer: struct of TIMER_RESOURE
 * timeout: the set time (uS)
 */
int32_t CK_Timer_Start(TIMER_RESOURE *ptimer, uint32_t timeout, uint32_t apbfreq)
{
   uint32_t load;
   if(!(ptimer->bopened))
   {
	return false;
   }

   load = (uint32_t)((apbfreq / 1000000) * timeout);
   /*  load time(us)  */
   ptimer->addr->TxLoadCount = load;
   ptimer->timeout = timeout;
   /*in user-defined running mode, if use design ware timer, you can dynamic change the user mode and free mode when you enable the timer*/
   ptimer->addr->TxControl |= CK_TIMER_TXCONTROL_MODE;
   /* enable the corresponding timer */
   ptimer->addr->TxControl |= CK_TIMER_TXCONTROL_ENABLE;
  /*in user-defined running mode*/
   return true;
}

/*
 *  the time mode select
 *
 * ptimer: struct of TIMER_RESOURE
 * stop_val: select mode number
 */
void CK_Timer_ModeSelect(TIMER_RESOURE *ptimer ,CKEnum_Timer_Modeselect mode)
{
   if(!(ptimer->bopened))
   {
	return ;
   }
   switch(mode)
   {
   case TIME_MODE_USER:
	   ptimer->addr->TxControl |= CK_TIMER_TXCONTROL_MODE;
	   break;
   case TIMER_MODE_FREE:
	   ptimer->addr->TxControl &= ~CK_TIMER_TXCONTROL_MODE;
	   break;
   }

}
/*
 * stop a designated timer
 *
 * ptimer: struct of TIMER_RESOURE
 * stop_val: the count value when the timer stops
 */
uint32_t CK_Timer_Stop(TIMER_RESOURE *ptimer)
{
   uint32_t stop_val;

   /* if the timer does not open,return false */
   if(!(ptimer->bopened))
   {
	return false;
   }

   /* disable the timer*/
   stop_val = ptimer->addr->TxCurrentValue;
   ptimer->addr->TxControl &= ~CK_TIMER_TXCONTROL_ENABLE;
   return stop_val;
}

/*
 * clear a timer interrupt
 * ptimer: struct of TIMER_RESOURE
 * by reading its End of Interrupt register(EOI)
 */
void  CK_Timer_ClearIrqFlag(TIMER_RESOURE *ptimer)
{
   *((volatile uint32_t *)(&(ptimer->addr->TxEOI)));
}

/*
 * read the current value of the timer
 *
 * ptimer: struct of TIMER_RESOURE
 */
uint32_t CK_Timer_CurrentValue(TIMER_RESOURE *ptimer)
{
   uint32_t current_val;

   current_val = ptimer->addr->TxCurrentValue;
   return current_val ;
}

/*
 *  timer Functions
 */

CSKY_DRIVER_VERSION CSKY_Timer_GetVersion(CSKY_DRIVER_TIMER *instance)
{
    return DriverVersion;
}

CSKY_TIMER_CAPABILITIES CSKY_Timer_GetCapabilities(CSKY_DRIVER_TIMER *instance)
{
	return DriverCapabilities;
}

int32_t CSKY_Timer_Initialize(CSKY_DRIVER_TIMER *instance, CSKY_TIMER_SignalEvent_t cb_event)
{
	TIMER_RESOURE *ptimer = instance->priv;
    CK_Deactive_TimerModule(ptimer);
    ptimer->cb_event = cb_event;
    return true;
}

int32_t CSKY_Timer_Uninitialize(CSKY_DRIVER_TIMER *instance)
{
	TIMER_RESOURE *ptimer = instance->priv;
    CK_Deactive_TimerModule(ptimer);
    ptimer->cb_event = NULL;
    return true;
}

int32_t CSKY_Timer_PowerControl(CSKY_DRIVER_TIMER *instance, CSKY_POWER_STATE state)
{
    switch (state)
    {
    case CSKY_POWER_FULL:
    case CSKY_POWER_OFF:
        return CSKY_DRIVER_OK;
    case CSKY_POWER_LOW:
    default:
        return CSKY_DRIVER_ERROR_UNSUPPORTED;
    }
}

int32_t CSKY_Timer_Open(CSKY_DRIVER_TIMER *instance, bool int_mode , uint32_t priority, bool bfast)
{
	TIMER_RESOURE *ptimer = instance->priv;
    return CK_Timer_Open(ptimer, int_mode, priority, bfast);
}

int32_t CSKY_Timer_Close(CSKY_DRIVER_TIMER *instance)
{
	TIMER_RESOURE *ptimer = instance->priv;
    return CK_Timer_Close(ptimer);
}

int32_t CSKY_Timer_Start(CSKY_DRIVER_TIMER *instance, uint32_t timeout, uint32_t apbfreq)
{
	TIMER_RESOURE *ptimer = instance->priv;
    return CK_Timer_Start(ptimer, timeout, apbfreq);
}

int32_t CSKY_Timer_Stop(CSKY_DRIVER_TIMER *instance)
{
	TIMER_RESOURE *ptimer = instance->priv;
    return CK_Timer_Stop(ptimer);
}

int32_t CSKY_Timer_GetCurrentValue(CSKY_DRIVER_TIMER *instance)
{
	TIMER_RESOURE *ptimer = instance->priv;
    return CK_Timer_CurrentValue(ptimer);
}

int32_t CSKY_Timer_Control(CSKY_DRIVER_TIMER *instance, uint32_t control, uint32_t arg)
{
	TIMER_RESOURE *ptimer = instance->priv;
    switch (control)
    {
    case CSKY_TIMER_MODE_SELECT:
	  CK_Timer_ModeSelect(ptimer,arg);
      break;
    case CSKY_TIMER_CLEAR_IRQFLAG:
      CK_Timer_ClearIrqFlag(ptimer);
      break;
    default:
      return CSKY_DRIVER_ERROR_UNSUPPORTED;
    }
    return true;
}

CSKY_TIMER_STATUS CSKY_Timer_GetStatus(CSKY_DRIVER_TIMER *instance)
{
	TIMER_RESOURE *ptimer = instance->priv;
	return *(ptimer->timer_status);
}

/* TIMER0 Interface */
CSKY_DRIVER_TIMER Driver_TIMER0 = {
   CSKY_Timer_GetVersion,
   CSKY_Timer_GetCapabilities,
   CSKY_Timer_Initialize,
   CSKY_Timer_Uninitialize,
   CSKY_Timer_PowerControl,
   CSKY_Timer_Open,
   CSKY_Timer_Close,
   CSKY_Timer_Start,
   CSKY_Timer_Stop,
   CSKY_Timer_GetCurrentValue,
   CSKY_Timer_Control,
   CSKY_Timer_GetStatus,
   &TIMER0_Resoure,
};

/* TIMER1 Interface */
CSKY_DRIVER_TIMER Driver_TIMER1 = {
    CSKY_Timer_GetVersion,
    CSKY_Timer_GetCapabilities,
    CSKY_Timer_Initialize,
    CSKY_Timer_Uninitialize,
    CSKY_Timer_PowerControl,
    CSKY_Timer_Open,
    CSKY_Timer_Close,
    CSKY_Timer_Start,
    CSKY_Timer_Stop,
    CSKY_Timer_GetCurrentValue,
    CSKY_Timer_Control,
    CSKY_Timer_GetStatus,
    &TIMER1_Resoure,
};

