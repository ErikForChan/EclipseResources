/******************************************************************************
 * @file     PHOBOS_I2C.c
 * @brief    CSI Source File for Inter-Integrated Circuit Driver
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
#include "PHOBOS_I2C.h"
#include "PHOBOS_GPIO.h"
#include "CSIDRV_I2C.h"

#define CSKY_I2C_DRV_VERSION    CSKY_DRIVER_VERSION_MAJOR_MINOR(1, 0) /* driver version */

/* Driver Version */
static const CSKY_DRIVER_VERSION DriverVersion = {
    CSKY_I2C_API_VERSION,
    CSKY_I2C_DRV_VERSION
};
/* Driver Capabilities */
static const CSKY_I2C_CAPABILITIES DriverCapabilities = {
    0  /* supports 10-bit addressing */
};

static CSKY_I2C_STATUS  I2C0_Status ={
		0,        /* /< Busy flag */
		0,        /* /< Mode: 0=Slave, 1=Master */
		0,        /* /< Direction: 0=Transmitter, 1=Receiver */
		0,        /* /< General Call indication (cleared on start of next Slave operation) */
		0,        /* /< Master lost arbitration (cleared on start of next Master operation) */
		0         /* /< Bus error detected (cleared on start of next Master/Slave operation) */
};

static CKS_IIC_DataCount I2C0_DataCount ={
	0,
	0,
	0,
	0
};
static I2C_RESOURE I2C0_Resource ={
    .id                 = 0,
    .cb_event           = NULL,
	.i2c_status         = &I2C0_Status,
	.i2c_addr           = ((CSKY_IIC_TypeDef *)CSKY_I2C0_BASE),
	.gp_iic_datacount   = &I2C0_DataCount
};

static CSKY_I2C_STATUS  I2C1_Status ={
		0,        /* /< Busy flag */
		0,        /* /< Mode: 0=Slave, 1=Master */
		0,        /* /< Direction: 0=Transmitter, 1=Receiver */
		0,        /* /< General Call indication (cleared on start of next Slave operation) */
		0,        /* /< Master lost arbitration (cleared on start of next Master operation) */
		0         /* /< Bus error detected (cleared on start of next Master/Slave operation) */
};

static CKS_IIC_DataCount I2C1_DataCount ={
	0,
	0,
	0,
	0
};
static I2C_RESOURE I2C1_Resoure ={
    .id                 = 1,
    .cb_event           = NULL,
	.i2c_status         = &I2C1_Status,
	.i2c_addr           = ((volatile CSKY_IIC_TypeDef *)CSKY_I2C1_BASE),
	.gp_iic_datacount   = &I2C1_DataCount
};

static void CK_IIC_WaitReceiveOver(I2C_RESOURE *pi2c);
static void CK_IIC_WaitTransmitOver(I2C_RESOURE *pi2c);

/*****************************************************************************
CK_IIC_Disable: Disable IIC

INPUT: I2C_RESOURE *

RETURN: true or false

*****************************************************************************/
uint8_t CK_IIC_Disable(I2C_RESOURE *pi2c)
{
	uint32_t  temp;
  /* First clear ACTIVITY, then Disable IIC */
  temp = pi2c->i2c_addr->IC_CLR_ACTIVITY;
  pi2c->i2c_addr->IC_ENABLE = CK_IIC_DISABLE;

  return true;
}


/*****************************************************************************
CK_IIC_Enable: Enable IIC

INPUT: I2C_RESOURE *

RETURN: true or false

*****************************************************************************/
int32_t CK_IIC_Enable(I2C_RESOURE *pi2c)
{
  pi2c->i2c_addr->IC_ENABLE = CK_IIC_ENABLE;

  return true;
}




/*****************************************************************************
CK_IIC_WaitReceiveOver: Wait until receive all data(used in query mode)

INPUT: I2C_RESOURE *

RETURN: NULL

*****************************************************************************/
static void CK_IIC_WaitReceiveOver(I2C_RESOURE *pi2c)
{
  while(1)
  {
    if((pi2c->i2c_addr->IC_STATUS & CK_IIC_RXFIFO_NOT_EMPTY) &&
        !(pi2c->i2c_addr->IC_STATUS & CK_IIC_ACTIVITY))
    {
      break;
    }
  }
}



