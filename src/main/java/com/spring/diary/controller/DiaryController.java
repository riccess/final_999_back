package com.spring.diary.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.spring.diary.common.dto.PageRequestDTO;
import com.spring.diary.common.dto.PageResultDTO;
import com.spring.diary.dto.DiaryDTO;
import com.spring.diary.dto.DiaryUpdateDTO;
import com.spring.diary.repository.DiaryRepository;
import com.spring.diary.service.DiaryServiceImpl;
import com.spring.diary.service.FileServiceImpl;

//import ch.qos.logback.classic.Logger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController		// 페이지 전환이 필요없으므로 restController 사용
@RequestMapping(value = "/api")
@CrossOrigin(origins = {"http://localhost:3000"})
@DynamicUpdate
public class DiaryController {
	// logger 객체 생성 및 현재 DiaryController 클래스 지정
	private final Logger logger = LoggerFactory.getLogger(this.getClass()); 
	
	// 편의를 위해 controller에 임시로 diaryRepo 불러옴. 실제로는 서비스를 통해야 함
	@Autowired
	DiaryRepository diaryRepo;
	
	@Autowired
	DiaryServiceImpl diaryService;
	
	@Autowired
	FileServiceImpl fileService;
	
//	//카카오 나의 정보 얻기
//	@RequestMapping(value = "/kakaoLogin", method = RequestMethod.GET, produces="application/json;charset=utf-8")
//	@ResponseBody
//	public String selectMyInfoWithKakao(@RequestParam String token) {
//
//		String myToken = "Bearer " + token;
//		
//		//헤더 객체 생성
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//		headers.set("Authorization", myToken);
//		
//		//요청 url
//		UriComponentsBuilder builder = 
//				UriComponentsBuilder.fromHttpUrl("https://kapi.kakao.com/v2/user/me");
//
//		HttpEntity<?> entity = new HttpEntity<>(headers);
//
//		HttpEntity<String> response = null;
//		
//        //요청
//		try {
//			
//			 response = RestTemplate.exchange(
//			        builder.toUriString(), 
//			        HttpMethod.GET, 
//			        entity, 
//			        String.class);
//			 
//			 System.out.println("응답결과 :" + response.getBody());
//
//		} catch (HttpStatusCodeException e) {
//			
//			System.out.println("error :" + e);
//
//		}
//		
//		return response.getBody();
//	}
	
	@PostMapping("/diary")
	public void insertDiary(@RequestParam("file") List<MultipartFile> files, @RequestParam("title") String title, @RequestParam("content") String content) {
		logger.info("다이어리 컨트롤러 : insert request");
		
		Long diaryNo = diaryService.insertDiary(title, content);
		
		if(diaryNo != null && files != null) {
			fileService.insertFile(files, diaryNo);
		}
	}
	
	@GetMapping("/diary/{diaryNo}")
	public DiaryDTO getDiary(@PathVariable Long diaryNo) {
		logger.info("다이어리 get : " + diaryNo);
		DiaryDTO diaryDTO = null;
		diaryDTO = diaryService.getDiaryByNo(diaryNo);
		return diaryDTO;
	}
	
	// diary update
	@PutMapping("/updateDiary/{diaryNo}")
	public void updateDiary(@PathVariable("diaryNo") Long diaryNo,
			@RequestParam("title") String title,
			@RequestParam("content") String content,
			@RequestParam(name = "addedFile", required = false) List<MultipartFile> files) {
		
		System.out.println(title + " - " + content);
		diaryService.DiaryDTOUpdate(diaryNo, title, content);
		
		System.out.println("files : " + files);
		// 추가 파일이 있다면 파일 해당 다이어리 넘버로 파일 추가저장 !
		fileService.insertFile(files, diaryNo);
	}
	
	// diary delete - 혜원
	@DeleteMapping("/page/delete")
	public void deleteDiary(@RequestParam("diaryNo") Long diaryNo) {
		diaryService.deleteDiary(diaryNo);
	}
	
