package org.daeun.msaclient.controller;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.daeun.msaclient.repository.CovidVaccineStatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.*;

@RestController
@Slf4j
public class CovidApiFrontController {

    @Autowired
    CovidVaccineStatRepository covidVaccineStatRepository;

    @GetMapping("/searchTodayData")
    public String searchTodayDataCovidVaccineStat(@RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now()}") @DateTimeFormat(pattern = "yyyyMMdd") LocalDate nowDate,
                                                  @RequestParam(required = false, defaultValue = "전국") String sido) {
        Map<String, Object> result = new HashMap<String, Object>();

        log.info("date = {}", nowDate);

        String jsonInString = "";
        try {
            RestTemplate restTemplate = new RestTemplate();

            String url = "http://localhost:9090/searchTodayData?nowDate="+nowDate+"&sido="+URLEncoder.encode(sido, "UTF-8");

            log.info(url);

            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);

            log.info("get TodayData");

            ResponseEntity<Map> resultMap = restTemplate.exchange(URI.create(url), HttpMethod.GET, entity, Map.class);

            result.put("statusCode", resultMap.getStatusCodeValue());
            result.put("header", resultMap.getHeaders());
            result.put("body", resultMap.getBody());

            log.info("body = {} ",resultMap.getBody());

            jsonInString = resultMap.getBody().toString();


        } catch (HttpClientErrorException | HttpServerErrorException e) {
            result.put("statusCode", e.getRawStatusCode());
            result.put("body", e.getStatusText());
            log.error(e.toString());

        } catch (Exception e) {
            result.put("statusCode", "999");
            result.put("body", "excpetion 오류");
            log.error(e.toString());
        }

        return jsonInString;
    }

    @RequestMapping("/searchPeriodData")
    public String searchCovidVaccineStatDb(@RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate startDate,
                                           @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate endDate,
                                           @RequestParam List<String> sido
                                           ) {

        log.info("list = {}",sido);
        Map<String, Object> result = new HashMap<String, Object>();
        String search = "";
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);

            log.info("get PeriodData");

            String sidoList = StringUtils.join(sido,",");
            log.info("sidoList = {}",sidoList);

            String url = "http://localhost:9091/searchPeriodDataCovidVaccineStat?startDate="+startDate+"&endDate="+endDate+"&sido="+URLEncoder.encode(sidoList, "UTF-8");
            log.info(url);

            ResponseEntity<List> resultMap = restTemplate.exchange(URI.create(url), HttpMethod.GET, entity, List.class);
            result.put("statusCode", resultMap.getStatusCodeValue());
            result.put("header", resultMap.getHeaders());
            result.put("body", resultMap.getBody());

            Gson gson = new Gson();

            search = gson.toJson(resultMap.getBody());
            log.info("SUCCESS DATA");

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            result.put("statusCode", e.getRawStatusCode());
            result.put("body", e.getStatusText());
            log.info(e.toString());

        } catch (Exception e) {
            result.put("statusCode", "999");
            result.put("body", "excpetion 오류");
            log.info(e.toString());

        }

        return search;
    }



}