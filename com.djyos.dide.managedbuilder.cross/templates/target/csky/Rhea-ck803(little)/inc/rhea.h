/*
 * Description: rhea.h - Define the system configuration, memory & IO base
 * address, flash size & address, interrupt resource for rhea soc.
 *
 * Copyright (C) : 2008 Hangzhou C-SKY Microsystems Co.,LTD.
 * Author(s): Liu Bing (bing_liu@c-sky.com)
 * Contributors: Liu Bing
 * Date:  2010-06-26
 * Modify by liu jirang(jirang_liu@c-sky.com)  on 2012-10-13
 */

#ifndef __INCLUDE_CKRHEA_H
#define __INCLUDE_CKRHEA_H

/**************************************
 * MCU & Borads.
 *************************************/

/* CPU frequence definition */
#define CPU_DEFAULT_FREQ       12000000  /* Hz */
/* AHB frequence definition */
#define AHB_DEFAULT_FREQ       12000000   /* Hz */
/* APB frequence definition */
#define APB_DEFAULT_FREQ       12000000   /* Hz */

/*
 * define irq number of perpheral modules
 */
#define  CK_INTC_GPIO0		0
#define  CK_INTC_DMAC		1
#define  CK_INTC_MAILBOX	2
#define  CK_INTC_RSA		3
#define  CK_INTC_DES		4
#define  CK_INTC_AES		5
#define  CK_INTC_RXAC		6
#define  CK_INTC_AUAC		7
#define  CK_INTC_Reserved	8
#define  CK_INTC_POWM		9
#define  CK_INTC_PWM		10
#define  CK_INTC_SSI		11
#define  CK_INTC_SPI		12
#define  CK_INTC_IIC		13
#define  CK_INTC_UART0		14
#define  CK_INTC_TIM1		15
#define  CK_INTC_TIM2		16
#define  CK_INTC_TIM3		17
#define  CK_INTC_TIM4		18
#define  CK_INTC_TIM5		19
#define  CK_INTC_TIM6       20

/***** Intc ******/
#define CK_INTC_BASEADDRESS			(0x40018000)

/***** GPIO *****/
#define PCK_GPIOA					(volatile CK_UINT32*)(0x40011000)
#define PCK_GPIOB					(volatile CK_UINT32*)(0x4001100c)
#define PCK_GPIO_Control			(volatile CK_UINT32*)(0x40011030)


#define PORTA0_IRQ_ID				CK_INTC_GPIO0

/***** Uart *******/
#define CK_UART_ADDRBASE0			(volatile CK_UINT32 *)(0x40016000)

#define CK_UART0_IRQID				CK_INTC_UART0

/**** Timer ****/
#define  CK_TIMER1_BASSADDR			(volatile CK_UINT32 *)(0x40017000)
#define  CK_TIMER2_BASSADDR			(volatile CK_UINT32 *)(0x40017014)
#define  CK_TIMER3_BASSADDR			(volatile CK_UINT32 *)(0x40017028)
#define  CK_TIMER4_BASSADDR			(volatile CK_UINT32 *)(0x4001703c)
#define  CK_TIMER5_BASSADDR			(volatile CK_UINT32 *)(0x40017050)
#define  CK_TIMER6_BASSADDR			(volatile CK_UINT32 *)(0x40017064)

#define  CK_TIMER_CONTROL_BASSADDR  (volatile CK_UINT32 *)(0x400170a0)

/*
 * Define number of the timer interrupt
 */
#define  CK_TIMER_IRQ1				CK_INTC_TIM1
#define  CK_TIMER_IRQ2				CK_INTC_TIM2
#define  CK_TIMER_IRQ3				CK_INTC_TIM3
#define  CK_TIMER_IRQ4				CK_INTC_TIM4
#define  CK_TIMER_IRQ5				CK_INTC_TIM5
#define  CK_TIMER_IRQ6				CK_INTC_TIM6

/********* DMA *********/
/*
 * Define DMA tunnel base address
 */
#define CK_DMAC_CH0_BASEADDR		(volatile CK_UINT32*)(0x40007000)
#define CK_DMAC_CH1_BASEADDR		(volatile CK_UINT32*)(0x40007058)
#define CK_DMAC_CH2_BASEADDR		(volatile CK_UINT32*)(0x400070B0)
#define CK_DMAC_CH3_BASEADDR		(volatile CK_UINT32*)(0x40007108)

/*
 * Define DMA control base address
 */
#define CK_DMAC_CONTROL_BASEADDR	(volatile CK_UINT32*)(0x400072C0)

/****** IIC *************/
/*
 * Define IIC base address
 */
#define CK_IIC_ADDRBASE0			(volatile CK_UINT32*)(0x40015000)

/****** SPI *************/
/*
 * Define SPI base address
 */
#define CK_SPI_ADDRBASE0			(volatile CK_UINT32*)(0x40014000)


/****** PWM  *************/
#define CK_PWM_ADDRBASE0			(volatile CK_UINT32*)(0x40012000)


/****** POWM  *************/
#define CK_POWM_ADDRBASE			(volatile CK_UINT32*)(0x4000A000)


#endif /* __INCLUDE_CKRHEA_H */
