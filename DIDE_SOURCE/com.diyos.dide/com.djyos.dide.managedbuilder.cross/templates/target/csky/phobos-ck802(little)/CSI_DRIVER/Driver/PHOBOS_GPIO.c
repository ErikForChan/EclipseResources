/******************************************************************************
 * @file     PHOBOS_GPIO.c
 * @brief    CSI Source File for GPIO Driver
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
#include "PHOBOS_GPIO.h"


CKStruct_GPIOInfo CK_GPIO_Table[] =
{
  {0,(uint32_t) CSKY_GPIOA_BASE,(uint32_t) CSKY_GPIOA_CONTROL_BASE}
  ,{1,(uint32_t) CSKY_GPIOB_BASE,(uint32_t) CSKY_GPIOB_CONTROL_BASE}
};

/*
 * Choose the software mode or hardware mode for any IO bit.
 * Parameters:
 *   port: use to choose a I/O port among Port A, B, or C.
 *   pins: choose the bits which you want to config.
 *   bSoftware:
 *       '1' -- the corresponding pins are software mode, or, as GPIO.  
 *       '0' -- the corresponding pins are hardware mode, or, used as UART, LCDC *               etc.
 * return: SUCCESS or FAILURE.
 */
int32_t CK_GPIO_SetReuse(
    CKEnum_GPIO_PORT port,
    uint32_t pins,
    bool bSoftware
    )
{
	CKStruct_GPIOInfo  *info;
	PCKStruct_GPIO reg;
    
    /*gain the base address of the gpio register*/
	info = &(CK_GPIO_Table[port]);
	reg = (PCKStruct_GPIO)(info->addr);
	if(bSoftware)
	{
	    reg->PORT_CTL &= (~pins);
	}
	else
	{
	    reg->PORT_CTL |= pins;
	}
    return true;
}