	// diary insert - 나현
	@PostMapping("/insert")
	public Long insertDiary(@RequestParam("title") String title, @RequestParam("content") String content, 
		   					@RequestParam(name="files", required = false) List<MultipartFile> files) {
		DiaryDTO newDiaryDTO = DiaryDTO.builder()
                              	.title(title)
                              	.content(content)
                              	.build();
      
		Long diaryNo = diaryService.insertDiary(newDiaryDTO);
		System.out.println(diaryNo);
   
		if (files != null) {
			fileService.insertFile(files, diaryNo);
		}
		return diaryNo;
	}

	
	@GetMapping("/batch")
	public void insertBatchData() {
		List<DiaryDTO> diaryList = new ArrayList<DiaryDTO>();
		
		// 200개의 intstream 생성 후 반복문 실행
		IntStream.rangeClosed(51, 250).forEach(i -> {
			DiaryDTO diaryDTO = DiaryDTO.builder()
										.title("Title" + i)
										.content("Content" + i)
										.build();
			diaryList.add(diaryDTO);
		});
		diaryService.insertBatchData(diaryList);
	}
	
	@GetMapping("/diaryPage")
	public PageResultDTO diaryPage(@RequestParam("page") int pageNo, @RequestParam("size") int size) {
		PageRequestDTO requestDTO2 = PageRequestDTO.builder()
				.page(pageNo)
				.size(size)
				.build();

		PageResultDTO pageResultDTO2 = diaryService.getList(requestDTO2);
		System.out.println(pageResultDTO2);
		return pageResultDTO2;
	}
	
//	@GetMapping("/diaryScroll")
//	public List<DiaryDTO> getAllDiary() {
//		
//		return diaryService.getAllDiary();
//	}
	
	// 페이징 처리
	// pageable 인터페이스 -> PageRequest 클래스
	// of(int page, int size)
	@GetMapping("/page")
	public PageResultDTO pageTest(@RequestParam("page") int pageNo, @RequestParam("size") int size) {
		// page 객체 생성
//		Pageable pageable = PageRequest.of(0,  10);
//		System.out.println(pageable);
		
//		Page<Diary> result = diaryRepo.findAll(pageable);
//		System.out.println(result);
		
//		// 총 페이지 수
//		System.out.println(result.getTotalPages());
//		
//		// 총 요소의 개수
//		System.out.println(result.getTotalElements());
//		
//		// 현재 페이지 번호 : 0부터 시작(표현은 +1 해서 하면 1페이지부터) 
//		System.out.println(result.getNumber());
//		
//		// 페이지당 데이터 개수
//		System.out.println(result.getSize());
//		
//		// 이전, 다음페이지 존재 여부
//		System.out.println(result.hasNext());
//		System.out.println(result.hasPrevious());
//		
//		// 모든 데이터 호출
//		for(Diary diary : result.getContent()) {
//			System.out.println(diary);
//		}
		
//		// 정렬(sorting)
//		Sort sort2 = Sort.by("no").descending();
//		Pageable pageable2 = PageRequest.of(0, 10, sort2);
//		Page<Diary> result2 = diaryRepo.findAll(pageable2);
//		System.out.println(result2);
//		
//		result2.forEach(diary -> {
//			System.out.println(diary);
//		});
//		
//		// RequestDTO(getParams로 받아오는 page, size변수를 넣어서 객체를 생성할 수 있음)
//		PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
//													.page(pageNo)
//													.size(size)
//													.build();
//		System.out.println(pageRequestDTO);
//		
//		Page<Diary> diaryEntity = diaryRepo.findAll(pageRequestDTO.getPageable());
//		diaryEntity.forEach(diary ->{
//			System.out.println(diary);
//		});
		
		PageRequestDTO requestDTO2 = PageRequestDTO.builder()
													.page(pageNo)
													.size(size)
													.build();
		
		PageResultDTO pageResultDTO2 = diaryService.getList(requestDTO2);
		System.out.println(pageResultDTO2);
		return pageResultDTO2;
	}
	
		
	// 이 controller에서 발생할 수 있는 모든 예외를 처리할 수 있는 annotation
//	@ExceptionHandler(NoSuchElementException.class)
//	public ResponseEntity<String> handlerNoSuchElementException(){
//		return ResponseEntity.status(404).body("해당 요청값은 존재하지 않습니다.");
//	}
	
	

}
