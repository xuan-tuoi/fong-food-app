package com.quintus.labs.grocerystore.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.quintus.labs.grocerystore.R;
import com.quintus.labs.grocerystore.adapter.NewProductAdapter;
import com.quintus.labs.grocerystore.api.clients.RestClient;
import com.quintus.labs.grocerystore.model.myModel.MyProduct;
import com.quintus.labs.grocerystore.model.Token;
import com.quintus.labs.grocerystore.model.User;
import com.quintus.labs.grocerystore.model.myModel.ProductResult;
import com.quintus.labs.grocerystore.util.localstorage.LocalStorage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewProductFragment extends Fragment {
    RecyclerView nRecyclerView;
    View progress;
    LocalStorage localStorage;
    Gson gson = new Gson();
    User user;
    Token token;
    List<MyProduct> myProductList = new ArrayList<>();
    List<MyProduct> myNewProductList = new ArrayList<>();
    private NewProductAdapter pAdapter;

    public NewProductFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new, container, false);
        nRecyclerView = view.findViewById(R.id.new_product_rv);

        progress = view.findViewById(R.id.progress_bar);

        localStorage = new LocalStorage(getContext());
        user = gson.fromJson(localStorage.getUserLogin(), User.class);
//        token = new Token(user.getToken());

        getNewProduct();

        return view;
    }


    private void getNewProduct() {
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

                        setupProductRecycleView();

                    }

                }

                hideProgressDialog();
            }

            @Override
            public void onFailure(Call<ProductResult> call, Throwable t) {

            }
        });
    }

    private void setupProductRecycleView() {
        pAdapter = new NewProductAdapter(myNewProductList, getContext(), "new");
        RecyclerView.LayoutManager pLayoutManager = new LinearLayoutManager(getContext());
        nRecyclerView.setLayoutManager(pLayoutManager);
        nRecyclerView.setItemAnimator(new DefaultItemAnimator());
        nRecyclerView.setAdapter(pAdapter);

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Sản phẩm mới");
    }

    private void hideProgressDialog() {
        progress.setVisibility(View.GONE);
    }

    private void showProgressDialog() {
        progress.setVisibility(View.VISIBLE);
    }

}
