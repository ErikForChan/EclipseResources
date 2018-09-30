/******************************************************************************
 * @file     PHOBOS_I2C.h
 * @brief    PHOBOS Header File for Inter-Integrated Circuit Driver definitions
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
#ifndef _PHOBOS_I2C_H_
#define _PHOBOS_I2C_H_

#include "CSIDRV_I2C.h"

/* 
 * Define the mode of I2C
 */ 
typedef enum{
  CK_IIC_Slave       = 0,
  CK_IIC_Master      = 1,
  CK_IIC_SlaveMaster = 2
}CKEnum_IIC_Mode;

/* 
 * Define the address mode of I2C
 */ 
typedef enum{
  CK_IIC_Address7bits  = 0,
  CK_IIC_Address10bits = 1
}CKEnum_IIC_AddressMode;

/* 
 * Define the speed of I2C
 */ 
typedef enum{
  CK_IIC_StandardSpeed = 1,
  CK_IIC_FastSpeed     = 2,
  CK_IIC_HighSpeed
}CKEnum_IIC_Speed;

/* 
 * Define the interrupt type of I2C
 */ 
typedef enum{
  CK_IIC_RX_UNDER  = 0,
  CK_IIC_RX_OVER   = 1,
  CK_IIC_RX_FULL   = 2,
  CK_IIC_TX_OVER   = 3,
  CK_IIC_TX_EMPTY  = 4,
  CK_IIC_RD_REQ    = 5,
  CK_IIC_TX_ABRT   = 6,
  CK_IIC_RX_DONE   = 7,
  CK_IIC_ACTIVITY  = 8,
  CK_IIC_STOP_DET  = 9,
  CK_IIC_START_DET = 10,
  CK_IIC_GEN_CALL  = 11
}CKEnum_IIC_InterruptType;

#define  CK_IIC_MIN_SCL_HIGHtime_Standard  1/250000    /* 4000ns */
#define  CK_IIC_MIN_SCL_HIGHtime_Fast      3/5000000   /* 600ns */
#define  CK_IIC_MIN_SCL_LOWtime_Standard   47/10000000 /* 4700ns */
#define  CK_IIC_MIN_SCL_LOWtime_Fast       13/10000000 /* 1300ns */

/* 
 * I2C register bit definitions
 */ 
#define  CK_IIC_DISABLE  0
#define  CK_IIC_ENABLE   1

#define  CK_IIC_EN_DMA_TX    (0x1 << 1)
#define  CK_IIC_EN_DMA_RX     0x1
#define  CK_IIC_DISABLE_DMA   0x0

#define  CK_IIC_RXFIFO_FULL       (0x1 << 4)
#define  CK_IIC_RXFIFO_NOT_EMPTY  (0x1 << 3)
#define  CK_IIC_TXFIFO_EMPTY      (0x1 << 2)
#define  CK_IIC_TXFIFO_NOT_FULL   (0x1 << 1)
#define  CK_IIC_ACTIVITY           0x1

#define  CK_IIC_Tfr   0
#define  CK_IIC_Block 1
#define  CK_IIC_Err   2

#define CK_IIC_FIFO_LV      0x4
#define CK_IIC_FIFO_MAX_LV  0x10


/* 
 * config the i2c
 */ 
typedef CSKY_IIC_TypeDef   CK_IIC_ADDRBASE;
typedef volatile CSKY_IIC_TypeDef*  PCK_IIC_ADDRBASE;

typedef struct CKS_IIC_DataCount_t{
	uint32_t IC_Mastertx_count;        /* master transmit data count */
    uint32_t IC_Masterrx_count;        /* master receive data count */
    uint32_t IC_Slavetx_count;         /* slave transmit data count */
    uint32_t IC_Slaverx_count;         /* slave receive data count */
}CKS_IIC_DataCount, *PCKS_IIC_DataCount;

/* 
 * Define the I2C status
 */ 
typedef struct {
    uint8_t             id;
    CSKY_I2C_SignalEvent_t cb_event;
	CSKY_I2C_STATUS     *i2c_status;
	PCK_IIC_ADDRBASE    i2c_addr;
	PCKS_IIC_DataCount  gp_iic_datacount;
}I2C_RESOURE;
#endif
