package service;

import enums.City;
import model.Theatre;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TheatreService {
    private final Map<String, Theatre> theatreMap = new ConcurrentHashMap<>();

    public void addTheatre(Theatre theatre) {
        theatreMap.put(theatre.getId(), theatre);
    }

    public Theatre getTheatre(String theatreId) {
        return theatreMap.get(theatreId);
    }

    public List<Theatre> getTheatresByCity(City city) {
        return theatreMap.values().stream()
                .filter(t -> t.getCity() == city)
                .collect(Collectors.toList());
    }
}
