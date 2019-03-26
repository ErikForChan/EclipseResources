/******************************************************************************
 * @file     PHOBOS_SHA.c
 * @brief    CSI Source File for SHA Driver
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
#include "CSIDRV_SHA.h"
#include "PHOBOS.h"
#include "PHOBOS_SHA.h"

#define CSKY_SHA_DRV_VERSION    CSKY_DRIVER_VERSION_MAJOR_MINOR(1, 0)  /* driver version */

/* Driver Version */
static const CSKY_DRIVER_VERSION DriverVersion = {
    CSKY_SHA_API_VERSION,
    CSKY_SHA_DRV_VERSION
};

/* Driver Capabilities */
static CSKY_SHA_CAPABILITIES DriverCapabilities = {
		   1,      /* /< supports sha1 mode */
		   0,      /* /< supports sha224 mode */
		   0,      /* /< supports sha256 mode */
		   0,      /* /< supports sha384 mode */
		   0,      /* /< supports sha512 mode */
		   0,      /* /< supports sha512_224 mode */
		   0,      /* /< supports sha512_256 mode */
		   1,      /* /< supports endian mode control */
		   1       /* /< supports interrupt mode */
};

static SHA_RESOURCES SHA_Resources = {
  {     /* Capabilities */
		   1,      /* /< supports sha1 mode */
		   0,      /* /< supports sha224 mode */
		   0,      /* /< supports sha256 mode */
		   0,      /* /< supports sha384 mode */
		   0,      /* /< supports sha512 mode */
		   0,      /* /< supports sha512_224 mode */
		   0,      /* /< supports sha512_256 mode */
		   1,      /* /< supports endian mode control */
		   1       /* /< supports interrupt mode */
  },
  0,
  CSKY_SHA_BASE,
  SHA_IRQn,
  0,
  NULL,
  CK_SHA_1,
  CK_SHA_BIG_ENDIAN
};

/*
 *   Functions
 */

/*****************************************************************************
CK_SHA_Set_Mode: set the mode of sha

INPUT: mode - sha mode.

RETURN: true or false

*****************************************************************************/
int32_t CK_SHA_Set_Mode(CKEnum_SHA_MODE mode)
{
    CSKY_SHA->SHA_CON = mode;
    return true;
}

/*****************************************************************************
CK_SHA_EnableInitial: enable init sha

INPUT: void.

RETURN: true or false

*****************************************************************************/
int32_t CK_SHA_EnableInitial(void)
{
	CSKY_SHA->SHA_CON |= 1<<3;
    return true;
}

/*****************************************************************************
CK_SHA_EnableCalculate: start the calculate the data

INPUT: void

RETURN: true or false

*****************************************************************************/
int32_t CK_SHA_EnableCalculate(void)
{
	CSKY_SHA->SHA_CON |= 1<<6;
    return true;
}

/*****************************************************************************
CK_SHA_EnableInterrupt: enable the interrupt of the sha

INPUT: void

RETURN: true or false

*****************************************************************************/
int32_t CK_SHA_EnableInterrupt(void)
{
	CSKY_SHA->SHA_CON |= 1<<4;
    return true;
}

/*****************************************************************************
CK_SHA_MessageDone: check the sha caculate is done

INPUT: void

RETURN: true or false

*****************************************************************************/
int32_t CK_SHA_MessageDone(void)
{
    while((CSKY_SHA->SHA_CON & 0x40)!=0);
    return true;
}

/*****************************************************************************
CK_SHA_Select_Endian_Mode: set the endian mode of sha

INPUT: mode - endian mode of sha

RETURN: true or false

*****************************************************************************/
int32_t CK_SHA_Select_Endian_Mode(CKEnum_SHA_ENDIAN mode)
{
	CSKY_SHA->SHA_CON |= mode<<5;
    return true;
}

