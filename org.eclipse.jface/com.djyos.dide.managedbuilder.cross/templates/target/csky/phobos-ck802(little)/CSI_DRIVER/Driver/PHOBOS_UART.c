/******************************************************************************
 * @file     PHOBOS_UART.c
 * @brief    CSI Source File for Universal Asynchronous Receiver
 *           Transmitter Driver
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
#include "CSIDRV_UART.h"
#include "PHOBOS.h"
#include "PHOBOS_UART.h"
#include "PHOBOS_GPIO.h"

#define CSKY_UART_DRV_VERSION    CSKY_DRIVER_VERSION_MAJOR_MINOR(1, 0)  /* driver version */

/* Driver Version */
static const CSKY_DRIVER_VERSION DriverVersion = { 
    CSKY_UART_API_VERSION,
    CSKY_UART_DRV_VERSION
};

/* Driver Capabilities */
static UART_RESOURCES UART0_Resources = {
  {     
		    1, /* supports UART (Asynchronous) mode */
		    0, /* supports Synchronous Master mode */
		    0, /* supports Synchronous Slave mode */
		    0, /* supports UART Single-wire mode */
		    0, /* supports UART IrDA mode */
		    0, /* supports UART Smart Card mode */
		    0, /* Smart Card Clock generator available */
		    0, /* RTS Flow Control available */
		    0, /* CTS Flow Control available */
		    0, /* Transmit completed event: \ref CSKY_UART_EVENT_TX_COMPLETE */
		    0, /* Signal receive character timeout event: \ref CSKY_UART_EVENT_RX_TIMEOUT */
		    0, /* RTS Line: 0=not available, 1=available */
		    0, /* CTS Line: 0=not available, 1=available */
		    0, /* DTR Line: 0=not available, 1=available */
		    0, /* DSR Line: 0=not available, 1=available */
		    0, /* DCD Line: 0=not available, 1=available */
		    0, /* RI Line: 0=not available, 1=available */
		    0, /* Signal CTS change event: \ref CSKY_UART_EVENT_CTS */
		    0, /* Signal DSR change event: \ref CSKY_UART_EVENT_DSR */
		    0, /* Signal DCD change event: \ref CSKY_UART_EVENT_DCD */
		    0  /* Signal RI change event: \ref CSKY_UART_EVENT_RI */
  },
  .id = 0,
  .addr = CSKY_UART0_BASE,
  .irq_num = UART0_IRQn,
  .priority = 0,
  .bopened = 0,
  .cb_event = NULL,
  .btxquery = true,
  .brxquery = true
};

static UART_RESOURCES UART1_Resources = {
  {     /* Capabilities */
		    1, /* supports UART (Asynchronous) mode */
		    0, /* supports Synchronous Master mode */
		    0, /* supports Synchronous Slave mode */
		    0, /* supports UART Single-wire mode */
		    0, /* supports UART IrDA mode */
		    0, /* supports UART Smart Card mode */
		    0, /* Smart Card Clock generator available */
		    0, /* RTS Flow Control available */
		    0, /* CTS Flow Control available */
		    0, /* Transmit completed event: \ref CSKY_UART_EVENT_TX_COMPLETE */
		    0, /* Signal receive character timeout event: \ref CSKY_UART_EVENT_RX_TIMEOUT */
		    0, /* RTS Line: 0=not available, 1=available */
		    0, /* CTS Line: 0=not available, 1=available */
		    0, /* DTR Line: 0=not available, 1=available */
		    0, /* DSR Line: 0=not available, 1=available */
		    0, /* DCD Line: 0=not available, 1=available */
		    0, /* RI Line: 0=not available, 1=available */
		    0, /* Signal CTS change event: \ref CSKY_UART_EVENT_CTS */
		    0, /* Signal DSR change event: \ref CSKY_UART_EVENT_DSR */
		    0, /* Signal DCD change event: \ref CSKY_UART_EVENT_DCD */
		    0  /* Signal RI change event: \ref CSKY_UART_EVENT_RI */
  },
  .id = 1,
  .addr = CSKY_UART1_BASE,
  .irq_num = UART1_IRQn,
  .priority = 0,
  .bopened = 0,
  .cb_event = NULL,
  .btxquery = true,
  .brxquery = true,
};

