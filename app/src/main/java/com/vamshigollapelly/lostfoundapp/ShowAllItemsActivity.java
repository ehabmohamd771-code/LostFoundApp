package com.vamshigollapelly.lostfoundapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ShowAllItemsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView   recyclerView;
    private Spinner        spinnerFilter;
    private final String[] CATS = {"All","Electronics","Pets",
            "Wallets","Keys","Bags","Clothing","Other","المعهد التقني"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_items);

        db            = new DatabaseHelper(this);
        recyclerView  = findViewById(R.id.recyclerView);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayAdapter<String> adp = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATS);
        adp.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adp);

        spinnerFilter.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int pos, long id) {
                        String sel = CATS[pos];
                        loadItems("All".equals(sel) ? null : sel);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

        loadItems(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sel = (String) spinnerFilter.getSelectedItem();
        loadItems("All".equals(sel) ? null : sel);
    }

    private void loadItems(String category) {
        List<LostFoundItem> items = db.getAllItems(category);
        recyclerView.setAdapter(new ItemAdapter(items, item -> {
            Intent i = new Intent(this, ItemDetailActivity.class);
            i.putExtra("item_id", item.getId());
            startActivity(i);
        }));
    }
}