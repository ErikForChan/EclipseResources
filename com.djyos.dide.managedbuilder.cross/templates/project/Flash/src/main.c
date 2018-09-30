/*******************************************************************************
 * Copyright (c) 2009 Hangzhou C-SKY Microsystems Co., Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Hangzhou C-SKY Microsystems Co., Ltd.
 *
 * 2009.12.1   Jing Qing(qing_jing@c-sky.com)
 * 2014.06.30  Fan Rundong(rundong_fan@c-sky.com)
 *
 *     Initial API and implementation
 *******************************************************************************/

/*
 * NOTING : this file read only.
 */
 
/*
 * set the version of flash driver
 */
#define VERS      3
unsigned int fd_version = 0x0000 + VERS;

int flashUnInit();
int flashInit();
int flashID(unsigned int* flashID);
int flashProgram(char* dst, char *src, int length);
int flashRead(char* dst, char *src, int length);
int flashErase(char *dst, int length);
int flashChipErase();
void __bkpt_label();

int flashTest();

int g_rwBuffer[4 * 1024];
int g_dstAddress = 0;
int g_length = 0;
int g_runmode = 0;

/**
 * The definition of functional options,
 * 0 corresponds to check id,1 corresponds
 * to program,2 corresponds to read,3 corresponds
 * to erase,4 corresponds to chip erase.
 */
int g_func = 0;
unsigned int g_flashID = 0;

/**
 * The engineering-driven operation error return
 */
int g_error = 0;

int main() {
	/* call nor flash drivers to program */
	flashInit();

	/* for debug flash driver */
	flashTest();

	do {
		__bkpt_label();
		switch (g_func) {
		case 0:
			g_error = flashID(&g_flashID);
			break;
		case 1:
			g_error = flashProgram((char *) g_dstAddress, (char *) g_rwBuffer,
					g_length);
			break;
		case 2:
			g_error = flashRead((char *) g_rwBuffer, (char *) g_dstAddress,
					g_length);
			break;
		case 3:
			g_error = flashErase((char *) g_dstAddress, g_length);
			break;
		case 4:
			g_error = flashChipErase();
			break;
		case 5:
			g_error = flashUnInit();
			break;
		default:
			break;
		}
	} while (1);

}
