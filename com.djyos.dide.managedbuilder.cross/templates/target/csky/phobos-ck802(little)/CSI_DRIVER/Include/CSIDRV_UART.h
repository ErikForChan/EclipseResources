/******************************************************************************
 * @file     CSIDRV_UART.h
 * @brief    CSI Header File Universal Asynchronous Receiver
 *           Transmitter Driver definitions
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

#ifndef __CSIDRV_UART_H
#define __CSIDRV_UART_H

#include "CSIDRV_Common.h"

#define CSKY_UART_API_VERSION CSKY_DRIVER_VERSION_MAJOR_MINOR(1,00)  /* API version */


/****** UART Control Codes *****/

#define CSKY_UART_CONTROL_Pos                0
#define CSKY_UART_CONTROL_Msk               (0xFFUL << CSKY_UART_CONTROL_Pos)

/*----- UART Control Codes: Mode -----*/
#define CSKY_UART_MODE_ASYNCHRONOUS         (0x01UL << CSKY_UART_CONTROL_Pos)   /* /< UART (Asynchronous); arg = Baudrate */
#define CSKY_UART_MODE_SYNCHRONOUS_MASTER   (0x02UL << CSKY_UART_CONTROL_Pos)   /* /< Synchronous Master (generates clock signal); arg = Baudrate */
#define CSKY_UART_MODE_SYNCHRONOUS_SLAVE    (0x03UL << CSKY_UART_CONTROL_Pos)   /* /< Synchronous Slave (external clock signal) */
#define CSKY_UART_MODE_SINGLE_WIRE          (0x04UL << CSKY_UART_CONTROL_Pos)   /* /< UART Single-wire (half-duplex); arg = Baudrate */
#define CSKY_UART_MODE_IRDA                 (0x05UL << CSKY_UART_CONTROL_Pos)   /* /< UART IrDA; arg = Baudrate */
#define CSKY_UART_MODE_SMART_CARD           (0x06UL << CSKY_UART_CONTROL_Pos)   /* /< UART Smart Card; arg = Baudrate */

/*----- UART Control Codes: Mode Parameters: Data Bits -----*/
#define CSKY_UART_DATA_BITS_Pos              8
#define CSKY_UART_DATA_BITS_Msk             (7UL << CSKY_UART_DATA_BITS_Pos)
#define CSKY_UART_DATA_BITS_5               (5UL << CSKY_UART_DATA_BITS_Pos)    /* /< 5 Data bits */
#define CSKY_UART_DATA_BITS_6               (6UL << CSKY_UART_DATA_BITS_Pos)    /* /< 6 Data bit */
#define CSKY_UART_DATA_BITS_7               (7UL << CSKY_UART_DATA_BITS_Pos)    /* /< 7 Data bits */
#define CSKY_UART_DATA_BITS_8               (0UL << CSKY_UART_DATA_BITS_Pos)    /* /< 8 Data bits (default) */
#define CSKY_UART_DATA_BITS_9               (1UL << CSKY_UART_DATA_BITS_Pos)    /* /< 9 Data bits */

/*----- UART Control Codes: Mode Parameters: Parity -----*/
#define CSKY_UART_PARITY_Pos                 12
#define CSKY_UART_PARITY_Msk                (3UL << CSKY_UART_PARITY_Pos)
#define CSKY_UART_PARITY_NONE               (0UL << CSKY_UART_PARITY_Pos)       /* /< No Parity (default) */
#define CSKY_UART_PARITY_EVEN               (1UL << CSKY_UART_PARITY_Pos)       /* /< Even Parity */
#define CSKY_UART_PARITY_ODD                (2UL << CSKY_UART_PARITY_Pos)       /* /< Odd Parity */

