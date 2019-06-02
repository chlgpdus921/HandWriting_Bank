package org.androidtown.mobile_term;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class TestFileList extends AppCompatActivity {
    String reqLocation;
    int foldernum = 0, filenum = 0;
    ArrayList<BookPojo> folderAndFileList;
    ArrayList<BookPojo> foldersList;
    ArrayList<BookPojo> filesList;

    BookAdapter FolderAdapter;
    ListView listView;
    String location;
    boolean pickFiles;
    Intent receivedIntent;
    String bookfolderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_file_list);
        if (!isExternalStorageReadable()) {
            Toast.makeText(this, "Storage access permission not given", Toast.LENGTH_LONG).show();
            finish();
        }
        Toolbar toolbar2 = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar2);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            Intent intent = getIntent();
            reqLocation = intent.getStringExtra("filename");
            bookfolderName = intent.getStringExtra("folder");
            location = Environment.getExternalStorageDirectory().getAbsolutePath() + "/시험지";
        } catch (Exception e) {
            e.printStackTrace();
        }
        pickFiles = true;
        loadLists(location);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    /* Checks if external storage is available to at least read */
    boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /*파일 load*/
    void loadLists(String location) {
        try {
            File folder = new File(location);
            if (!folder.isDirectory())
                exit();
            File[] files = folder.listFiles();
            foldersList = new ArrayList<>();
            filesList = new ArrayList<>();
            for (File currentFile : files) {
                long lastModifie = currentFile.lastModified();
                String patter = "yyyy년 MM월 dd일 aa hh:mm";
                SimpleDateFormat simpleDateForma = new SimpleDateFormat(patter);
                Date lastModifiedDat = new Date(lastModifie);
                Long L = currentFile.length();
                if (currentFile.isDirectory()) {
                    BookPojo bookPojo = new BookPojo(currentFile.getName(), true);
                    bookPojo.setDay(simpleDateForma.format(lastModifiedDat));
                    bookPojo.setSize(getFileCount(currentFile, 0) + "개");
                    bookPojo.setPosi(foldernum);
                    foldersList.add(bookPojo);
                    foldernum++;
                } else {
                    BookPojo bookPojo = new BookPojo(currentFile.getName(), false);
                    bookPojo.setDay(simpleDateForma.format(lastModifiedDat));
                    bookPojo.setSize(formatFileSize(L));
                    bookPojo.setLocation(location);
                    bookPojo.setPosi(filenum);
                    filesList.add(bookPojo);
                    filenum++;
                }
            }
            // sort & add to final List - as we show folders first add folders first to the final list
            Collections.sort(foldersList, comparatorAscending);
            folderAndFileList = new ArrayList<>();
            folderAndFileList.addAll(foldersList);
            //if we have to show files, then add files also to the final list
            if (pickFiles) {
                Collections.sort(filesList, comparatorAscending);
                folderAndFileList.addAll(filesList);
            }
            showList();

        } catch (Exception e) {
            e.printStackTrace();
        }

    } // load List

    Comparator<BookPojo> comparatorAscending = new Comparator<BookPojo>() {
        @Override
        public int compare(BookPojo f1, BookPojo f2) {
            return f1.getName().compareTo(f2.getName());
        }
    };

    private String formatFileSize(long bytes) {
        return android.text.format.Formatter.formatFileSize(getApplicationContext(), bytes);
    }

    public int getFileCount(File f, int totalCount) {
        if (f.isDirectory()) {
            String[] list = f.list();
            for (int i = 0; i < list.length; i++) {
                totalCount++;
            }
        } else {
            totalCount++;
        }
        return totalCount;
    }

    void showList() {
        try {
            FolderAdapter = new BookAdapter(this, folderAndFileList);
            listView = (ListView) findViewById(R.id.test_listView);
            listView.setAdapter(FolderAdapter);
            //이부분은 나중에 pdf여는 코드
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    listClick(position);
                }
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                    PopupMenu popup = new PopupMenu(getApplicationContext(), view);
                    getMenuInflater().inflate(R.menu.listview_popup, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {//눌러진 MenuItem의 Item Id를 얻어와 식별
                                case R.id.pop_name_change:
                                    namechange(position);
                                    break;
                                case R.id.pop_delete:
                                    filedelete(position);
                                    break;
                                case R.id.pop_share:
                                    fileshare(position);
                                    break;
                                case R.id.pop_info:
                                    fileinfo(position);
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void listClick(int position) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "시험지" + folderAndFileList.get(position).getName());
        int K = 0;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
        String fileExtend = getExtension(folderAndFileList.get(position).getName());
        if (fileExtend.equalsIgnoreCase("pdf")) {
            K = 1;
            Intent intent1 = new Intent(this, TestMake.class);
            intent1.putExtra("filename", folderAndFileList.get(position).getName());
            intent1.putExtra("folder", "시험지");
            intent1.putExtra("state", "true");
            startActivity(intent1);
        }
        try {
            if (K == 0)
                startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "기기에 파일을 열수 있는 어플이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    /*파일 확장자 찾기*/
    public static String getExtension(String fileStr) {
        return fileStr.substring(fileStr.lastIndexOf(".") + 1, fileStr.length());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLists(location);
    }

    @Override
    public void onBackPressed() {
        goBack(null);
    }

    public void goBack(View v) {
        exit();
    }

    void exit() {
        setResult(RESULT_CANCELED, receivedIntent);
        finish();
    }

    public void cancel(View v) {
        exit();
    }

    /*순서대로 파일이름변경, 삭제, 공유, 상세정보*/
    public void namechange(final int position) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("파일 이름 변경");
        // ad.setMessage("Message");   // 내용 설정
        final EditText et = new EditText(this);
        et.setText(folderAndFileList.get(position).getName());
        et.setSelection(et.length() - 4);
        ad.setView(et);
        ad.setPositiveButton("이름 변경", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = et.getText().toString();
                File filePre = new File(location, folderAndFileList.get(position).getName());
                File fileNow = new File(location, value);
                filePre.renameTo(fileNow);
                dialog.dismiss();
                loadLists(location);
            }
        });
        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    public void filedelete(final int position) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setMessage("파일을 삭제할까요?");   // 내용 설정
        ad.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Text 값 받아서 로그 남기기
                File file = new File(location, folderAndFileList.get(position).getName());
                file.delete();
                dialog.dismiss();
                loadLists(location);
            }
        });
        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    public void fileinfo(final int position) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("상세정보");   // 내용 설정
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.file_info_dialog, null);
        ad.setView(dialogView);
        TextView name = (TextView) dialogView.findViewById(R.id.name);
        TextView size = (TextView) dialogView.findViewById(R.id.size);
        TextView day = (TextView) dialogView.findViewById(R.id.day);
        TextView locat = (TextView) dialogView.findViewById(R.id.location);
        name.setText(folderAndFileList.get(position).getName());
        size.setText(folderAndFileList.get(position).getSize());
        day.setText(folderAndFileList.get(position).getDay());
        locat.setText(location + folderAndFileList.get(position).getName());
        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    private void fileshare(int position) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        //   intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID,
                new File(Environment.getExternalStorageDirectory().getAbsolutePath() + bookfolderName + folderAndFileList.get(position).getName())));
        Intent chooser = Intent.createChooser(intent, "공유하기");
        this.startActivity(chooser);
    }
}