/*****************************************************************************
CK_IIC_WaitTransmitOver: Wait until transmit all data(used in query mode)

INPUT: I2C_RESOURE *

RETURN: NULL


*****************************************************************************/
static void CK_IIC_WaitTransmitOver(I2C_RESOURE *pi2c)
{
  while(1)
  {
    if((pi2c->i2c_addr->IC_STATUS & CK_IIC_TXFIFO_EMPTY) &&
       !(pi2c->i2c_addr->IC_STATUS & CK_IIC_ACTIVITY))
    {
        break;
    }
  }
}



/*****************************************************************************
CK_IIC_DisableRestart: Disable IIC restart

INPUT: I2C_RESOURE

RETURN: NULL

*****************************************************************************/
uint32_t CK_IIC_DisableRestart(I2C_RESOURE *pi2c)
{
  pi2c->i2c_addr->IC_CON &= ~(1 << 5);

  return true;
}

/*****************************************************************************
CK_IIC_SetTargetAddress: Set target address

INPUT: pi2c-I2C_RESOURE address - target address to be set

RETURN: true or false

*****************************************************************************/
static int32_t CK_IIC_SetTargetAddress(I2C_RESOURE *pi2c, uint16_t address)
{
  uint16_t temp;

  temp = pi2c->i2c_addr->IC_TAR;
  temp &= 0xfc00;
  temp |= address;
  pi2c->i2c_addr->IC_TAR = temp;

  return true;
}

/*****************************************************************************
CK_IIC_SetMode: Set work mode for IIC

INPUT: pi2c-I2C_RESOURE mode - Work mode of IIC

RETURN: true or false

*****************************************************************************/
int32_t CK_IIC_SetMode(I2C_RESOURE *pi2c, CKEnum_IIC_Mode mode)
{
  uint16_t temp;

  temp = pi2c->i2c_addr->IC_CON;
  temp &= ~((1 << 6) + 1);
  /* Slave is disabled when bit 6 is set */
  if(mode == CK_IIC_Slave);
  /* Master is enabled when bit 0 is set */
  else if(mode == CK_IIC_Master)
  {
    temp |= (1 + (1 << 6));
  }
  else if(mode == CK_IIC_SlaveMaster)
  {
    temp |= 1;
  }
  pi2c->i2c_addr->IC_CON = temp;

  return true;

}

/*****************************************************************************
SetTransferAddressMode: Configure addressing which type address as a master

INPUT: pi2c-I2C_RESOURE mode - Address mode

RETURN: true or false

*****************************************************************************/
int32_t CK_IIC_SetTransferAddressMode(I2C_RESOURE *pi2c, CKEnum_IIC_AddressMode mode)
{
  uint16_t temp;

  temp = pi2c->i2c_addr->IC_CON;
  temp &= ~(1 << 4);
  temp |= mode << 4;
  pi2c->i2c_addr->IC_CON = temp;

 return true;

}


/*****************************************************************************
SetSlaveAddressMode: Configure supporting which type address as a slave

INPUT: pi2c-I2C_RESOURE mode - Address mode

RETURN: true or false

*****************************************************************************/
int32_t CK_IIC_SetSlaveAddressMode(I2C_RESOURE *pi2c, CKEnum_IIC_AddressMode mode)
{
  uint16_t temp;

  temp = pi2c->i2c_addr->IC_CON;
  temp &= ~(1 << 3);
  temp |= mode << 3;
  pi2c->i2c_addr->IC_CON = temp;

 return true;
}


/*****************************************************************************
SetTransferSpeed: Selects standard speed or fast speed.

INPUT:pi2c-I2C_RESOURE speed - Type of speed

RETURN: true or false

*****************************************************************************/
int32_t CK_IIC_SetTransferSpeed(I2C_RESOURE *pi2c, CKEnum_IIC_Speed speed)
{
  uint16_t temp;
  temp = pi2c->i2c_addr->IC_CON;
  temp &= ~((1 << 1) + (1 << 2));
  temp |= speed << 1;
  pi2c->i2c_addr->IC_CON = temp;

  return true;
}