/*----- UART Control Codes: Mode Parameters: Stop Bits -----*/
#define CSKY_UART_STOP_BITS_Pos              14
#define CSKY_UART_STOP_BITS_Msk             (3UL << CSKY_UART_STOP_BITS_Pos)
#define CSKY_UART_STOP_BITS_1               (0UL << CSKY_UART_STOP_BITS_Pos)    /* /< 1 Stop bit (default) */
#define CSKY_UART_STOP_BITS_2               (1UL << CSKY_UART_STOP_BITS_Pos)    /* /< 2 Stop bits */
#define CSKY_UART_STOP_BITS_1_5             (2UL << CSKY_UART_STOP_BITS_Pos)    /* /< 1.5 Stop bits */
#define CSKY_UART_STOP_BITS_0_5             (3UL << CSKY_UART_STOP_BITS_Pos)    /* /< 0.5 Stop bits */

/*----- UART Control Codes: Mode Parameters: Flow Control -----*/
#define CSKY_UART_FLOW_CONTROL_Pos           16
#define CSKY_UART_FLOW_CONTROL_Msk          (3UL << CSKY_UART_FLOW_CONTROL_Pos)
#define CSKY_UART_FLOW_CONTROL_NONE         (0UL << CSKY_UART_FLOW_CONTROL_Pos) /* /< No Flow Control (default) */
#define CSKY_UART_FLOW_CONTROL_RTS          (1UL << CSKY_UART_FLOW_CONTROL_Pos) /* /< RTS Flow Control */
#define CSKY_UART_FLOW_CONTROL_CTS          (2UL << CSKY_UART_FLOW_CONTROL_Pos) /* /< CTS Flow Control */
#define CSKY_UART_FLOW_CONTROL_RTS_CTS      (3UL << CSKY_UART_FLOW_CONTROL_Pos) /* /< RTS/CTS Flow Control */

/*----- UART Control Codes: Mode Parameters: Clock Polarity (Synchronous mode) -----*/
#define CSKY_UART_CPOL_Pos                   18
#define CSKY_UART_CPOL_Msk                  (1UL << CSKY_UART_CPOL_Pos)
#define CSKY_UART_CPOL0                     (0UL << CSKY_UART_CPOL_Pos)         /* /< CPOL = 0 (default) */
#define CSKY_UART_CPOL1                     (1UL << CSKY_UART_CPOL_Pos)         /* /< CPOL = 1 */

/*----- UART Control Codes: Mode Parameters: Clock Phase (Synchronous mode) -----*/
#define CSKY_UART_CPHA_Pos                   19
#define CSKY_UART_CPHA_Msk                  (1UL << CSKY_UART_CPHA_Pos)
#define CSKY_UART_CPHA0                     (0UL << CSKY_UART_CPHA_Pos)         /* /< CPHA = 0 (default) */
#define CSKY_UART_CPHA1                     (1UL << CSKY_UART_CPHA_Pos)         /* /< CPHA = 1 */


/*----- UART Control Codes: Miscellaneous Controls  -----*/
#define CSKY_UART_SET_DEFAULT_TX_VALUE      (0x10UL << CSKY_UART_CONTROL_Pos)   /* /< Set default Transmit value (Synchronous Receive only); arg = value */
#define CSKY_UART_SET_IRDA_PULSE            (0x11UL << CSKY_UART_CONTROL_Pos)   /* /< Set IrDA Pulse in ns; arg: 0=3/16 of bit period */
#define CSKY_UART_SET_SMART_CARD_GUARD_TIME (0x12UL << CSKY_UART_CONTROL_Pos)   /* /< Set Smart Card Guard Time; arg = number of bit periods */
#define CSKY_UART_SET_SMART_CARD_CLOCK      (0x13UL << CSKY_UART_CONTROL_Pos)   /* /< Set Smart Card Clock in Hz; arg: 0=Clock not generated */
#define CSKY_UART_CONTROL_SMART_CARD_NACK   (0x14UL << CSKY_UART_CONTROL_Pos)   /* /< Smart Card NACK generation; arg: 0=disabled, 1=enabled */
#define CSKY_UART_CONTROL_TX                (0x15UL << CSKY_UART_CONTROL_Pos)   /* /< Transmitter; arg: 0=disabled, 1=enabled */
#define CSKY_UART_CONTROL_RX                (0x16UL << CSKY_UART_CONTROL_Pos)   /* /< Receiver; arg: 0=disabled, 1=enabled */
#define CSKY_UART_CONTROL_BREAK             (0x17UL << CSKY_UART_CONTROL_Pos)   /* /< Continuous Break transmission; arg: 0=disabled, 1=enabled */
#define CSKY_UART_ABORT_SEND                (0x18UL << CSKY_UART_CONTROL_Pos)   /* /< Abort \ref CSKY_UART_Send */
#define CSKY_UART_ABORT_RECEIVE             (0x19UL << CSKY_UART_CONTROL_Pos)   /* /< Abort \ref CSKY_UART_Receive */
#define CSKY_UART_ABORT_TRANSFER            (0x1AUL << CSKY_UART_CONTROL_Pos)   /* /< Abort \ref CSKY_UART_Transfer */



