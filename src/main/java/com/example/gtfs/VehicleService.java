package com.example.gtfs;

import com.example.data.Vehicle;
import com.google.transit.realtime.GtfsRealtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    private Map<String, String> _vehicleIdsByEntityIds = new HashMap<String, String>();
    private Map<String, Vehicle> _vehiclesById = new ConcurrentHashMap<String, Vehicle>();

    public List<Vehicle> getVehicles(GtfsRealtime.FeedMessage feed) {
        return handleVechicles(feed);
    }

    private String getVehicleId(GtfsRealtime.VehiclePosition vehicle) {
        if (!vehicle.hasVehicle()) {
            return null;
        }
        GtfsRealtime.VehicleDescriptor desc = vehicle.getVehicle();
        if (!desc.hasId()) {
            return null;
        }
        return desc.getId();
    }

    private String getVehicleLabel(GtfsRealtime.VehiclePosition vehicle) {
        if (!vehicle.hasVehicle()) {
            return null;
        }
        GtfsRealtime.TripDescriptor trip = vehicle.getTrip();
        if (!trip.hasRouteId()) {
            return null;
        }
        return trip.getRouteId();
    }

    private List<Vehicle> handleVechicles(GtfsRealtime.FeedMessage feed) {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();

        for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
            if (entity.hasIsDeleted() && entity.getIsDeleted()) {
                String vehicleId = _vehicleIdsByEntityIds.get(entity.getId());
                if (vehicleId == null) {
                    log.warn("unknown entity id in deletion request: " + entity.getId());
                    continue;
                }
                _vehiclesById.remove(vehicleId);
                continue;
            }
            if (!entity.hasVehicle()) {
                continue;
            }
            GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();
            String vehicleId = getVehicleId(vehicle);
            if (vehicleId == null) {
                continue;
            }
            String vehicleLabel = getVehicleLabel(vehicle);
            _vehicleIdsByEntityIds.put(entity.getId(), vehicleId);
            if (!vehicle.hasPosition()) {
                continue;
            }
            GtfsRealtime.Position position = vehicle.getPosition();
            Vehicle v = new Vehicle();
            v.setVid(vehicleId);
            v.setLabel(vehicleLabel);
            v.setLat(position.getLatitude());
            v.setLon(position.getLongitude());
            v.setLastUpdate(System.currentTimeMillis());

            Vehicle existing = _vehiclesById.get(vehicleId);
            if (existing == null || existing.getLat() != v.getLat()
                    || existing.getLon() != v.getLon()) {
                _vehiclesById.put(vehicleId, v);
            } else {
                v.setLastUpdate(existing.getLastUpdate());
            }

            vehicles.add(v);
        }

        log.info("vehicles updated: " + vehicles.size());

        return vehicles;
    }

}