/*****************************************************************************
CK_SHA_Input_Data: fill the data in the data register

INPUT: data - the pointer to the input data.
        length - the input data length

RETURN: true or false

*****************************************************************************/
static int32_t CK_SHA_Input_Data(uint32_t *data, uint32_t length)
{
  uint32_t *input_data = (uint32_t *)&(CSKY_SHA->SHA_DATA1);
  uint8_t i;

  for(i=0; i<length; i++) {
    *(input_data + i) = *(data + i);
  }
  return true;
}


static CSKY_DRIVER_VERSION CSKY_SHA_GetVersion(CSKY_DRIVER_SHA *instance)
{
    return DriverVersion;
}

void CSKY_SHA_SignalEvent(uint32_t event)
{
    /* function body */
}

static CSKY_SHA_CAPABILITIES CSKY_SHA_GetCapabilities (CSKY_DRIVER_SHA *instance) {
	return DriverCapabilities;
}

static int32_t CSKY_SHA_Initialize (CSKY_DRIVER_SHA *instance, CSKY_SHA_SignalEvent_t cb_event) {

	SHA_RESOURCES *sha = instance->priv;
	sha->cb_event = cb_event;
	return CSKY_DRIVER_OK;
}

static int32_t CSKY_SHA_Uninitialize (CSKY_DRIVER_SHA *instance) {
	SHA_RESOURCES *sha = instance->priv;
	sha->cb_event = NULL;
	return CSKY_DRIVER_OK;
}

static int32_t CSKY_SHA_PowerControl (CSKY_DRIVER_SHA *instance, CSKY_POWER_STATE state) {

	SHA_RESOURCES *sha = instance->priv;
    switch (state)
    {
    /* disable the irq of the sha */
    case CSKY_POWER_OFF:
        NVIC_DisableIRQ(sha->irq_num);
        break;

    case CSKY_POWER_LOW:
        return CSKY_DRIVER_ERROR_UNSUPPORTED;

    /* enable the irq of the sha */
    case CSKY_POWER_FULL:
        NVIC_EnableIRQ(sha->irq_num);
        NVIC_EnableSIRQ(sha->irq_num);
        break;

    default:
        return CSKY_DRIVER_ERROR_UNSUPPORTED;
    }
    return CSKY_DRIVER_OK;
}

static uint32_t input_message[150];
static int32_t CSKY_SHA_FillData (CSKY_DRIVER_SHA *instance, void *data, uint32_t length, uint32_t total_length) {
	SHA_RESOURCES *sha = instance->priv;
	uint32_t index = 0;
	uint32_t *q = (uint32_t *)data;
	uint8_t block_word=0;
	uint8_t i,j;
	uint8_t rmdata_num;
	uint8_t block_num = 0;
	uint32_t left_len = 0;
	uint8_t temp_data[4];
	uint32_t fill_data = 0x00000080;

	block_num = length >> 6;
	left_len = length & 0x3f;

	length = length << 3;
	total_length = total_length << 3;
	index = left_len >> 2;

	if(index >= 14){
	    block_word = 16;
	}

	rmdata_num = (left_len & 0x3);

    /* fill the redundant data with 0*/
	memset(input_message, 0, sizeof(input_message));
	memcpy((void *)input_message, (void *)(((char *)data)+(block_num << 6)), left_len+4-rmdata_num);


    if (sha->endian_mode == CK_SHA_LITTLE_ENDIAN) {
	 	fill_data = 0x80000000;
	}
	if (rmdata_num) {
	    if(sha->endian_mode == CK_SHA_LITTLE_ENDIAN) {
            /* the first word is 0x00000080*/
			for(i=0; i<(3-rmdata_num); i++) {
			    *((uint8_t *)input_message+left_len+i) = 0x00;
			}
			*((uint8_t *)input_message+left_len+i-rmdata_num) = 0x80;
		}
		else{
            /* the first word is 0x80000000*/
			*((uint8_t *)input_message+left_len) = 0x80;
		    for(i=1; i<(4-rmdata_num); i++) {
		         *((uint8_t *)input_message+left_len+i) = 0x00;
		    }
		}
		index++;
		input_message[index] = 0x00000000;
	}
	else{
	    input_message[index] = fill_data;
	}

	for(i=index+1; i<block_word+15; i++) {
	    input_message[i] = 0x0;
	}

	if(sha->endian_mode == CK_SHA_BIG_ENDIAN) {
	/*calculate the final word*/
	    for(j=0; j<4; j++) {
	        temp_data[j] = (total_length >> (8*j))&0xff;
	    }
	    total_length = (temp_data[0] << 24) | (temp_data[1] << 16) | (temp_data[2] << 8) | (temp_data[3]);
	}
	input_message[i] = total_length;

	/*cal the input data*/
	for(i=0; i<block_num; i++) {
	    CK_SHA_Input_Data(q+(i << 4), 16);
	    CK_SHA_EnableCalculate();
	    CK_SHA_MessageDone();
	}

    /* cal the redundant data */
	q = (uint32_t *)input_message;
	CK_SHA_Input_Data(q, 16);
	CK_SHA_EnableCalculate();
	CK_SHA_MessageDone();

	if(index >= 14) {
	    CK_SHA_Input_Data(q+16, 16);
	    CK_SHA_EnableCalculate();
	    CK_SHA_MessageDone();
	}

    return CSKY_DRIVER_OK;
}

