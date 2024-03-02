package com.Huard.PhoneRFFL;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.LinkedList;
import java.util.Queue;

public class FilterFragment extends Fragment {
    private TextView lblFilter;
    private ChannelViewModel channelViewModel;
    private boolean isFiltered = false;
    private final FilterManager filterManager = new FilterManager();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container, false);  // Inflate the layout for this fragment
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lblFilter = view.findViewById(R.id.lblFilter);

        channelViewModel = new ViewModelProvider(requireActivity()).get(ChannelViewModel.class);

        FilterViewModel filterViewModel = new ViewModelProvider(requireActivity()).get(FilterViewModel.class);
        filterViewModel.getAzimuth().observe(getViewLifecycleOwner(), this::receiveAzimuth);
        filterViewModel.getElevation().observe(getViewLifecycleOwner(), this::receiveElevation);

        SideViewModel sideViewModel = new ViewModelProvider(requireActivity()).get(SideViewModel.class);
        sideViewModel.getFilterSelected().observe(getViewLifecycleOwner(), this::OnPressChkFilter);

        showLabel(isFiltered);
    }

    private void OnPressChkFilter(boolean isFiltered) {
        this.isFiltered = isFiltered;
        filterManager.Reset();
        showLabel(isFiltered);
    }

    private void showLabel(boolean show) {
        if (show)
            lblFilter.setVisibility(View.VISIBLE);
        else
            lblFilter.setVisibility(View.INVISIBLE);
    }

    private void receiveAzimuth(Queue<Pair<Short, Short>> pairQueue) {
        if (pairQueue.isEmpty())
            return;

        if (isFiltered) {
            sendFilteredAzimuth(filterManager.filterAzimuth(pairQueue));
        } else {
            sendFilteredAzimuth(pairQueue);
        }
    }

    private void receiveElevation(Queue<Pair<Short, Short>> pairQueue) {
        if (pairQueue.isEmpty())
            return;

        if (isFiltered) {
            sendFilteredElevation(filterManager.filterElevation(pairQueue));
        } else {
            sendFilteredElevation(pairQueue);
        }
    }





    private void sendFilteredAzimuth(Queue<Pair<Short, Short>> values) {
        if (!values.isEmpty())
            channelViewModel.setAzimuth(new LinkedList<>(values));
    }

    private void sendFilteredElevation(Queue<Pair<Short, Short>> values) {
        if (!values.isEmpty())
            channelViewModel.setElevation(new LinkedList<>(values));
    }


}