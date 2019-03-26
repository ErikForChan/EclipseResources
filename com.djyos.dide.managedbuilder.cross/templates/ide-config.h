/*
 * ide-config.h -- config about cpu cache and mgu for CKCORE.
 *
 */

#ifndef __CKCORE_IDE_CONFIG_H__
#define __CKCORE_IDE_CONFIG_H__

/* CONFIG_CKCPU_MGU_BLOCKS	---- MGU block priority setting value */
#define CONFIG_CKCPU_MGU_BLOCKS 0xff0f  

/* CONFIG_CKCPU_MGU_REGION(1-4)	---- MGU (1-4) block base address and size. */
/* #define CONFIG_CKCPU_MGU_REGION1 */  
/* #define CONFIG_CKCPU_MGU_REGION2 */  
/* #define CONFIG_CKCPU_MGU_REGION3 */  
/* #define CONFIG_CKCPU_MGU_REGION4 */  

/* CONFIG_CKCPU_ICACHE		---- Instruction cache enable */
#define CONFIG_CKCPU_ICACHE

/* CONFIG_CKCPU_DCACHE		---- Data cache enable */
#define CONFIG_CKCPU_DCACHE



#endif  

