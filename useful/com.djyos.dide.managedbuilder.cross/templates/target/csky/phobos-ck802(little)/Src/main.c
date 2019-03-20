/*
 * Name        : main.c
 * Author      : $(author)
 * Version     :
 * Copyright   : $(copyright)
 * Description : Simple function in C, Ansi-style
 */

#include "PHOBOS.h"
#include "CSIDRV_UART.h"
#include "PHOBOS_UART.h"

extern CSKY_DRIVER_UART Driver_UART1;

CSKY_DRIVER_UART *console_uart = &Driver_UART1;

int main() {
	/* TODO */
	return 0;
}
