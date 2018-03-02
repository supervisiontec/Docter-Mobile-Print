package lk.supervision.doctermaster.common;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import lk.supervision.doctermaster.model.DoctorAndLocation;
import lk.supervision.doctermaster.model.MaxAppointmentAndRuningNo;

/**
 * Created by kavish manjitha on 2/9/2018.
 */

public class RestApiRecever {

    public DoctorAndLocation[] getDocterAndLocationDetails(String userName, String password, String date) throws RuntimeException {
        final String url = AppEnvironmentValues.MAIN_SERVER_ADDRESS + "/api/v1/doctor-channel/mobile/android/get-all-doctors-location/" + userName + "/" + password + "/" + date;
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        DoctorAndLocation[] response = restTemplate.getForObject(url, DoctorAndLocation[].class);
        return response;
    }

    public MaxAppointmentAndRuningNo getMaxAppointmentAndRuningNo(String date, Integer docter, Integer location) throws RuntimeException {
        final String url = AppEnvironmentValues.MAIN_SERVER_ADDRESS + "/api/v1/doctor-channel/mobile/android/get-max-appointment-and-runing-no/" + date + "/" + docter + "/" + location;
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        MaxAppointmentAndRuningNo response = restTemplate.getForObject(url, MaxAppointmentAndRuningNo.class);
        return response;
    }

    public Integer getPrintNextNo(String date, Integer docter, Integer location) throws RuntimeException {
        final String url = AppEnvironmentValues.MAIN_SERVER_ADDRESS + "/api/v1/doctor-channel/mobile/android/save-appointment-detail-onfoot-client/" + date + "/" + docter + "/" + location;
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        Integer response = restTemplate.getForObject(url, Integer.class);
        return response;
    }

    public Integer getOnlinePrintCode(String date, Integer docter, Integer location, String code) throws RuntimeException {
        final String url = AppEnvironmentValues.MAIN_SERVER_ADDRESS + "/api/v1/doctor-channel/mobile/android/online-client-get-appointment-chit/" + date + "/" + docter + "/" + location + "/" + code;
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        Integer response = restTemplate.getForObject(url, Integer.class);
        return response;
    }
}
