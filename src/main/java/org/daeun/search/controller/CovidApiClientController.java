package org.daeun.search.controller;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class CovidApiClientController {

    @GetMapping("/searchTodayData")
    public String searchTodayDataCovidVaccineStat(@RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now()}") @DateTimeFormat(pattern = "yyyyMMdd") LocalDate nowDate,
                                                  @RequestParam(required = false, defaultValue = "전국") String sido) {
        String mapInString = "";
        try {
            RestTemplate restTemplate = new RestTemplate();

            String url = "http://localhost:9090/searchCovidVaccineStat  TodayData?nowDate="+nowDate+"&sido="+URLEncoder.encode(sido, "UTF-8");

            log.info("url = {}",url);

            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);

            log.info("get TodayData");

            ResponseEntity<Map> resultMap = restTemplate.exchange(URI.create(url), HttpMethod.GET, entity, Map.class);

            log.info("body = {} ",resultMap.getBody());

            mapInString = resultMap.getBody().toString();


        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error(e.toString());

        } catch (Exception e) {
            log.error(e.toString());
        }

        return mapInString;
    }

    @RequestMapping("/searchPeriodData")
    public String searchCovidVaccineStatDb(@RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate startDate,
                                           @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate endDate,
                                           @RequestParam List<String> sidoList
                                           ) {

        log.info("sidoList = {}",sidoList);

        String jsonInString = "";
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);

            log.info("get PeriodData");

            String sido = StringUtils.join(sidoList,",");
            log.info("sido = {}",sido);

            String url = "http://localhost:9091/searchPeriodDataCovidVaccineStat?startDate="+startDate+"&endDate="+endDate+"&sido="+URLEncoder.encode(sido, "UTF-8");
            log.info(url);

            ResponseEntity<List> resultMap = restTemplate.exchange(URI.create(url), HttpMethod.GET, entity, List.class);

            Gson gson = new Gson();

            jsonInString = gson.toJson(resultMap.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.info(e.toString());

        } catch (Exception e) {
            log.info(e.toString());

        }

        return jsonInString;
    }



}
