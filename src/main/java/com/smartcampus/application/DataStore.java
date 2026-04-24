package com.smartcampus.application;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum DataStore {
    INSTANCE;
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    public Map<String, Room> getRooms() { return rooms; }
    public Room getRoom(String id) { return rooms.get(id); }
    public void putRoom(Room r) { rooms.put(r.getId(), r); }
    public boolean deleteRoom(String id) { return rooms.remove(id) != null; }

    public Map<String, Sensor> getSensors() { return sensors; }
    public Sensor getSensor(String id) { return sensors.get(id); }
    public void putSensor(Sensor s) { sensors.put(s.getId(), s); }
    public boolean deleteSensor(String id) { return sensors.remove(id) != null; }

    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }
    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}
