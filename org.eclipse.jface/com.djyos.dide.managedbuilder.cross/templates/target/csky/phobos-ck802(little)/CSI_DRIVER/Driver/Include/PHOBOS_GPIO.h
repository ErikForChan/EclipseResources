/******************************************************************************
 * @file     PHOBOS_GPIO.h
 * @brief    PHOBOS Header File for GPIO Driver definitions
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
#ifndef __PHOBOS_GPIO_H__
#define __PHOBOS_GPIO_H__

/* PIO Definitions for one of the Port, and we do the max size possibilaty. */
#define CK_GPIO_P0     ((unsigned int) 1 << 0)  /* Pin Controlled by P0 */
#define CK_GPIO_P1     ((unsigned int) 1 << 1)  /* Pin Controlled by P1 */
#define CK_GPIO_P2     ((unsigned int) 1 << 2)  /* Pin Controlled by P2 */
#define CK_GPIO_P3     ((unsigned int) 1 << 3)  /* Pin Controlled by P3 */
#define CK_GPIO_P4     ((unsigned int) 1 << 4)  /* Pin Controlled by P4 */
#define CK_GPIO_P5     ((unsigned int) 1 << 5)  /* Pin Controlled by P5 */
#define CK_GPIO_P6     ((unsigned int) 1 << 6)  /* Pin Controlled by P6 */
#define CK_GPIO_P7     ((unsigned int) 1 << 7)  /* Pin Controlled by P7 */
#define CK_GPIO_P8     ((unsigned int) 1 << 8)  /* Pin Controlled by P8 */
#define CK_GPIO_P9     ((unsigned int) 1 << 9)  /* Pin Controlled by P9 */
#define CK_GPIO_P10     ((unsigned int) 1 << 10)  /* Pin Controlled by P10 */
#define CK_GPIO_P11     ((unsigned int) 1 << 11)  /* Pin Controlled by P11 */
#define CK_GPIO_P12     ((unsigned int) 1 << 12)  /* Pin Controlled by P12 */
#define CK_GPIO_P13     ((unsigned int) 1 << 13)  /* Pin Controlled by P13 */
#define CK_GPIO_P14     ((unsigned int) 1 << 14)  /* Pin Controlled by P14 */
#define CK_GPIO_P15     ((unsigned int) 1 << 15)  /* Pin Controlled by P15 */
#define CK_GPIO_P16     ((unsigned int) 1 << 16)  /* Pin Controlled by P16 */
#define CK_GPIO_P17     ((unsigned int) 1 << 17)  /* Pin Controlled by P17 */
#define CK_GPIO_P18     ((unsigned int) 1 << 18)  /* Pin Controlled by P18 */
#define CK_GPIO_P19     ((unsigned int) 1 << 19)  /* Pin Controlled by P19 */
#define CK_GPIO_P20     ((unsigned int) 1 << 20)  /* Pin Controlled by P20 */
#define CK_GPIO_P21     ((unsigned int) 1 << 21)  /* Pin Controlled by P21 */



/* 
 * Define IRQBurst type, GPIO Port, and Port's direction. They are all enum 
 * type.
 */ 
typedef enum{
    CK_GPIO_Intact_High_Level,
    CK_GPIO_Intact_Low_Level,
    CK_GPIO_Intact_High_Edge,
    CK_GPIO_Intact_Low_Edge
}CKEnum_GPIO_IRQBurst;

typedef enum{
    CK_GPIO_PORTA,
    CK_GPIO_PORTB,
    CK_GPIO_PORTC
}CKEnum_GPIO_PORT;

/* 
 * Define the direction of the gpio
 */ 
typedef enum{
    CK_GPIO_INPUT,
    CK_GPIO_OUTPUT
}CKEnum_GPIO_Direction;

/* 
 * Define the mode of the gpio
 */ 
typedef enum{
    CK_GPIO_BEHARDWARE,
    CK_GPIO_BESOFTWARE
}CSKYEnum_GPIO_CTL;

/* 
 * Define the infomation of the gpio
 */ 
typedef struct CK_GPIO_Info
{
  uint32_t   id;
  uint32_t   addr;
  uint32_t   controladdr;
}CKStruct_GPIOInfo, *PCKStruct_GPIOInfo;


/* Define the functions in gpio.c. */

/*
 * Choose the software mode or hardware mode for any IO bit.
 * Parameters:
 *   port: use to choose a I/O port among Port A, B, or C.
 *   pins: choose the bits which you want to config.
 *   bSoftware:
 *       '1' -- the corresponding pins are software mode, or, as GPIO.  
 *       '0' -- the corresponding pins are hardware mode, or, used as UART, LCDC *   etc.
 * return: true or false.
 */
int32_t CK_GPIO_SetReuse(
	CKEnum_GPIO_PORT port,
    uint32_t pins,
    bool bSoftware
);
#endif
