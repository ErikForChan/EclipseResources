/*
 * crt0.S -- startup codes for CSKY.
 *
 * Define a exception vector table
 *
 * Initial PSR register.
 * Initial VBR register.
 * Initial r0(stack pointer) register.
 * Initial MGU (for csky having MGU)
 * Initial Cache.
 * Initial SPM (for csky having SPM).
 * Initial BSS section to "0"
 * Then goto "main".
 *
 * Copyright (C) 2008  Hangzhou C-SKY Microsystems Co., Ltd
 */

/*#include "$(projectName).h"*/

.file "crt0.S"

/*
 * For importing variable or functions from script file "csky.ld".
 */
.import __stack		/* The stack pointer from linking script file */
.import	__sbss		/* The end of bss from linking script file */
.import __ebss		/* The end of bss from linking script file */

/*
 * For importing variable or functions from other c or assemble files.
 */
.import main			/* The c codes entry */
.import spm_init        /* Initial csky SPM */


/* For exporting varialbe or functions */
.export	csky_vsr_table	/* Vector table base address. */
.export __start		/* The system entry. */
.export	__bkpt_label

.section .exp_table
/* Vector table space. */
.align 10
csky_vsr_table:
.rept 128
.long __default_exception_handler
.endr

/* The csky startup codes. */
.text
	/*
	 *	
	 * This is the codes first entry point. This is where it all
	 * begins...
	 */

__start:
	/* Init psr value */
	lrw		r1, 0x80000100
	mtcr	r1, psr
	
	/*
	 * Setup initial vector base table for interrupts and exceptions
	 */
	lrw		r1, csky_vsr_table
	mtcr	r1, vbr
	
	/* Initialize the stack pointer from the linker definition. */
	lrw		r7,	__stack
	mov		sp,	r7

	/*
	 * Zero out the bss region.
	 */
	lrw		r1,	__sbss	/* Get start of bss from linking script file */
	lrw		r2, __ebss	/* Get end of bss from linking script file */
	subu	r2, r1		/* Calculate size of bss */
	lsri	r2, 2		/* Size of whole words */
	cmpnei	r2, 0
	bf		__goto_c
	movi	r3, 0		/* Set zero value to write */
__clear_bss:
	stw		r3, (r1)	/* Zero next word */
	addi	r1, 4		/* Increase bss pointer */
	decne	r2		/* Decrease counter */
	bt		__clear_bss	/* Repeat for all bss */

	
	/*
	 * Assember start up done, start code proper.
	 * C codes start here!
	 */
__goto_c:
	jbsr	main
	
	/* Should never get here. */
__exit:
    bkpt
	br		__exit

__default_exception_handler:
	br		__default_exception_handler
	rte

__bkpt_label:
	mov		r0,	r0
	rts
