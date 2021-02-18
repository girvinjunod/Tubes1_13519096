package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

public class Occupier {
    @SerializedName("id")
    public int id;

    @SerializedName("playerId")
    public int playerId;

    @SerializedName("health")
    public int health;

    @SerializedName("position")
    public Position position;
}