/****** UART specific error codes *****/
#define CSKY_UART_ERROR_MODE                (CSKY_DRIVER_ERROR_SPECIFIC - 1)     /* /< Specified Mode not supported */
#define CSKY_UART_ERROR_BAUDRATE            (CSKY_DRIVER_ERROR_SPECIFIC - 2)     /* /< Specified baudrate not supported */
#define CSKY_UART_ERROR_DATA_BITS           (CSKY_DRIVER_ERROR_SPECIFIC - 3)     /* /< Specified number of Data bits not supported */
#define CSKY_UART_ERROR_PARITY              (CSKY_DRIVER_ERROR_SPECIFIC - 4)     /* /< Specified Parity not supported */
#define CSKY_UART_ERROR_STOP_BITS           (CSKY_DRIVER_ERROR_SPECIFIC - 5)     /* /< Specified number of Stop bits not supported */
#define CSKY_UART_ERROR_FLOW_CONTROL        (CSKY_DRIVER_ERROR_SPECIFIC - 6)     /* /< Specified Flow Control not supported */
#define CSKY_UART_ERROR_CPOL                (CSKY_DRIVER_ERROR_SPECIFIC - 7)     /* /< Specified Clock Polarity not supported */
#define CSKY_UART_ERROR_CPHA                (CSKY_DRIVER_ERROR_SPECIFIC - 8)     /* /< Specified Clock Phase not supported */


/**
\brief UART Status
*/
typedef struct _CSKY_UART_STATUS {
  uint32_t tx_busy          : 1;        /* /< Transmitter busy flag */
  uint32_t rx_busy          : 1;        /* /< Receiver busy flag */
  uint32_t tx_underflow     : 1;        /* /< Transmit data underflow detected (cleared on start of next send operation) */
  uint32_t rx_overflow      : 1;        /* /< Receive data overflow detected (cleared on start of next receive operation) */
  uint32_t rx_break         : 1;        /* /< Break detected on receive (cleared on start of next receive operation) */
  uint32_t rx_framing_error : 1;        /* /< Framing error detected on receive (cleared on start of next receive operation) */
  uint32_t rx_parity_error  : 1;        /* /< Parity error detected on receive (cleared on start of next receive operation) */
} CSKY_UART_STATUS;

/**
\brief UART Modem Control
*/
typedef enum _CSKY_UART_MODEM_CONTROL {
  CSKY_UART_RTS_CLEAR,                  /* /< Deactivate RTS */
  CSKY_UART_RTS_SET,                    /* /< Activate RTS */
  CSKY_UART_DTR_CLEAR,                  /* /< Deactivate DTR */
  CSKY_UART_DTR_SET                     /* /< Activate DTR */
} CSKY_UART_MODEM_CONTROL;

/**
\brief UART Modem Status
*/
typedef struct _CSKY_UART_MODEM_STATUS {
  uint32_t cts : 1;                     /* /< CTS state: 1=Active, 0=Inactive */
  uint32_t dsr : 1;                     /* /< DSR state: 1=Active, 0=Inactive */
  uint32_t dcd : 1;                     /* /< DCD state: 1=Active, 0=Inactive */
  uint32_t ri  : 1;                     /* /< RI  state: 1=Active, 0=Inactive */
} CSKY_UART_MODEM_STATUS;