static int32_t CSKY_SHA_GetResult (CSKY_DRIVER_SHA *instance, void *data) {
	SHA_RESOURCES *sha = instance->priv;

    uint32_t *addr = (uint32_t *)&CSKY_SHA->SHA_H0L;
    uint8_t i;

    switch(sha->mode){
    case CK_SHA_1:
        for(i=0; i<5; i++){
            *((uint32_t *)data+i) = *(addr+i);
        }
        break;
    case CK_SHA_256:
    case CK_SHA_224:
    case CK_SHA_512:
    case CK_SHA_384:
    case CK_SHA_512_256:
    case CK_SHA_512_224:
    default:
        return CSKY_DRIVER_ERROR_UNSUPPORTED;
    }
    return CSKY_DRIVER_OK;
}

static int32_t CSKY_SHA_Control (CSKY_DRIVER_SHA *instance, uint32_t control, uint32_t arg) {

	SHA_RESOURCES *sha = instance->priv;
	CKEnum_SHA_MODE mode;
	CKEnum_SHA_ENDIAN endian_mode;

    /* control the mode of sha*/
    switch(control & CSKY_SHA_CONTROL_Msk){
    case CSKY_SHA_MODE_1:
        mode = CK_SHA_1;
        break;
    default:
        return CSKY_DRIVER_ERROR_UNSUPPORTED;
    }
    /* control the endian mode of sha*/
    switch(control & CSKY_SHA_ENDIAN_MODE_Msk) {
    case CSKY_SHA_ENDIAN_MODE_BIG:
    	endian_mode = CK_SHA_BIG_ENDIAN;
        break;
    case CSKY_SHA_ENDIAN_MODE_LITTLE:
    	endian_mode = CK_SHA_LITTLE_ENDIAN;
        break;
    default:
        return CSKY_DRIVER_ERROR_UNSUPPORTED;
    }

    /* set the para into the register of the sha*/
    CK_SHA_Set_Mode(mode);
    CK_SHA_Select_Endian_Mode(endian_mode);
    CK_SHA_EnableInitial();
    sha->mode = mode;
    sha->endian_mode = endian_mode;
    return CSKY_DRIVER_OK;

}
static CSKY_SHA_STATUS CSKY_SHA_GetStatus (CSKY_DRIVER_SHA *instance) {
	CSKY_SHA_STATUS sha_status={0};
	return sha_status;
}

CSKY_DRIVER_SHA Driver_SHA = {
    CSKY_SHA_GetVersion,
    CSKY_SHA_GetCapabilities,
    CSKY_SHA_Initialize,
    CSKY_SHA_Uninitialize,
    CSKY_SHA_PowerControl,
    CSKY_SHA_FillData,
    CSKY_SHA_GetResult,
    CSKY_SHA_Control,
    CSKY_SHA_GetStatus,
    &SHA_Resources,
};
