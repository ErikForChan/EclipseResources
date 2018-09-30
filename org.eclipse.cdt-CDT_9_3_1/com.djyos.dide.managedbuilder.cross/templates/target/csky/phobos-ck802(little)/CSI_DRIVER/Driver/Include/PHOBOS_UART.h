/******************************************************************************
 * @file     PHOBOS_UART.h
 * @brief    PHOBOS Header File for Universal Asynchronous Receiver
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
#ifndef __PHOBOS_UART_H
#define __PHOBOS_UART_H

#include "CSIDRV_UART.h"

/* 
 * Define the baudrate of UART
 */ 
extern CSKY_DRIVER_UART *console_uart;
typedef enum{
  B1200  =1200,
  B2400  =2400,
  B4800  =4800,
  B9600  =9600,
  B14400 =14400,
  B19200 =19200,
  B56000 =56000,
  B38400 =38400,
  B57600 =57600,
  B115200=115200
}CK_Uart_Baudrate;

#define BAUDRATE   19200

/* 
 * Define the word size of UART
 */ 
typedef enum{
  WORD_SIZE_5 =0,
  WORD_SIZE_6,
  WORD_SIZE_7,
  WORD_SIZE_8
}CK_Uart_WordSize;

/* 
 * Define the parity type of UART
 */ 
typedef enum{
  ODD = 0,
  EVEN,
  NONE
}CK_Uart_Parity;

/* 
 * Define the stopbit of UART
 */ 
typedef enum{
  LCR_STOP_BIT_1 = 0,
  LCR_STOP_BIT_2
}CK_Uart_StopBit;


/* 
 * Define the error of UART
 */ 
typedef enum{
  CK_Uart_CTRL_C      = 0,
  CK_Uart_FrameError  = 1,
  CK_Uart_ParityError = 2
}CKEnum_Uart_Error;




#define UART_BUSY_TIMEOUT      1000000
#define UART_RECEIVE_TIMEOUT   1000
#define UART_TRANSMIT_TIMEOUT  1000


/* UART register bit definitions */

#define USR_UART_BUSY           0x01
#define USR_UART_TFE            0x04
#define USR_UART_RFNE           0x08
#define LSR_DATA_READY          0x01
#define LSR_THR_EMPTY           0x20
#define IER_RDA_INT_ENABLE      0x01
#define IER_THRE_INT_ENABLE     0x82
#define IIR_NO_ISQ_PEND         0x01

#define LCR_SET_DLAB            0x80   /* enable r/w DLR to set the baud rate */
#define LCR_PARITY_ENABLE	    0x08   /* parity enabled */
#define LCR_PARITY_EVEN         0x10   /* Even parity enabled */
#define LCR_PARITY_ODD          0xef   /* Odd parity enabled */
#define LCR_WORD_SIZE_5         0xfc   /* the data length is 5 bits */
#define LCR_WORD_SIZE_6         0x01   /* the data length is 6 bits */
#define LCR_WORD_SIZE_7         0x02   /* the data length is 7 bits */
#define LCR_WORD_SIZE_8         0x03   /* the data length is 8 bits */
#define LCR_STOP_BIT1           0xfb   /* 1 stop bit */
#define LCR_STOP_BIT2           0x04   /* 1.5 stop bit */

#define CK_LSR_PFE              0x80
#define CK_LSR_TEMT             0x40
#define CK_LSR_THRE             0x40
#define	CK_LSR_BI               0x10
#define	CK_LSR_FE               0x08
#define	CK_LSR_PE               0x04
#define	CK_LSR_OE               0x02
#define	CK_LSR_DR               0x01
#define CK_LSR_TRANS_EMPTY      0x20


/*config the uart */

typedef struct _UART_TRANSFER_INFO {
    uint32_t        rx_num;
    uint32_t        tx_num;
    uint8_t         *rx_buf;
    uint8_t         *tx_buf;
    volatile uint32_t rx_cnt;
    volatile uint32_t tx_cnt;
}UART_TRANSFER_INFO;

typedef struct {
  CSKY_UART_CAPABILITIES  capabilities;  /* Capabilities */
  uint8_t                  id;
  uint32_t                addr;          /* Pointer to UART peripheral */
  uint8_t                 irq_num;       /* UART IRQ Number */
  uint32_t                priority;
  bool                    bopened;
  CSKY_UART_SignalEvent_t cb_event;      /* Event callback */
  CK_Uart_Baudrate baudrate;
  CK_Uart_Parity parity;
  CK_Uart_WordSize word;
  CK_Uart_StopBit stopbit;
  bool btxquery;
  bool brxquery;
  UART_TRANSFER_INFO   xfer;
} UART_RESOURCES;
#endif
