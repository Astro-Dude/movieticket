package model;

import enums.City;
import java.util.List;

public class Theatre {
    private final String id;
    private final String name;
    private final City city;
    private final List<Screen> screens;

    public Theatre(String id, String name, City city, List<Screen> screens) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.screens = screens;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public City getCity() { return city; }
    public List<Screen> getScreens() { return screens; }

    @Override
    public String toString() {
        return "Theatre{" + name + ", " + city + "}";
    }
}