static UART_RESOURCES UART2_Resources = {
  {     /* Capabilities */
		    1, /* supports UART (Asynchronous) mode */
		    0, /* supports Synchronous Master mode */
		    0, /* supports Synchronous Slave mode */
		    0, /* supports UART Single-wire mode */
		    0, /* supports UART IrDA mode */
		    0, /* supports UART Smart Card mode */
		    0, /* Smart Card Clock generator available */
		    0, /* RTS Flow Control available */
		    0, /* CTS Flow Control available */
		    0, /* Transmit completed event: \ref CSKY_UART_EVENT_TX_COMPLETE */
		    0, /* Signal receive character timeout event: \ref CSKY_UART_EVENT_RX_TIMEOUT */
		    0, /* RTS Line: 0=not available, 1=available */
		    0, /* CTS Line: 0=not available, 1=available */
		    0, /* DTR Line: 0=not available, 1=available */
		    0, /* DSR Line: 0=not available, 1=available */
		    0, /* DCD Line: 0=not available, 1=available */
		    0, /* RI Line: 0=not available, 1=available */
		    0, /* Signal CTS change event: \ref CSKY_UART_EVENT_CTS */
		    0, /* Signal DSR change event: \ref CSKY_UART_EVENT_DSR */
		    0, /* Signal DCD change event: \ref CSKY_UART_EVENT_DCD */
		    0  /* Signal RI change event: \ref CSKY_UART_EVENT_RI */
  },
  .id = 2,
  .addr = CSKY_UART2_BASE,
  .irq_num = UART2_IRQn,
  .priority = 0,
  .bopened = 0,
  .cb_event = NULL,
  .btxquery = true,
  .brxquery = true
};

static UART_RESOURCES UART3_Resources = {
  {     /* Capabilities */
		    1, /* supports UART (Asynchronous) mode */
		    0, /* supports Synchronous Master mode */
		    0, /* supports Synchronous Slave mode */
		    0, /* supports UART Single-wire mode */
		    0, /* supports UART IrDA mode */
		    0, /* supports UART Smart Card mode */
		    0, /* Smart Card Clock generator available */
		    0, /* RTS Flow Control available */
		    0, /* CTS Flow Control available */
		    0, /* Transmit completed event: \ref CSKY_UART_EVENT_TX_COMPLETE */
		    0, /* Signal receive character timeout event: \ref CSKY_UART_EVENT_RX_TIMEOUT */
		    0, /* RTS Line: 0=not available, 1=available */
		    0, /* CTS Line: 0=not available, 1=available */
		    0, /* DTR Line: 0=not available, 1=available */
		    0, /* DSR Line: 0=not available, 1=available */
		    0, /* DCD Line: 0=not available, 1=available */
		    0, /* RI Line: 0=not available, 1=available */
		    0, /* Signal CTS change event: \ref CSKY_UART_EVENT_CTS */
		    0, /* Signal DSR change event: \ref CSKY_UART_EVENT_DSR */
		    0, /* Signal DCD change event: \ref CSKY_UART_EVENT_DCD */
		    0  /* Signal RI change event: \ref CSKY_UART_EVENT_RI */
  },
  .id = 3,
  .addr = CSKY_UART3_BASE,
  .irq_num = UART3_IRQn,
  .priority = 0,
  .bopened = 0,
  .cb_event = NULL,
  .btxquery = true,
  .brxquery = true
};

/*
 *   Functions
 */

/*
 * This function is used to change the bautrate of uart.
 * Parameters:
 * uart-- the pointer to the UART_RESOURCES.
 * baudrate--the baudrate that user typed in.
 * apbfreq--the frequence of the apb.
 * return: true or false 
*/

