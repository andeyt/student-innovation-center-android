package inventionstudio.inventionstudioandroid.Fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import inventionstudio.inventionstudioandroid.API.SumsApiService;
import inventionstudio.inventionstudioandroid.Adapters.GroupAdapter;
import inventionstudio.inventionstudioandroid.Model.Equipment;
import inventionstudio.inventionstudioandroid.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class EquipmentGroupFragment extends Fragment {


    public static final String BASE_URL = "https://sums.gatech.edu/SUMSAPI/rest/API/";
    private static Retrofit retrofit = null;
    private HashSet<String> groups;
    private ListView listView;
    private Call<List<Equipment>> call;
    private ProgressBar loadProgress;
    private SwipeRefreshLayout refreshLayout;
    public static final String USER_PREFERENCES = "UserPrefs";

    public EquipmentGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_equipment_group, container, false);
        BottomNavigationView bottom =  (getActivity().findViewById(R.id.bottomBar));
        getActivity().setTitle(bottom.getMenu().findItem(bottom.getSelectedItemId()).getTitle());
        listView = (ListView) rootView.findViewById(R.id.listview);
        listView.addHeaderView(new View(getContext()), null, true);

        loadProgress = (ProgressBar) rootView.findViewById(R.id.progressBar);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = listView.getItemAtPosition(position);
                String str = o.toString();

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment fragment2 = new EquipmentListFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("MachineGroup", str);
                fragment2.setArguments(bundle);
                // Replace the contents of the container with the new fragment
                ft.replace(R.id.fragment_container, fragment2);
                ft.addToBackStack(null);
                // or ft.add(R.id.your_placeholder, new FooFragment());
                // Complete the changes added above
                ft.commit();
            }
        });

        connectAndGetEquipmentGroups();

        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeToRefresh);
        refreshLayout.setColorSchemeResources(R.color.IS_AccentPrimary_Light);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                connectAndGetEquipmentGroups();
            }
        });

        return rootView;

    }

    @Override
    public void onPause () {
        super.onPause();
        if (call != null) {
            call.cancel();
        }
    }

    /**
     * Get equipment groups from SUMS
     */
    public void connectAndGetEquipmentGroups() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        SumsApiService sumsApiService = retrofit.create(SumsApiService.class);
        SharedPreferences prefs = getContext().getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
        String username = prefs.getString("username", "");
        String otp = prefs.getString("otp", "");
        call = sumsApiService.getEquipmentList(8, username, otp);
        call.enqueue(new Callback<List<Equipment>>() {
            @Override
            public void onResponse(Call<List<Equipment>> call, Response<List<Equipment>> response) {
                List<Equipment> e = response.body();
                groups = new HashSet<>();
                for (Equipment m : e) {
                    if (!(m.getLocationName().equals(""))) {
                        groups.add(m.getLocationName());
                    }
                }

                ArrayList<String> groupList = new ArrayList<>(groups);
                Collections.sort(groupList);
                GroupAdapter adapter = new GroupAdapter(getActivity(),
                        R.layout.queue_member, groupList);
                listView.setAdapter(adapter);
                loadProgress.setVisibility(View.GONE);
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Equipment>> call, Throwable throwable) {
                loadProgress.setVisibility(View.GONE);
                refreshLayout.setRefreshing(false);
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "An Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}


