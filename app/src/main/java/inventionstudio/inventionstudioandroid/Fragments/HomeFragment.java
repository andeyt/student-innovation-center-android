package inventionstudio.inventionstudioandroid.Fragments;


import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import inventionstudio.inventionstudioandroid.API.SumsApiService;
import inventionstudio.inventionstudioandroid.Model.StudioDescription;
import inventionstudio.inventionstudioandroid.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private Retrofit retrofit;
    private Call call;
    private ProgressBar loadProgress;
    private TextView studioDescriptionText;
    private ImageView image;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setTitle("Invention Studio");
        // Inflate the layout for this fragment
        studioDescriptionText = rootView.findViewById(R.id.studio_description);
        loadProgress = (ProgressBar) rootView.findViewById(R.id.progressBar);
        image = (ImageView) rootView.findViewById(R.id.imageView);
        connectAndGetStudioDescription();
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
     * Get studio description from IS Server
     */
    public void connectAndGetStudioDescription() {

        retrofit = new Retrofit.Builder()
                .baseUrl("https://sums.gatech.edu/SUMSAPI/rest/API/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SumsApiService sumsApiService = retrofit.create(SumsApiService.class);
        call = sumsApiService.getStudioDescription(8);
        call.enqueue(new Callback<StudioDescription>() {
            @Override
            public void onResponse(Call<StudioDescription> call, Response<StudioDescription> response) {
                StudioDescription description = response.body();
                studioDescriptionText.setText(Html.fromHtml(description.getEquipmentGroupDescriptionHtml()));
                Picasso.get()
                        .load(
                                "https://is-apps.me.gatech.edu/resources/images/headers/invention_studio.jpg")
                        .into(image, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                loadProgress.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });

            }
            @Override
            public void onFailure(Call<StudioDescription> call, Throwable throwable) {
                loadProgress.setVisibility(View.GONE);
            }
        });
    }
}
