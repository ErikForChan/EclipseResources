/******************************************************************************
 * @file     CSIDRV_UART.h
 * @brief    CSI Header File Inter-Integrated Circuit Driver definitions
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
#ifndef __CSIDRV_I2C_H
#define __CSIDRV_I2C_H

#include "CSIDRV_Common.h"

#define CSKY_I2C_API_VERSION CSKY_DRIVER_VERSION_MAJOR_MINOR(1,00)  /* API version */


/****** I2C Control Codes *****/

#define CSKY_I2C_OWN_ADDRESS             (0x01)      /* /< Set Own Slave Address; arg = address */
#define CSKY_I2C_BUS_SPEED               (0x02)      /* /< Set Bus Speed; arg = speed */
#define CSKY_I2C_BUS_CLEAR               (0x03)      /* /< Execute Bus clear: send nine clock pulses */
#define CSKY_I2C_ABORT_TRANSFER          (0x04)      /* /< Abort Master/Slave Transmit/Receive */

/*----- I2C Bus Speed -----*/
#define CSKY_I2C_BUS_SPEED_STANDARD      (0x01)      /* /< Standard Speed (100kHz) */
#define CSKY_I2C_BUS_SPEED_FAST          (0x02)      /* /< Fast Speed     (400kHz) */
#define CSKY_I2C_BUS_SPEED_FAST_PLUS     (0x03)      /* /< Fast+ Speed    (  1MHz) */
#define CSKY_I2C_BUS_SPEED_HIGH          (0x04)      /* /< High Speed     (3.4MHz) */


/****** I2C Address Flags *****/

#define CSKY_I2C_ADDRESS_10BIT           0x0400      /* /< 10-bit address flag */
#define CSKY_I2C_ADDRESS_GC              0x8000      /* /< General Call flag */


/**
\brief I2C Status
*/
typedef struct _CSKY_I2C_STATUS {
  uint32_t busy             : 1;        /* /< Busy flag */
  uint32_t mode             : 1;        /* /< Mode: 0=Slave, 1=Master */
  uint32_t direction        : 1;        /* /< Direction: 0=Transmitter, 1=Receiver */
  uint32_t general_call     : 1;        /* /< General Call indication (cleared on start of next Slave operation) */
  uint32_t arbitration_lost : 1;        /* /< Master lost arbitration (cleared on start of next Master operation) */
  uint32_t bus_error        : 1;        /* /< Bus error detected (cleared on start of next Master/Slave operation) */
} CSKY_I2C_STATUS;


/****** I2C Event *****/
#define CSKY_I2C_EVENT_TRANSFER_DONE       (1UL << 0)  /* /< Master/Slave Transmit/Receive finished */
#define CSKY_I2C_EVENT_TRANSFER_INCOMPLETE (1UL << 1)  /* /< Master/Slave Transmit/Receive incomplete transfer */
#define CSKY_I2C_EVENT_SLAVE_TRANSMIT      (1UL << 2)  /* /< Slave Transmit operation requested */
#define CSKY_I2C_EVENT_SLAVE_RECEIVE       (1UL << 3)  /* /< Slave Receive operation requested */
#define CSKY_I2C_EVENT_ADDRESS_NACK        (1UL << 4)  /* /< Address not acknowledged from Slave */
#define CSKY_I2C_EVENT_GENERAL_CALL        (1UL << 5)  /* /< General Call indication */
#define CSKY_I2C_EVENT_ARBITRATION_LOST    (1UL << 6)  /* /< Master lost arbitration */
#define CSKY_I2C_EVENT_BUS_ERROR           (1UL << 7)  /* /< Bus error detected (START/STOP at illegal position) */
#define CSKY_I2C_EVENT_BUS_CLEAR           (1UL << 8)  /* /< Bus clear finished */



