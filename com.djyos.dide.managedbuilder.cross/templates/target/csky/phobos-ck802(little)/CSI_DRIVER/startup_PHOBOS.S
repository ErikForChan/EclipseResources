/******************************************************************************
 * @file     startup_PHOBOS.S
 * @brief    startup file for PHOBOS. Should use with
 *           GCC for CSKY Embedded Processors
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



	.section .vectors
	.align 10
	.globl	__Vectors
	.type	__Vectors, @object
__Vectors:
	.long	Reset_Handler         /* Reset Handler */
	.rept	31
	.long	Default_Handler
	.endr

	/* External interrupts */
	.long	Default_Handler         /*  0:  GPIOA          */
	.long	CORET_IRQHandler        /*  1:  core Timer     */
	.long	TIM0_IRQHandler         /*  2:  Timer0         */
	.long	TIM1_IRQHandler         /*  3:  Timer1         */
	.long	Default_Handler
	.long	Default_Handler
	.long	UART0_IRQHandler        /*  6:  UART           */
	.long	UART1_IRQHandler        /*  7:  UART           */
	.long	UART2_IRQHandler        /*  8:  UART           */

	.rept	29
	.long	Default_Handler
	.endr

	.size	__Vectors, . - __Vectors

	.text
	.align	1
_start:
	.text
	.align	1
	.globl	Reset_Handler
	.type	Reset_Handler, %function
Reset_Handler:

/* Initialize the normal stack pointer from the linker definition. */
	lrw	a1, __StackTop
	mov	sp, a1

/*
 *  The ranges of copy from/to are specified by following symbols
 *    __etext: LMA of start of the section to copy from. Usually end of text
 *    __data_start__: VMA of start of the section to copy to
 *    __data_end__: VMA of end of the section to copy to
 *
 *  All addresses must be aligned to 4 bytes boundary.
 */
	lrw	r1, __erodata
	lrw	r2, __data_start__
	lrw	r3, __data_end__

	subu	r3, r2
	cmpnei	r3, 0
	bf	.L_loop0_done

.L_loop0:
	ldw	r0, (r1, 0)
	stw	r0, (r2, 0)
	addi	r1, 4
	addi	r2, 4
	subi	r3, 4
	cmpnei	r3, 0
	bt	.L_loop0

.L_loop0_done:

/*
 *  The BSS section is specified by following symbols
 *    __bss_start__: start of the BSS section.
 *    __bss_end__: end of the BSS section.
 *
 *  Both addresses must be aligned to 4 bytes boundary.
 */
	lrw	r1, __bss_start__
	lrw	r2, __bss_end__

	movi	r0, 0

	subu	r2, r1
	cmpnei	r2, 0
	bf	.L_loop1_done

.L_loop1:
	stw	r0, (r1, 0)
	addi	r1, 4
	subi	r2, 4
	cmpnei	r2, 0
	bt	.L_loop1
.L_loop1_done:

#ifndef __NO_SYSTEM_INIT
	bsr	SystemInit
#endif

	bsr	main

__exit:
	bkpt
	.size	Reset_Handler, . - Reset_Handler

	.align	1
	.weak	Default_Handler
	.type	Default_Handler, %function
Default_Handler:
	br	Default_Handler
	.size	Default_Handler, . - Default_Handler

/*    Macro to define default handlers. Default handler
 *    will be weak symbol and just dead loops. They can be
 *    overwritten by other handlers */
	.macro	def_irq_handler	handler_name
	.weak	\handler_name
	.set	\handler_name, Default_Handler
	.endm

	def_irq_handler	CORET_IRQHandler
	def_irq_handler TIM0_IRQHandler
	def_irq_handler TIM1_IRQHandler
	def_irq_handler	UART_IRQHandler

	.end
