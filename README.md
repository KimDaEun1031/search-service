# Spring Boot 기반 Microservice Architecture 구현
![search](https://user-images.githubusercontent.com/73865700/126298148-528f25d9-6966-4ad7-b9dc-f1b71ed114f4.png)
> NOTE

> + Covid19 예방접종 통계 Data를 MSA 기반으로 구축해본다.
> + Service는 Spring boot Project로 구현된다.
> + JPA repository로 DB(MongoDB)에 접근한다.
> + MSA는 서비스 별로 형성관리를 분리함으로 이번 Study에서 분리 개발했다.
> + Collector Service는 DB Service에 데이터를 Push하거나 Search Service에 받은 요청에 따라 값을 return 한다.
> + https://github.com/KimDaEun1031/collector_service
> + DB Service는 Collector Service에서 Push한 데이터를 받아 DB에 Insert한다.
> + https://github.com/KimDaEun1031/db_service

## Search Service Description
#### Project directory tree
```
.
├─ mvnw
├─ mvnw.cmd
├─ pom.xml
├─ src/main/java/org/daeun/search
│       │                     ├─ SearchApplication.java
│       │                     └─ controller
│       │                            └─ CovidApiSearchController.java
│       └─ resources
│           └─ application.properties
│  
└─ target
     ├─classes
     ├─generated-sources ...
```
Search Service는 DB 저장된 데이터와 Collector Service의 데이터를 검색하여 화면에 보여준다.
> + 참고 사이트들을 블로그에 올려놓았다.  
> https://relaxed-it-study.tistory.com/category/JAVA/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8

## 1. Dependency
MongoDB와 Spring Web, Gson, Lombok를 추가한다.
Gson과 Apache-Commons를 제외하고는 Spring boot를 설치할 때 나오는 dependency에서 추가할 수 있다.

```
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.5.2</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	
	<groupId>org.daeun</groupId>
	<artifactId>search</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>search</name>
	<description>Spring Boot</description>
	
	<properties>
		<java.version>1.8</java.version>
	</properties>
	
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-mongodb</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web-services</artifactId>
		</dependency>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>com.google.code.gson</groupId>
			<artifactId>gson</artifactId>
			<version>2.8.5</version>
		</dependency>
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-lang3</artifactId>
			<version>3.12.0</version>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
```

## 2. Configuration
application.yml에서는 application의 port를 입력한다.
```
server.port=9092
``` 

## 3. REST API Server

#### REST API
|METHOD|PATH + PARAMETER|DESCRIPTION|
|------|----|-----------|
|GET|/searchTodayData|오늘 데이터와 지역 데이터 검색 후 반환|
|GET|/searchPeriodData|기간 데이터와 지역 리스트 데이터 검색 후 반환|

## 4. Controller
Today 데이터와 지역 데이터를 Collector Service에 요청한 후 값을 return받는다.
```
@GetMapping("/searchTodayData")
public String searchTodayDataCovidVaccineStat(@RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now()}") @DateTimeFormat(pattern = "yyyyMMdd") LocalDate nowDate, @RequestParam(required = false, defaultValue = "전국") String sido) {
        String mapInString = "";
        try {
            RestTemplate restTemplate = new RestTemplate();

            String url = "http://localhost:9090/searchCovidVaccineStatTodayData?nowDate="+nowDate+"&sido="+URLEncoder.encode(sido, "UTF-8");
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
```
**HOST**  
localhost:9092

**PATH(GET)**  
/searchTodayData

**PARAMETERS**  
1. nowDate  
	- in : query  
	- description : 지정한 날짜
	- type : LocalDate  
	- default : todayDate

2. sido
	- in : query  
	- description : 지역명칭
	- type : string  
	- default : 전국

**EXAMPLE**
1. Basic - localhost:9092/searchTodayData  
2. Parameter - localhost:9092/searchTodayData?nowDate=20210405&sido=서울특별시

---
지정한 시작 날짜와 끝 날짜, 지역 리스트를 DB Service에 요청해 값을 return받는다.
```
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
```
**HOST**  
localhost:9092

**PATH(GET)**  
/searchPeriodData

**PARAMETERS**  
1. startDate  
	- in : query  
	- description : 지정한 시작 날짜
	- type : LocalDate  

2. endDate
	- in : query  
	- description : 지정한 끝 날짜
	- type : LocalDate

3. sido
	- in : query  
	- description : 지역명칭
	- type : List

**EXAMPLE**
localhost:9092/searchPeriodData?startDate=20210405&endDate=20210501&sido=전국,서울특별시,경기도
