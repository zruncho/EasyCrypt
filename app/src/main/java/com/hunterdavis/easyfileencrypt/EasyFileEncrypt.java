package com.hunterdavis.easyfileencrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.crypto.BadPaddingException;
import javax.crypto.SecretKeyFactory;

//import com.google.ads.AdRequest;
//import com.google.ads.AdView;

public class EasyFileEncrypt extends Activity {

    private static final AlgorithmParameterSpec AES_Key_Size = null;

    int SELECT_FILE = 122;
    int SELECT_KEYFILE = 333;

    String filePath = "";
    String password = "";
    int PROCESS_FILE = 223;


    static long fileSizeInBytes = 0;
    private Context magicContext;
    public Vector magicNamesVector;
    public Boolean lastEncrypt = false;
    public Boolean encok=false;

    private String  TargetFileMD5="";

    private File Keyfile = null;
    private String KeyFilePass = "";

    private String decrypted = "";
    private String encrypted = "";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        lastEncrypt = false;
        magicNamesVector = new Vector();



       // Create an anonymous implementation of OnClickListener
        OnClickListener loadButtonListner = new OnClickListener() {
            public void onClick(View v) {
                // do something when the button is clicked

                // in onCreate or any event where your want the user to
                Intent intent = new Intent(v.getContext(), FileDialog.class);
                intent.putExtra(FileDialog.START_PATH, "/sdcard");
                startActivityForResult(intent, SELECT_FILE);

            }
        };

        // Create an anonymous implementation of OnClickListener
        OnClickListener saveButtonListner = new OnClickListener() {
            public void onClick(final View v) {
                // do something when the button is clicked
                encok=false;
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        v.getContext());

                alert.setTitle("Password");
                alert.setMessage("Please Enter A Password to Encrypt File");

                // Set an EditText view to get user input
                final EditText input = new EditText(v.getContext());
                alert.setView(input);

                alert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String tempName = input.getText().toString();

                                if (tempName.length() > 0) {
                                    password = tempName;



                                    if (scrambleFile(false)) {
                                        TextView MD5sum = (TextView) findViewById(R.id.MD5sum);
                                        TargetFileMD5 = calculateMD5(new File(filePath));
                                        MD5sum.setText(TargetFileMD5);

                                        if (Keyfile != null) {

                                            AlertDialog.Builder alert1 = new AlertDialog.Builder(
                                                    v.getContext());

                                            alert1.setTitle("KeyFile entry");
                                            alert1.setMessage("Whould you like to add checksum and password to Keyfile?");

                                            // Set an EditText view to get user input
                                            alert1.setPositiveButton("Ok",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                            String KeyFileEntry = TargetFileMD5 + "  " + password + "\n";
                                                            decrypted = decrypted + KeyFileEntry;
                                                            SimpleCrypto crypto = new SimpleCrypto();
                                                            try {
                                                                encrypted = crypto.encrypt(decrypted, KeyFilePass.toCharArray());
                                                            } catch (GeneralSecurityException e) {
                                                                e.printStackTrace();
                                                            } catch (UnsupportedEncodingException e) {
                                                                e.printStackTrace();
                                                            }
                                                            writeKeyFIle(Keyfile);
                                                        }
                                                    });

                                            alert1.setNegativeButton("Cancel",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog,
                                                                            int whichButton) {
                                                            // Canceled.
                                                        }
                                                    });