/*****************************************************************************
CK_IIC_Tx: Using query mode for transmiting data.

INPUT: pi2c-I2C_RESOURE
	   slave - Address of targert
       pTXBuffer - Base address of data to be transmitted in the memory.
       TXLength - Length of data to be transmitted
       xfer_pending - transmit operation pending -- enbale generate  suspensive condition
RETURN: true or false

*****************************************************************************/

uint32_t CK_IIC_Tx(I2C_RESOURE *pi2c, uint16_t slave, uint8_t *pTXBuffer, uint32_t TXLength, bool xfer_pending)
{
  if(TXLength <= 0)
  {
    return false;
  }

  CK_IIC_Disable(pi2c);
  /* Configure target address */
  pi2c->i2c_addr->IC_TAR = slave;
  pi2c->i2c_addr->IC_RX_TL = 0x10; /* Sets receive FIFO threshold */
  pi2c->i2c_addr->IC_TX_TL = 0x10; /* Sets transmit FIFO threshold */
  CK_IIC_Enable(pi2c);

  for(;TXLength > 0;TXLength--)
  {
    while((pi2c->i2c_addr->IC_TXFLR) > 6);    /* transmit fifo exceed threshold */
    pi2c->i2c_addr->IC_DATA_CMD = *pTXBuffer;
    pTXBuffer++;
    pi2c->gp_iic_datacount->IC_Mastertx_count++;
  }
  if(xfer_pending == false)
    CK_IIC_WaitTransmitOver(pi2c);

   CK_IIC_Disable(pi2c);

   return true;
}

/*****************************************************************************
CK_IIC_GetDataCount: get the count value of the transmit.

INPUT: pi2c the pointer to the I2C_RESOURCE.

RETURN: the count value.

*****************************************************************************/
uint32_t CK_IIC_GetDataCount(I2C_RESOURE *pi2c)
{
    return pi2c->gp_iic_datacount->IC_Mastertx_count;
}
/*****************************************************************************
CK_IIC_Rx: Using query mode for Receiving data.

INPUT: pi2c-I2C_RESOURE
	   slave - Address of targert
       pTXBuffer - Base address of data to be received in the memory.
       TXLength - Length of data to be received
       sub_addr - Sub address of slave
       sub_mode - Indicates slave having sub address or not

RETURN: true or false

*****************************************************************************/
uint32_t CK_IIC_Rx(I2C_RESOURE *pi2c, uint16_t slave, uint8_t *pRXBuffer, uint8_t RXLength, bool xfer_pending)
{

  if(RXLength <= 0)
  {
    return false;
  }

  CK_IIC_Disable(pi2c);
  pi2c->i2c_addr->IC_TAR = slave; /* Configure target address */
  pi2c->i2c_addr->IC_RX_TL = 0x10; /* Sets receive FIFO threshold */
  pi2c->i2c_addr->IC_TX_TL = 0x10; /* Sets transmit FIFO threshold */
  CK_IIC_Enable(pi2c);

  pi2c->i2c_addr->IC_DATA_CMD = *pRXBuffer;
  pRXBuffer++;
  RXLength--;
  pi2c->i2c_addr->IC_DATA_CMD = *pRXBuffer;
  pRXBuffer++;
  RXLength--;
  /* Waits till transmitting completes */
  CK_IIC_WaitTransmitOver(pi2c);

  /* Then receives data */
  while(RXLength > 0 )
  {
    pi2c->i2c_addr->IC_DATA_CMD = 1 << 8;
    CK_IIC_WaitReceiveOver(pi2c);
    *pRXBuffer = ((pi2c->i2c_addr->IC_DATA_CMD) & 0xff);
    pRXBuffer++;
    RXLength--;
  }

  CK_IIC_Disable(pi2c);
  return true;
}

/*****************************************************************************
CK_IIC_Stop: Stop IIC

INPUT: pi2c-I2C_RESOURE

RETURN: true of false

*****************************************************************************/
uint32_t CK_IIC_Stop(I2C_RESOURE *pi2c)
{
  CK_IIC_Disable(pi2c);
  return true;
}

