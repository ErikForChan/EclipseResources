/*
 * cka5102.h -- config about cpu cache and mgu for CSKY.
 *
 */

#ifndef __CSKY_IDE_CONFIG_H__
#define __CSKY_IDE_CONFIG_H__

/* CONFIG_CKCPU_MGU_BLOCKS	---- MGU block priority setting value */
#define CONFIG_CKCPU_MGU_BLOCKS 0xff0f  

/* CONFIG_CKCPU_MGU_REGION1(0-3)	---- MGU (0-3) block base address and size. */
/* 0- Disable */
#undef CONFIG_CKCPU_MGU_REGION1
/* 1- Disable */
#undef CONFIG_CKCPU_MGU_REGION2  
/* 2- Disable */
#undef CONFIG_CKCPU_MGU_REGION3
/* 3- Disable */
#undef CONFIG_CKCPU_MGU_REGION4  

/* CONFIG_CK510_ICACHE		---- Instruction cache enable */
#define CONFIG_CKCPU_ICACHE       1

/* CONFIG_CK510_DCACHE		---- Data cache enable */
#define CONFIG_CKCPU_DCACHE       1



#endif  

