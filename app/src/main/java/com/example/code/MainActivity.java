/*
   activity_main.xml : Selection of Language
    c : Image Button for C
    cpp : Image Button for C++
    java : Image Button for Java
    python : Image Button for Python

   input_code.xml-Main Code
    inp_code_txt : Textbox
    inp_code_clr : Clear Button
    inp_code_ok : Execute Button

   user_input_code.xml- Stdin
    u_i_c : Label
    u_i_txt : stdin Textbox
    u_i_ok : OK Button

   execute_code.xml : Only Display

 */


package com.example.code;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {


    private HttpURLConnection connect;
    private String lang = null;
    private String code = null;
    private String stdin = null;
    private String host = "http://192.168.43.94:8000/api/";
    private String id = "";
    private boolean status_global = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void cprgm(View view) {
        setContentView(R.layout.input_code);
        lang = "c";
    }

    public void cpp(View view) {
        setContentView(R.layout.input_code);
        lang = "cpp";
    }

    public void java(View view) {
        setContentView(R.layout.input_code);
        lang = "java";
    }

    public void python(View view) {
        setContentView(R.layout.input_code);
        lang = "python";
    }

    public void inp_code_clr(View view) {
        EditText txt;
        txt = (EditText) findViewById(R.id.inp_code_txt);
        txt.setText("");
    }

    public void inp_code_ok(View view) {
        code = findViewById(R.id.inp_code_txt).toString();

        if (lang.equalsIgnoreCase("java"))
            setContentView(R.layout.user_input_code_java);
        else
            setContentView(R.layout.user_input_code);
    }

    public void u_i_ok(View view) {
        EditText txt1 = (EditText) findViewById(R.id.u_i_txt);
        stdin = txt1.getText().toString();
        final TextView txt;
        setContentView(R.layout.execute_code);
        txt = (TextView) findViewById(R.id.finall);
        txt.setText(stdin);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL u_submit = new URL(host + "submit-code/?format=json");
                    connect = (HttpURLConnection) u_submit.openConnection();
                    connect.setRequestMethod("POST");
                    connect.setDoOutput(true);

                    String s = "lang=python&"
                            + "code=print \"Hello\"";

                    DataOutputStream dos = new DataOutputStream(connect.getOutputStream());
                    dos.writeBytes(s);
                    dos.flush();
                    dos.close();

                    InputStream inpstrm = connect.getInputStream();
                    BufferedReader response = new BufferedReader(new InputStreamReader(inpstrm));

                    String str, jstr = "";

                    while ((str = response.readLine()) != null) {
                        jstr = jstr + str;
                    }

                    JSONObject jobj = new JSONObject(jstr);
                    id = jobj.get("id").toString();
                } catch (Exception e) {
                    setContentView(R.layout.execute_code);
                    txt.setText(e.toString());
                }
            }
        }).start();


        //Status


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (; ; ) {
                        URL u_status = new URL(host + "status-code/" + id + "/?format=json");
                        connect = (HttpURLConnection) u_status.openConnection();

                        BufferedReader status = new BufferedReader(new InputStreamReader(connect.getInputStream()));

                        String res, stat = "";

                        while ((res = status.readLine()) != null) {
                            stat += res;
                        }

                        JSONObject jstatobj = new JSONObject(stat);

                        if ((jstatobj.get("status").toString().equals("2"))) {
                            status_global = true;
                            break;
                        } else {
                            Thread.sleep(5000);
                        }
                    }
                } catch (Exception e) {
                    txt.setText(e.toString());
                }
            }
        }).start();


        //Result

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if (status_global) {
                        URL u_result = new URL(host + "result-code/" + id + "/?format=json");
                        connect = (HttpURLConnection) u_result.openConnection();

                        int res_code = connect.getResponseCode();

                        if (res_code == 200) {
                            BufferedReader result = new BufferedReader(new InputStreamReader(connect.getInputStream()));
                            String res, r = "";

                            while ((res = result.readLine()) != null) {
                                r += res;
                            }

                            JSONObject jresobj = new JSONObject(r);

                            if (jresobj.get("stderr") == "")
                                txt.setText(jresobj.get("stdout").toString());
                            else
                                txt.setText(jresobj.get("stderr").toString());

                        } else
                            setContentView(R.layout.try_again);
                    }
                } catch (Exception e) {
                    Log.e("u_i_ok", e.getMessage(), e.fillInStackTrace());
                    setContentView(R.layout.execute_code);
                    txt.setText(e.toString());
                }
            }

        }).start();


    }
}
/*

public class Extra {
    class Async extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            int s = 1;

            try {
                URL u_status = new URL(host + "status-code/" + id + "/?format=json");
                connect = (HttpURLConnection) u_status.openConnection();

                while (s != 2) {
                    BufferedReader status = new BufferedReader(new InputStreamReader(connect.getInputStream()));

                    String res, stat = "";

                    while ((res = status.readLine()) != null) {
                        stat += res;
                    }

                    JSONObject jstatobj = new JSONObject(stat);

                    s = Integer.parseInt(jstatobj.get("status").toString());
                    Thread.sleep(5000);
                }
                status_global = true;
            } catch (Exception e) {
                setContentView(R.layout.try_again);
            }
            return status_global;
        }

        @Override
        protected void onPreExecute() {
            try {
                stdin = findViewById(R.id.u_i_txt).toString();

                setContentView(R.layout.execute_code);

                URL u_submit = new URL(host + "submit-code/?format=json");
                connect = (HttpURLConnection) u_submit.openConnection();
                connect.setRequestMethod("POST");
                connect.setDoOutput(true);

                String s = "lang=python&"
                        + "code=print \"Hello\"";

                DataOutputStream dos = new DataOutputStream(connect.getOutputStream());
                dos.writeBytes(s);
                dos.flush();
                dos.close();

                InputStream inpstrm = connect.getInputStream();
                BufferedReader response = new BufferedReader(new InputStreamReader(inpstrm));

                String str, jstr = "";

                while ((str = response.readLine()) != null) {
                    jstr = jstr + str;
                }

                JSONObject jobj = new JSONObject(jstr);
                id = jobj.get("id").toString();
            } catch (Exception e) {
                setContentView(R.layout.try_again);
            }

        }

        protected void onPostExecute(Boolean b) {
            try {
                if (b) {
                    URL u_result = new URL(host + "result-code/" + id + "/?format=json");
                    connect = (HttpURLConnection) u_result.openConnection();
                    int res_code = connect.getResponseCode();

                    if (res_code == 201) {
                        BufferedReader result = new BufferedReader(new InputStreamReader(connect.getInputStream()));
                        String res, r = "";

                        while ((res = result.readLine()) != null) {
                            r += res;
                        }

                        JSONObject jresobj = new JSONObject(r);

                        if (jresobj.get("stderr") == "")
                            txt.setText(jresobj.get("stdout").toString());
                        else
                            txt.setText(jresobj.get("stderr").toString());
                    } else
                        setContentView(R.layout.try_again);
                }
            } catch (Exception e) {
                //  Log.e("u_i_ok", e.getMessage(), e.fillInStackTrace());
                txt.setText(e.toString().toCharArray(), 0, e.toString().toCharArray().length);

            }
        }

    }

    public void u_i_ok(View view) {
        AsyncTask as = new Async();
        as.execute();
        try {
            setContentView(R.layout.execute_code);
            txt = (TextView) findViewById(R.id.finall);
        } catch (Exception e) {
            Log.e("1", e.getMessage(), e.fillInStackTrace());
        }
        try {
            txt.setText("");
        } catch (Exception e) {
            Log.e("2", e.getMessage(), e.fillInStackTrace());
        }
        try {
            AsyncTask as = new Async();
            as.execute();
        } catch (Exception e) {
            Log.e("3", e.getMessage(), e.fillInStackTrace());
        }


    public void u_i_ok(View view) {
        stdin = findViewById(R.id.u_i_txt).toString();
        final TextView txt;
        setContentView(R.layout.execute_code);
        txt = (TextView) findViewById(R.id.finall);
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL u_submit = new URL(host + "submit-code/?format=json");
                        connect = (HttpURLConnection) u_submit.openConnection();
                        connect.setRequestMethod("POST");
                        connect.setDoOutput(true);

                        String s = "lang=python&"
                                + "code=print \"Hello\"";

                        DataOutputStream dos = new DataOutputStream(connect.getOutputStream());
                        dos.writeBytes(s);
                        dos.flush();
                        dos.close();

                        InputStream inpstrm = connect.getInputStream();
                        BufferedReader response = new BufferedReader(new InputStreamReader(inpstrm));

                        String str, jstr = "";

                        while ((str = response.readLine()) != null) {
                            jstr = jstr + str;
                        }

                        JSONObject jobj = new JSONObject(jstr);
                        id = jobj.get("id").toString();


                        out();

                        setContentView(R.layout.execute_code);
                        if (status_global) {
                            URL u_result = new URL(host + "result-code/" + id + "/?format=json");
                            connect = (HttpURLConnection) u_result.openConnection();
                            int res_code = connect.getResponseCode();

                            if (res_code == 200) {
                                BufferedReader result = new BufferedReader(new InputStreamReader(connect.getInputStream()));
                                String res, r = "";

                                while ((res = result.readLine()) != null) {
                                    r += res;
                                }

                                JSONObject jresobj = new JSONObject(r);

                                if (jresobj.get("stderr") == "")
                                    txt.setText(jresobj.get("stdout").toString());
                                else
                                    txt.setText(jresobj.get("stderr").toString());


                            } else
                                setContentView(R.layout.try_again);
                        } else
                            out();

                    } catch (Exception e) {
                        Log.e("u_i_ok", e.getMessage(), e.fillInStackTrace());
                        //   setContentView(R.layout.execute_code);
                        //  txt.setText(e.toString().toCharArray(), 0, e.toString().toCharArray().length);

                    }
                }
            }
            ).start();
        } catch (Exception e) {
            Log.e("2", e.getMessage(), e.fillInStackTrace());
            setContentView(R.layout.try_again);
        }


    }


    public void out() {
//        Toast.makeText(MainActivity.this,"out is called" + f, LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL u_status = new URL(host + "status-code/" + id + "/?format=json");
                    connect = (HttpURLConnection) u_status.openConnection();

                    BufferedReader status = new BufferedReader(new InputStreamReader(connect.getInputStream()));

                    String res, stat = "";

                    while ((res = status.readLine()) != null) {
                        stat += res;
                    }

                    JSONObject jstatobj = new JSONObject(stat);

                    if (true || (jstatobj.get("status").toString() == "2")) {
                        status_global = true;

                        return;
                    } else {
                        Thread.sleep(5000);
                        out();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }


    public void retry() {
        ;
    }
}
}
*/