uint32_t CK_Uart_ChangeBaudrate(
  UART_RESOURCES *uart,
  CK_Uart_Baudrate baudrate,
  uint32_t apbfreq
)
{
  int32_t divisor;
  int32_t timecount;
  CSKY_UART_TypeDef *addr;

  addr = (CSKY_UART_TypeDef *)uart->addr;
  
  timecount=0; 	
  /*the baudrates that uart surported as follows:*/  
  if((baudrate == B4800) || (baudrate == B9600) ||
     (baudrate == B14400) || (baudrate == B19200) ||
     (baudrate == B38400) || (baudrate == B56000) ||
     (baudrate == B57600) || (baudrate == B115200))           
  {
    /*
     * DLH and DLL may be accessed when the UART is not 
     * busy(USR[0]=0) and the DLAB bit(LCR[7]) is set.
     */
    while((addr->USR & USR_UART_BUSY)
  	  && (timecount < UART_BUSY_TIMEOUT))
    {
      timecount++;
    }
    if(timecount >= UART_BUSY_TIMEOUT)
    {
      return CSKY_DRIVER_ERROR_TIMEOUT;
    }
    else
    {
      /*baudrate=(seriak clock freq)/(16*divisor).*/
      divisor = ((apbfreq / baudrate) >> 4);
      addr->LCR |= LCR_SET_DLAB;
      /* DLL and DLH is lower 8-bits and higher 8-bits of divisor.*/
      addr->R1.DLL = divisor & 0xff;
      addr->R2.DLH = (divisor >> 8) & 0xff;
      /* 
       * The DLAB must be cleared after the baudrate is setted
       * to access other registers. 
       */    
      addr->LCR &= (~LCR_SET_DLAB);
      uart->baudrate = baudrate;
      return true;
      }
    }
    return false;
}

/*
 * This function is used to enable or disable parity, also to set ODD or EVEN
 * parity.
 * Parameters:
 *   uart-- the pointer to the UART_RESOURCES.
 *   parity--ODD=8, EVEN=16, or NONE=0.
 * return: SUCCESS or FAILURE
*/

uint32_t CK_Uart_SetParity(
  UART_RESOURCES *uart,
  CK_Uart_Parity parity
)
{
	
  int32_t timecount;
  CSKY_UART_TypeDef *addr;

  addr = (CSKY_UART_TypeDef *)uart->addr;
  timecount = 0;
  /* PEN bit(LCR[3]) is writeable when the UART is not busy(USR[0]=0).*/
  while((addr->USR & USR_UART_BUSY) &&
        (timecount < UART_BUSY_TIMEOUT))
  {
    timecount++;
  }
  if(timecount >= UART_BUSY_TIMEOUT)
  {
      return CSKY_DRIVER_ERROR_TIMEOUT;
  }
  else
  {
    switch(parity)
    {
      case NONE:     
	        /*CLear the PEN bit(LCR[3]) to disable parity.*/
	        addr->LCR &= (~LCR_PARITY_ENABLE);
	  	break;
					
      case ODD:
		/* Set PEN and clear EPS(LCR[4]) to set the ODD parity. */  
		addr->LCR |= LCR_PARITY_ENABLE;
		addr->LCR &= LCR_PARITY_ODD;
	  	break;
				  
      case EVEN:
		/* Set PEN and EPS(LCR[4]) to set the EVEN parity.*/
		addr->LCR |= LCR_PARITY_ENABLE;
		addr->LCR |= LCR_PARITY_EVEN;
		break;
				             
      default:
	        return false;
		break;				 
    }
    uart->parity = parity;
    return true;
  }
}
	
	
/*
 * We can call this function to set the stop bit--1 bit, 1.5 bits, or 2 bits.
 * But that it's 1.5 bits or 2, is decided by the wordlenth. When it's 5 bits,
 * there are 1.5 stop bits, else 2.
 * Parameters:
 *   uart-- the pointer to the UART_RESOURCES.
 *	 stopbit--it has two possible value: STOP_BIT_1 and STOP_BIT_2.
 * return: SUCCESS or FAILURE
*/

uint32_t CK_Uart_SetStopBit(
  UART_RESOURCES *uart,
  CK_Uart_StopBit stopbit
)
{
	
  int32_t timecount;
  CSKY_UART_TypeDef *addr;

  addr = (CSKY_UART_TypeDef *)uart->addr;
  timecount = 0;
  /* PEN bit(LCR[3]) is writeable when the UART is not busy(USR[0]=0).*/
  while((addr->USR & USR_UART_BUSY) &&
        (timecount < UART_BUSY_TIMEOUT))
  {
    timecount++;
  }
  if(timecount >= UART_BUSY_TIMEOUT)
  {
      return CSKY_DRIVER_ERROR_TIMEOUT;
  }
  else
  {
    switch(stopbit)
    {
      case LCR_STOP_BIT_1:
        /* Clear the STOP bit to set 1 stop bit*/
        addr->LCR &= LCR_STOP_BIT1;
        break;
				
      case LCR_STOP_BIT_2:
        /* 
         * If the STOP bit is set "1",we'd gotten 1.5 stop  
         * bits when DLS(LCR[1:0]) is zero, else 2 stop bits.
         */
         addr->LCR |= LCR_STOP_BIT2;
         break;
				
      default:
	return false;
        break;
					
    }
  }
  uart->stopbit = stopbit;
  return true;
}
	
	
/*
 * We can use this function to reset the transmit data length,and we
 * have four choices:5, 6, 7, and 8 bits.
 * Parameters:
 *  uart-- the pointer to the UART_RESOURCES.
 * 	wordsize--the data length that user decides
 * return: SUCCESS or FAILURE
*/

