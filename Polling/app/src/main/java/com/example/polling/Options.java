package com.example.polling;

import java.util.ArrayList;

public class Options {
    private int count;
    private String optionAns;
    private ArrayList<String> selectedUID;

    public Options(String optionAns) {
        this.count = 0;
        this.optionAns = optionAns;
        this.selectedUID = null;
    }

    public Options(String optionAns, int count) {
        this.count = count;
        this.optionAns = optionAns;
        this.selectedUID = null;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getOptionAns() {
        return optionAns;
    }

    public void setOptionAns(String optionAns) {
        this.optionAns = optionAns;
    }

    public ArrayList<String> getSelectedUID() {
        return selectedUID;
    }

    public void setSelectedUID(ArrayList<String> selectedUID) {
        this.selectedUID = selectedUID;
    }
}