                                            alert1.show();
                                        }
                                    }
                                } else {
                                            Toast.makeText(getBaseContext(),
                                            "Invalid Password!",
                                            Toast.LENGTH_LONG).show();
                                       }
                            }

                        });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                // Canceled.
                            }
                        });

                alert.show();

            }
        };

        // Create an anonymous implementation of OnClickListener
        final OnClickListener decryptButtonListener = new OnClickListener() {
            public void onClick(View v) {
                magicNamesVector.clear();
                // do something when the button is clicked
                String KFPTF=FindMD5(TargetFileMD5);

                if ( KFPTF.isEmpty() ) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(
                            v.getContext());

                    alert.setTitle("Password");
                    alert.setMessage("Please Enter A Password to Decrypt File");

                    // Set an EditText view to get user input
                    final EditText input = new EditText(v.getContext());
                    alert.setView(input);

                    alert.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    String tempName = input.getText().toString();

                                    if (tempName.length() > 0) {
                                        password = tempName;
                                        scrambleFile(true);
                                    } else {
                                        Toast.makeText(getBaseContext(),
                                                "Invalid Password!",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                            });

                    alert.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    // Canceled.
                                }
                            });

                    alert.show();
                } else {
                    password = KFPTF.toString();
                    scrambleFile(true);
                    decrypted=RemoveMD5Key(TargetFileMD5);
                    SimpleCrypto crypto = new SimpleCrypto();
                    try {
                        encrypted = crypto.encrypt(decrypted, KeyFilePass.toCharArray());
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    writeKeyFIle(Keyfile);
                    TextView MD5sum = (TextView) findViewById(R.id.MD5sum);
                    TargetFileMD5 = calculateMD5(new File(filePath));
                    MD5sum.setText(TargetFileMD5);


                }

            }
        };


        OnClickListener createKeyListener = new OnClickListener() {
            public void onClick(final View v) {
                // do something when the button is clicked
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        v.getContext());


                LinearLayout layout = new LinearLayout(v.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText titleBox = new EditText(v.getContext());
                titleBox.setHint("KeyFile Name");
                layout.addView(titleBox);
                final EditText descriptionBox = new EditText(v.getContext());
                descriptionBox.setHint("KeyFile Password");
                layout.addView(descriptionBox);

                alert.setView(layout);


                alert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String tempFileName = titleBox.getText().toString();
                                String tempName = descriptionBox.getText().toString();
                                Integer NamePass = 0;

                                if (tempFileName.length() > 0) {
                                    NamePass++;
                                } else {
                                    Toast.makeText(getBaseContext(),
                                            "Invalid Name!",
                                            Toast.LENGTH_LONG).show();
                                }
                                if (tempName.length() > 0) {
                                    NamePass++;
                                } else {
                                    Toast.makeText(getBaseContext(),
                                            "Invalid Password!",
                                            Toast.LENGTH_LONG).show();
                                }
                                if (NamePass == 2) {
                                    //Keyfile = new File(v.getContext().getFilesDir(), tempFileName);
                                    Keyfile = new File(v.getContext().getFilesDir(), tempFileName);
                                    try {
                                        Keyfile.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    KeyFilePass = tempName;
                                    decrypted="";
                                    encrypted="";
                                }
                            }

                        });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                // Canceled.
                            }
                        });

                alert.show();

            }
        };

        OnClickListener openKeyFileButtonListener = new OnClickListener() {
            public void onClick(View v) {
                // do something when the button is clicked

                // in onCreate or any event where your want the user to
                Intent intent = new Intent(v.getContext(), FileDialog.class);
                intent.putExtra(FileDialog.START_PATH, String.valueOf(v.getContext().getFilesDir()));
                startActivityForResult(intent, SELECT_KEYFILE);

            }
        };

       /* OnClickListener ShowDecryptListner = new OnClickListener() {
            public void onClick(View v) {
                // do something when the button is clicked
                // in onCreate or any event where your want the user to
                //worx
                TextView Keys = (TextView) findViewById(R.id.LoadKeys);
                Keys.setText(decrypted + "\n find " + FindMD5(TargetFileMD5) + "rem :" + RemoveMD5Key(TargetFileMD5) );

                SecretKeyFactory keyFactory = null;
                Intent intent = new Intent(v.getContext(), ProcessActivity.class);
                intent.putExtra(ProcessActivity.ACTION, "encrypt");
                intent.putExtra(ProcessActivity.OLDFILE, "encrypt");
                intent.putExtra(ProcessActivity.NEWFILE, "encrypt");
                intent.putExtra(ProcessActivity.PASS,"mypass");
                startActivity(intent);


            }
        };*/


        // Create an anonymous implementation of OnClickListener


        Button loadButton = (Button) findViewById(R.id.loadButton);
        loadButton.setOnClickListener(loadButtonListner);

        Button saveButton = (Button) findViewById(R.id.encryptbutton);
        saveButton.setOnClickListener(saveButtonListner);

        Button decryptButton = (Button) findViewById(R.id.decryptbutton);
        decryptButton.setOnClickListener(decryptButtonListener);

        Button createKeyButton = (Button) findViewById(R.id.create_keyfile);
        createKeyButton.setOnClickListener(createKeyListener);

        Button openKeyButton = (Button) findViewById(R.id.open_keyfile);
        openKeyButton.setOnClickListener(openKeyFileButtonListener);

        //Button decryptedButton = (Button) findViewById(R.id.decrypto);
        //decryptedButton.setOnClickListener(ShowDecryptListner);
    }


    public Boolean scrambleFile(Boolean descramble) {

       if (descramble == true) {

            Intent intent = new Intent(this, ProcessActivity.class);
            intent.putExtra(ProcessActivity.ACTION, "decrypt");
            intent.putExtra(ProcessActivity.OLDFILE, filePath);
            intent.putExtra(ProcessActivity.NEWFILE, filePath + ".old");
            intent.putExtra(ProcessActivity.PASS, password);
            startActivityForResult(intent,PROCESS_FILE);
       } else {

            Intent intent = new Intent(this, ProcessActivity.class);
            intent.putExtra(ProcessActivity.ACTION, "encrypt");
            intent.putExtra(ProcessActivity.OLDFILE, filePath);
            intent.putExtra(ProcessActivity.NEWFILE, filePath + ".old");
            intent.putExtra(ProcessActivity.PASS, password);
           startActivityForResult(intent,PROCESS_FILE);

       }
        return true;
    }

    public String getFileName() {
        int slashloc = filePath.lastIndexOf("/");
        if (slashloc < 0) {
            return filePath;
        } else {
            return filePath.substring(slashloc + 1);
        }
    }

    public void onActivityResult(final int requestCode, int resultCode,
                                 final Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                filePath = data.getStringExtra(FileDialog.RESULT_PATH);
                // set the filename txt
                changeFileNameText(filePath);
                Button enabButton = (Button) findViewById(R.id.encryptbutton);
                enabButton.setEnabled(true);
                enabButton = (Button) findViewById(R.id.decryptbutton);
                enabButton.setEnabled(true);
                TextView MD5sum = (TextView) findViewById(R.id.MD5sum);
                TargetFileMD5 = calculateMD5(new File(filePath));
                MD5sum.setText(TargetFileMD5);
            }else if (requestCode == SELECT_KEYFILE){
                filePath = data.getStringExtra(FileDialog.RESULT_PATH);
                Keyfile = new File(filePath);


                //Context mContext = getApplicationContext();
                AlertDialog.Builder alert = new AlertDialog.Builder(
                       this);
                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                final EditText descriptionBox = new EditText(this);
                descriptionBox.setHint("KeyFile Password");
                layout.addView(descriptionBox);
                alert.setView(layout);
                alert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String tempName = descriptionBox.getText().toString();
                                Integer NamePass = 0;

                                if (tempName.length() > 0) {
                                    KeyFilePass = tempName;
                                      if(Keyfile.length() > 0) {
                                            encrypted = readKeyFile(Keyfile);
                                            decrypted = "";
                                            try{
                                            decrypted=new SimpleCrypto().decrypt(encrypted, KeyFilePass.toCharArray() );
                                             } catch (GeneralSecurityException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                           if (decrypted.length() == 0) {
                                               Toast.makeText(getBaseContext(),
                                                       "No keyFile entries loaded, bad password try again",
                                                       Toast.LENGTH_LONG).show();
                                           }
                                      }
                                } else {
                                    Toast.makeText(getBaseContext(),
                                            "Invalid Password!",
                                            Toast.LENGTH_LONG).show();

                            }

                        }});

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                // Canceled.
                            }
                        });
                alert.show();
            }else if (requestCode == PROCESS_FILE) {
                //worx
                filePath = data.getStringExtra(ProcessActivity.OLDFILE);
                TargetFileMD5=calculateMD5(new File(filePath));
                TextView mytext =(TextView) findViewById(R.id.MD5sum);
                mytext.setText(TargetFileMD5);
            }
        } else if (resultCode == RESULT_CANCELED) {
        }
    }


    public void changeFileNameText(String newFileName) {
        TextView t = (TextView) findViewById(R.id.fileText);
        t.setText(newFileName);
    }

    public String readKeyFile(File file) {
        String result="";
        long length = file.length();
        if (length < 1 || length > Integer.MAX_VALUE) {
            result = "";
            Log.w("MainActivity","Keyfile too small or too big");
        } else {
            try (FileReader in = new FileReader(file)) {
                char[] content = new char[(int) length];
                int numRead = in.read(content);
                if (numRead != length) {
                    Log.w("MainActivity","Could not read the hole keyfile");
                }
                result = new String(content, 0, numRead);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

     public void writeKeyFIle(File OutputKeyFile){
        try {
            FileWriter fw = new FileWriter(OutputKeyFile);
            fw.write(encrypted);
            fw.close();
        } catch (IOException iox) {
        //do stuff with exception
        iox.printStackTrace();
        }
    }

    public static String calculateMD5(File updateFile) {
            MessageDigest digest;
            final String TAG = "MyActivity";
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "Exception while getting digest", e);
                return null;
            }

            InputStream is;
            try {
                is = new FileInputStream(updateFile);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Exception while getting FileInputStream", e);
                return null;
            }

            byte[] buffer = new byte[8192];
            int read;
            try {
                while ((read = is.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
                byte[] md5sum = digest.digest();
                BigInteger bigInt = new BigInteger(1, md5sum);
                String output = bigInt.toString(16);
                // Fill to 32 chars
                output = String.format("%32s", output).replace(' ', '0');
                return output;
            } catch (IOException e) {
                throw new RuntimeException("Unable to process file for MD5", e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception on closing MD5 input stream", e);
                }
            }



    }

 public String FindMD5 (String SearchMD5 ){
     if(decrypted.length() > 0 )
     {
         String pattern = "("+SearchMD5+") (.*)\n";

         // Create a Pattern object
         Pattern r = Pattern.compile(pattern);

         // Now create matcher object.
         Matcher m = r.matcher(decrypted);
         if (m.find( )) {
             return m.group(2).trim().replaceAll("\n", "");
         }
    }
     return "";
 }

    public String RemoveMD5Key (String SearchMD5){
        if(decrypted.length() > 0)
        {
            String ndecrypted= "";
            String line="";
             try (Scanner scanner = new Scanner(decrypted)) {
                 while (scanner.hasNextLine()) {
                     line = scanner.nextLine();
                     if (!line.matches(SearchMD5 + "(.*)")) {
                         ndecrypted =ndecrypted + line + "\n";
                     }
                 }    // process the line
            }

            return ndecrypted;
        }
       return "";
    }


}