/****** UART Event *****/
#define CSKY_UART_EVENT_SEND_COMPLETE       (1UL << 0)  /* /< Send completed; however UART may still transmit data */
#define CSKY_UART_EVENT_RECEIVE_COMPLETE    (1UL << 1)  /* /< Receive completed */
#define CSKY_UART_EVENT_TRANSFER_COMPLETE   (1UL << 2)  /* /< Transfer completed */
#define CSKY_UART_EVENT_TX_COMPLETE         (1UL << 3)  /* /< Transmit completed (optional) */
#define CSKY_UART_EVENT_TX_UNDERFLOW        (1UL << 4)  /* /< Transmit data not available (Synchronous Slave) */
#define CSKY_UART_EVENT_RX_OVERFLOW         (1UL << 5)  /* /< Receive data overflow */
#define CSKY_UART_EVENT_RX_TIMEOUT          (1UL << 6)  /* /< Receive character timeout (optional) */
#define CSKY_UART_EVENT_RX_BREAK            (1UL << 7)  /* /< Break detected on receive */
#define CSKY_UART_EVENT_RX_FRAMING_ERROR    (1UL << 8)  /* /< Framing error detected on receive */
#define CSKY_UART_EVENT_RX_PARITY_ERROR     (1UL << 9)  /* /< Parity error detected on receive */
#define CSKY_UART_EVENT_CTS                 (1UL << 10) /* /< CTS state changed (optional) */
#define CSKY_UART_EVENT_DSR                 (1UL << 11) /* /< DSR state changed (optional) */
#define CSKY_UART_EVENT_DCD                 (1UL << 12) /* /< DCD state changed (optional) */
#define CSKY_UART_EVENT_RI                  (1UL << 13) /* /< RI  state changed (optional) */


/* Function documentation */
/**
  \fn          CSKY_DRIVER_VERSION CSKY_UART_GetVersion (CSKY_DRIVER_UART *instance)
  \brief       Get driver version.
  \param[in]   instance     Pointer to UART's instance
  \return      \ref CSKY_DRIVER_VERSION

  \fn          CSKY_UART_CAPABILITIES CSKY_UART_GetCapabilities (CSKY_DRIVER_UART *instance)
  \brief       Get driver capabilities
  \param[in]   instance     Pointer to UART's instance
  \return      \ref CSKY_UART_CAPABILITIES

  \fn          int32_t CSKY_UART_Initialize (CSKY_DRIVER_UART *instance, CSKY_UART_SignalEvent_t cb_event)
  \brief       Initialize UART Interface.
  \param[in]   instance  Pointer to UART's instance
  \param[in]   cb_event  Pointer to \ref CSKY_UART_SignalEvent
  \return      \ref execution_status

  \fn          int32_t CSKY_UART_Uninitialize (CSKY_DRIVER_UART *instance)
  \brief       De-initialize UART Interface.
  \param[in]   instance  Pointer to UART's instance
  \return      \ref execution_status

  \fn          int32_t CSKY_UART_PowerControl (CSKY_DRIVER_UART *instance, CSKY_POWER_STATE state)
  \brief       Control UART Interface Power.
  \param[in]   instance  Pointer to UART's instance
  \param[in]   state  Power state
  \return      \ref execution_status

  \fn          int32_t CSKY_UART_Send (CSKY_DRIVER_UART *instance, const void *data, uint32_t num)
  \brief       Start sending data to UART transmitter.
  \param[in]   instance  Pointer to UART's instance
  \param[in]   data  Pointer to buffer with data to send to UART transmitter
  \param[in]   num   Number of data items to send
  \return      \ref execution_status

  \fn          int32_t CSKY_UART_Receive (CSKY_DRIVER_UART *instance, void *data, uint32_t num)
  \brief       Start receiving data from UART receiver.
  \param[in]   instance  Pointer to UART's instance
  \param[out]  data  Pointer to buffer for data to receive from UART receiver
  \param[in]   num   Number of data items to receive
  \return      \ref execution_status

  \fn          int32_t CSKY_UART_Transfer (CSKY_DRIVER_UART *instance,
                                            const void *data_out,
                                                 void *data_in,
                                           uint32_t    num)
  \brief       Start sending/receiving data to/from UART transmitter/receiver.
  \param[in]   instance  Pointer to UART's instance
  \param[in]   data_out  Pointer to buffer with data to send to UART transmitter
  \param[out]  data_in   Pointer to buffer for data to receive from UART receiver
  \param[in]   num       Number of data items to transfer
  \return      \ref execution_status

  \fn          uint32_t CSKY_UART_GetTxCount (CSKY_DRIVER_UART *instance)
  \brief       Get transmitted data count.
  \param[in]   instance  Pointer to UART's instance
  \return      number of data items transmitted

  \fn          uint32_t CSKY_UART_GetRxCount (CSKY_DRIVER_UART *instance)
  \brief       Get received data count.
  \param[in]   instance  Pointer to UART's instance
  \return      number of data items received

  \fn          int32_t CSKY_UART_Control (CSKY_DRIVER_UART *instance, uint32_t control, uint32_t arg)
  \brief       Control UART Interface.
  \param[in]   instance  Pointer to UART's instance
  \param[in]   control  Operation
  \param[in]   arg      Argument of operation (optional)
  \return      common \ref execution_status and driver specific \ref usart_execution_status

  \fn          CSKY_UART_STATUS CSKY_UART_GetStatus (CSKY_DRIVER_UART *instance)
  \brief       Get UART status.
  \param[in]   instance  Pointer to UART's instance
  \return      UART status \ref CSKY_UART_STATUS

  \fn          int32_t CSKY_UART_SetModemControl (CSKY_DRIVER_UART *instance, CSKY_UART_MODEM_CONTROL control)
  \brief       Set UART Modem Control line state.
  \param[in]   instance  Pointer to UART's instance
  \param[in]   control  \ref CSKY_UART_MODEM_CONTROL
  \return      \ref execution_status

  \fn          CSKY_UART_MODEM_STATUS CSKY_UART_GetModemStatus (CSKY_DRIVER_UART *instance)
  \brief       Get UART Modem Status lines state.
  \param[in]   instance  Pointer to UART's instance
  \return      modem status \ref CSKY_UART_MODEM_STATUS

  \fn          void CSKY_UART_SignalEvent (uint32_t event)
  \brief       Signal UART Events.
  \param[in]   event  \ref UART_events notification mask
  \return      none
*/

