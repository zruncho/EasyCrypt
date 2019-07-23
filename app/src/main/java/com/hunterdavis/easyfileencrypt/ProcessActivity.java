package com.hunterdavis.easyfileencrypt;

import android.app.Activity;
//import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class ProcessActivity extends Activity{
    public ProgressBar bar;
    public Handler handler = new Handler();
    TextView label;

    public static String ACTION="action";
    public static String OLDFILE="oldfile";
    public static String NEWFILE="newfile";
    public static String PASS="password";



    private String act="";
    private File oldf;
    private File newf;
    private String pass="";
    private String oldfile="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        bar = (ProgressBar) findViewById(R.id.progBar);

        Intent myin= getIntent();
        Bundle b = myin.getExtras();

        if(b!=null)
        {
             act =(String) b.get(ACTION);
            oldf = new File((String) b.get(OLDFILE));
            newf = new File((String) b.get(NEWFILE));
            pass =(String) b.get(PASS);
            oldfile= (String) b.get(OLDFILE);


            TextView workf = (TextView) findViewById(R.id.FileLabel);
            workf.setText("File: " + oldfile);

            TextView infop = (TextView) findViewById(R.id.ProcessInfo);
            infop.setText("Process info: " + act + " using DESede/CBC/PKCS5Padding");

        }



        Thread process = new Thread(new Runnable() {

                public void run() {
                    oldf.renameTo(newf);
                    bar.setProgress(0);
                    if (act.equals("decrypt")) {
                        SimpleCrypto crypto = new SimpleCrypto();
                        try {
                             crypto.decryptFile(newf, oldf, pass, handler, bar);
                        } catch (InvalidKeyException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (InvalidAlgorithmParameterException e) {
                            e.printStackTrace();
                        }
                        newf.delete();
                     //   Toast.makeText(getBaseContext(), "File Decrypted",
                     //           Toast.LENGTH_SHORT).show();

                    } else {

                        SimpleCrypto crypto = new SimpleCrypto();
                        try {
                             crypto.encryptFile(newf, oldf, pass, handler, bar);
                        } catch (InvalidKeyException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (InvalidAlgorithmParameterException e) {


                        }
                        newf.delete();
                       // Toast.makeText(getBaseContext(), "Encrypted ",
                       // Toast.LENGTH_SHORT).show();

                    }

                    handler.post( new Runnable() {
                        public void run() {
                            ProcessActivity.this.finish();
                        }
                    } );
                }
            });
        process.start();

        Intent returnIntent = new Intent();
        returnIntent.putExtra(OLDFILE,oldfile);
        setResult(Activity.RESULT_OK,returnIntent);
       // finish();

    }
   /* @Override
    public void onDestroy(){
        super.onDestroy();
        Intent returnIntent = new Intent();
        returnIntent.putExtra(OLDFILE,oldfile);
        setResult(Activity.RESULT_OK,returnIntent);
    }*/

    }



