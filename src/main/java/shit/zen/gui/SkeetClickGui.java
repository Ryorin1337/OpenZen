package shit.zen.gui;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import shit.zen.ZenClient;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.render.FontStore;
import shit.zen.render.CustomFont;
import shit.zen.render.StencilHelper;
import shit.zen.settings.Setting;
import shit.zen.settings.impl.*;
import shit.zen.utils.misc.CursorUtil;
import shit.zen.utils.render.ColorUtil;
import shit.zen.utils.render.RenderUtil;

import java.util.*;

public class SkeetClickGui extends Screen {
    private static final int PG=0xFF0F0F0F,CG=0xFF0F0F0F,GG=0xFF151515;
    private static final int B10=0xFF404040,B3C=0xFF363636,B30=0xFF4A4A4A;
    private static final int AC=ColorUtil.fromRGB(140,170,80);
    private static final int TX=ColorUtil.fromRGB(220,220,220),TXD=ColorUtil.fromRGB(140,140,140);
    private static final int PW=410,PH=340,CS=40,CT=15;
    private static final float TOP_SCROLL = -15.0f;
    private static final Map<Category,String> CI=new HashMap<>();
    static { CI.put(Category.COMBAT,"A"); CI.put(Category.MOVEMENT,"G");
             CI.put(Category.PLAYER,"F"); CI.put(Category.RENDER,"C");
             CI.put(Category.WORLD,"I");CI.put(Category.EXPLOIT,"J");}

    private static CustomFont fI,fS,fT,fB,fX;
    private static CustomFont ic(){if(fI==null)fI=FontStore.SKEET_ICONS;return fI==null?FontStore.AXIFORMA_BOLD_16:fI;}
    private static CustomFont sm(){if(fS==null)fS=FontStore.AXIFORMA_BOLD_16;return fS==null?FontStore.PINGFANG_16:fS;}
    private static CustomFont tn(){if(fT==null)fT=FontStore.AXIFORMA_REGULAR_14;return fT==null?FontStore.PINGFANG_16:fT;}
    private static CustomFont bd(){if(fB==null)fB=FontStore.AXIFORMA_BOLD_13;return fB==null?FontStore.AXIFORMA_BOLD_16:fB;}
    private static CustomFont xs(){if(fX==null)fX=FontStore.AXIFORMA_REGULAR_12;return fX;}
    private static final float S = 0.80f;

    private final Minecraft mc=Minecraft.getInstance();
    private float px,py,dx,dy; private boolean dr;
    private Category sel=Category.COMBAT;
    private float op=1; private boolean cl;
    private float sc;
    private float cx,cy,cw,ch;
    private String drSlKey; private boolean drSl;
    private float popAnim;
    private float fade=1;
    private float catFade=1f;             // 分类切换渐隐渐现 0→1
    private Category prevCat=Category.COMBAT;
    private final Map<Category,Float> catHoverAnim=new HashMap<>();
    private String popK; private float popX,popY,popW;
    private String[] popOs; private String popC;

    public SkeetClickGui(){super(Component.literal("Skeet"));}

