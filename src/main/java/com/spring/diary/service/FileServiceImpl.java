package com.spring.diary.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.spring.diary.dto.FileDTO;
import com.spring.diary.entity.Diary;
import com.spring.diary.entity.FileEntity;
import com.spring.diary.repository.DiaryRepository;
import com.spring.diary.repository.FileRepository;

@Service
public class FileServiceImpl implements FileService {
	@Autowired
	FileRepository fileRepo;
	
	@Autowired
	DiaryRepository diaryRepo;

	@Override
	public void insertFile(List<MultipartFile> files, Long diaryNo) {
		Diary diary = diaryRepo.getById(diaryNo);
		
		for(MultipartFile file : files) {
			System.out.println(file.getSize());
			String originalFileName = file.getOriginalFilename();
			String fileName = UUID.randomUUID().toString() + originalFileName;
			String savePath = System.getProperty("user.dir") + "\\files";
			
			
			if(file.getSize() == 0) {
				System.out.println("file없음");
				break;
			}else {
				FileDTO fileDTO = FileDTO.builder()
										.originalFileName(originalFileName)
										.fileName(fileName)
										.filePath(savePath)
										.build();
				
				// DTO -> Entity
				FileEntity fileEntity = FileDTO.dtoToEntity(fileDTO);
				// FileEntity의 diary 객체 변수 재설정
				fileEntity.updateDiary(diary);
				fileRepo.save(fileEntity);
				
				// 디렉토리에 파일 저장
				try {
					file.transferTo(new File(savePath + "\\" + fileName));
				} catch (IllegalStateException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public List<FileDTO> getByDiaryNo(Long diaryNo) {
		List<FileEntity> files = fileRepo.findAllByDiaryNo(diaryNo);
		
		System.out.println("file entity : " + files);
		
		List<FileDTO> fileDTOs = files.stream()
									.map(file -> file.entityToDto(file))
									.collect(Collectors.toList());
				
		return fileDTOs;
	}

	@Override
	@Transactional
	public void deleteFile(Long fileNo) {
		fileRepo.deleteById(fileNo);
	}


}
