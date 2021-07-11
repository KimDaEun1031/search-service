package org.daeun.msafront.repository;

import org.daeun.msafront.vo.CovidVaccineStatVO;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CovidVaccineStatRepository extends MongoRepository<CovidVaccineStatVO, String>{

//	@Query("SELECT new org.daeun.restapi.vo.CovidVaccineStatVO(c.baseDate, c.sido) FROM covidPractice c WHERE c.baseDate = :baseDate")
	List<CovidVaccineStatVO> findByBaseDateAndSido(String baseDate, String sido);
	List<CovidVaccineStatVO> findByBaseDateBetween(String startDate, String endDate);
	List<CovidVaccineStatVO> findByBaseDate(String startDate);
}
