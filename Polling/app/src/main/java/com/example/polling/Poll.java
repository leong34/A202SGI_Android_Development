package com.example.polling;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Poll {
    private String pollId;
    private String creatorUID;
    private String question;
    private ArrayList<Options> options;
    private int participant;
    private boolean isPublic;
    private String createdDate;
    private String creatorName;
    private String status = "Available";
    private int viewed;
    private ArrayList<Tag> tags;

    public Poll(String pollId, String creatorUID, String question, ArrayList<Options> options, String creatorName, boolean isPublic){
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        this.pollId = pollId;
        this.creatorUID = creatorUID;
        this.question = question;
        this.options = options;
        this.createdDate = format.format(date);
        this.creatorName = creatorName;
        this.isPublic = isPublic;
    }

    public Poll(String pollId, String creatorUID, String creatorName, String question, ArrayList<Options> options, int participant, String createdDate, boolean isPublic) {
        this.pollId = pollId;
        this.creatorUID = creatorUID;
        this.creatorName = creatorName;
        this.question = question;
        this.options = options;
        this.participant = participant;
        this.createdDate = createdDate;
        this.isPublic = isPublic;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Tag> tags) {
        this.tags = tags;
    }

    public int getViewed() {
        return viewed;
    }

    public void setViewed(int viewed) {
        this.viewed = viewed;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getPollId() {
        return pollId;
    }

    public void setPollId(String pollId) {
        this.pollId = pollId;
    }

    public String getCreatorUID() {
        return creatorUID;
    }

    public void setCreatorUID(String creatorUID) {
        this.creatorUID = creatorUID;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public ArrayList<Options> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<Options> options) {
        this.options = options;
    }

    public int getParticipant() {
        return participant;
    }

    public void setParticipant(int participant) {
        this.participant = participant;
    }
}
