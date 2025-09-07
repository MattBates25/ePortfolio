    // ProgressFragment.java
    package com.test.weighttrackingapplicationmatthewbates;

    import android.os.Bundle;
    import androidx.fragment.app.Fragment;
    import androidx.recyclerview.widget.GridLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;

    import java.util.ArrayList;
    import java.util.List;

    public class ProgressFragment extends Fragment {

        private RecyclerView recyclerView;
        private WeightAdapter weightAdapter;
        private List<ProgressItem> weightItemList;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_progress, container, false);

            // Initialize RecyclerView
            recyclerView = view.findViewById(R.id.weight_grid);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

            // Create a list of weight items (example data)
            weightItemList = new ArrayList<ProgressItem>();
            weightItemList.add(new ProgressItem("2024-10-01", 150.0f));
            weightItemList.add(new ProgressItem("2024-10-02", 149.5f));

            // Initialize and set the adapter
            weightAdapter = new WeightAdapter(weightItemList);
            recyclerView.setAdapter(weightAdapter);

            return view;
        }
    }