uint32_t CK_Uart_SetWordSize(
  UART_RESOURCES *uart,
  CK_Uart_WordSize wordsize)
{
  int timecount=0;
  CSKY_UART_TypeDef *addr;

  addr = (CSKY_UART_TypeDef *)uart->addr;

  /* DLS(LCR[1:0]) is writeable when the UART is not busy(USR[0]=0).*/
  while((addr->USR & USR_UART_BUSY) &&
        (timecount < UART_BUSY_TIMEOUT))
  {
    timecount++;
  }
  if(timecount >= UART_BUSY_TIMEOUT)
  {
      return CSKY_DRIVER_ERROR_TIMEOUT;
  }
  else
  {
		
    /* The word size decides by the DLS bits(LCR[1:0]), and the 
     * corresponding relationship between them is:
     *    DLS   word size
     *	  00 -- 5 bits
     *		01 -- 6 bits
     *		10 -- 7 bits
     *		11 -- 8 bits
     */
    timecount = 0;
    switch(wordsize)
    {
      case WORD_SIZE_5:
        addr->LCR &= LCR_WORD_SIZE_5;
        break;
				
      case WORD_SIZE_6:
        addr->LCR &= 0xfd;
        addr->LCR |= LCR_WORD_SIZE_6;
        break;
				
      case WORD_SIZE_7:
        addr->LCR &= 0xfe;
        addr->LCR |= LCR_WORD_SIZE_7;
        break;
               				
      case WORD_SIZE_8:
        addr->LCR |= LCR_WORD_SIZE_8;
        break;
               				
      default:
        break;
    }			
  }
  uart->word = wordsize;
  return true;
}
	
	
/*
 * This function is used to set the transmit mode, interrupt mode or
 * query mode.
 * Parameters:
 *  uart-- the pointer to the UART_RESOURCES.
 *  bQuery--it indicates the transmit mode: TRUE - query mode, FALSE - 
 *  inerrupt mode
 * return: SUCCESS or FAILURE
*/

uint32_t CK_Uart_SetTXMode(UART_RESOURCES *uart, bool bQuery)
{
	
  int32_t timecount;
  CSKY_UART_TypeDef *addr;

  addr = (CSKY_UART_TypeDef *)uart->addr;
  timecount = 0;
  while((addr->USR & USR_UART_BUSY) &&
        (timecount < UART_BUSY_TIMEOUT))
  {
    timecount++;
  }
  if(timecount >= UART_BUSY_TIMEOUT)
  {
      return CSKY_DRIVER_ERROR_TIMEOUT;
  }
  else
  {
    if(bQuery)
    {
      /* When query mode, disable the Transmit Holding Register Empty
       * Interrupt. To do this, we clear the ETBEI bit(IER[1]).
       */
      addr->R2.IER &= (~IER_THRE_INT_ENABLE);
      /* Refresh the uart uart: transmit mode - query.*/
      uart->btxquery = true;
    }
    else
    {
      /* Refresh the uart uart: transmit mode - interrupt.*/
      uart->btxquery = false;
    }
  }
  return true;
}
	
	
/*
 * This function is used to set the receive mode, interrupt mode or
 * query mode.
 * Parameters:
 *  uart-- the pointer to the UART_RESOURCES.
 *  bQuery--it indicates the receive mode: TRUE - query mode, FALSE - 
 *  interrupt mode
 * return: SUCCESS or FAILURE

*/
uint32_t CK_Uart_SetRXMode(UART_RESOURCES *uart , bool  bQuery)
{
	
  int32_t timecount;
  timecount = 0;
  CSKY_UART_TypeDef *addr;

  addr = (CSKY_UART_TypeDef *)uart->addr;
  /* PEN bit(LCR[3]) is writeable when the UART is not busy(USR[0]=0).*/
  while((addr->USR & USR_UART_BUSY) &&
        (timecount < UART_BUSY_TIMEOUT))
  {
    timecount++;
  }
  if(timecount >= UART_BUSY_TIMEOUT)
  {
      return CSKY_DRIVER_ERROR_TIMEOUT;
  }
  else
  {
    if(bQuery)
    {
       /* When query mode, disable the Received Data Available 
       * Interrupt. To do this, we clear the ERBFI bit(IER[0]).
       */
      addr->R2.IER &= (~IER_RDA_INT_ENABLE);
      /* Refresh the uart uart: receive mode - query.*/
      uart->brxquery = true;
    }
    else
    {
      /* When interrupt mode, inable the Received Data Available 
       * Interrupt. To do this, we set the ERBFI bit(IER[0]).
       */
      addr->R2.IER |= IER_RDA_INT_ENABLE;
      /* Refresh the uart uart: receive mode - interrupt.*/
      uart->brxquery = false;
    }
  }
  return true;
}

