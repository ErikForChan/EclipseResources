/*
 * ckm5108.h -- config about cpu cache and mgu for CSKY.
 *
 */

#ifndef __CSKY_IDE_CONFIG_H__
#define __CSKY_IDE_CONFIG_H__

/* CONFIG_CK510_MGU_BLOCKS	---- MGU block priority setting value */
#define CONFIG_CKCPU_MGU_BLOCKS 0xff0f  

/* CONFIG_CK510_MGU_REGION(1-4)	---- MGU (1-4) block base address and size. */

/* 0 - baseaddr: 0x0; size: 4G */
#define CONFIG_CKCPU_MGU_REGION1        0x3f
/* 1- baseaddr: 0x28000000; size: 8M */
#define CONFIG_CKCPU_MGU_REGION2        0x2800002d
/* 2- Disable */
#undef CONFIG_CKCPU_MGU_REGION3
/* 3- Disable */
#undef CONFIG_CKCPU_MGU_REGION4  

/* CONFIG_CKCPU_ICACHE		---- Instruction cache enable */
#define CONFIG_CK510_ICACHE

/* CONFIG_CK510_DCACHE		---- Data cache enable */
#define CONFIG_CK510_DCACHE



#endif  