typedef void (*CSKY_UART_SignalEvent_t) (uint32_t event);  /* /< Pointer to \ref CSKY_UART_SignalEvent : Signal UART Event. */


/**
\brief UART Device Driver Capabilities.
*/
typedef struct _CSKY_UART_CAPABILITIES {
  uint32_t asynchronous       : 1;      /* /< supports UART (Asynchronous) mode */
  uint32_t synchronous_master : 1;      /* /< supports Synchronous Master mode */
  uint32_t synchronous_slave  : 1;      /* /< supports Synchronous Slave mode */
  uint32_t single_wire        : 1;      /* /< supports UART Single-wire mode */
  uint32_t irda               : 1;      /* /< supports UART IrDA mode */
  uint32_t smart_card         : 1;      /* /< supports UART Smart Card mode */
  uint32_t smart_card_clock   : 1;      /* /< Smart Card Clock generator available */
  uint32_t flow_control_rts   : 1;      /* /< RTS Flow Control available */
  uint32_t flow_control_cts   : 1;      /* /< CTS Flow Control available */
  uint32_t event_tx_complete  : 1;      /* /< Transmit completed event: \ref CSKY_UART_EVENT_TX_COMPLETE */
  uint32_t event_rx_timeout   : 1;      /* /< Signal receive character timeout event: \ref CSKY_UART_EVENT_RX_TIMEOUT */
  uint32_t rts                : 1;      /* /< RTS Line: 0=not available, 1=available */
  uint32_t cts                : 1;      /* /< CTS Line: 0=not available, 1=available */
  uint32_t dtr                : 1;      /* /< DTR Line: 0=not available, 1=available */
  uint32_t dsr                : 1;      /* /< DSR Line: 0=not available, 1=available */
  uint32_t dcd                : 1;      /* /< DCD Line: 0=not available, 1=available */
  uint32_t ri                 : 1;      /* /< RI Line: 0=not available, 1=available */
  uint32_t event_cts          : 1;      /* /< Signal CTS change event: \ref CSKY_UART_EVENT_CTS */
  uint32_t event_dsr          : 1;      /* /< Signal DSR change event: \ref CSKY_UART_EVENT_DSR */
  uint32_t event_dcd          : 1;      /* /< Signal DCD change event: \ref CSKY_UART_EVENT_DCD */
  uint32_t event_ri           : 1;      /* /< Signal RI change event: \ref CSKY_UART_EVENT_RI */
} CSKY_UART_CAPABILITIES;


