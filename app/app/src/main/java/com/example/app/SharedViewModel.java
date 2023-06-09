package com.example.app;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> waterState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> lightState = new MutableLiveData<>();

    public void setWaterState(Boolean state) {        waterState.setValue(state);}

    public LiveData<Boolean> getWaterState() {
        return waterState;
    }

    public void setLightState(Boolean state) {
        lightState.setValue(state);
    }

    public LiveData<Boolean> getLightState() {
        return lightState;
    }
}