/*****************************************************************************
CK_IIC_Init: Initialize IIC

INPUT: pi2c-I2C_RESOURE

RETURN: true of false

*****************************************************************************/
uint32_t CK_IIC_Init(I2C_RESOURE *pi2c)
{

  /* Masks all interrupts */
  pi2c->i2c_addr->IC_INTR_MASK = 0x00;
  pi2c->i2c_addr->IC_CON = 0x23;

  return true;
}

/*****************************************************************************
CK_IIC_Uninit: Uninitialize IIC

INPUT: pi2c-the pointer to the I2C_RESOURE.

RETURN: true of false

*****************************************************************************/
uint8_t CK_IIC_Uninit(I2C_RESOURE *pi2c)
{
  return CK_IIC_Disable(pi2c);
}

/*
 *  I2C Functions
 */
CSKY_DRIVER_VERSION CSKY_I2C_GetVersion(CSKY_DRIVER_I2C *instance)
{
    return DriverVersion;

}

CSKY_I2C_CAPABILITIES CSKY_I2C_GetCapabilities(CSKY_DRIVER_I2C *instance)
{
	return  DriverCapabilities;
}

int32_t CSKY_I2C_Initialize(CSKY_DRIVER_I2C *instance, CSKY_I2C_SignalEvent_t cb_event)
{
	I2C_RESOURE *i2c = instance->priv;
    if(i2c->id == 0) {
        CK_GPIO_SetReuse(CK_GPIO_PORTA, 0x30, CK_GPIO_BEHARDWARE);
    }
    else if(i2c->id == 1) {
        CK_GPIO_SetReuse(CK_GPIO_PORTA, 0xc000, CK_GPIO_BEHARDWARE);
    }
    else {
        return CSKY_DRIVER_ERROR_PARAMETER;
    }
    i2c->cb_event = cb_event;
    CK_IIC_Init(i2c);
    return CSKY_DRIVER_OK;
}

int32_t CSKY_I2C_Uninitialize(CSKY_DRIVER_I2C *instance)
{
	I2C_RESOURE *i2c = instance->priv;
    if(i2c->id == 0) {
        CK_GPIO_SetReuse(CK_GPIO_PORTA, 0x30, CK_GPIO_BESOFTWARE);
    }
    else if(i2c->id == 1) {
        CK_GPIO_SetReuse(CK_GPIO_PORTA, 0xc000, CK_GPIO_BESOFTWARE);
    }
    else {
        return CSKY_DRIVER_ERROR_PARAMETER;
    }
    i2c->cb_event = NULL;
    CK_IIC_Uninit(i2c);
    return CSKY_DRIVER_OK;
}

int32_t CSKY_I2C_PowerControl(CSKY_DRIVER_I2C *instance, CSKY_POWER_STATE state)
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
    return CSKY_DRIVER_OK;
}

/*****************************************************************************
CSKY_I2C_MasterTransmit: i2c start transmit data in master mode

INPUT: addr - Address of targert
           date - Base address of data to be transmitted in the memory.
            num - Length of data to be transmitted
            xfer_pending - transmit operation pending -- enbale generate  suspensive condition

RETURN: execution_status

*****************************************************************************/
int32_t CSKY_I2C_MasterTransmit(CSKY_DRIVER_I2C *instance, uint32_t addr, const uint8_t *data, uint32_t num, bool xfer_pending)
{
	I2C_RESOURE *i2c = instance->priv;
    uint32_t ret;
    if(*data == 1) {
    	data += 1;
    	num -= 1;
    }
	ret = CK_IIC_Tx(i2c, addr, (uint8_t *)data, num, xfer_pending);
    if(ret < 0)
    {
      return CSKY_DRIVER_ERROR_PARAMETER;
    }
    return CSKY_DRIVER_OK;
}