#define  CK_IIC_SLAVE_ADDR      0x57
#define  EEPROM_PAGE_SIZE       32
#define  EEPROM_SIZE            8192
#define  EEPROM_SUB_ADDR_START     0x0000
#define  EEPROM_SUB_ADDR_END       0x1FFF
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
/* Function documentation */
/**
  \fn          CSKY_DRIVER_VERSION CSKY_I2C_GetVersion (const struct _CSKY_DRIVER_I2C *instance)
  \brief       Get driver version.
  \return      \ref CSKY_DRIVER_VERSION

  \fn          CSKY_I2C_CAPABILITIES CSKY_I2C_GetCapabilities (const struct _CSKY_DRIVER_I2C *instance)
  \brief       Get driver capabilities.
  \return      \ref CSKY_I2C_CAPABILITIES

  \fn          int32_t CSKY_I2C_Initialize (const struct _CSKY_DRIVER_I2C *instance, CSKY_I2C_SignalEvent_t cb_event)
  \brief       Initialize I2C Interface.
  \param[in]   cb_event  Pointer to \ref CSKY_I2C_SignalEvent
  \return      \ref execution_status

  \fn          int32_t CSKY_I2C_Uninitialize (const struct _CSKY_DRIVER_I2C *instance)
  \brief       De-initialize I2C Interface.
  \return      \ref execution_status

  \fn          int32_t CSKY_I2C_PowerControl (const struct _CSKY_DRIVER_I2C *instance, CSKY_POWER_STATE state)
  \brief       Control I2C Interface Power.
  \param[in]   state  Power state
  \return      \ref execution_status

  \fn          int32_t CSKY_I2C_MasterTransmit (const struct _CSKY_DRIVER_I2C *instance, uint32_t addr, const uint8_t *data, uint32_t num, bool xfer_pending)
  \brief       Start transmitting data as I2C Master.
  \param[in]   addr          Slave address (7-bit or 10-bit)
  \param[in]   data          Pointer to buffer with data to transmit to I2C Slave
  \param[in]   num           Number of data bytes to transmit
  \param[in]   xfer_pending  Transfer operation is pending - Stop condition will not be generated
  \return      \ref execution_status

  \fn          int32_t CSKY_I2C_MasterReceive (const struct _CSKY_DRIVER_I2C *instance, uint32_t addr, uint8_t *data, uint32_t num, bool xfer_pending)
  \brief       Start receiving data as I2C Master.
  \param[in]   addr          Slave address (7-bit or 10-bit)
  \param[out]  data          Pointer to buffer for data to receive from I2C Slave
  \param[in]   num           Number of data bytes to receive
  \param[in]   xfer_pending  Transfer operation is pending - Stop condition will not be generated
  \return      \ref execution_status

  \fn          int32_t CSKY_I2C_SlaveTransmit (const struct _CSKY_DRIVER_I2C *instance, const uint8_t *data, uint32_t num)
  \brief       Start transmitting data as I2C Slave.
  \param[in]   data  Pointer to buffer with data to transmit to I2C Master
  \param[in]   num   Number of data bytes to transmit
  \return      \ref execution_status

  \fn          int32_t CSKY_I2C_SlaveReceive (const struct _CSKY_DRIVER_I2C *instance, uint8_t *data, uint32_t num)
  \brief       Start receiving data as I2C Slave.
  \param[out]  data  Pointer to buffer for data to receive from I2C Master
  \param[in]   num   Number of data bytes to receive
  \return      \ref execution_status

  \fn          int32_t CSKY_I2C_GetDataCount (const struct _CSKY_DRIVER_I2C *instance)
  \brief       Get transferred data count.
  \return      number of data bytes transferred; -1 when Slave is not addressed by Master

  \fn          int32_t CSKY_I2C_Control (const struct _CSKY_DRIVER_I2C *instance, uint32_t control, uint32_t arg)
  \brief       Control I2C Interface.
  \param[in]   control  Operation
  \param[in]   arg      Argument of operation (optional)
  \return      \ref execution_status

  \fn          CSKY_I2C_STATUS CSKY_I2C_GetStatus (const struct _CSKY_DRIVER_I2C *instance)
  \brief       Get I2C status.
  \return      I2C status \ref CSKY_I2C_STATUS

  \fn          void CSKY_I2C_SignalEvent (uint32_t event)
  \brief       Signal I2C Events.
  \param[in]   event  \ref I2C_events notification mask
*/