/**
\brief Access structure of the UART Driver.
*/
typedef struct _CSKY_DRIVER_UART {
  CSKY_DRIVER_VERSION     (*GetVersion)      (const struct _CSKY_DRIVER_UART *instance);                                             /* /< Pointer to \ref CSKY_UART_GetVersion : Get driver version. */
  CSKY_UART_CAPABILITIES  (*GetCapabilities) (const struct _CSKY_DRIVER_UART *instance);                                             /* /< Pointer to \ref CSKY_UART_GetCapabilities : Get driver capabilities. */
  int32_t                 (*Initialize)      (const struct _CSKY_DRIVER_UART *instance, CSKY_UART_SignalEvent_t cb_event);           /* /< Pointer to \ref CSKY_UART_Initialize : Initialize UART Interface. */
  int32_t                 (*Uninitialize)    (const struct _CSKY_DRIVER_UART *instance);                                             /* /< Pointer to \ref CSKY_UART_Uninitialize : De-initialize UART Interface. */
  int32_t                 (*PowerControl)    (const struct _CSKY_DRIVER_UART *instance, CSKY_POWER_STATE state);                     /* /< Pointer to \ref CSKY_UART_PowerControl : Control UART Interface Power. */
  int32_t                 (*Send)            (const struct _CSKY_DRIVER_UART *instance, const void *data, uint32_t num);             /* /< Pointer to \ref CSKY_UART_Send : Start sending data to UART transmitter. */
  int32_t                 (*Receive)         (const struct _CSKY_DRIVER_UART *instance, void *data, uint32_t num);                   /* /< Pointer to \ref CSKY_UART_Receive : Start receiving data from UART receiver. */
  int32_t                 (*Transfer)        (const struct _CSKY_DRIVER_UART *instance, void *data_out, void *data_in, uint32_t num);/* /< Pointer to \ref CSKY_UART_Transfer : Start sending/receiving data to/from UART. */
  uint32_t                (*GetTxCount)      (const struct _CSKY_DRIVER_UART *instance);                                             /* /< Pointer to \ref CSKY_UART_GetTxCount : Get transmitted data count. */
  uint32_t                (*GetRxCount)      (const struct _CSKY_DRIVER_UART *instance);                                             /* /< Pointer to \ref CSKY_UART_GetRxCount : Get received data count. */
  int32_t                 (*Control)         (const struct _CSKY_DRIVER_UART *instance, uint32_t control, uint32_t arg);             /* /< Pointer to \ref CSKY_UART_Control : Control UART Interface. */
  CSKY_UART_STATUS        (*GetStatus)       (const struct _CSKY_DRIVER_UART *instance);                                             /* /< Pointer to \ref CSKY_UART_GetStatus : Get UART status. */
  int32_t                 (*SetModemControl) (const struct _CSKY_DRIVER_UART *instance, CSKY_UART_MODEM_CONTROL control);            /* /< Pointer to \ref CSKY_UART_SetModemControl : Set UART Modem Control line state. */
  CSKY_UART_MODEM_STATUS  (*GetModemStatus)  (const struct _CSKY_DRIVER_UART *instance);                                             /* /< Pointer to \ref CSKY_UART_GetModemStatus : Get UART Modem Status lines state. */
  void                    *priv;                                                                                                     /* /< Pointer to UART's private data. */
} const CSKY_DRIVER_UART;

#endif /* __CSIDRV_UART_H */