/*
 * This function is the interrupt service function of uart
 * Parameters:
 *  uart-- the pointer to the UART_RESOURCES.
 * Return: NULL.
 */
static void UART_IRQHandler(UART_RESOURCES *uart)
{
  uint8_t int_state;
  CSKY_UART_TypeDef *addr;

  addr = (CSKY_UART_TypeDef *)uart->addr;

  int_state = addr->IIR & 0xf;
  if((int_state == 0x2) && !(uart->btxquery))
  {
	  while ((!(addr->LSR & CK_LSR_TRANS_EMPTY)));
	  if(*(uart->xfer.tx_buf) == '\n') {
		  addr->R1.THR = '\r';
		  uart->xfer.tx_cnt++;
		  while ((!(addr->LSR & CK_LSR_TRANS_EMPTY)));
		  addr->R1.THR = *(uart->xfer.tx_buf);
	  }else {
		  addr->R1.THR = *(uart->xfer.tx_buf);
		  (uart->xfer.tx_buf)++;
		  uart->xfer.tx_cnt++;

	  }
	  if(uart->xfer.tx_cnt == (uart->xfer.tx_num + 1)) {
	      addr->R2.IER &= (~IER_THRE_INT_ENABLE);
	  }
  }
  if((int_state == 0x4) && !(uart->brxquery))
  {
	  *(uart->xfer.rx_buf) = addr->R1.RBR;
	  uart->xfer.rx_cnt++;
  }
}

/* This function is used to get character,in query mode.
 * Parameters:
 *  uart-- the pointer to the UART_RESOURCES.
 *  ch --  the pointer to the recieve charater.
 * return: true or false
 */
int32_t CK_Uart_GetChar(UART_RESOURCES *uart, uint8_t *ch)
{
    CSKY_UART_TypeDef *addr;

    addr = (CSKY_UART_TypeDef *)uart->addr;

	while (!(addr->LSR & LSR_DATA_READY));

	*ch = addr->R1.RBR;
    uart->xfer.rx_cnt++;
	return true;

}

/* This function is used to transmit character,in query mode.
 * Parameters:
 *   uart-- the pointer to the UART_RESOURCES.
 *   ch  -- the input charater.
 * Return: true or false.
 */
int32_t CK_Uart_PutChar(UART_RESOURCES *uart, uint8_t ch)
{
    CSKY_UART_TypeDef *addr;
    addr = (CSKY_UART_TypeDef *)uart->addr;

    while ((!(addr->LSR & CK_LSR_TRANS_EMPTY)));
    if(ch == '\n')
    {
      addr->R1.THR = '\r';
      while ((!(addr->LSR & CK_LSR_TRANS_EMPTY)));
    }

    addr->R1.THR = ch;
    uart->xfer.tx_cnt++;

    return true;

}

CSKY_DRIVER_VERSION CSKY_UART_GetVersion(CSKY_DRIVER_UART *instance)
{
    return DriverVersion; 
}