typedef void (*CSKY_I2C_SignalEvent_t) (uint32_t event);  /* /< Pointer to \ref CSKY_I2C_SignalEvent : Signal I2C Event. */


/**
\brief I2C Driver Capabilities.
*/
typedef struct _CSKY_I2C_CAPABILITIES {
  uint32_t address_10_bit : 1;          /* /< supports 10-bit addressing */
} CSKY_I2C_CAPABILITIES;


/**
\brief Access structure of the I2C Driver.
*/
typedef struct _CSKY_DRIVER_I2C {
  CSKY_DRIVER_VERSION   (*GetVersion)      (const struct _CSKY_DRIVER_I2C *instance);                                                                /* /< Pointer to \ref CSKY_I2C_GetVersion : Get driver version. */
  CSKY_I2C_CAPABILITIES (*GetCapabilities) (const struct _CSKY_DRIVER_I2C *instance);                                                                /* /< Pointer to \ref CSKY_I2C_GetCapabilities : Get driver capabilities. */
  int32_t               (*Initialize)      (const struct _CSKY_DRIVER_I2C *instance, CSKY_I2C_SignalEvent_t cb_event);                               /* /< Pointer to \ref CSKY_I2C_Initialize : Initialize I2C Interface. */
  int32_t               (*Uninitialize)    (const struct _CSKY_DRIVER_I2C *instance);                                                                /* /< Pointer to \ref CSKY_I2C_Uninitialize : De-initialize I2C Interface. */
  int32_t               (*PowerControl)    (const struct _CSKY_DRIVER_I2C *instance, CSKY_POWER_STATE state);                                             /* /< Pointer to \ref CSKY_I2C_PowerControl : Control I2C Interface Power. */
  int32_t               (*MasterTransmit)  (const struct _CSKY_DRIVER_I2C *instance, uint32_t addr, const uint8_t *data, uint32_t num, bool xfer_pending); /* /< Pointer to \ref CSKY_I2C_MasterTransmit : Start transmitting data as I2C Master. */
  int32_t               (*MasterReceive)   (const struct _CSKY_DRIVER_I2C *instance, uint32_t addr, uint8_t *data, uint32_t num, bool xfer_pending); /* /< Pointer to \ref CSKY_I2C_MasterReceive : Start receiving data as I2C Master. */
  int32_t               (*SlaveTransmit)   (const struct _CSKY_DRIVER_I2C *instance, const uint8_t *data, uint32_t num);                              /* /< Pointer to \ref CSKY_I2C_SlaveTransmit : Start transmitting data as I2C Slave. */
  int32_t               (*SlaveReceive)    (const struct _CSKY_DRIVER_I2C *instance, uint8_t *data, uint32_t num);                                   /* /< Pointer to \ref CSKY_I2C_SlaveReceive : Start receiving data as I2C Slave. */
  int32_t               (*GetDataCount)    (const struct _CSKY_DRIVER_I2C *instance);                                                                  /* /< Pointer to \ref CSKY_I2C_GetDataCount : Get transferred data count. */
  int32_t               (*Control)         (const struct _CSKY_DRIVER_I2C *instance, uint32_t control, uint32_t arg);                                      /* /< Pointer to \ref CSKY_I2C_Control : Control I2C Interface. */
  CSKY_I2C_STATUS       (*GetStatus)       (const struct _CSKY_DRIVER_I2C *instance);                                                                  /* /< Pointer to \ref CSKY_I2C_GetStatus : Get I2C status. */
  void                    *priv;                                                                                                                /* /< Pointer to I2C's private data. */
}const  CSKY_DRIVER_I2C;

#endif /* __CSIDRV_I2C_H */
