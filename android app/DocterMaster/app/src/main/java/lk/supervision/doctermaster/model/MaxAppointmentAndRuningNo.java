package lk.supervision.doctermaster.model;

import java.io.Serializable;

/**
 * Created by kavish manjitha on 2/9/2018.
 */

public class MaxAppointmentAndRuningNo implements Serializable {

    private Integer maxAppointmentNo;
    private Integer runningNo;

    public MaxAppointmentAndRuningNo() {
    }

    public Integer getMaxAppointmentNo() {
        return maxAppointmentNo;
    }

    public void setMaxAppointmentNo(Integer maxAppointmentNo) {
        this.maxAppointmentNo = maxAppointmentNo;
    }

    public Integer getRunningNo() {
        return runningNo;
    }

    public void setRunningNo(Integer runningNo) {
        this.runningNo = runningNo;
    }
}