CSKY_UART_CAPABILITIES CSKY_UART_GetCapabilities(CSKY_DRIVER_UART *instance)
{
	UART_RESOURCES *uart = instance->priv;
    return uart->capabilities;
}

int32_t CSKY_UART_Initialize(CSKY_DRIVER_UART *instance, CSKY_UART_SignalEvent_t cb_event)
{
	CSKY_UART_TypeDef *addr;
	UART_RESOURCES *uart = instance->priv;
	addr = (CSKY_UART_TypeDef *)uart->addr;

    /* set the the hardware mode of uart*/
    if(uart->id == 0) {
        CK_GPIO_SetReuse(CK_GPIO_PORTA, 0x3, CK_GPIO_BEHARDWARE);
    }
    else if(uart->id == 1) {
        CK_GPIO_SetReuse(CK_GPIO_PORTA, 0xc00, CK_GPIO_BEHARDWARE);
    }
    else if(uart->id == 2) {
        CK_GPIO_SetReuse(CK_GPIO_PORTA, 0xc00000, CK_GPIO_BEHARDWARE);
    }
    else if(uart->id == 3) {
        CK_GPIO_SetReuse(CK_GPIO_PORTA, 0xc000000, CK_GPIO_BEHARDWARE);
    }
    else {
        return CSKY_DRIVER_ERROR_PARAMETER;
    }
    
    /* set the transfer mode */
    if(cb_event) {
        CK_Uart_SetRXMode(uart, false);
        CK_Uart_SetTXMode(uart, false);
    }
    uart->cb_event = cb_event;
    return CSKY_DRIVER_OK;
}

int32_t CSKY_UART_Uninitialize(CSKY_DRIVER_UART *instance)
{
	UART_RESOURCES *uart = instance->priv;
    /* set the the software mode of uart*/
    if(uart->id == 0) {
        CK_GPIO_SetReuse(CK_GPIO_PORTA, 0x3, CK_GPIO_BESOFTWARE);
    }
    else if(uart->id == 1) {
        CK_GPIO_SetReuse(CK_GPIO_PORTA, 0xc00, CK_GPIO_BESOFTWARE);
    }
    else if(uart->id == 2) {
        CK_GPIO_SetReuse(CK_GPIO_PORTA, 0xc00000, CK_GPIO_BESOFTWARE);
    }
    else if(uart->id == 3) {
        CK_GPIO_SetReuse(CK_GPIO_PORTA, 0xc000000, CK_GPIO_BESOFTWARE);
    }
    else {
        return CSKY_DRIVER_ERROR_PARAMETER;
    }
    uart->cb_event = NULL;

    return CSKY_DRIVER_OK;
}

int32_t CSKY_UART_PowerControl(CSKY_DRIVER_UART *instance, CSKY_POWER_STATE state)
{
	UART_RESOURCES *uart = instance->priv;
    switch (state)
    {
    /* disable the irq of the uart*/
    case CSKY_POWER_OFF:
        NVIC_DisableIRQ(uart->irq_num);
        uart->bopened = 0;
        break;

    case CSKY_POWER_LOW:
        return CSKY_DRIVER_ERROR_UNSUPPORTED;

    /* enable the irq of the uart*/
    case CSKY_POWER_FULL:
        NVIC_EnableIRQ(uart->irq_num);
        NVIC_EnableSIRQ(uart->irq_num);
        uart->bopened = true;
        break;

    default:
        return CSKY_DRIVER_ERROR_UNSUPPORTED;
    }
    return CSKY_DRIVER_OK;
}

int32_t CSKY_UART_Send(CSKY_DRIVER_UART *instance, const void *data, uint32_t num)
{
	UART_RESOURCES *uart = instance->priv;
	CSKY_UART_TypeDef *addr;
	uint8_t *source = NULL;

    /* get the baseaddr of the uart*/
	addr = (CSKY_UART_TypeDef *)uart->addr;
	source = (uint8_t *)data;
    if(data == NULL || num == 0 || uart == NULL) {
        return CSKY_DRIVER_ERROR_PARAMETER;
    }
    if(uart->bopened == 0) {
        return CSKY_DRIVER_ERROR;
    }
    uart->xfer.tx_buf = (uint8_t *)data;
    uart->xfer.tx_num = num;
    uart->xfer.tx_cnt = 0;
    /* check the data is transmit done*/
    while(uart->xfer.tx_cnt != (uart->xfer.tx_num + 1)) {
    	if(uart->btxquery) {
              CK_Uart_PutChar(uart, *source);
              source++;
    	}
        else{
        	if(uart->xfer.tx_cnt == 0) {
            /* disable the interrupt*/
        	    addr->R2.IER |= IER_THRE_INT_ENABLE;
            }
        }
    }
    return CSKY_DRIVER_OK;
}