    @Override protected void init(){super.init();px=(width-PW)/2f;py=(height-PH)/2f-20;sc=-15;fade=0;cl=false;
        ZenClient.getInstance().getEventBus().register(this);}
    @Override public void onClose(){cl=true;
        ZenClient.getInstance().getEventBus().unregister(this);}
    private int _mx,_my;
    @Override
    public void render(GuiGraphics g,int mx,int my,float pt){
        _mx=mx;_my=my;
        try {
            if(cl){fade=Math.max(0,fade-0.04f);if(fade<=0){cl=false;if(mc.screen==this)mc.setScreen(null);return;}
            }else fade=Math.min(1,fade+0.04f);
            if(fade<0.005f)return;
            op = ease(fade);
            popAnim = popK != null ? Math.min(1, popAnim + 0.08f) : Math.max(0, popAnim - 0.06f);
            if(width<=0||height<=0)return;
            if(dr){px=mx+dx;py=my+dy;}clamp();
            cx=px+3+CS+14; cy=py+14; cw=PW-CS-28; ch=PH-24;
            clampScroll();
            if (catFade < 1f) catFade = Math.min(1f, catFade + 0.025f);
            var po=g.pose(); po.pushPose();
            bg(g); drawCats(g,mx,my); content(g,mx,my);
            po.popPose();
            super.render(g,mx,my,pt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void clamp(){px=Math.max(20-CS,Math.min(width-PW+60,px));py=Math.max(10,Math.min(height-PH+40,py));}
    public static SkeetClickGui SkeetClickGui = new SkeetClickGui();

    private void bg(GuiGraphics g){
        int A=a(op); var p=g.pose();
        bz(p,px-0.3f,py-0.3f,px+PW+0.5f,py+PH+0.3f,0.5f,0,withAlpha(B10,A));
        bz(p,px,py,px+PW,py+PH,0.5f,0,withAlpha(B3C,A));
        bz(p,px+2,py+2,px+PW-2,py+PH-2,0.5f,0,withAlpha(B3C,A));
        bz(p,px+0.6f,py+0.6f,px+PW-0.5f,py+PH-0.6f,1.3f,B10,withAlpha(0x282828,A));
        bz(p,px+2.5f,py+2.5f,px+PW-2.5f,py+PH-2.5f,0.5f,0,withAlpha(B30,A));
        
        // ?????
        fi(p,px+2,py+2.5f,px+PW-2.5f,py+PH-2.5f,withAlpha(0x000000,A));
        float hw=(PW-6)/2f;
        fi(p,px+6,py+CT,px+PW-43,py+CT+CS*5,withAlpha(0x111111,(int)(120*op)));
        gh(p,px+3,py+3,hw,1,withAlpha(ColorUtil.fromRGB(69,170,242),A),withAlpha(ColorUtil.fromRGB(252,92,101),A));
        gh(p,px+3+hw,py+3,hw,1,withAlpha(ColorUtil.fromRGB(252,92,101),A),withAlpha(ColorUtil.fromRGB(254,211,48),A));
    }

    private void drawCats(GuiGraphics g,int mx,int my){
        float x=px+6; Category[] cs=Category.values();
        for(int i=0;i<cs.length;i++){
            float by=py+CT+i*CS;
            boolean h=mx>=x&&mx<=x+CS&&my>=by&&my<=by+CS;
            boolean s=cs[i]==sel;
            float target = s ? 1f : (h ? 0.65f : 0f);
            float cur = catHoverAnim.getOrDefault(cs[i], 0f);
            cur += (target - cur) * 0.15f;
            if (Math.abs(cur - target) < 0.005f) cur = target;
            catHoverAnim.put(cs[i], cur);
            int br = (int)(91 + cur * (210 - 91));
            String ic2=CI.getOrDefault(cs[i],"?");
            ic().drawString(g.pose(),ic2,x+(CS-ic().getStringWidth(ic2))/2f,
                by+(CS-ic().getFontHeight())/2f+2,withAlpha(ColorUtil.fromRGB(br,br,br),a(op)));
            if(i<cs.length-1) fi(g.pose(),x+4,by+CS-1,x+CS-4,by+CS,withAlpha(B30,(int)(24*a(op))));
        }
    }

    private static class Cell{final Module m;float x,y,w,h;Cell(Module m){this.m=m;}}

    private float calch(Module m){
        float sy=20;
        sy+=16; // Enable
        for(Setting<?> s:m.getSettings()){if(!s.getVisibility().displayable())continue;
            if(s instanceof BooleanSetting)sy+=16;
            else if(s instanceof NumberSetting)sy+=22;
            else if(s instanceof ModeSetting)sy+=24;}
        return sy+10;
    }

    private List<Cell> layoutFor(Category cat){
        var mods=ZenClient.getInstance().getModuleManager().getModulesByCategory(cat);
        List<Cell> out=new ArrayList<>();
        float x0=cx+2, cw2=cw/2f;
        float[] colY={cy+2-sc, cy+2-sc};
        int col=0;
        for(Module m:mods){
            float h=calch(m);
            Cell c=new Cell(m); c.x=x0+(col==0?0:cw2); c.y=colY[col]; c.w=cw2-3; c.h=h;
            out.add(c);
            colY[col]+=h+10;
            col=1-col;
        }
        return out;
    }
    private List<Cell> layout(){
        var mods=ZenClient.getInstance().getModuleManager().getModulesByCategory(sel);
        List<Cell> out=new ArrayList<>();
        float x0=cx+2, cw2=cw/2f;
        float[] colY={cy+2-sc, cy+2-sc};
        int col=0;
        for(Module m:mods){
            float h=calch(m);
            Cell c=new Cell(m); c.x=x0+(col==0?0:cw2); c.y=colY[col]; c.w=cw2-3; c.h=h;
            out.add(c);
            colY[col]+=h+10;
            col=1-col;
        }
        return out;
    }

    private float getMaxScroll() {
        var mods = ZenClient.getInstance().getModuleManager().getModulesByCategory(sel);
        float[] colHeights = {0.0f, 0.0f};
        int col = 0;
        for (Module m : mods) {
            float h = calch(m);
            colHeights[col] += h + 10.0f;
            col = 1 - col;
        }
        float contentHeight = Math.max(colHeights[0], colHeights[1]);
        return Math.max(TOP_SCROLL, contentHeight - (PH - 24.0f) + 2.0f);
    }

    private void clampScroll() {
        sc = Math.max(TOP_SCROLL, Math.min(sc, getMaxScroll()));
    }

    private void content(GuiGraphics g,int mx,int my){
        StencilHelper.beginWrite(false);
        fi(g.pose(),cx,cy,cx+cw,cy+ch,-1);
        StencilHelper.beginRead(true);
        if (catFade < 1f) {
            // 旧分类渐出
            float saved = op;
            op = saved * (1f - catFade);
            for(Cell c:layoutFor(prevCat)){if(c.y+c.h<cy||c.y>cy+ch)continue;card(g,c,mx,my);}
            // 新分类渐入
            op = saved * catFade;
            for(Cell c:layout()){if(c.y+c.h<cy||c.y>cy+ch)continue;card(g,c,mx,my);}
            op = saved;
        } else {
            for(Cell c:layout()){if(c.y+c.h<cy||c.y>cy+ch)continue;card(g,c,mx,my);}
        }
        StencilHelper.end();
        drawPopup(g,mx,my);
    }

    private void card(GuiGraphics g,Cell c,int mx,int my){
        Module m=c.m; int A=a(op); var p=g.pose(); float x=c.x,y=c.y,w=c.w,h=c.h;
        bz(p,x,y-6,x+w,y+h+2,0.5f,0,withAlpha(B10,(int)(255*op)));
        fi(p,x+0.5f,y-5.5f,x+w-0.5f,y+h+1.5f,withAlpha(ColorUtil.fromRGB(25,25,25),A));
        bz(p,x+0.5f,y-5.5f,x+w-0.5f,y+h+1.5f,0.5f,0,withAlpha(ColorUtil.fromRGB(28,28,28),(int)(255*op)));
        float lw=xs().getStringWidth(m.getName());
        fi(p,x+5,y-6,x+7+lw,y-4,withAlpha(ColorUtil.fromRGB(25,25,25),A));
        xs().drawString(p,m.getName(),x+6,y-7,withAlpha(TX,A));

        boolean hs=false; for(Setting<?> s:m.getSettings())if(s.getVisibility().displayable()){hs=true;break;}

        float sx=x+2, sy=y+16;
        // Enable???????????
        { boolean en=m.isEnabled();
          float eNameX=sx+((w-4)-Math.min((w-4)-6,100))/2f;
          float eBtnX=sx+13, eBtnY=sy+(16-6)/2f;
          fi(p,eBtnX,eBtnY,eBtnX+6,eBtnY+6,withAlpha(ColorUtil.fromRGB(40,40,40),A));
          fi(p,eBtnX+1,eBtnY+1,eBtnX+5,eBtnY+5,withAlpha(ColorUtil.fromRGB(71,71,71),A));
          if(en) fi(p,eBtnX+1,eBtnY+1,eBtnX+5,eBtnY+5,withAlpha(AC,A));
          xs().drawString(p,"Enable",eNameX,sy+(16-xs().getFontHeight())/2f,withAlpha(TX,(int)(A*0.8f)));
        } sy+=13;
        for(Setting<?> s:m.getSettings()){if(!(s instanceof BooleanSetting bs)||!s.getVisibility().displayable())continue;
            tog(g,bs,sx,sy,w-4); sy+=13;}
        for(Setting<?> s:m.getSettings()){if(!(s instanceof NumberSetting ns)||!s.getVisibility().displayable())continue;
            sld(g,ns,sx,sy,w-4,mx); sy+=19;}
        for(Setting<?> s:m.getSettings()){if(!(s instanceof ModeSetting ms)||!s.getVisibility().displayable())continue;
            modd(g,ms,m,sx,sy,w-4,mx,my); sy+=23;}
    }
    private void tog(GuiGraphics g,BooleanSetting s,float x,float y,float cw2){
        int A=a(op); var p=g.pose();
        boolean v=s.getValue();
        float nameX=x+(cw2-Math.min(cw2-6,100))/2f;
        float btnX=x+13, btnY=y+(16-6)/2f, btnS=6;
        fi(p,btnX,btnY,btnX+btnS,btnY+btnS,withAlpha(ColorUtil.fromRGB(40,40,40),A));
        fi(p,btnX+1,btnY+1,btnX+btnS-1,btnY+btnS-1,withAlpha(ColorUtil.fromRGB(71,71,71),A));
        if(v) fi(p,btnX+1,btnY+1,btnX+btnS-1,btnY+btnS-1,withAlpha(AC,A));
        xs().drawString(p,s.getName(),nameX,y+(16-xs().getFontHeight())/2f,withAlpha(TX,(int)(A*0.8f)));
    }
    private void sld(GuiGraphics g,NumberSetting ns,float x,float y,float cw2,int mx){
        int A=a(op); var p=g.pose(); float sw=Math.min(cw2-6,100);
        double mn=ns.getMin().doubleValue(),mx2=ns.getMax().doubleValue(),rg=Math.max(1,mx2-mn);
        double cur=ns.getValue().doubleValue();
        float slCX=x+(cw2-sw)/2f, sy=y+11;
        String nm=cap(ns.getName());
        xs().drawString(p,nm,slCX,y+1,withAlpha(TX,(int)(A*0.8f)));
        String vs=(cur==(int)cur)?String.valueOf((int)cur):String.format("%.1f",cur);
        float vsW=xs().getStringWidth(vs);
        xs().drawString(p,vs,slCX+sw-vsW,y+1,withAlpha(TX,(int)(A*0.92f)));
        // ???
        if(drSl&&drSlKey!=null){
            int ci=drSlKey.indexOf(':');
            if(ci>0&&drSlKey.substring(ci+1).equals(ns.getName())){
                double pr2=(mx-slCX)/sw;
                double v=mn+pr2*(mx2-mn);
                double st=ns.getStep().doubleValue();v=Math.round(v/st)*st;
                ns.setValue(Math.max(mn,Math.min(mx2,v)));cur=ns.getValue().doubleValue();
            }
        }
        double pr=(cur-mn)/rg;
        // 滑轨背景 — 深灰
        fi(p,slCX,sy,slCX+sw,sy+2,withAlpha(ColorUtil.fromRGB(60,60,60),A));
        // 滑块填充 — 绿色
        float fw=(float)(pr*sw); if(fw>0) fi(p,slCX,sy,slCX+fw,sy+2,withAlpha(AC,(int)(A)));
    }
    private void modd(GuiGraphics g,ModeSetting ms,Module mod,float x,float y,float cw2,int mx,int my){
        int A=a(op); var p=g.pose(); float dw=Math.min(cw2-6,100),ih=10;
        float boxX=x+(cw2-dw)/2f, dy2=y+11;
        // ??????
        xs().drawString(p,ms.getName(),boxX,y+1,withAlpha(TX,(int)(A*0.8f)));
        int bg=ColorUtil.fromRGB(40,40,40);
        fi(p,boxX,dy2,boxX+dw,dy2+ih,withAlpha(bg,A));
        bz(p,boxX,dy2,boxX+dw,dy2+ih,0.08f,0,withAlpha(0x888888,(int)(200*A)));
        // ????????? 0.7????????
        xs().drawString(p,ms.getValue(),boxX+3,dy2+2,withAlpha(TX,(int)(A*0.8f)));
        if(popK!=null&&popK.equals(mod.getName()+":"+ms.getName())){popX=boxX;popY=dy2+ih;popW=dw;popOs=ms.getModes();popC=ms.getValue();}
    }
    private void drawPopup(GuiGraphics g,int mx,int my){
        if(popOs==null||popK==null)return;
        int A=a(op); var p=g.pose(); float bx=popX,by=popY;
        float pw=popW; for(String o:popOs){float w=xs().getStringWidth(o)+8;if(w>pw)pw=w;}
        float ph=popOs.length*14+4;
        if(bx+pw>cx+cw)bx=cx+cw-pw; if(by+ph>cy+ch)by=cy+ch-ph;
        // ????
        float scaleY = ease(popAnim);
        float clipH = ph * scaleY;
        fi(p,bx,by,bx+pw,by+clipH,withAlpha(ColorUtil.fromRGB(40,40,40),A));
        bz(p,bx,by,bx+pw,by+clipH,0.5f,0,withAlpha(0x000000,(int)(200*A)));
        for(int i=0;i<popOs.length;i++){ boolean s=popOs[i].equals(popC); xs().drawString(p,popOs[i],bx+4,by+3+i*14,s?withAlpha(AC,A):withAlpha(TX,A)); }
    }


    @Override
    public boolean mouseClicked(double mxd,double myd,int btn){
        float mx=(float)mxd,my=(float)myd;
        if(popK!=null&&popOs!=null){
            float bx=popX,by=popY; float pw=60;
            for(String o:popOs){float w=xs().getStringWidth(o)+8;if(w>pw)pw=w;}
            float ph=popOs.length*14+4;
            if(mx>=bx&&mx<=bx+pw&&my>=by&&my<=by+ph){
                for(int i=0;i<popOs.length;i++){if(my>=by+2+i*14&&my<=by+2+i*14+14){
                    int ci=popK.indexOf(':');if(ci>0){
                        String mn=popK.substring(0,ci),sn=popK.substring(ci+1);
                        for(Module m2:ZenClient.getInstance().getModuleManager().getModules())
                            if(m2.getName().equals(mn))for(Setting<?> s:m2.getSettings())
                                if(s instanceof ModeSetting&&s.getName().equals(sn))((ModeSetting)s).setValue(popOs[i]);
                    }popK=null;popOs=null;return true;
                }}
            }popK=null;popOs=null;return true;
        }
        if(btn==0&&CursorUtil.isInBounds(mx,my,px,py,PW,12)){dr=true;dx=px-mx;dy=py-my;return true;}
        float cxp=px+6; Category[] cs=Category.values();
        for(int i=0;i<cs.length;i++){float by=py+CT+i*CS;
            if(CursorUtil.isInBounds(mx,my,cxp,by,CS,CS)){
                if (cs[i] != sel) { prevCat = sel; sel = cs[i]; catFade = 0f; }
                sc=-15;return true;}}
        for(Cell c:layout()){
            Module m2=c.m; float x=c.x,y=c.y,w=c.w;
            float sx=x+2,sy=y+16;
            // Enable ???
            { if(CursorUtil.isInBounds(mx,my,sx+13,sy+5,6,6)){m2.toggle();return true;}
            } sy+=13;
            for(Setting<?> s:m2.getSettings()){if(!(s instanceof BooleanSetting bs)||!s.getVisibility().displayable())continue;
                if(CursorUtil.isInBounds(mx,my,sx,sy,w-4,16)){bs.setValue(!bs.getValue());return true;} sy+=13;}
            for(Setting<?> s:m2.getSettings()){if(!(s instanceof NumberSetting ns)||!s.getVisibility().displayable())continue;
                float slW=Math.min(w-4-6,100),slY=sy+11;
                float slX=sx+((w-4)-slW)/2f;
                if(CursorUtil.isInBounds(mx,my,slX,slY,slW,3)&&btn==0){
                    drSlKey=m2.getName()+":"+ns.getName();drSl=true;
                    double pr2=(mx-slX)/slW,v=ns.getMin().doubleValue()+pr2*(ns.getMax().doubleValue()-ns.getMin().doubleValue());
                    double st=ns.getStep().doubleValue();v=Math.round(v/st)*st;
                    ns.setValue(Math.max(ns.getMin().doubleValue(),Math.min(ns.getMax().doubleValue(),v)));return true;}
                sy+=19;}
            for(Setting<?> s:m2.getSettings()){if(!(s instanceof ModeSetting ms)||!s.getVisibility().displayable())continue;
                float mdw=Math.min(w-4-6,100),dy2=sy+11,ih=10;
                float boxX=sx+((w-4)-mdw)/2f;
                if(CursorUtil.isInBounds(mx,my,boxX,dy2,mdw,ih)){
                    String k2=m2.getName()+":"+ms.getName();
                    if(k2.equals(popK)){popK=null;popOs=null;}
                    else{popK=k2;popOs=null;}
                    return true;
                } sy+=23;}
        }
        return super.mouseClicked(mxd,myd,btn);
    }

    @Override public boolean mouseReleased(double mxd,double myd,int btn){dr=false;drSl=false;drSlKey=null;return super.mouseReleased(mxd,myd,btn);}
    @Override public boolean mouseScrolled(double mxd,double myd,double delta){
        if(CursorUtil.isInBounds((float)mxd,(float)myd,cx,cy,cw,ch)){
            sc = Math.max(TOP_SCROLL, Math.min(sc - ((float)delta * 15.0f), getMaxScroll()));
            return true;
        }
        return super.mouseScrolled(mxd,myd,delta);
    }
    @Override public boolean keyPressed(int k,int s2,int mo){
        if(k==GLFW.GLFW_KEY_ESCAPE||k==GLFW.GLFW_KEY_INSERT||k==GLFW.GLFW_KEY_DELETE){onClose();return true;}
        return super.keyPressed(k,s2,mo);
    }
    @Override public boolean isPauseScreen(){return false;}

    private static void fi(PoseStack p,float x1,float y1,float x2,float y2,int c){
        if(x2<=x1||y2<=y1)return;
        RenderUtil.drawFilledRect(p,x1,y1,x2-x1,y2-y1,c);}
    private static void bz(PoseStack p,float x1,float y1,float x2,float y2,float lw,int f,int b){
        if(x2<=x1||y2<=y1)return;
        // 填充
        if((f&0xFF000000)!=0) fi(p,x1,y1,x2,y2,f);
        // 四条边 = 四个 fi 矩形
        fi(p,x1,y1,x2,y1+lw,b);            // 上
        fi(p,x1,y2-lw,x2,y2,b);            // 下
        fi(p,x1,y1+lw,x1+lw,y2-lw,b);      // 左（不含角）
        fi(p,x2-lw,y1+lw,x2,y2-lw,b);      // 右（不含角）
    }
    private static void gh(PoseStack p,float x,float y,float w,float h,int ca,int cb){
        if(w<=0||h<=0)return;
        // 水平渐变：分 N 段插值，平滑过渡
        int steps = Math.max(2, Math.min(64, (int)Math.ceil(w / 4f)));
        float segW = w / steps;
        for (int i = 0; i < steps; i++) {
            float t = (float)i / steps;
            int c = lerpColor(ca, cb, t);
            fi(p, x + i * segW, y, x + (i + 1) * segW, y + h, c);
        }
    }
    private static int lerpColor(int a, int b, float t){
        int ia = (int)(((a>>24&0xFF) * (1-t) + (b>>24&0xFF) * t));
        int ir = (int)(((a>>16&0xFF) * (1-t) + (b>>16&0xFF) * t));
        int ig = (int)(((a>> 8&0xFF) * (1-t) + (b>> 8&0xFF) * t));
        int ib = (int)(((a    &0xFF) * (1-t) + (b    &0xFF) * t));
        return (ia<<24)|(ir<<16)|(ig<<8)|ib;
    }
    private static int withAlpha(int rgb,int alpha){return(alpha&0xFF)<<24|rgb&0xFFFFFF;}
    private static int a(float f){return(int)(255*Math.max(0,Math.min(1,f)));}
    private static float ease(float t){return(float)(1-Math.pow(1-t,3));}
    private static String cap(String s){return s.isEmpty()?s:s.substring(0,1).toUpperCase()+s.substring(1).toLowerCase();}
}
