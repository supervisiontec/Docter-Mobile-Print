package lk.supervision.doctermaster.model;

import java.io.Serializable;

/**
 * Created by kavish manjitha on 2/9/2018.
 */

public class DoctorAndLocation implements Serializable {

    private Integer LocationIndexNo;
    private String location;
    private Integer doctorIndexNo;
    private String doctor;

    public DoctorAndLocation() {
    }

    public Integer getLocationIndexNo() {
        return LocationIndexNo;
    }

    public void setLocationIndexNo(Integer LocationIndexNo) {
        this.LocationIndexNo = LocationIndexNo;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getDoctorIndexNo() {
        return doctorIndexNo;
    }

    public void setDoctorIndexNo(Integer doctorIndexNo) {
        this.doctorIndexNo = doctorIndexNo;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    @Override
    public String toString() {
        return location;
    }
}
