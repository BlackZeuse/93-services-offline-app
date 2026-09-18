package co.za.techart.ninetythree;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class PdfGenerator {
    static final int PAGE_W = 595;
    static final int PAGE_H = 842;
    static final int ORANGE = Color.rgb(242, 140, 0);
    static final int BLACK = Color.rgb(20, 20, 20);
    static final int GRAY = Color.rgb(95, 95, 95);

    public static class Doc {
        public String type, number, date, customer, phone, email, address, jobRef, vehicleReg;
        public ArrayList<MainActivity.Item> items;
        public double discount, rate, vat, total, paid;
        public boolean applyVat;
        public String paymentStatus;

        public Doc(String t, String n, String d, String c, String p, String e, String a, String j, String v,
                   ArrayList<MainActivity.Item> i, double disc, boolean av, double r, double va, double tot,
                   String ps, double pd) {
            type=t; number=n; date=d; customer=c; phone=p; email=e; address=a; jobRef=j; vehicleReg=v;
            items=new ArrayList<>(i); discount=disc; applyVat=av; rate=r; vat=va; total=tot; paymentStatus=ps; paid=pd;
        }
    }

    public static File generate(Context ctx, Doc d) throws Exception {
        PdfDocument pdf = new PdfDocument();
        Bitmap logo = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.logo);
        int itemIndex = 0;
        int pageNo = 1;

        while (itemIndex < d.items.size() || pageNo == 1) {
            PdfDocument.Page page = pdf.startPage(new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create());
            Canvas c = page.getCanvas();
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
            float y = drawHeader(c, p, logo, d);

            if (pageNo == 1) {
                y = drawCustomer(c, p, d, y);
                y += 8;
            }

            y = drawTableHeader(c, p, y);
            while (itemIndex < d.items.size()) {
                MainActivity.Item i = d.items.get(itemIndex);
                float rowH = Math.max(30, wrapHeight(i.description, p, 188));
                if (y + rowH > 575) break;
                drawItemRow(c, p, i, itemIndex + 1, y, rowH);
                y += rowH;
                itemIndex++;
            }

            boolean last = itemIndex >= d.items.size();
            if (last) {
                y += 10;
                if (y > 625) {
                    pdf.finishPage(page);
                    pageNo++;
                    continue;
                }
                y = drawTotals(c, p, d, y);
                drawPaymentFooter(c, p, d, y);
            } else {
                p.setTextSize(8); p.setColor(GRAY); p.setTypeface(Typeface.create("sans", Typeface.ITALIC));
                c.drawText("Continued on next page…", 450, 812, p);
            }

            pdf.finishPage(page);
            if (last) break;
            pageNo++;
        }

        File base = ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (base == null) throw new IllegalStateException("App document storage is not available.");
        if (!base.exists()) base.mkdirs();
        String safe = d.number.replaceAll("[^A-Za-z0-9_-]", "_");
        File out = new File(base, safe + ".pdf");
        try (FileOutputStream os = new FileOutputStream(out)) { pdf.writeTo(os); }
        pdf.close();
        saveToDownloads(ctx, out, safe + ".pdf");
        return out;
    }

    static float drawHeader(Canvas c, Paint p, Bitmap logo, Doc d) {
        p.setColor(Color.WHITE); c.drawRect(0,0,PAGE_W,PAGE_H,p);
        p.setColor(ORANGE); c.drawRect(0,0,PAGE_W,5,p);
        if (logo != null) {
            float maxW=135, maxH=88, scale=Math.min(maxW/logo.getWidth(), maxH/logo.getHeight());
            float w=logo.getWidth()*scale, h=logo.getHeight()*scale;
            c.drawBitmap(logo, null, new android.graphics.RectF(45,22,45+w,22+h), p);
        }
        p.setColor(BLACK); p.setTypeface(Typeface.create("sans", Typeface.BOLD)); p.setTextSize(16);
        c.drawText("93 SERVICES (PTY) LTD", 180, 35, p);
        p.setTypeface(Typeface.create("sans", Typeface.NORMAL)); p.setTextSize(9);
        String[] lines = {"REG: 2019/238585/07", "SASOL MVUDI PARK, THOHOYANDOU, 0950", "Cell: 079 319 9611", "Email: 93eventtravel@gmail.com"};
        float yy=51; for(String s:lines){ c.drawText(s,180,yy,p); yy+=12; }
        p.setTypeface(Typeface.create("sans", Typeface.BOLD)); p.setTextSize(20); p.setTextAlign(Paint.Align.RIGHT);
        c.drawText(d.type, 550, 38, p); p.setTextSize(9); p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        c.drawText("No: "+d.number, 550, 55, p); c.drawText("Date: "+d.date, 550, 69, p); p.setTextAlign(Paint.Align.LEFT);
        p.setColor(ORANGE); c.drawRect(45,102,550,104,p);
        return 116;
    }

    static float drawCustomer(Canvas c, Paint p, Doc d, float y) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1); p.setColor(Color.rgb(190,190,190));
        c.drawRect(45,y,550,y+92,p); p.setStyle(Paint.Style.FILL);
        p.setColor(BLACK); p.setTypeface(Typeface.create("sans", Typeface.BOLD)); p.setTextSize(10);
        c.drawText("CUSTOMER / JOB DETAILS", 58, y+17, p);
        p.setTypeface(Typeface.create("sans", Typeface.NORMAL)); p.setTextSize(9);
        c.drawText("Name: "+safe(d.customer), 58, y+35, p);
        c.drawText("Phone: "+safe(d.phone), 300, y+35, p);
        c.drawText("Email: "+safe(d.email), 58, y+51, p);
        c.drawText("Job/Ref: "+safe(d.jobRef), 300, y+51, p);
        c.drawText("Vehicle Reg: "+safe(d.vehicleReg), 58, y+67, p);
        c.drawText("Address:", 300, y+67, p);
        drawWrapped(c,p,safe(d.address),340,y+67,11,205);
        return y+104;
    }

    static float drawTableHeader(Canvas c, Paint p, float y) {
        float x0=45,x1=90,x2=300,x3=365,x4=460,x5=550;
        p.setStyle(Paint.Style.FILL); p.setColor(ORANGE); c.drawRect(x0,y,x5,y+32,p);
        p.setStyle(Paint.Style.STROKE); p.setColor(Color.rgb(150,150,150)); c.drawRect(x0,y,x5,y+32,p);
        c.drawLine(x1,y,x1,y+32,p); c.drawLine(x2,y,x2,y+32,p); c.drawLine(x3,y,x3,y+32,p); c.drawLine(x4,y,x4,y+32,p);
        p.setStyle(Paint.Style.FILL); p.setColor(BLACK); p.setTypeface(Typeface.create("sans", Typeface.BOLD)); p.setTextSize(8);
        p.setTextAlign(Paint.Align.CENTER); c.drawText("ITEM",67,y+13,p); c.drawText("NO",67,y+25,p);
        c.drawText("DESCRIPTION",195,y+20,p); c.drawText("QTY",332,y+20,p); c.drawText("UNIT PRICE",412,y+20,p); c.drawText("TOTAL",505,y+20,p);
        p.setTextAlign(Paint.Align.LEFT); return y+32;
    }

    static void drawItemRow(Canvas c, Paint p, MainActivity.Item i, int no, float y, float h) {
        float x0=45,x1=90,x2=300,x3=365,x4=460,x5=550;
        p.setStyle(Paint.Style.FILL); p.setColor(Color.WHITE); c.drawRect(x0,y,x5,y+h,p);
        p.setStyle(Paint.Style.STROKE); p.setColor(Color.LTGRAY); c.drawRect(x0,y,x5,y+h,p);
        c.drawLine(x1,y,x1,y+h,p); c.drawLine(x2,y,x2,y+h,p); c.drawLine(x3,y,x3,y+h,p); c.drawLine(x4,y,x4,y+h,p);
        p.setStyle(Paint.Style.FILL); p.setColor(BLACK); p.setTypeface(Typeface.create("sans", Typeface.NORMAL)); p.setTextSize(9); p.setTextAlign(Paint.Align.CENTER);
        c.drawText(String.format(Locale.US,"%02d",no),67,y+18,p); c.drawText(formatQty(i.qty),332,y+18,p);
        p.setTextAlign(Paint.Align.LEFT); drawWrapped(c,p,safe(i.description),98,y+17,13,190);
        c.drawText("R "+fmt(i.unitPrice),375,y+18,p); c.drawText("R "+fmt(i.qty*i.unitPrice),468,y+18,p);
    }

    static float drawTotals(Canvas c, Paint p, Doc d, float y) {
        double subtotal = d.total + d.discount - d.vat;
        float left=345,right=550,top=y+5;
        p.setStyle(Paint.Style.STROKE); p.setColor(Color.rgb(110,110,110)); c.drawRect(left,top,right,top+120,p);
        c.drawLine(left,top+30,right,top+30,p); c.drawLine(left,top+60,right,top+60,p); c.drawLine(left,top+90,right,top+90,p);
        p.setStyle(Paint.Style.FILL); p.setColor(BLACK); p.setTypeface(Typeface.create("sans", Typeface.BOLD)); p.setTextSize(9);
        c.drawText("SUBTOTAL", left+10, top+19, p); c.drawText("DISCOUNT", left+10, top+49, p); c.drawText(d.applyVat?"VAT @ "+fmt(d.rate)+"%":"VAT", left+10, top+79, p); c.drawText("TOTAL", left+10, top+109,p);
        p.setTypeface(Typeface.create("sans", Typeface.NORMAL)); p.setTextAlign(Paint.Align.RIGHT);
        c.drawText("R "+fmt(subtotal), right-10, top+19,p); c.drawText("R "+fmt(d.discount), right-10, top+49,p); c.drawText(d.applyVat?"R "+fmt(d.vat):"N/A", right-10, top+79,p);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD)); c.drawText("R "+fmt(d.total), right-10, top+109,p); p.setTextAlign(Paint.Align.LEFT);
        return top+135;
    }

    static void drawPaymentFooter(Canvas c, Paint p, Doc d, float y) {
        float top=Math.min(y,690);
        p.setColor(BLACK); p.setTypeface(Typeface.create("sans", Typeface.BOLD)); p.setTextSize(8);
        c.drawText("PAYMENT DETAILS",45,top,p);
        p.setTypeface(Typeface.create("sans", Typeface.NORMAL)); p.setTextSize(8);
        String[] bank={"PAYMENT CAN BE DONE AT ANY FIRST NATIONAL BANK BRANCH",
                "NAME: 93 SERVICES (PTY) LTD", "ACCOUNT NO: 62813167013", "BRANCH: 250655", "THOHOYANDOU"};
        float yy=top+15; for(String s:bank){c.drawText(s,45,yy,p);yy+=12;}
        if ("INVOICE".equalsIgnoreCase(d.type)) {
            c.drawText("Payment status: "+safe(d.paymentStatus), 320, top+15,p);
            c.drawText("Amount paid: R "+fmt(d.paid), 320, top+29,p);
            c.drawText("Balance: R "+fmt(Math.max(0,d.total-d.paid)), 320, top+43,p);
        }
        p.setTypeface(Typeface.create("sans", Typeface.ITALIC)); p.setTextSize(8);
        c.drawText("Please send proof of payment to 93eventtravel@gmail.com after payment in order for an order to be released.",45,top+80,p);
        c.drawText("Please use the document number as your reference.",45,top+92,p);
        p.setTypeface(Typeface.create("sans", Typeface.NORMAL)); p.setTextSize(7); p.setColor(GRAY);
        c.drawText("Designed & Developed by TECHART (PTY) LTD",45,818,p);
        p.setTextAlign(Paint.Align.RIGHT); c.drawText("079 319 9611 - MR NDOU",550,818,p); p.setTextAlign(Paint.Align.LEFT);
    }

    static float wrapHeight(String s, Paint p, float maxW) { return Math.max(30, wrap(s,p,maxW).size()*13+9); }
    static void drawWrapped(Canvas c, Paint p, String s, float x, float y, float line, float maxW) { for(String z:wrap(s,p,maxW)){c.drawText(z,x,y,p);y+=line;} }
    static ArrayList<String> wrap(String s, Paint p, float maxW) {
        ArrayList<String> out=new ArrayList<>(); if(s==null)s=""; StringBuilder b=new StringBuilder();
        for(String w:s.split(" ")){String t=b.length()==0?w:b+" "+w; if(p.measureText(t)>maxW && b.length()>0){out.add(b.toString());b=new StringBuilder(w);} else b=new StringBuilder(t);} if(b.length()>0)out.add(b.toString()); return out;
    }
    static String safe(String s){return s==null?"":s.trim();}
    static String fmt(double v){return String.format(Locale.US,"%,.2f",v);}
    static String formatQty(double v){return v==(long)v?Long.toString((long)v):String.format(Locale.US,"%.2f",v);}

    static void saveToDownloads(Context ctx, File src, String name) {
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                ContentResolver r=ctx.getContentResolver();
                ContentValues v=new ContentValues();
                v.put(MediaStore.Downloads.DISPLAY_NAME,name); v.put(MediaStore.Downloads.MIME_TYPE,"application/pdf");
                v.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/93 Services");
                v.put(MediaStore.Downloads.IS_PENDING,1);
                Uri u=r.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);
                if(u!=null){try(FileInputStream in=new FileInputStream(src);OutputStream out=r.openOutputStream(u)){byte[]b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);}ContentValues done=new ContentValues();done.put(MediaStore.Downloads.IS_PENDING,0);r.update(u,done,null,null);}
            }
        } catch(Exception ignored) { }
    }
}