int32_t CSKY_UART_Receive(CSKY_DRIVER_UART *instance, void *data, uint32_t num)
{
	UART_RESOURCES *uart = instance->priv;
	uint8_t *dest = NULL;

	dest = (uint8_t *)data;
    if ((data == NULL) || (num == 0)) {
      /* Invalid parameters */
      return CSKY_DRIVER_ERROR_PARAMETER;
    }

    if (uart->bopened == 0) {
      /* UART is not configured (mode not selected) */
      return CSKY_DRIVER_ERROR;
    }

    /* Save number of data to be received */
    uart->xfer.rx_num = num;

    /* Save receive buffer uart */
    uart->xfer.rx_buf = (uint8_t *)data;
    uart->xfer.rx_cnt = 0;
    /* check the recieve count is fit the user demand */
    while(uart->xfer.rx_cnt != uart->xfer.rx_num) {
    	if(uart->brxquery) {
            if (CK_Uart_GetChar(uart, dest) == 0) {
        	    return -1;
            }
            dest++;
    	}
    }
    return CSKY_DRIVER_OK;
}

int32_t CSKY_UART_Transfer(CSKY_DRIVER_UART *instance, void *data_out, void *data_in, uint32_t num)
{
	return true;
}

uint32_t CSKY_UART_GetTxCount(CSKY_DRIVER_UART *instance)
{
	UART_RESOURCES *uart = instance->priv;
    if(uart->bopened == 0) {
        return CSKY_DRIVER_ERROR;
    }
    return uart->xfer.tx_cnt;
}

uint32_t CSKY_UART_GetRxCount(CSKY_DRIVER_UART *instance)
{
	UART_RESOURCES *uart = instance->priv;
    if(uart->bopened == 0) {
        return CSKY_DRIVER_ERROR;
    }
    return uart->xfer.rx_cnt;
}

int32_t CSKY_UART_Control(CSKY_DRIVER_UART *instance, uint32_t control, uint32_t arg)
{
	UART_RESOURCES *uart = instance->priv;
    CK_Uart_Parity parity = NONE;
    CK_Uart_StopBit stopbit;
    CK_Uart_WordSize wordsize;
    uint8_t mode;
    /* control the mode of the uart*/
    switch(control & CSKY_UART_CONTROL_Msk){
    case CSKY_UART_MODE_ASYNCHRONOUS:
        mode = CSKY_UART_MODE_ASYNCHRONOUS;
        break;
    default:
        return CSKY_DRIVER_ERROR_UNSUPPORTED;
    }
    /* control the data_bit of the uart*/
    switch(control & CSKY_UART_DATA_BITS_Msk) {
    case CSKY_UART_DATA_BITS_5:
        wordsize = WORD_SIZE_5;
        break;
    case CSKY_UART_DATA_BITS_6:
        wordsize = WORD_SIZE_6;
        break;
    case CSKY_UART_DATA_BITS_7:
        wordsize = WORD_SIZE_7;
        break;
    case CSKY_UART_DATA_BITS_8:
        wordsize = WORD_SIZE_8;
        break;
    default:
        return CSKY_DRIVER_ERROR_UNSUPPORTED;
    }
    /* control the parity of the uart*/
    switch(control & CSKY_UART_PARITY_Msk) {
    case CSKY_UART_PARITY_NONE:
        parity = NONE;
        break;
    case CSKY_UART_PARITY_EVEN:
        parity = EVEN;
        break;
    case CSKY_UART_PARITY_ODD:
        parity = ODD;
        break;
    }
    /* control the stopbit of the uart*/
    switch(control & CSKY_UART_STOP_BITS_Msk) {
    case CSKY_UART_STOP_BITS_1:
        stopbit = LCR_STOP_BIT_1;
        break;
    case CSKY_UART_STOP_BITS_2:
        stopbit = LCR_STOP_BIT_2;
        break;
    default:
        return CSKY_DRIVER_ERROR_UNSUPPORTED;
    }
    /* set the para to the register of uart*/
    CK_Uart_SetParity(uart, parity);
    CK_Uart_SetStopBit(uart, stopbit);
    CK_Uart_SetWordSize(uart, wordsize);
    CK_Uart_ChangeBaudrate(uart, arg, SYSTEM_CLOCK);
    return CSKY_DRIVER_OK;
}

