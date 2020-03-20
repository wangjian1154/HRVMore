package com.example.hrvmore;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.hrvmore.demo.PullLeftToRefreshLayout;

public class Main2Activity extends AppCompatActivity {

    PullLeftToRefreshLayout pull_refresh;
    private int[] img={R.drawable.a,
            R.drawable.b,
            R.drawable.c,
            R.drawable.d,
            R.drawable.c};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        pull_refresh=findViewById(R.id.pull_refresh);
        RecyclerView recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        MyListAdapter adapter=new MyListAdapter(img,this);
        recyclerView.setAdapter(adapter);
    }


}
