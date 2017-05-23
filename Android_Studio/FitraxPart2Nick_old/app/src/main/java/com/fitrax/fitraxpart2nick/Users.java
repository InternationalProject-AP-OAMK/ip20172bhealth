package com.fitrax.fitraxpart2nick;

import java.io.Serializable;

/**
 * Created by Koen on 22-5-2017.
 */

public class Users implements Serializable{
    private String userName, teamName, heartRate;

    public Users(String userName, String teamName, Object o){

    }

    public Users(String name, String userName, String teamName, String heartRate){
        this.userName = userName;
        this.teamName = teamName;
        this.heartRate = heartRate;
    }



    public String getUserName(){
        return userName;
    }

    public String getTeamName(){
        return teamName;
    }

    public String getHeartRate(){
        return heartRate;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public void setTeamName(String teamName){
        this.teamName = teamName;
    }

    public void setHeartRate(String heartRate){
        this.heartRate = heartRate;
    }

}
