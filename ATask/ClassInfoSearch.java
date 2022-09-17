package com.example.project01.ATask;

import static com.example.project01.common.CommonMethod.ipConfig;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import com.example.project01.Adapter.C_infoAdapter;
import com.example.project01.DTO.C_infoDTO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ClassInfoSearch extends AsyncTask<Void,Void,Void> {

    private static final String TAG = "확인";

    ArrayList<C_infoDTO> dtos;
    C_infoAdapter adapter;
    String class_id, name, order;

    public ClassInfoSearch(ArrayList<C_infoDTO> dtos, C_infoAdapter adapter, String class_id, String name, String order) {
        this.dtos = dtos;
        this.adapter = adapter;
        this.class_id = class_id;
        this.name = name;
        this.order = order;
    }

    // 반드시 선언해야 할것들 : 무조건 해야함 복,붙
    HttpClient httpClient;       // 클라이언트 객체
    HttpPost httpPost;           // 클라이언트에 붙일 본문
    HttpResponse httpResponse;   // 서버에서의 응답을 받는 부분
    HttpEntity httpEntity;       // 응답내용

    @Override
    protected Void doInBackground(Void... voids) {

        try {
            // 무조건 해야함 : 복,붙
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.setCharset(Charset.forName("UTF-8"));

            // 여기가 우리가 수정해야 하는 부분 : 서버로 보내는 데이터
            // builder에 문자열 및 파일 첨부하는곳
            builder.addTextBody("class_id", class_id, ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody("name", name, ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody("order", order, ContentType.create("Multipart/related", "UTF-8"));


            // 전송
            // 전송 Url : 우리가 수정해야 하는 부분
            String postURL = ipConfig + "/app/hamClassInfoSearch";

            // 그대로 복,붙
            InputStream inputStream = null;
            httpClient = AndroidHttpClient.newInstance("Android");
            httpPost = new HttpPost(postURL);
            httpPost.setEntity(builder.build());
            httpResponse = httpClient.execute(httpPost); // 보내고 응답받는 부분
            httpEntity = httpResponse.getEntity();  // 응답내용을 저장
            inputStream = httpEntity.getContent();  // 응답내용을 inputStream에 넣음

            // 응답처리 : 데이터가  ArrayList<dto>  DTO 형태 :
            readJsonStream(inputStream);
            Log.d(TAG, "readJson: " + inputStream);


        }catch (Exception e){
            e.getMessage();
        }finally {
            if(httpEntity != null){
                httpEntity = null;
            }
            if(httpResponse != null){
                httpResponse = null;
            }
            if(httpPost != null){
                httpPost = null;
            }
            if(httpClient != null){
                httpClient = null;
            }
        }

        return null;

    }

    // 3. doInBackground 실행(작업)후에 오는부분
    @Override                   // 세번째 파라메터
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        //화면갱신
        adapter.notifyDataSetChanged();
    }

    //ArrayList<DTO>로 넘어 왔을 때
    private  void readJsonStream(InputStream inputStream) throws IOException {
        JsonReader reader = new JsonReader
                (new InputStreamReader(inputStream, "utf-8"));
        try {
            reader.beginArray();
            while (reader.hasNext()){
                dtos.add(readMessage(reader));
            }
            reader.endArray();

        }catch(Exception e) {
            e.getMessage();
        }finally {
            reader.close();
        }

    }
    // 하나의 DTO형태로 데이터를 받을때 파싱하는 부분
    private C_infoDTO readMessage(JsonReader reader) throws IOException {

        String student_id="", student_name="", school_name="", parent_phone="", chk="";

        reader.beginObject();

        while (reader.hasNext()){
            String readStr = reader.nextName();
            if(readStr.equals("student_name")){
                student_name = reader.nextString();
            } else if(readStr.equals("school_name")){
                school_name = reader.nextString();
            } else if(readStr.equals("student_id")){
                student_id = reader.nextString();
            } else if(readStr.equals("parent_phone")){
                parent_phone = reader.nextString();
            } else if(readStr.equals("chk")){
                chk = reader.nextString();
            } else {
                reader.skipValue();
            }
        } // while
        reader.endObject();
        Log.d(TAG, "readMessage: " + student_name + ", " +school_name);

        return new C_infoDTO(student_id, student_name, school_name, parent_phone, chk);
    }




}
