package com.quintus.labs.grocerystore.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;
import com.quintus.labs.grocerystore.R;
import com.quintus.labs.grocerystore.activity.MainActivity;
import com.quintus.labs.grocerystore.adapter.CategoryAdapter;
import com.quintus.labs.grocerystore.adapter.HomeSliderAdapter;
import com.quintus.labs.grocerystore.adapter.NewProductAdapter;
import com.quintus.labs.grocerystore.adapter.PopularProductAdapter;
import com.quintus.labs.grocerystore.adapter.listCateAdapter;
import com.quintus.labs.grocerystore.api.clients.RestClient;
import com.quintus.labs.grocerystore.helper.Data;
import com.quintus.labs.grocerystore.model.Category;
import com.quintus.labs.grocerystore.model.myModel.CategoryResult;
import com.quintus.labs.grocerystore.model.myModel.ProductResult;
import com.quintus.labs.grocerystore.model.myModel.MyCategory;
import com.quintus.labs.grocerystore.model.myModel.MyProduct;
import com.quintus.labs.grocerystore.model.Token;
import com.quintus.labs.grocerystore.model.User;
import com.quintus.labs.grocerystore.model.listCate;
import com.quintus.labs.grocerystore.util.localstorage.LocalStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    ViewPager viewPager;
    LinearLayout sliderDotspanel;
    Timer timer;
    int page_position = 0;
    Data data;
    View progress;
    LocalStorage localStorage;
    Gson gson = new Gson();
    User user;
    Token token;
    private int dotscount;
    private ImageView[] dots;
    private List<MyCategory> categoryList = new ArrayList<>();
    private List<MyProduct> myProductList = new ArrayList<>();
    private List<MyProduct> myNewProductList = new ArrayList<>();
//    private List<com.quintus.labs.grocerystore.model.myModel.MyProduct> popularMyProductList = new ArrayList<>();
    private RecyclerView cateRecycleView, recyclerView, nRecyclerView, pRecyclerView;
    private CategoryAdapter mAdapter;
    private NewProductAdapter nAdapter;
    private PopularProductAdapter pAdapter;
    private Integer[] images = {R.drawable.slider1, R.drawable.slider2, R.drawable.slider3, R.drawable.slider4, R.drawable.slider5};


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        recyclerView = view.findViewById(R.id.category_rv);
        pRecyclerView = view.findViewById(R.id.popular_product_rv);
        nRecyclerView = view.findViewById(R.id.new_product_rv);
        progress = view.findViewById(R.id.progress_bar);

        localStorage = new LocalStorage(getContext());
        user = gson.fromJson(localStorage.getUserLogin(), User.class);
//        token = "abc";
        getCategoryData();

        getPopularProduct();

