package dev.visualfx;
final class Config {
 double speed=1,radius=.85,glow=.7,hitStrength=1;int shards=18,optimizationPreset;boolean optimization;
 int color(double phase){return 0x39FF14;}
 int effectShards(){return optimization?Math.min(shards,switch(optimizationPreset){case 0->24;case 1->12;default->6;}):shards;}
 int detailSegments(int original){return optimization?Math.max(12,original/(optimizationPreset+2)):original;}
 int glowPasses(){return optimization?(optimizationPreset==0?2:1):3;}
}
