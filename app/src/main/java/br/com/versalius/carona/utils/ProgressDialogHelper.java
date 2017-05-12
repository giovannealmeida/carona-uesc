package br.com.versalius.carona.utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Giovanne on 30/06/2016.
 */
public class ProgressDialogHelper {

    private ProgressDialog progressDialog;
    private Context context;

    public ProgressDialogHelper(Context context) {
        this.context = context;
    }

    public void showSpinner(String title, String message, boolean indeterminate, boolean cancelable) {
        progressDialog = ProgressDialog.show(context, title, message, indeterminate, cancelable);
    }

    public void showProgressBar(String title, String message, int maxProgress, boolean cancelable) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setProgress(0);
        progressDialog.setMax(maxProgress);
        progressDialog.setCancelable(cancelable);
        progressDialog.show();
    }

    public void incrementProgressBy(int inc) {
        progressDialog.incrementProgressBy(inc);
        if(progressDialog.getMax() == progressDialog.getProgress()){
            progressDialog.dismiss();
        }
    }

    public void dismiss() {
        progressDialog.dismiss();
    }
}