//        getNewProduct();

        timer = new Timer();
        viewPager = view.findViewById(R.id.viewPager);

        sliderDotspanel = view.findViewById(R.id.SliderDots);

        HomeSliderAdapter viewPagerAdapter = new HomeSliderAdapter(getContext(), images);

        viewPager.setAdapter(viewPagerAdapter);

        dotscount = viewPagerAdapter.getCount();
        dots = new ImageView[dotscount];

        for (int i = 0; i < dotscount; i++) {

            dots[i] = new ImageView(getContext());
            dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.non_active_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(8, 0, 8, 0);

            sliderDotspanel.addView(dots[i], params);

        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                for (int i = 0; i < dotscount; i++) {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.non_active_dot));
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        scheduleSlider();

        return view;
    }

    private void getPopularProduct() {

        setupPopularProductRecycleView();

        showProgressDialog();
        Call<ProductResult> call = RestClient.getRestService(getContext()).getAllProduct();
        call.enqueue(new Callback<ProductResult>() {
            @Override
            public void onResponse(Call<ProductResult> call, Response<ProductResult> response) {

                if (response != null) {

                    ProductResult productResult = response.body();
                    if (productResult.getStatus() == 200) {
                        myProductList = productResult.getProductList();
//                        Log.d("popMyProductList :=>", myProductList + "");
                        for(int i=0;i<myProductList.size();i++){
                            myProductList.get(i).setAttribute("unit");
                            myProductList.get(i).setCurrency("VND");
                            myProductList.get(i).setPrice(String.valueOf(Integer.parseInt(myProductList.get(i).getDiscount())+ 30000));
                            myProductList.get(i).setHomepage("home");
                        }

                        myNewProductList.add(myProductList.get(19));
                        myNewProductList.add(myProductList.get(18));
                        myNewProductList.add(myProductList.get(17));
                        myNewProductList.add(myProductList.get(16));
                        myNewProductList.add(myProductList.get(15));

                        Log.d("popMyProductList :=>", myNewProductList + "");
                        setupPopularProductRecycleView();
//                        setupProductRecycleView();

                    }

                }

                hideProgressDialog();
            }

            @Override
            public void onFailure(Call<ProductResult> call, Throwable t) {

            }
        });
    }

    private void setupPopularProductRecycleView() {

        pAdapter = new PopularProductAdapter(myProductList, getContext(), "Home");
        RecyclerView.LayoutManager pLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        pRecyclerView.setLayoutManager(pLayoutManager);
        pRecyclerView.setItemAnimator(new DefaultItemAnimator());
        pRecyclerView.setAdapter(pAdapter);

    }

    private void getNewProduct() {
        setupProductRecycleView();
    }

    private void setupProductRecycleView() {
        nAdapter = new NewProductAdapter(myNewProductList, getContext(), "Home");
        RecyclerView.LayoutManager nLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        nRecyclerView.setLayoutManager(nLayoutManager);
        nRecyclerView.setItemAnimator(new DefaultItemAnimator());
        nRecyclerView.setAdapter(nAdapter);

    }

    private void getCategoryData() {

        showProgressDialog();

        Call<CategoryResult> call = RestClient.getRestService(getContext()).getAllCategory();
        call.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(Call<CategoryResult> call, Response<CategoryResult> response) {
                if (response != null) {

                    CategoryResult categoryResult = response.body();
                    if (categoryResult.getStatus() == 200) {
                        categoryList = categoryResult.getCategoryList();
                        categoryList.get(0).setImgCate("https://m.economictimes.com/thumb/msid-93337841,width-1200,height-900,resizemode-4,imgsize-130870/1-25.jpg");
                        categoryList.get(1).setImgCate("https://cdn.quantrinhahang.edu.vn/wp-content/uploads/2019/06/fast-food-la-gi.jpg");
                        categoryList.get(2).setImgCate("https://vn-test-11.slatic.net/p/eed436bd8431e03d499f1e1d831fb5f6.jpg");
                        categoryList.get(3).setImgCate("https://statics.vinpearl.com/tra-sua-da-nang-01_1630901177.jpg");
                        categoryList.get(4).setImgCate("https://images.healthshots.com/healthshots/en/uploads/2022/04/17151621/fruit-salad-1600x900.jpg");

                        setupCategoryRecycleView();
                    }

                }

                hideProgressDialog();
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {
                Log.d("Error==>", t.getMessage());
            }
        });

    }

    private void setupCategoryRecycleView() {
        mAdapter = new CategoryAdapter(categoryList, getContext(), "Home");
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


    }


    public void scheduleSlider() {

        final Handler handler = new Handler();

        final Runnable update = new Runnable() {
            public void run() {
                if (page_position == dotscount) {
                    page_position = 0;
                } else {
                    page_position = page_position + 1;
                }
                viewPager.setCurrentItem(page_position, true);
            }
        };

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                handler.post(update);
            }
        }, 500, 4000);
    }

    @Override
    public void onStop() {
        timer.cancel();
        super.onStop();
    }

    @Override
    public void onPause() {
        timer.cancel();
        super.onPause();
    }

    public void onLetsClicked(View view) {
        startActivity(new Intent(getContext(), MainActivity.class));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Trang chủ");
    }

    private void hideProgressDialog() {
        progress.setVisibility(View.GONE);
    }

    private void showProgressDialog() {
        progress.setVisibility(View.VISIBLE);
    }

}
