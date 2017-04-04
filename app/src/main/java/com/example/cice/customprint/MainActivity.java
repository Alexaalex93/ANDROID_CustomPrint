package com.example.cice.customprint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.printservice.PrintDocument;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.support.v7.view.ViewPropertyAnimatorCompatSet;
import android.view.View;

import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    public class MyPrintDocumentAdapter extends PrintDocumentAdapter {

        Context context;
        private int pageHeight;
        private int pageWidth;
        public PdfDocument myPdfDocument;
        public int totalPages = 4;

        public MyPrintDocumentAdapter(Context context) {
            this.context = context;
        }

        @Override

        public void onLayout(PrintAttributes printAttributes, PrintAttributes printAttributes1, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {

            myPdfDocument = new PrintedPdfDocument(context, printAttributes1);

            pageHeight = printAttributes1.getMediaSize().getHeightMils() / 1000 * 72;
            pageWidth = printAttributes1.getMediaSize().getWidthMils() / 1000 * 72;

            if (cancellationSignal.isCanceled()) {
                layoutResultCallback.onLayoutCancelled();
                return;
            }
            if (totalPages > 0) {
                PrintDocumentInfo.Builder builder = new PrintDocumentInfo.Builder("print_output.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(totalPages);
                PrintDocumentInfo info = builder.build();
                layoutResultCallback.onLayoutFinished(info, true);

            } else {
                layoutResultCallback.onLayoutFailed("Numero de paginas = 0");

            }
        }

        @Override
        public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor parcelFileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {

            for (int i = 0; i < totalPages; i++) {
                if (pageInRange(pageRanges, i)) {
                        PageInfo newPage = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i).create();
                        PdfDocument.Page page = myPdfDocument.startPage(newPage);



                    if (cancellationSignal.isCanceled()) {
                        writeResultCallback.onWriteCancelled();
                        myPdfDocument.close();
                        myPdfDocument = null;

                        return;
                    }

                    drawPage(page, i);
                    myPdfDocument.finishPage(page);

                }
            }
            try {
                myPdfDocument.writeTo(new FileOutputStream(parcelFileDescriptor.getFileDescriptor()));

            } catch (IOException e) {
                writeResultCallback.onWriteFailed(e.toString());
                return;
            } finally {
                myPdfDocument.close();
                myPdfDocument = null;
            }

            writeResultCallback.onWriteFinished(pageRanges);
        }
    }

    private boolean pageInRange(PageRange[] pageRanges, int page) {
        for(int i = 0; i < pageRanges.length; i++) {
            if ((page >= pageRanges[i].getStart()) && (page <= pageRanges[i].getEnd())) {
                return true;
            }
        }
        return false;

    }

    private void drawPage(PdfDocument.Page page, int pageNumber) {
        Canvas canvas = page.getCanvas();
        pageNumber++;

        int titleBaseLine = 72;
        int leftMargin = 54;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        canvas.drawText("Pruea de documento " + pageNumber, leftMargin, titleBaseLine, paint);
        paint.setTextSize(14);
        canvas.drawText("Funciona",
                leftMargin, titleBaseLine
                , paint);
        if (pageNumber % 2 == 0) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.GREEN);
        }
        PageInfo pageInfo = page.getInfo();

        canvas.drawCircle(pageInfo.getPageWidth() / 2, pageInfo.getPageHeight() / 2
                , 150, paint);
    }

    public void imprimir(View view) {
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
        String jobName = "Documento";
        printManager.print(jobName, new MyPrintDocumentAdapter(this), null);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