CSKY_UART_STATUS CSKY_UART_GetStatus(CSKY_DRIVER_UART *instance)
{
	CSKY_UART_STATUS uart_status = {0};
	return uart_status;
}

int32_t  CSKY_UART_SetModemControl (CSKY_DRIVER_UART *instance, CSKY_UART_MODEM_CONTROL control)
{
	return 0;
}

CSKY_UART_MODEM_STATUS CSKY_UART_Get_ModemStatus  (CSKY_DRIVER_UART *instance)
{
	CSKY_UART_MODEM_STATUS uart_modemstatus = {0};
	return uart_modemstatus;
}

__attribute__((isr)) void UART0_IRQHandler (void) {
  UART_IRQHandler (&UART0_Resources);
}

CSKY_DRIVER_UART Driver_UART0 = {
    CSKY_UART_GetVersion,
    CSKY_UART_GetCapabilities,
    CSKY_UART_Initialize,
    CSKY_UART_Uninitialize,
    CSKY_UART_PowerControl,
    CSKY_UART_Send,
    CSKY_UART_Receive,
    CSKY_UART_Transfer,
    CSKY_UART_GetTxCount,
    CSKY_UART_GetRxCount,
    CSKY_UART_Control,
    CSKY_UART_GetStatus,
    CSKY_UART_SetModemControl,
    CSKY_UART_Get_ModemStatus,
    &UART0_Resources,
};

__attribute__((isr)) void UART1_IRQHandler (void) {
  UART_IRQHandler (&UART1_Resources);
}

CSKY_DRIVER_UART Driver_UART1 = {
    CSKY_UART_GetVersion,
    CSKY_UART_GetCapabilities,
    CSKY_UART_Initialize,
    CSKY_UART_Uninitialize,
    CSKY_UART_PowerControl,
    CSKY_UART_Send,
    CSKY_UART_Receive,
    CSKY_UART_Transfer,
    CSKY_UART_GetTxCount,
    CSKY_UART_GetRxCount,
    CSKY_UART_Control,
    CSKY_UART_GetStatus,
    CSKY_UART_SetModemControl,
    CSKY_UART_Get_ModemStatus,
    &UART1_Resources,
};

__attribute__((isr)) void UART2_IRQHandler (void) {
  UART_IRQHandler (&UART2_Resources);
}

CSKY_DRIVER_UART Driver_UART2 = {
    CSKY_UART_GetVersion,
    CSKY_UART_GetCapabilities,
    CSKY_UART_Initialize,
    CSKY_UART_Uninitialize,
    CSKY_UART_PowerControl,
    CSKY_UART_Send,
    CSKY_UART_Receive,
    CSKY_UART_Transfer,
    CSKY_UART_GetTxCount,
    CSKY_UART_GetRxCount,
    CSKY_UART_Control,
    CSKY_UART_GetStatus,
    CSKY_UART_SetModemControl,
    CSKY_UART_Get_ModemStatus,
    &UART2_Resources,
};

__attribute__((isr)) void UART3_IRQHandler (void) {
  UART_IRQHandler (&UART3_Resources);
}

CSKY_DRIVER_UART Driver_UART3 = {
    CSKY_UART_GetVersion,
    CSKY_UART_GetCapabilities,
    CSKY_UART_Initialize,
    CSKY_UART_Uninitialize,
    CSKY_UART_PowerControl,
    CSKY_UART_Send,
    CSKY_UART_Receive,
    CSKY_UART_Transfer,
    CSKY_UART_GetTxCount,
    CSKY_UART_GetRxCount,
    CSKY_UART_Control,
    CSKY_UART_GetStatus,
    CSKY_UART_SetModemControl,
    CSKY_UART_Get_ModemStatus,
    &UART3_Resources,
};

int fputc(int ch, FILE *stream)
{
	while (console_uart->Send(console_uart, &ch, 1) != 0);
    return 0;
}
