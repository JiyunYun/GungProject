package lecture.mobile.final_project.ma02_20151024;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.File;

public class DiaryFromHomeActivity extends AppCompatActivity {
    SimpleCursorAdapter diaryAdapter;
    Cursor cursor;
    DiaryDBHelper helper;
    ListView lvDiary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_main);

        helper = new DiaryDBHelper(this);

        // 어댑터에 SimpleCursorAdapter 연결
        diaryAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null, new String[] {DiaryDBHelper.TITLE, DiaryDBHelper.DATE}, new int[] {android.R.id.text1, android.R.id.text2}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        lvDiary = (ListView)findViewById(R.id.lvDiary);
        lvDiary.setAdapter(diaryAdapter);

        lvDiary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long _id) {
                // ShowMemoActivity 호출
                Intent intent = new Intent(DiaryFromHomeActivity.this, ShowDiaryActivity.class);
                intent.putExtra("index", (int) _id);
                startActivity(intent);
            }
        });

        lvDiary.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
                final int i = (int) id;

                AlertDialog.Builder builder = new AlertDialog.Builder(DiaryFromHomeActivity.this);

                builder.setTitle("DELETE DIARY");
                builder.setMessage("기록을 삭제하시겠습니까?");
                builder.setIcon(R.drawable.icon_delete);
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = helper.getWritableDatabase();

                        // 리스트에서 롱클릭된 항목의 이미지 파일을 저장소에서 삭제
                        Cursor cursor = db.rawQuery("SELECT * FROM " + DiaryDBHelper.TABLE_NAME + " WHERE _id = " + i + ";", null);
                        cursor.moveToNext();
                        String path = cursor.getString(4);
                        File file = new File(path);
                        file.delete();

                        db.execSQL("DELETE FROM " + DiaryDBHelper.TABLE_NAME + " WHERE _id = " + i + ";");
                        diaryAdapter.notifyDataSetChanged();
                        onResume();
                    }
                });
                builder.setNegativeButton("취소", null);
                builder.show();

                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // DB 에서 모든 레코드를 가져와 Adapter에 설정
        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.rawQuery("select * from " + DiaryDBHelper.TABLE_NAME, null);
        diaryAdapter.changeCursor(cursor);
        helper.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null) cursor.close();
    }
}
