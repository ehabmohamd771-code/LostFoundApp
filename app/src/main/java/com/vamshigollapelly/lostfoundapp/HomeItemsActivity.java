package com.vamshigollapelly.lostfoundapp;

import android.app.Activity;
import android.os.Bundle;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class HomeItemsActivity extends Activity {

    private HomeDB db;
    private EditText nameInput, placeInput, notesInput;
    private Spinner roomSpinner;
    private LinearLayout itemsContainer;

    private final String[] rooms = {
        "غرفة النوم",
        "المطبخ",
        "الصالة",
        "الحمام",
        "غرفة الجلوس",
        "المخزن",
        "المدخل",
        "غرفة أخرى"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new HomeDB(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 20, 24, 24);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(0xFFF5F7FB);

        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("أغراضي في المنزل 🏠");
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF1F3864);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("سجّل مكان أغراضك لتتذكر أين وضعتها");
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 20);
        root.addView(subtitle);

        nameInput = makeInput("اسم الغرض، مثال: مفتاح المنزل");
        root.addView(nameInput);

        TextView roomLabel = makeLabel("الغرفة");
        root.addView(roomLabel);

        roomSpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            rooms
        );
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );
        roomSpinner.setAdapter(adapter);
        root.addView(roomSpinner);

        placeInput = makeInput(
            "المكان الدقيق، مثال: الدرج الثاني"
        );
        root.addView(placeInput);

        notesInput = makeInput(
            "ملاحظات إضافية (اختياري)"
        );
        notesInput.setMinLines(2);
        notesInput.setGravity(Gravity.TOP | Gravity.RIGHT);
        root.addView(notesInput);

        Button saveButton = new Button(this);
        saveButton.setText("حفظ مكان الغرض");
        saveButton.setTextSize(16);
        saveButton.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(0xFF1F3864)
        );
        root.addView(saveButton);

        TextView listTitle = makeLabel("الأغراض المحفوظة");
        listTitle.setTextSize(20);
        listTitle.setPadding(0, 24, 0, 12);
        root.addView(listTitle);

        itemsContainer = new LinearLayout(this);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(itemsContainer);

        saveButton.setOnClickListener(v -> saveItem());

        setContentView(scroll);
        loadItems();
    }

    private EditText makeInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(16);
        input.setSingleLine(false);
        input.setPadding(12, 8, 12, 8);

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
        params.setMargins(0, 8, 0, 8);
        input.setLayoutParams(params);

        return input;
    }

    private TextView makeLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(16);
        label.setTypeface(null, Typeface.BOLD);
        label.setTextColor(0xFF1F3864);
        label.setPadding(0, 8, 0, 4);
        return label;
    }

    private void saveItem() {
        String name = nameInput.getText().toString().trim();
        String room = roomSpinner.getSelectedItem().toString();
        String place = placeInput.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("أدخل اسم الغرض");
            nameInput.requestFocus();
            return;
        }

        if (place.isEmpty()) {
            placeInput.setError("اكتب مكان الغرض");
            placeInput.requestFocus();
            return;
        }

        long id = db.insertItem(name, room, place, notes);

        if (id != -1) {
            Toast.makeText(
                this,
                "تم حفظ مكان الغرض",
                Toast.LENGTH_SHORT
            ).show();

            nameInput.setText("");
            placeInput.setText("");
            notesInput.setText("");
            roomSpinner.setSelection(0);

            loadItems();
        } else {
            Toast.makeText(
                this,
                "تعذر حفظ الغرض",
                Toast.LENGTH_LONG
            ).show();
        }
    }

    private void loadItems() {
        itemsContainer.removeAllViews();

        SQLiteDatabase database = db.getReadableDatabase();

        Cursor cursor = database.query(
            "home_items",
            null,
            null,
            null,
            null,
            null,
            "id DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
                );
                String room = cursor.getString(
                    cursor.getColumnIndexOrThrow("room")
                );
                String place = cursor.getString(
                    cursor.getColumnIndexOrThrow("place")
                );
                String notes = cursor.getString(
                    cursor.getColumnIndexOrThrow("notes")
                );

                TextView card = new TextView(this);

                String text = "📦 " + name
                    + "\nالغرفة: " + room
                    + "\nالمكان: " + place;

                if (notes != null && !notes.trim().isEmpty()) {
                    text += "\nملاحظات: " + notes;
                }

                card.setText(text);
                card.setTextSize(16);
                card.setTextColor(0xFF222222);
                card.setPadding(16, 16, 16, 16);
                card.setBackgroundColor(0xFFFFFFFF);
                card.setElevation(3);
                card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

                LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                params.setMargins(0, 6, 0, 6);

                itemsContainer.addView(card, params);

            } while (cursor.moveToNext());
        } else {
            TextView empty = new TextView(this);
            empty.setText("لم تحفظ أي أغراض بعد.");
            empty.setTextSize(15);
            empty.setPadding(0, 8, 0, 8);
            itemsContainer.addView(empty);
        }

        cursor.close();
        database.close();
    }

    private static class HomeDB extends SQLiteOpenHelper {

        HomeDB(Context context) {
            super(context, "dalili_home.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                "CREATE TABLE home_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "room TEXT NOT NULL," +
                "place TEXT NOT NULL," +
                "notes TEXT)"
            );
        }

        @Override
        public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
        ) {
        }

        long insertItem(
            String name,
            String room,
            String place,
            String notes
        ) {
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("room", room);
            values.put("place", place);
            values.put("notes", notes);

            long id = db.insert("home_items", null, values);
            db.close();

            return id;
        }
    }
}
