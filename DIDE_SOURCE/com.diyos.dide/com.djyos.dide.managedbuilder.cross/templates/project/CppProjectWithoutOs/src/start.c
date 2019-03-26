/*
 * start.c
 *
 * Copyright (C) 2008  Hangzhou C-SKY Microsystems Co., Ltd
 *
 */

/*
 *  There are some files - crti.o crtn.o crtbegin.o and crtend.o
 *
 *  crti.o     -- The prologue for _init and _fini (function entry).
 *  crtn.o     -- The epilog for _init and _fini (function return).
 *  crtbegin.o -- Function "__do_global_dtors_aux" and "frame_dummy" from GCC,
 *				  and calling instruction placed in _finit and _init.
 *	crtend.o   -- Function "__do_global_ctors_aux" function from GCC,
 *				  and calling instruction placed in _init.
 */
extern void _init();
extern void _fini();

extern void main();

int firstc ()
{

    /* 
     *  Call C++ constructor function
     * _init:
     *       The initial function will call "__do_global_ctors_aux",
     *   then "__do_global_dtors_aux" will call discontructor functions.
     *   The GCC will generate _init in crtbegin.o
     */
    _init();

	main();

	/*
	 * Call c++ discontructor function.
	 * _fini:
	 *       The finish function will call "__do_global_dtors_aux",
	 *   then "__do_global_dtors_aux" will call discontructor functions.
	 *   The GCC will generate _fini in crtn.o
	 */
	_fini();

    return 0;
}
