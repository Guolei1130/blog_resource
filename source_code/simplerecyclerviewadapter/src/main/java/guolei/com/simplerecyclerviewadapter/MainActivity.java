package guolei.com.simplerecyclerviewadapter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import simpleadapter.BaseItem;
import simpleadapter.SimpleRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity {

    private List<BaseItem> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        generateMockData();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SimpleRecyclerViewAdapter(mData));
    }

    private void generateMockData() {
        mData.add(new Item1());
        mData.add(new Item2());
        mData.add(new ItemEmpty());
        mData.add(new Item3());
        mData.add(new Item3());
        mData.add(new ItemEmpty());
        mData.add(new Item2());
        mData.add(new Item1());
    }
}
