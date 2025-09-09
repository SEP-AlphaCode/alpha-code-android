package com.ubtrobot.mini.sdkdemo.models.response;

import java.util.List;

public class Detection {
    public String label;
    public double confidence;
    public List<Integer> bbox;
    public Double depth_avg;    // nullable
    public Double depth_min;    // nullable
    public Double depth_median; // nullable
}