/*****************************************************************************
CSKY_I2C_MasterReceive: i2c  receive data in master mode

INPUT: addr - Address of targert
           date - Base address of data to be received in the memory.
            num - Length of data to be received
            xfer_pending - receive operation pending -- enbale generate  suspensive condition

RETURN: execution_status

*****************************************************************************/
int32_t CSKY_I2C_MasterReceive(CSKY_DRIVER_I2C *instance, uint32_t addr, uint8_t *data, uint32_t num, bool xfer_pending)
{
  int32_t ret;
  I2C_RESOURE *i2c = instance->priv;
  if(*data == 1) {
	  data += 1;
	  num -= 1;
  }
  ret = CK_IIC_Rx(i2c, addr, data, num, xfer_pending);
  if(ret < 0)
  {
    return CSKY_DRIVER_ERROR_PARAMETER;
  }
  return CSKY_DRIVER_OK;

}

int32_t CSKY_I2C_SlaveTransmit(CSKY_DRIVER_I2C *instance, const uint8_t *data, uint32_t num)
{
	return CSKY_DRIVER_OK;
}

int32_t CSKY_I2C_SlaveReceive(CSKY_DRIVER_I2C *instance, uint8_t *data, uint32_t num)
{
	return CSKY_DRIVER_OK;
}

int32_t CSKY_I2C_GetDataCount(CSKY_DRIVER_I2C *instance)
{
	I2C_RESOURE *i2c = instance->priv;
    return CK_IIC_GetDataCount(i2c);
}

int32_t CSKY_I2C_Control(CSKY_DRIVER_I2C *instance, uint32_t control, uint32_t arg)
{
	I2C_RESOURE *i2c = instance->priv;
    switch (control)
    {
    /* set the target address */
    case CSKY_I2C_OWN_ADDRESS:
        CK_IIC_SetTargetAddress(i2c, arg);
        break;

    /* change the speed of i2c */
    case CSKY_I2C_BUS_SPEED:
        switch (arg)
        {
        case CSKY_I2C_BUS_SPEED_STANDARD:;
        case CSKY_I2C_BUS_SPEED_FAST:;
        case CSKY_I2C_BUS_SPEED_FAST_PLUS:;
            CK_IIC_SetTransferSpeed(i2c, arg);
            break;
        default:
            return CSKY_DRIVER_ERROR_UNSUPPORTED;
        }
        break;

    case CSKY_I2C_BUS_CLEAR:
        break;

    case CSKY_I2C_ABORT_TRANSFER:
        CK_IIC_Disable(i2c);
        break;

    default:
        return CSKY_DRIVER_ERROR_UNSUPPORTED;
    }
    return CSKY_DRIVER_OK;
}

CSKY_I2C_STATUS CSKY_I2C_GetStatus(CSKY_DRIVER_I2C *instance)
{
	I2C_RESOURE *i2c = instance->priv;
    return *(i2c->i2c_status);
}

/*  I2C0 Interface */
CSKY_DRIVER_I2C Driver_I2C0 = {
    CSKY_I2C_GetVersion,
    CSKY_I2C_GetCapabilities,
    CSKY_I2C_Initialize,
    CSKY_I2C_Uninitialize,
    CSKY_I2C_PowerControl,
    CSKY_I2C_MasterTransmit,
    CSKY_I2C_MasterReceive,
    CSKY_I2C_SlaveTransmit,
    CSKY_I2C_SlaveReceive,
    CSKY_I2C_GetDataCount,
    CSKY_I2C_Control,
    CSKY_I2C_GetStatus,
    &I2C0_Resource,
};

/*  I2C1 Interface */
CSKY_DRIVER_I2C Driver_I2C1 = {
    CSKY_I2C_GetVersion,
    CSKY_I2C_GetCapabilities,
    CSKY_I2C_Initialize,
    CSKY_I2C_Uninitialize,
    CSKY_I2C_PowerControl,
    CSKY_I2C_MasterTransmit,
    CSKY_I2C_MasterReceive,
    CSKY_I2C_SlaveTransmit,
    CSKY_I2C_SlaveReceive,
    CSKY_I2C_GetDataCount,
    CSKY_I2C_Control,
    CSKY_I2C_GetStatus,
    &I2C1_Resoure,
};
