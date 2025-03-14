package org.androidtown.mobile_term;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Gachon_Login extends AppCompatActivity {

    EditText logid;
    EditText logpass;
    Button login;
    Button cancel;

    String loginID;
    String loginPASS;

    int check = 0;
    static ArrayList<String> arr = new ArrayList<String>();

    private FirebaseUser user;
    private FirebaseAuth mAuth; //Firebase로 로그인한 사용자 정보 알기 위해
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gachon__login);

        mAuth = FirebaseAuth.getInstance(); // 인증
        user = mAuth.getCurrentUser(); //현재 로그인한 유저

        logid = (EditText) findViewById(R.id.type_id);
        logpass = (EditText) findViewById(R.id.type_password);
        logpass.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        login = (Button) findViewById(R.id.loginBt);
        cancel = (Button) findViewById(R.id.cancelBt);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginID = logid.getText().toString();
                loginPASS = logpass.getText().toString();

                new Description().execute();

                finish();
            }
        });
    }

    public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence mSource;
            public PasswordCharSequence(CharSequence source) {
                mSource = source; // Store char sequence
            }
            public char charAt(int index) {
                return '*'; // This is the important part
            }
            public int length() {
                return mSource.length(); // Return default
            }
            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    };

    private class Description extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Connection.Response res = Jsoup.connect("https://cyber.gachon.ac.kr/login.php")
                        .data("username", loginID, "password", loginPASS)
                        .method(Connection.Method.POST)
                        .execute();

                Map<String, String> loginCookie = res.cookies();

                Document doc = Jsoup.connect("https://cyber.gachon.ac.kr")
                        .cookies(loginCookie)
                        .followRedirects(true)
                        .get();

                Elements els = doc.select("div[class=course-title]");
                check = els.size();

                for (Element elem : els) {
                    String mytitle = elem.select("h3").text();
                    arr.add(mytitle);
                }

            } catch (IOException e) { //네트워크 혹은 홈페이지 오류

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (check == 0)
                Toast.makeText(getApplicationContext(), "ID/PW 오류 혹은 저장된 과목이 없음", Toast.LENGTH_SHORT).show();
            else {
                Intent myIntent = new Intent(Gachon_Login.this, select_course.class);
                startActivity(myIntent); //select_course
            }
        }
    }
}
